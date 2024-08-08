package com.github.jonasmelchior.js.controller;

import com.github.jonasmelchior.js.data.lrwan.backendif.AppSKeyAns;
import com.github.jonasmelchior.js.data.lrwan.backendif.JoinAns;
import com.google.gson.*;
import com.github.jonasmelchior.js.service.device.DevKeyIdService;
import com.github.jonasmelchior.js.service.device.DeviceService;
import com.github.jonasmelchior.js.service.device.KeyCredentialService;
import com.github.jonasmelchior.js.service.log.AppSKeyReqLogService;
import com.github.jonasmelchior.js.service.log.JoinLogService;
import com.github.jonasmelchior.js.service.lrwan.AppSKeyReqHandler;
import com.github.jonasmelchior.js.service.lrwan.JoinProcessor;
import com.github.jonasmelchior.js.service.utils.RunningJobService;
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
    public String join(@RequestBody String requestBody) {
        logger.info("Incoming JoinReq:\n" + requestBody);
        this.joinProcessor = new JoinProcessor(
                deviceService,
                keyCredentialService,
                devIdService,
                joinLogService,
                jobService
        );

        JoinAns joinAns = joinProcessor.processJoinReq(requestBody);
        logger.info("Resulting JoinAns:\n" + gson.toJson(joinAns));

        return gson.toJson(joinAns);
    }

    @PostMapping("/appskey")
    public String appSKeyRequest(@RequestBody String requestBody) {
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