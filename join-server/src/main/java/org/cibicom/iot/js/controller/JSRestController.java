package org.cibicom.iot.js.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.cibicom.iot.js.data.lrwan.backendif.AppSKeyAns;
import org.cibicom.iot.js.data.lrwan.backendif.JoinAns;
import com.google.gson.*;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.log.AppSKeyReqLogService;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.lrwan.AppSKeyReqHandler;
import org.cibicom.iot.js.service.lrwan.JoinProcessor;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.cibicom.iot.js.service.utils.rest.RestControllerTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lrwan")
public class JSRestController {
    @Autowired
    private DeviceService deviceService;
    @Autowired
    private KeyCredentialService keyCredentialService;
    @Autowired
    private DevKeyIdService devIdService;
    JoinProcessor joinProcessor;
    @Autowired
    private JoinLogService joinLogService;
    @Autowired
    private RunningJobService jobService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;

    Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    // Exclude 'lifetime' field from serialization
                    return f.getName().equals("lifeTime");
                }
                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

    Logger logger = LoggerFactory.getLogger(JSRestController.class);

    @PostMapping("/join")
    public String join(@RequestBody String requestBody, HttpServletRequest request) {
        logger.debug("Received JoinReq from " + RestControllerTools.getClientIp(request));
        this.joinProcessor = new JoinProcessor(
                deviceService,
                keyCredentialService,
                devIdService,
                joinLogService
        );

        JoinAns joinAns = joinProcessor.processJoinReq(requestBody);

        return gson.toJson(joinAns);
    }

    @PostMapping("/appskey")
    public String appSKeyRequest(@RequestBody String requestBody, HttpServletRequest request) {
        logger.debug("Received AppSKeyReq from " + RestControllerTools.getClientIp(request));
        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(
                deviceService,
                keyCredentialService,
                devIdService,
                appSKeyReqLogService
        );

        AppSKeyAns appSKeyAns = appSKeyReqHandler.processAppSKeyReq(requestBody);

        return gson.toJson(appSKeyAns);
    }
}