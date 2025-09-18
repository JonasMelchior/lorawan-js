package org.cibicom.iot.js;

import org.cibicom.iot.js.data.device.Device;
import org.cibicom.iot.js.data.keys.KeySpec;
import org.cibicom.iot.js.data.keys.KeyType;
import org.cibicom.iot.js.data.lrwan.MACVersion;
import org.cibicom.iot.js.data.user.User;
import org.cibicom.iot.js.data.user.UserType;
import org.cibicom.iot.js.service.device.DevKeyIdService;
import org.cibicom.iot.js.service.device.DeviceService;
import org.cibicom.iot.js.service.device.KeyCredentialService;
import org.cibicom.iot.js.service.device.keys.DeviceKeyHandler;
import org.cibicom.iot.js.service.device.keys.KeyHandler;
import org.cibicom.iot.js.service.log.AppSKeyReqLogService;
import org.cibicom.iot.js.service.log.JoinLogService;
import org.cibicom.iot.js.service.lrwan.AppSKeyReqHandler;
import org.cibicom.iot.js.service.lrwan.JoinProcessor;
import org.cibicom.iot.js.service.user.UserService;
import org.cibicom.iot.js.service.utils.RunningJobService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Optional;

import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
public class RestTests {
    @Rule
    public final JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation("target/generated-snippets");
    @Autowired
    private WebApplicationContext context;
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
    private RunningJobService jobService;
    @Autowired
    private AppSKeyReqLogService appSKeyReqLogService;
    @Autowired
    private Utils utils;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation)
                        .uris()
                        .withHost("host")
                        .withPort(7090)
                        .and().snippets())
                .alwaysDo(document("{method-name}",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()))
                )
                .build();
    }

    @Test
    public void testAuthentication() throws Exception {
        initUser();

        this.mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\n" +
                        "    \"email\": \"jonas@gmail.com\",\n" +
                        "    \"password\": \"a_strong_pwd\"\n" +
                        "}"))
                .andExpect(status().isOk())
                .andDo(document("auth-login",
                        requestFields(
                                fieldWithPath("email").description("Email of user to be authorized"),
                                fieldWithPath("password").description("Password for associated account")
                        ),
                        responseFields(
                                fieldWithPath("email").description("Email of user who has been authorized"),
                                fieldWithPath("token").description("Returned JWT Token")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testDeviceCreation() throws Exception {
        initUser();

        this.mockMvc.perform(post("/1/devices").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer <JWT Token>")
                .content("{\n" +
                        "    \"devEUI\": \"123ed12c867671ac\",\n" +
                        "    \"keySpecs\": [\n" +
                        "        {\n" +
                        "            \"key\": \"12345109875671ab123451875671abde\",\n" +
                        "            \"keyType\": \"AppKey1_0\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"kek\": {\n" +
                        "         \"kekLabel\": \"my_label\",\n" +
                        "         \"aesKey\": \"3ac2e183ef16dfe9b331d5c586ca8be1\"\n" +
                        "     },\n" +
                        "    \"macVersion\": \"LORAWAN_1_0\",\n" +
                        "    \"credential\": {\n" +
                        "        \"credentialID\": \"Test Credential ID4\",\n" +
                        "        \"password\": \"a_strong_pwd\"\n" +
                        "    },\n" +
                        "    \"forwardAppSKeyToNS\": true" +
                        "}\n" +
                        "\n"))
                .andExpect(status().isOk())
                .andDo(document("create-device",
                        requestFields(
                                fieldWithPath("devEUI").description("64-bit globally unique identifier"),
                                fieldWithPath("keySpecs").description("JSON Object for the key to be persisted"),
                                fieldWithPath("keySpecs[].key").description("The key in plaintext"),
                                fieldWithPath("keySpecs[].keyType").description("The type of key to be persisted. Allowed values: [AppKey1_0, AppKey1_1, NwkKey1_1]"),
                                fieldWithPath("kek").description("JSON Object for the Key Encryption Key used for wrapping the root key(s) when interfacing with AS or NS as specified in LoRaWAN Backend Interfaces Specification"),
                                fieldWithPath("kek.kekLabel").description("KEKLabel as specified in Backend Interfaces"),
                                fieldWithPath("kek.aesKey").description("Key used for wrapping operation as specified in RFC 3394. Allowed key sizes [128, 192, 256]"),
                                fieldWithPath("macVersion").description("MAC Version of the LoRaWAN Protocol which the end-device implements. Allowed values: [LORAWAN_1_0, LORAWAN_1_0_2, LORAWAN_1_0_3, LORAWAN_1_0_4, LORAWAN_1_1]"),
                                fieldWithPath("credential").description("JSON Object containing credential information"),
                                fieldWithPath("credential.credentialID").description("ID of the credential"),
                                fieldWithPath("credential.password").description("Password associated with the credential"),
                                fieldWithPath("forwardAppSKeyToNS").description("Boolean value indicating whether the AppSKey should be forwarded to NS in a JoinAns")
                        ),
                        responseFields(
                                fieldWithPath("devEUI").description("DevEUI of created device"),
                                fieldWithPath("rootKeysExposed").description("Concealment status of root key(s)")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testDeviceUpdate() throws Exception {
        initTestDevice1_0_update();

        this.mockMvc.perform(RestDocumentationRequestBuilders.post("/1/devices/{DevEUI}", "0000000000000302").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer <JWT Token>")
                        .header("Credential", "My_Test_Password")
                        .content("{\n" +
                                "    \"devEUI\": \"123ed12c875671ac\",\n" +
                                "    \"keySpecs\": [\n" +
                                "        {\n" +
                                "            \"key\": \"12345109875671ab123451875671abde\",\n" +
                                "            \"keyType\": \"AppKey1_0\"\n" +
                                "        }\n" +
                                "    ],\n" +
                                "    \"kek\": {\n" +
                                "        \"kekLabel\": \"my_label\",\n" +
                                "        \"aesKey\": \"3ac2e183ef16dfe9b121d5c586ac8be1\"\n" +
                                "    },\n" +
                                "    \"macVersion\": \"LORAWAN_1_0\", \n" +
                                "    \"forwardAppSKeyToNS\": true" +
                                "}\n" +
                                "\n"))
                .andExpect(status().isOk())
                .andDo(document("update-device",
                        requestFields(
                                fieldWithPath("devEUI").description("64-bit globally unique identifier"),
                                fieldWithPath("keySpecs").description("JSON Object describing the key to be persisted"),
                                fieldWithPath("keySpecs[].key").description("The key in plaintext"),
                                fieldWithPath("keySpecs[].keyType").description("The type of key to be persisted. Allowed values: [AppKey1_0, AppKey1_1, NwkKey1_1]"),
                                fieldWithPath("kek").description("JSON Object for the Key Encryption Key used for wrapping the root key(s) when interfacing with AS or NS as specified in LoRaWAN Backend Interfaces Specification"),
                                fieldWithPath("kek.kekLabel").description("KEKLabel as specified in Backend Interfaces"),
                                fieldWithPath("kek.aesKey").description("Key used for wrapping operation as specified in RFC 3394. Allowed key sizes [128, 192, 256]"),
                                fieldWithPath("macVersion").description("MAC Version of the LoRaWAN Protocol which the end-device implements. Allowed values: [LORAWAN_1_0, LORAWAN_1_0_2, LORAWAN_1_0_3, LORAWAN_1_0_4, LORAWAN_1_1]"),
                                fieldWithPath("forwardAppSKeyToNS").description("Boolean value indicating whether the AppSKey should be forwarded to NS in a JoinAns")
                        ),
                        responseFields(
                                fieldWithPath("devEUI").description("DevEUI of created device"),
                                fieldWithPath("rootKeysExposed").description("Concealment status of root key(s)")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testDeviceDelete() throws Exception {
        utils.initTestDevice1_0(true);

        this.mockMvc.perform(RestDocumentationRequestBuilders.delete("/1/devices/{DevEUI}", "0000000000000301").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer <JWT Token>")
                        .header("Credential", "My_Test_Password1"))
                .andExpect(status().isOk())
                .andDo(document("delete-device",
                        responseFields(
                                fieldWithPath("httpStatus").description("HTTP Status"),
                                fieldWithPath("message").description("Indicative message on success or error")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testDevicesGet() throws Exception {
        utils.initTestDevice1_0(true);

        this.mockMvc.perform(get("/1/devices?page=1&perPage=20&search=devEUI==0000000000000301").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer <JWT Token>"))
                .andExpect(status().isOk())
                .andDo(document("get-devices",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to retrieve."),
                                parameterWithName("perPage").description("The number of items per page."),
                                parameterWithName("search").description("rsql query")
                        ),
                        responseFields(
                                fieldWithPath("content").description("Content of the page returned"),
                                fieldWithPath("content[].devEUI").description("64-bit globally unique identifier"),
                                fieldWithPath("content[].rootKeysExposed").description("Concealment status of root key(s)"),
                                fieldWithPath("content[].macVersion").description("MAC Version which the end-device implements"),
                                fieldWithPath("content[].lastJoin").description("Latest Join-request of a End-device").type("String (date)"),
                                fieldWithPath("content[].createdAt").description("Device created at").type("String (date)"),
                                fieldWithPath("content[].updatedAt").description("Device updated at").type("String (date)"),
                                fieldWithPath("hasNext").description("Indication of whether next page is present"),
                                fieldWithPath("hasContent").description("Indication of whether current page has content"),
                                fieldWithPath("last").description("Indication of whether the current page is the last page"),
                                fieldWithPath("totalPages").description("Total number of pages for the specified query"),
                                fieldWithPath("totalElements").description("Total elements, i.e. ")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testDeviceGet() throws Exception {
        utils.initTestDevice1_0(true);

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/1/devices/{DevEUI}", "0000000000000301").contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer <JWT Token>"))
                .andExpect(status().isOk())
                .andDo(document("get-device",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("DevEUI").description("64-bit globally unique identifier (case insensitive)")
                        ),
                        responseFields(
                                fieldWithPath("devEUI").description("64-bit globally unique identifier"),
                                fieldWithPath("rootKeysExposed").description("Concealment status of root key(s)"),
                                fieldWithPath("sessionStatus").description("Session Status of the device"),
                                fieldWithPath("sessionStatus.devAddr").description("Network Address of device"),
                                fieldWithPath("sessionStatus.lastDevNonce").description("Latest DevNonce used by end-device"),
                                fieldWithPath("sessionStatus.lastJoinNonce").description("Latest JoinNonce used by Join Server"),
                                fieldWithPath("sessionStatus.sessionKeyId").description("Application Session Key ID as specified in LoRaWAN Backend Interfaces Specification"),
                                fieldWithPath("sessionStatus.sessionNum").description("Current session number (incrementing in case of successful Join Procedure)"),
                                fieldWithPath("sessionStatus.usedDevNonces").description("A list of all DevNonces used for a device, when registered under the current Join Server"),
                                fieldWithPath("sessionStatus.usedJoinNonces").description("A list of all JoinNonces used for the Join Server"),
                                fieldWithPath("sessionStatus.state").description("State of the current device"),
                                fieldWithPath("isKekEnabled").description("Boolean value indicating whether Key Wrapping is enabled when interfacing with NS or AS"),
                                fieldWithPath("kekLabel").description("Common label between JS and NS or AS to identify the Key to wrap root key(s) or session keys with"),
                                fieldWithPath("forwardAppSKeyToNS").description("Boolean value indicating whether the AppSKey should be forwarded to NS in a JoinAns"),
                                fieldWithPath("owner.email").description("Owner email"),
                                fieldWithPath("owner.firstName").description("Owner first name"),
                                fieldWithPath("owner.lastName").description("Owner last name"),
                                fieldWithPath("owner.organization").description("Organization of owner"),
                                fieldWithPath("owner.userType").description("Owner user type"),
                                fieldWithPath("macVersion").description("MAC Version of device"),
                                fieldWithPath("lastJoin").description("Last join attempt by device").type("String (date)"),
                                fieldWithPath("createdAt").description("Creation date of device").type("String (date)"),
                                fieldWithPath("updatedAt").description("Last date device was updated").type("String (date)")
                                //TODO list the last ones in here...
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testRootKeyGet() throws Exception {
        utils.initTestDevice1_0(true);

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/1/devices/{DevEUI}/rkeys", "0000000000000301").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer <JWT Token>")
                        .header("Credential", "My_Test_Password1"))
                .andExpect(status().isOk())
                .andDo(document("get-rkeys",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        pathParameters(
                                parameterWithName("DevEUI").description("64-bit globally unique identifier (case insensitive)")
                        ),
                        responseFields(
                                fieldWithPath("[].key").description("Root key in plaintext"),
                                fieldWithPath("[].keyType").description("Type of root key. Allowed values: [AppKey1_0, AppKey1_1, NwkKey1_1]")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testJoinLogsGet() throws Exception {
        utils.initTestDevice1_0(true);
        testJoin();

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/1/devices/{DevEUI}/joinlog?page=1&perPage=20&search=success==true", "0000000000000301").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer <JWT Token>"))
                .andExpect(status().isOk())
                .andDo(document("get-joinlogs",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to retrieve."),
                                parameterWithName("perPage").description("The number of items per page."),
                                parameterWithName("search").description("rsql query")
                        ),
                        responseFields(
                                fieldWithPath("content").description("Content of the page returned"),
                                fieldWithPath("content[].joinReq").description("JoinReq message"),
                                fieldWithPath("content[].joinAns").description("JoinAns message exclusive root key(s)"),
                                fieldWithPath("content[].success").description("Success of Join Procedure"),
                                fieldWithPath("content[].joinReqAttemptTime").description("Time and date of JoinReq received by JS").type("String (date)"),
                                fieldWithPath("hasNext").description("Indication of whether next page is present"),
                                fieldWithPath("hasContent").description("Indication of whether current page has content"),
                                fieldWithPath("last").description("Indication of whether the current page is the last page"),
                                fieldWithPath("totalPages").description("Total number of pages for the specified query"),
                                fieldWithPath("totalElements").description("Total elements, i.e. ")
                        )
                ));
    }

    @Test
    @WithMockUser(username = "jonas@gmail.com", roles = {"USER"})
    public void testAppSKeyReqLogsGet() throws Exception {
        utils.initTestDevice1_0(true);
        testJoin();
        testAppSKeyReq();

        this.mockMvc.perform(RestDocumentationRequestBuilders.get("/1/devices/{DevEUI}/appskeyreqlog?page=1&perPage=20&search=success==true", "0000000000000301").contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer <JWT Token>"))
                .andExpect(status().isOk())
                .andDo(document("get-appskeyreqlogs",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        queryParameters(
                                parameterWithName("page").description("The page number to retrieve."),
                                parameterWithName("perPage").description("The number of items per page."),
                                parameterWithName("search").description("rsql query")
                        ),
                        responseFields(
                                fieldWithPath("content").description("Content of the page returned"),
                                fieldWithPath("content[].appSKeyReq").description("AppSKeyReq message"),
                                fieldWithPath("content[].appSKeyAns").description("AppSKeyAns message exclusive AppSKey"),
                                fieldWithPath("content[].success").description("Success of AppSKey retrieval"),
                                fieldWithPath("content[].appSKeyReqAttemptTime").description("Time and date of AppSKeyReq received by JS").type("String (date)"),
                                fieldWithPath("hasNext").description("Indication of whether next page is present"),
                                fieldWithPath("hasContent").description("Indication of whether current page has content"),
                                fieldWithPath("last").description("Indication of whether the current page is the last page"),
                                fieldWithPath("totalPages").description("Total number of pages for the specified query"),
                                fieldWithPath("totalElements").description("Total elements, i.e. ")
                        )
                ));
    }

    private void testJoin() {
        String joinReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070497," +
                "\"MessageType\":\"JoinReq\",\"MACVersion\":\"1.0.2\",\"DevAddr\":\"00F3BFBF\",\"PHYPayload\":\"000318AF9F5BCF4BD80103000000000000ADE74C3F6B8D\"," +
                "\"DLSettings\":\"00\",\"RxDelay\":1,\"DevEUI\":\"0000000000000301\",\"CFList\":\"184f84e85684b85e84886684586e8400\"}";

        JoinProcessor joinProcessor = new JoinProcessor(deviceService, keyCredentialService, devIdService, joinLogService);
        joinProcessor.processJoinReq(joinReqJson);
    }

    private void testAppSKeyReq() {
        String appSKeyReqJson = "{\"ProtocolVersion\":\"1.0\",\"SenderID\":\"000000\",\"ReceiverID\":\"D84BCF5B9FAF1803\",\"TransactionID\":3502070498," +
                "\"MessageType\":\"AppSKeyReq\", \"DevEUI\":\"0000000000000301\",\"SessionKeyID\":\"1\"}";

        AppSKeyReqHandler appSKeyReqHandler = new AppSKeyReqHandler(deviceService, keyCredentialService, devIdService, appSKeyReqLogService);
        appSKeyReqHandler.processAppSKeyReq(appSKeyReqJson);
    }

    private void initUser() {
        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        User user;

        if (userOptional.isEmpty()) {
            user = new User(
                    "jonas@gmail.com",
                    "Jonas",
                    "Jensen",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }
    }

    private boolean initTestDevice1_0_update() {
        DeviceKeyHandler deviceKeyHandler = new DeviceKeyHandler(deviceService, new KeyHandler(
                keyCredentialService,
                devIdService
        ), joinLogService, jobService, appSKeyReqLogService);

        Optional<User> userOptional = userService.findByEmail("jonas@gmail.com");
        User user;

        if (userOptional.isEmpty()) {
            user = new User(
                    "jonas@gmail.com",
                    "Jonas",
                    "Jensen",
                    "a_strong_pwd",
                    UserType.USER
            );
            userService.save(user);
        }
        else {
            user = userOptional.get();
        }

        Optional<Device> optionalDevice = deviceService.findByDevEUI("0000000000000302");
        if (optionalDevice.isEmpty()) {
            deviceKeyHandler.init(
                    new KeySpec("0000000000000302", "00000000000000000000000706050407", KeyType.AppKey1_0),
                    null,
                    "My_Test_Password",
                    "Credential2",
                    user,
                    false,
                    MACVersion.LORAWAN_1_0,
                    true

            );
            return true;
        }

        return false;
    }

    @After
    public void clearDB() {
        utils.clearDB();
    }
}
