package com.github.jonasmelchior.js;

import com.github.jonasmelchior.js.data.device.Device;
import com.github.jonasmelchior.js.data.keys.KeyCredential;
import com.github.jonasmelchior.js.data.keys.KeySpec;
import com.github.jonasmelchior.js.data.keys.KeyType;
import com.github.jonasmelchior.js.data.lrwan.MACVersion;
import com.github.jonasmelchior.js.data.user.User;
import com.github.jonasmelchior.js.data.user.UserType;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.device.keys.DeviceKeyHandler;
import com.github.jonasmelchior.js.service.device.keys.KeyHandler;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.user.UserService;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class Utils {
    @Autowired
    private DeviceService deviceService;

    @Autowired
    private KeyCredentialService keyCredentialService;

    @Autowired
    private DevKeyIdService devKeyIdService;

    @Autowired
    private JoinLogService joinLogService;

    @Autowired
    private RunningJobService jobService;

    @Autowired
    private UserService userService;

    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;
    @Autowired
    private DevKeyIdService devIdService;

    public boolean initTestDevice1_0() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devKeyIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        User user;

        if (userOptional.isEmpty()) {
            user = new User(
                    "jonas@gmail.com",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI("0000000000000301");
        if (optionalDevice.isEmpty()) {
            deviceKeyHandler.init(
                    new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.AppKey1_0),
                    "My_Test_Password1",
                    "Credential1",
                    user,
                    false,
                    MACVersion.LORAWAN_1_0
            );
            return true;
        }

        return false;
    }

    public boolean initTestDevice1_1() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        User user;

        if (userOptional.isEmpty()) {
            user = new User(
                    "jonas@gmail.com",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI("0000000000000301");
        if (optionalDevice.isEmpty()) {
            deviceKeyHandler.init(
                    new ArrayList<>(List.of(
                            new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.AppKey1_1),
                            new KeySpec("0000000000000301", "00000000000000000000000706050407", KeyType.NwkKey1_1)
                    )),
                    "My_Test_Password1",
                    "Credential1",
                    user,
                    false,
                    MACVersion.LORAWAN_1_1
            );
            return true;
        }


        return false;
    }

    public void clearDB() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devKeyIdService
        ), joinLogService, jobService, appSKeyReqLogService);


        Optional<Device> device = deviceService.findByDevEUI("0000000000000301");
        Optional<Device> device1 = deviceService.findByDevEUI("123ED12C867671AC");
        Optional<Device> device2 = deviceService.findByDevEUI("123ED12C875671AC");
        Optional<Device> device3 = deviceService.findByDevEUI("0000000000000302");
        Optional<Device> device4 = deviceService.findByDevEUI("1D4A7D0000927185");

        Optional<KeyCredential> keyCredential = keyCredentialService.findById("Credential1");
        Optional<KeyCredential> keyCredential1 = keyCredentialService.findById("Test Credential ID4");
        Optional<KeyCredential> keyCredential2 = keyCredentialService.findById("Credential2");
        Optional<KeyCredential> keyCredential3 = keyCredentialService.findById("Credential");

        Optional<User> user = userService.findByEmail("jonas@gmail.com");

        device.ifPresent(deviceKeyHandler::delete);
        device1.ifPresent(deviceKeyHandler::delete);
        device2.ifPresent(deviceKeyHandler::delete);
        device3.ifPresent(deviceKeyHandler::delete);
        device4.ifPresent(deviceKeyHandler::delete);
        keyCredential.ifPresent(keyCredentialService::delete);
        keyCredential1.ifPresent(keyCredentialService::delete);
        keyCredential2.ifPresent(keyCredentialService::delete);
        keyCredential3.ifPresent(keyCredentialService::delete);

        user.ifPresent(userService::delete);
    }
}
