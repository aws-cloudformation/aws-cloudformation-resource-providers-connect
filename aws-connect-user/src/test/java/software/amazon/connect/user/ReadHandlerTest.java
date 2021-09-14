package software.amazon.connect.user;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeUserRequest;
import software.amazon.awssdk.services.connect.model.DescribeUserResponse;
import software.amazon.awssdk.services.connect.model.PhoneType;
import software.amazon.awssdk.services.connect.model.User;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.user.UserTestDataProvider.DIRECTORY_USER_ID;
import static software.amazon.connect.user.UserTestDataProvider.EMAIL;
import static software.amazon.connect.user.UserTestDataProvider.FIRST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ID;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.INVALID_USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.LAST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_TYPE_DESK;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ID;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ID;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_ONE;
import static software.amazon.connect.user.UserTestDataProvider.USERNAME;
import static software.amazon.connect.user.UserTestDataProvider.USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.buildUserDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success() {
        final ArgumentCaptor<DescribeUserRequest> describeUserRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserRequest.class);

        final DescribeUserResponse describeUserResponse = DescribeUserResponse.builder()
                .user(getDescribeUserResponseObject())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describeUser(describeUserRequestArgumentCaptor.capture())).thenReturn(describeUserResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getUserArn()).isEqualTo(USER_ARN);
        assertThat(response.getResourceModel().getDirectoryUserId()).isEqualTo(DIRECTORY_USER_ID);
        assertThat(response.getResourceModel().getRoutingProfileArn()).isEqualTo(ROUTING_PROFILE_ARN);
        assertThat(response.getResourceModel().getSecurityProfileArns().size()).isEqualTo(1);
        assertThat(response.getResourceModel().getSecurityProfileArns().iterator().next()).isEqualTo(SECURITY_PROFILE_ARN);
        assertThat(response.getResourceModel().getUsername()).isEqualTo(USERNAME);
        assertThat(response.getResourceModel().getPhoneConfig().getPhoneType()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(response.getResourceModel().getPhoneConfig().getAfterContactWorkTimeLimit()).isEqualTo(0);
        assertThat(response.getResourceModel().getPhoneConfig().getAutoAccept()).isTrue();
        assertThat(response.getResourceModel().getPhoneConfig().getDeskPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(response.getResourceModel().getIdentityInfo().getFirstName()).isEqualTo(FIRST_NAME);
        assertThat(response.getResourceModel().getIdentityInfo().getLastName()).isEqualTo(LAST_NAME);
        assertThat(response.getResourceModel().getIdentityInfo().getEmail()).isEqualTo(EMAIL);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeUser(describeUserRequestArgumentCaptor.capture());
        assertThat(describeUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeUserRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribeUserRequest> describeUserRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserRequest.class);
        final ResourceModel model = buildUserDesiredStateResourceModel();
        model.setUserArn(INVALID_USER_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeUserRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribeUserRequest> describeUserRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describeUser(describeUserRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeUser(describeUserRequestArgumentCaptor.capture());
        assertThat(describeUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeUserRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    private User getDescribeUserResponseObject() {

        return User.builder()
                .arn(USER_ARN)
                .directoryUserId(DIRECTORY_USER_ID)
                .routingProfileId(ROUTING_PROFILE_ID)
                .hierarchyGroupId(HIERARCHY_GROUP_ID)
                .securityProfileIds(SECURITY_PROFILE_ID)
                .username(USERNAME)
                .identityInfo(software.amazon.awssdk.services.connect.model.UserIdentityInfo.builder()
                        .firstName(FIRST_NAME)
                        .lastName(LAST_NAME)
                        .email(EMAIL).build())
                .phoneConfig(software.amazon.awssdk.services.connect.model.UserPhoneConfig.builder()
                        .phoneType(PhoneType.DESK_PHONE)
                        .afterContactWorkTimeLimit(0)
                        .autoAccept(true)
                        .deskPhoneNumber(PHONE_NUMBER)
                        .build())
                .tags(TAGS_ONE)
                .build();
    }
}
