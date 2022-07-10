package software.amazon.connect.user;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserIdentityInfoRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserIdentityInfoResponse;
import software.amazon.awssdk.services.connect.model.UpdateUserPhoneConfigRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserPhoneConfigResponse;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyResponse;
import software.amazon.awssdk.services.connect.model.UpdateUserSecurityProfilesRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserSecurityProfilesResponse;
import software.amazon.awssdk.services.connect.model.UpdateUserRoutingProfileRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserRoutingProfileResponse;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

import software.amazon.cloudformation.proxy.Logger;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.user.UserTestDataProvider.AFTER_CONTACT_WORK_TIME_LIMIT;
import static software.amazon.connect.user.UserTestDataProvider.AUTO_ACCEPT;
import static software.amazon.connect.user.UserTestDataProvider.DIRECTORY_USER_ID_TWO;
import static software.amazon.connect.user.UserTestDataProvider.EMAIL;
import static software.amazon.connect.user.UserTestDataProvider.FIRST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.FIRST_NAME_ONE;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ARN;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ARN_TWO;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN_TWO;
import static software.amazon.connect.user.UserTestDataProvider.LAST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.MOBILE;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_TYPE_DESK;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_TYPE_SOFT;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ARN_TWO;
import static software.amazon.connect.user.UserTestDataProvider.SECONDARY_EMAIL;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ARN_TWO;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_ONE;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_THREE;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_TWO;
import static software.amazon.connect.user.UserTestDataProvider.USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.user.UserTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.user.UserTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.user.UserTestDataProvider.buildUserDesiredStateResourceModel;
import static software.amazon.connect.user.UserTestDataProvider.buildUserPreviousStateResourceModel;
import static software.amazon.connect.user.UserTestDataProvider.getUserIdentityInfo;
import static software.amazon.connect.user.UserTestDataProvider.getUserPhoneConfig;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private ProxyClient<ConnectClient> proxyClient;

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ConnectClient connectClient;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy((LoggerProxy) logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success_UpdateFirstNameOnly() {
        String updateFirstName = "NameUpdate";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(updateFirstName, LAST_NAME, EMAIL, SECONDARY_EMAIL, MOBILE))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(updateFirstName);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateLastNameOnly() {

        String updateLastName = "NameUpdateLast";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(FIRST_NAME, updateLastName, EMAIL, SECONDARY_EMAIL, MOBILE))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(updateLastName);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateEmailOnly() {

        final String updateEmail = "update@gmail.com";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(FIRST_NAME, LAST_NAME, updateEmail, SECONDARY_EMAIL, MOBILE))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(updateEmail);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);
        verify(connectClient, times(1)).serviceName();
    }


    @Test
    public void testHandleRequest_Success_UpdateSecondaryEmailOnly() {

        final String updateSecondaryEmail = "update@gmail.com";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(FIRST_NAME, LAST_NAME, EMAIL, updateSecondaryEmail, MOBILE))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(updateSecondaryEmail);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateMobileOnly() {

        final String updateMobile = "+17745785123";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(FIRST_NAME, LAST_NAME, EMAIL, SECONDARY_EMAIL, updateMobile))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(updateMobile);
        verify(connectClient, times(1)).serviceName();
    }
    @Test
    public void testHandleRequest_Success_PhoneConfig_UpdateAfterContactWorkTimeLimitOnly() {

        final Integer updateAfterContactWorkTimeLimit = 1;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig(updateAfterContactWorkTimeLimit, Boolean.TRUE, PHONE_NUMBER, PHONE_TYPE_DESK))
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(updateAfterContactWorkTimeLimit);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(Boolean.TRUE);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_PhoneConfig_UpdateAutoAcceptOnly() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig(AFTER_CONTACT_WORK_TIME_LIMIT, Boolean.FALSE, PHONE_NUMBER, PHONE_TYPE_DESK))
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isFalse();
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_PhoneConfig_UpdatePhoneTypeOnly() {

        final String updatePhoneType = PHONE_TYPE_SOFT;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig(AFTER_CONTACT_WORK_TIME_LIMIT, AUTO_ACCEPT, PHONE_NUMBER, updatePhoneType))
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(AUTO_ACCEPT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(updatePhoneType);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_PhoneConfig_UpdateDeskPhoneNumberOnly() {

        final String updatePhoneNumber = "+14682001887";
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig(AFTER_CONTACT_WORK_TIME_LIMIT, AUTO_ACCEPT, updatePhoneNumber, PHONE_TYPE_DESK))
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(AUTO_ACCEPT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(updatePhoneNumber);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateUserRoutingProfile() {
        final String updateRoutingProfileArn = ROUTING_PROFILE_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(updateRoutingProfileArn)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserRoutingProfileRequest> updateUserRoutingProfileRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserRoutingProfileRequest.class);

        final UpdateUserRoutingProfileResponse updateUserRoutingProfileResponse = UpdateUserRoutingProfileResponse.builder().build();
        when(proxyClient.client().updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture())).thenReturn(updateUserRoutingProfileResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture());
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(updateRoutingProfileArn);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateUserSecurityProfiles() {
        final String updateSecurityProfileArn = SECURITY_PROFILE_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(updateSecurityProfileArn))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserSecurityProfilesRequest> updateUserSecurityProfilesRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserSecurityProfilesRequest.class);

        final UpdateUserSecurityProfilesResponse updateUserSecurityProfilesResponse = UpdateUserSecurityProfilesResponse.builder().build();
        when(proxyClient.client().updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture())).thenReturn(updateUserSecurityProfilesResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture());
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(updateSecurityProfileArn);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateUserHierarchy() {
        final String updateUserHierarchy = HIERARCHY_GROUP_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(updateUserHierarchy)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserHierarchyRequest> updateUserHierarchyRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyRequest.class);

        final UpdateUserHierarchyResponse updateUserHierarchyResponse = UpdateUserHierarchyResponse.builder().build();
        when(proxyClient.client().updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture())).thenReturn(updateUserHierarchyResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(updateUserHierarchy);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserIdentityInfo() {

        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo(FIRST_NAME_ONE, LAST_NAME, EMAIL, SECONDARY_EMAIL, MOBILE))
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);

        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME_ONE);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);
        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserPhoneConfig() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig(AFTER_CONTACT_WORK_TIME_LIMIT, Boolean.FALSE, PHONE_NUMBER, PHONE_TYPE_DESK))
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isFalse();
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserRoutingProfile() {
        final String updateRoutingProfileArn = ROUTING_PROFILE_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(updateRoutingProfileArn)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserRoutingProfileRequest> updateUserRoutingProfileRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserRoutingProfileRequest.class);
        when(proxyClient.client().updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture());
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(updateRoutingProfileArn);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserSecurityProfiles() {
        final String updateSecurityProfileArn = SECURITY_PROFILE_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(updateSecurityProfileArn))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .hierarchyGroupArn(HIERARCHY_GROUP_ARN)
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateUserSecurityProfilesRequest> updateUserSecurityProfilesRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserSecurityProfilesRequest.class);

        when(proxyClient.client().updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture());
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(updateSecurityProfileArn);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserHierarchy() {
        final String updateUserHierarchy = HIERARCHY_GROUP_ARN_TWO;
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .userArn(USER_ARN)
                .instanceArn(INSTANCE_ARN)
                .identityInfo(getUserIdentityInfo())
                .phoneConfig(getUserPhoneConfig())
                .securityProfileArns(Collections.singleton(SECURITY_PROFILE_ARN))
                .routingProfileArn(ROUTING_PROFILE_ARN)
                .tags(TAGS_SET_ONE)
                .hierarchyGroupArn(updateUserHierarchy)
                .build();

        final ArgumentCaptor<UpdateUserHierarchyRequest> updateUserHierarchyRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyRequest.class);
        when(proxyClient.client().updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(updateUserHierarchy);

        verify(connectClient, times(1)).serviceName();
    }


    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);
        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);
        final ArgumentCaptor<UpdateUserRoutingProfileRequest> updateUserRoutingProfileRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserRoutingProfileRequest.class);
        final ArgumentCaptor<UpdateUserSecurityProfilesRequest> updateUserSecurityProfilesRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserSecurityProfilesRequest.class);
        final ArgumentCaptor<UpdateUserHierarchyRequest> updateUserHierarchyRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);

        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final UpdateUserRoutingProfileResponse updateUserRoutingProfileResponse = UpdateUserRoutingProfileResponse.builder().build();
        when(proxyClient.client().updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture())).thenReturn(updateUserRoutingProfileResponse);

        final UpdateUserSecurityProfilesResponse updateSecurityProfilesResponse = UpdateUserSecurityProfilesResponse.builder().build();
        when(proxyClient.client().updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture())).thenReturn(updateSecurityProfilesResponse);

        final UpdateUserHierarchyResponse updateUserHierarchyResponse = UpdateUserHierarchyResponse.builder().build();
        when(proxyClient.client().updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture())).thenReturn(updateUserHierarchyResponse);
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .previousResourceState(buildUserPreviousStateResourceModel())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(Boolean.TRUE);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(proxyClient.client()).updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture());
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(ROUTING_PROFILE_ARN);

        verify(proxyClient.client()).updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture());
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(SECURITY_PROFILE_ARN);

        verify(proxyClient.client()).updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(HIERARCHY_GROUP_ARN);

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(USER_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(tagsAdded);

        verify(connectClient, times(6)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UnTagResource() {
        final ArgumentCaptor<UpdateUserPhoneConfigRequest> updateUserPhoneConfigRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserPhoneConfigRequest.class);
        final ArgumentCaptor<UpdateUserIdentityInfoRequest> updateUserIdentityInfoRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserIdentityInfoRequest.class);
        final ArgumentCaptor<UpdateUserRoutingProfileRequest> updateUserRoutingProfileRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserRoutingProfileRequest.class);
        final ArgumentCaptor<UpdateUserSecurityProfilesRequest> updateUserSecurityProfilesRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserSecurityProfilesRequest.class);
        final ArgumentCaptor<UpdateUserHierarchyRequest> updateUserHierarchyRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateUserPhoneConfigResponse updateUserPhoneConfigResponse = UpdateUserPhoneConfigResponse.builder().build();
        when(proxyClient.client().updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture())).thenReturn(updateUserPhoneConfigResponse);

        final UpdateUserIdentityInfoResponse updateUserIdentityInfoResponse = UpdateUserIdentityInfoResponse.builder().build();
        when(proxyClient.client().updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture())).thenReturn(updateUserIdentityInfoResponse);

        final UpdateUserRoutingProfileResponse updateUserRoutingProfileResponse = UpdateUserRoutingProfileResponse.builder().build();
        when(proxyClient.client().updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture())).thenReturn(updateUserRoutingProfileResponse);

        final UpdateUserSecurityProfilesResponse updateSecurityProfilesResponse = UpdateUserSecurityProfilesResponse.builder().build();
        when(proxyClient.client().updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture())).thenReturn(updateSecurityProfilesResponse);

        final UpdateUserHierarchyResponse updateUserHierarchyResponse = UpdateUserHierarchyResponse.builder().build();
        when(proxyClient.client().updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture())).thenReturn(updateUserHierarchyResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .previousResourceState(buildUserPreviousStateResourceModel())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserIdentityInfo(updateUserIdentityInfoRequestArgumentCaptor.capture());
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(updateUserIdentityInfoRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);

        verify(proxyClient.client()).updateUserPhoneConfig(updateUserPhoneConfigRequestArgumentCaptor.capture());
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(Boolean.TRUE);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(updateUserPhoneConfigRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);

        verify(proxyClient.client()).updateUserRoutingProfile(updateUserRoutingProfileRequestArgumentCaptor.capture());
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserRoutingProfileRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(ROUTING_PROFILE_ARN);

        verify(proxyClient.client()).updateUserSecurityProfiles(updateUserSecurityProfilesRequestArgumentCaptor.capture());
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(updateUserSecurityProfilesRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(SECURITY_PROFILE_ARN);

        verify(proxyClient.client()).updateUserHierarchy(updateUserHierarchyRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);
        assertThat(updateUserHierarchyRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(HIERARCHY_GROUP_ARN);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(USER_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(6)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnInvalidRequestException_UpdateInstanceArn() {
        final ResourceModel model = buildUserDesiredStateResourceModel();
        model.setInstanceArn(INSTANCE_ARN_TWO);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(buildUserPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnInvalidRequestException_UpdateDirectoryUserId() {
        final ResourceModel model = buildUserDesiredStateResourceModel();
        model.setDirectoryUserId(DIRECTORY_USER_ID_TWO);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(buildUserPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, never()).serviceName();
    }
}
