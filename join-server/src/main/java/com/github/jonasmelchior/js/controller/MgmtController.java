package com.github.jonasmelchior.js.controller;

import com.fasterxml.jackson.annotation.JsonView;
import com.github.jonasmelchior.js.data.device.AppSKeyReqLog;
import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.device.JoinLog;
import com.github.jonasmelchior.js.data.device.dto.CreateDeviceDTO;
import com.github.jonasmelchior.js.data.device.dto.UpdateCreateDeviceResponseDTO;
import com.github.jonasmelchior.js.data.device.dto.UpdateDeviceDTO;
import com.github.jonasmelchior.js.data.http.Res;
import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.json.JsonPage;
import com.github.jonasmelchior.js.json.Views;
import com.github.jonasmelchior.js.rsql.CustomRsqlVisitor;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.*;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.user.UserService;
import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/1")
public class MgmtController {
    @Autowired
    private UserService userService;
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;


    @ResponseBody
    @PostMapping("/devices")
    public ResponseEntity createDevice(@RequestBody CreateDeviceDTO createDeviceDTO) {
        createDeviceDTO.setDevEUI(createDeviceDTO.getDevEUI().toUpperCase());

        KeySpec kek = null;
        if (createDeviceDTO.getKek() != null) {
            kek = new KeySpec(
                    createDeviceDTO.getKek().getKekLabel(),
                    createDeviceDTO.getKek().getAesKey(),
                    KeyType.KEK
            );
        }

        Optional<User> user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isEmpty()) {
            Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "The authorized user could not be retrieved on the JS");
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }

        Optional<Device> device = deviceService.findByDevEUI(createDeviceDTO.getDevEUI());
        if (device.isPresent()) {
            Res res = new Res(HttpStatus.CONFLICT, "Device with provided DevEUI already exists on the JS");
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }

        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devIdService
                )
        );

        try {
            // Exception is only thrown if credential exists and the provided password is wrong
            KeyCredential existingCredential = deviceKeyHandler.validateCredential(
                    createDeviceDTO.getCredential().getCredentialID(),
                    createDeviceDTO.getCredential().getPassword(),
                    user.get()
            );

            for (KeySpec keySpec : createDeviceDTO.getKeySpecs()) {
                // Set DevEUI of KeySpec object, since it isn't apart of the DTO object (see JsonIgnore annotation)
                keySpec.setIdentifier(createDeviceDTO.getDevEUI().toUpperCase());
                keySpec.setKey(keySpec.getKey().toUpperCase());
            }

            Pair<Boolean, String> result;

            // If credential exists, use it
            if (existingCredential != null) {
                result = deviceKeyHandler.init(
                        createDeviceDTO.getKeySpecs(),
                        kek,
                        existingCredential,
                        user.get(),
                        false,
                        createDeviceDTO.getMacVersion()
                );
            }
            // Otherwise, generate new
            else {
                result = deviceKeyHandler.init(
                        createDeviceDTO.getKeySpecs(),
                        kek,
                        createDeviceDTO.getCredential().getPassword(),
                        createDeviceDTO.getCredential().getCredentialID(),
                        user.get(),
                        false,
                        createDeviceDTO.getMacVersion()
                );
            }

            if (!result.a) {
                Res res = new Res(HttpStatus.BAD_REQUEST, result.b);
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }

            Optional<Device> createdDevice = deviceService.findByDevEUI(createDeviceDTO.getDevEUI().toUpperCase());
            if (createdDevice.isEmpty()) {
                Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong went fetching the newly created device from the database");
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }
            else {
                return ResponseEntity.ok(new UpdateCreateDeviceResponseDTO(createdDevice.get()));
            }

        } catch (CredentialAuthenticationException e) {
            Res res = new Res(HttpStatus.BAD_REQUEST, e.getMessage());
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }
    }

    @ResponseBody
    @PostMapping("/devices/{devEUI}")
    public ResponseEntity updateDevice(@PathVariable String devEUI,
                                       @RequestBody UpdateDeviceDTO updateDeviceDTO,
                                       @RequestHeader("Credential") String password) {
        devEUI = devEUI.toUpperCase();

        KeySpec kek = null;
        if (updateDeviceDTO.getKek() != null) {
            kek = new KeySpec(
                    updateDeviceDTO.getKek().getKekLabel(),
                    updateDeviceDTO.getKek().getAesKey(),
                    KeyType.KEK
            );
        }

        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }

        userDeviceAuthenticationResult.b.setDevEUI(updateDeviceDTO.getDevEUI());
        userDeviceAuthenticationResult.b.setMacVersion(updateDeviceDTO.getMacVersion());
        MACVersion fromMAcVersion = userDeviceAuthenticationResult.b.getMacVersion();

        updateDeviceDTO.setDevEUI(updateDeviceDTO.getDevEUI().toUpperCase());
        if (deviceService.findByDevEUI(updateDeviceDTO.getDevEUI()).isPresent() &&
            !devEUI.equals(updateDeviceDTO.getDevEUI())) {
            Res res = new Res(HttpStatus.CONFLICT, "Device with provided DevEUI already exists");
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }

        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                deviceService,
                new KeyHandler(
                        keyCredentialService,
                        devIdService
                ),
                joinLogService,
                appSKeyReqLogService,
                devIdService
        );

        for (KeySpec keySpec : updateDeviceDTO.getKeySpecs()) {
            // Set DevEUI of KeySpec object, since it isn't apart of the DTO object (see JsonIgnore annotation)
            keySpec.setIdentifier(updateDeviceDTO.getDevEUI().toUpperCase());
            keySpec.setKey(keySpec.getKey().toUpperCase());
        }

        try {
            Pair<Boolean, String> result;

            if (updateDeviceDTO.getKeySpecs().size() == 1) {
               result =  deviceKeyHandler.updateAuthorized(
                        devEUI,
                        userDeviceAuthenticationResult.b,
                        updateDeviceDTO.getKeySpecs().get(0),
                        kek,
                        password,
                        fromMAcVersion,
                        updateDeviceDTO.getMacVersion()
                );
            }
            else {
                result = deviceKeyHandler.updateAuthorized(
                        devEUI,
                        userDeviceAuthenticationResult.b,
                        updateDeviceDTO.getKeySpecs(),
                        kek,
                        password,
                        fromMAcVersion,
                        updateDeviceDTO.getMacVersion()
                );
            }

            if (!result.a) {
                Res res = new Res(HttpStatus.BAD_REQUEST, result.b);
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }
            Optional<Device> updatedDevice = deviceService.findByDevEUI(updateDeviceDTO.getDevEUI().toUpperCase());
            if (updatedDevice.isEmpty()) {
                Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong went fetching the updated device from the database");
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }
            else {
                return ResponseEntity.ok(new UpdateCreateDeviceResponseDTO(updatedDevice.get()));
            }

        } catch (UpdateDeviceException e) {
            Res res = new Res(HttpStatus.NOT_FOUND, e.getMessage());
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        } catch (CredentialAuthenticationException e) {
            Res res = new Res(HttpStatus.FORBIDDEN, e.getMessage());
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }
    }

    @DeleteMapping("/devices/{devEUI}")
    public ResponseEntity deleteDevice(@PathVariable("devEUI") String devEUI,
                                       @RequestHeader("Credential") String password) {
        devEUI = devEUI.toUpperCase();
        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }
        else {
            DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                    deviceService,
                    new KeyHandler(
                            keyCredentialService,
                            devIdService
                    ),
                    joinLogService,
                    appSKeyReqLogService,
                    devIdService
            );

            Pair<Boolean, String> result = null;
            try {
                result = deviceKeyHandler.deleteAuthorized(userDeviceAuthenticationResult.b, password);
            } catch (DeleteDeviceException e) {
                Res res = new Res(HttpStatus.NOT_FOUND, e.getMessage());
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            } catch (CredentialAuthenticationException e) {
                Res res = new Res(HttpStatus.FORBIDDEN, e.getMessage());
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }
            if (!result.a) {
                Res res = new Res(HttpStatus.BAD_REQUEST, result.b);
                return ResponseEntity.status(res.getHttpStatus()).body(res);
            }
            else {
                Res res = new Res(HttpStatus.OK, "Device " + devEUI + " has successfully been removed");
                return ResponseEntity.ok().body(res);
            }
        }
    }

    @ResponseBody
    @GetMapping("/devices/{devEUI}")
    @JsonView(Views.Detailed.class)
    public ResponseEntity getDevice(@PathVariable("devEUI") String devEUI) {
        devEUI = devEUI.toUpperCase();
        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }
        else {
            return ResponseEntity.ok(userDeviceAuthenticationResult.b);
        }
    }

    @GetMapping("/devices")
    @JsonView(Views.Public.class)
    public ResponseEntity getDevicesPaginated(@RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                              @RequestParam(value = "perPage", required = false, defaultValue = "20") int perPage,
                                              @RequestParam(value = "search", required = false) String search) {
        Node rootNode = null;
        if (search != null) {
            rootNode = new RSQLParser().parse(search);
        }

        Optional<User> user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
        if (user.isPresent()) {
            Specification<Device> deviceSpec;
            if (rootNode != null) {
                deviceSpec = rootNode.accept(new CustomRsqlVisitor<>());
                // Only fetch devices for current user
                deviceSpec = deviceSpec.and((root, query, criteriaBuilder) ->
                        criteriaBuilder.equal(root.get("owner"), user.get()));
            }
            else {
                // Only fetch devices for current user
                deviceSpec = (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("owner"), user.get());
            }

            Page<Device> devicePage = deviceService.getPageJSon(PageRequest.of(page - 1, perPage), deviceSpec);
            return ResponseEntity.ok(devicePage);
        }

        Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "The authorized user could not be retrieved on the JS");
        return ResponseEntity.status(res.getHttpStatus()).body(res);
    }

    @ResponseBody
    @GetMapping(value = "/devices/{devEUI}/joinlog")
    @JsonView(Views.Public.class)
    public ResponseEntity getJoinLogPaginated(@PathVariable("devEUI") String devEUI,
                                     @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                     @RequestParam(value = "perPage", required = false, defaultValue = "20") int perPage,
                                     @RequestParam(value = "search", required = false) String search) {
        devEUI = devEUI.toUpperCase();
        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }
        else {
            Node rootNode = null;
            if (search != null) {
                rootNode = new RSQLParser().parse(search);
            }

            Optional<User> user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            if (user.isPresent()) {
                Specification<JoinLog> joinLogSpec;
                if (rootNode != null) {
                    joinLogSpec = rootNode.accept(new CustomRsqlVisitor<>());
                    joinLogSpec = joinLogSpec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("device"), userDeviceAuthenticationResult.b));

                }
                else {
                    // Only fetch devices for current user
                    joinLogSpec = (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("device"), userDeviceAuthenticationResult.b);
                }

                JsonPage<JoinLog> joinLogPage = joinLogService.getPageJson(PageRequest.of(page - 1, perPage), joinLogSpec);
                return ResponseEntity.ok(joinLogPage);
            }

            Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "The authorized user could not be retrieved on the JS");
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }
    }

    @ResponseBody
    @GetMapping(value = "/devices/{devEUI}/appskeyreqlog")
    @JsonView(Views.Public.class)
    public ResponseEntity getAppSKeyReqLogsPaginated(@PathVariable("devEUI") String devEUI,
                                              @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                              @RequestParam(value = "perPage", required = false, defaultValue = "20") int perPage,
                                              @RequestParam(value = "search", required = false) String search) {
        devEUI = devEUI.toUpperCase();
        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }
        else {
            Node rootNode = null;
            if (search != null) {
                rootNode = new RSQLParser().parse(search);
            }

            Optional<User> user = userService.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName());
            if (user.isPresent()) {
                Specification<AppSKeyReqLog> appSKeyReqLogSpec;
                if (rootNode != null) {
                    appSKeyReqLogSpec = rootNode.accept(new CustomRsqlVisitor<>());
                    appSKeyReqLogSpec = appSKeyReqLogSpec.and((root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("device"), userDeviceAuthenticationResult.b));

                }
                else {
                    // Only fetch devices for current user
                    appSKeyReqLogSpec = (root, query, criteriaBuilder) ->
                            criteriaBuilder.equal(root.get("device"), userDeviceAuthenticationResult.b);
                }

                JsonPage<AppSKeyReqLog> appSKeyReqLogPage = appSKeyReqLogService.getPageJson(PageRequest.of(page - 1, perPage), appSKeyReqLogSpec);
                return ResponseEntity.ok(appSKeyReqLogPage);
            }

            Res res = new Res(HttpStatus.INTERNAL_SERVER_ERROR, "The authorized user could not be retrieved on the JS");
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }
    }

    @ResponseBody
    @GetMapping("/devices/{devEUI}/rkeys")
    public ResponseEntity getRootKeys(@PathVariable("devEUI") String devEUI,
                                   @RequestHeader("Credential") String password) {
        devEUI = devEUI.toUpperCase();
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(
                new KeyHandler(
                        keyCredentialService,
                        devIdService
                ),
                devIdService
        );

        Pair<ResponseEntity<?>, Device> userDeviceAuthenticationResult = authenticateDeviceOwner(devEUI);
        if (userDeviceAuthenticationResult.a != null) {
            return userDeviceAuthenticationResult.a;
        }

        try {
            return ResponseEntity.ok(deviceKeyHandler.getRootKeysDTOAuthorized(devEUI, password));
        } catch (RootKeyRetrievalException e) {
            Res res = new Res(HttpStatus.NOT_FOUND, e.getMessage());
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        } catch (CredentialAuthenticationException e) {
            Res res = new Res(HttpStatus.FORBIDDEN, e.getMessage());
            return ResponseEntity.status(res.getHttpStatus()).body(res);
        }
    }


    private Pair<ResponseEntity<?>, Device> authenticateDeviceOwner(String devEUI) {
        Optional<Device> device = deviceService.findByDevEUI(devEUI);
        if (device.isEmpty()) {
            Res res = new Res(HttpStatus.NOT_FOUND, "Device with provided DevEUI could not be found on JS");
            return new Pair<>(ResponseEntity.status(res.getHttpStatus()).body(res), null);
        }

        String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!device.get().getOwner().getEmail().equals(authenticatedUserEmail)) {
            Res res = new Res(HttpStatus.FORBIDDEN, "Unauthorized access to the device");
            return new Pair<>(ResponseEntity.status(res.getHttpStatus()).body(res), null);
        }

        return new Pair<>(null, device.get());
    }
}
