package software.amazon.connect.user;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateUserRequest;
import software.amazon.awssdk.services.connect.model.CreateUserResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.user.UserTestDataProvider.AFTER_CONTACT_WORK_TIME_LIMIT;
import static software.amazon.connect.user.UserTestDataProvider.DIRECTORY_USER_ID;
import static software.amazon.connect.user.UserTestDataProvider.EMAIL;
import static software.amazon.connect.user.UserTestDataProvider.FIRST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.HIERARCHY_GROUP_ARN;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.LAST_NAME;
import static software.amazon.connect.user.UserTestDataProvider.MOBILE;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.user.UserTestDataProvider.PHONE_TYPE_DESK;
import static software.amazon.connect.user.UserTestDataProvider.ROUTING_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.SECONDARY_EMAIL;
import static software.amazon.connect.user.UserTestDataProvider.SECURITY_PROFILE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_ONE;
import static software.amazon.connect.user.UserTestDataProvider.USERNAME;
import static software.amazon.connect.user.UserTestDataProvider.USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.USER_ID;
import static software.amazon.connect.user.UserTestDataProvider.USER_PASSWORD;
import static software.amazon.connect.user.UserTestDataProvider.buildUserDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    private CreateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verify(connectClient, times(1)).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequest_Success() {
        final ArgumentCaptor<CreateUserRequest> createUserRequestArgumentCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);
        final CreateUserResponse createUserResponse = CreateUserResponse.builder()
                .userId(USER_ID)
                .userArn(USER_ARN)
                .build();

        when(proxyClient.client().createUser(createUserRequestArgumentCaptor.capture())).thenReturn(createUserResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getUserArn()).isEqualTo(USER_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createUser(createUserRequestArgumentCaptor.capture());
        assertThat(createUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().username()).isEqualTo(USERNAME);
        assertThat(createUserRequestArgumentCaptor.getValue().password()).isEqualTo(USER_PASSWORD);
        assertThat(createUserRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(createUserRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(SECURITY_PROFILE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(ROUTING_PROFILE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(Boolean.TRUE);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);
        assertThat(createUserRequestArgumentCaptor.getValue().directoryUserId()).isEqualTo(DIRECTORY_USER_ID);
        assertThat(createUserRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(HIERARCHY_GROUP_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }

    @Test
    void testHandleRequest_Exception_CreateUserConnect() {
        final ArgumentCaptor<CreateUserRequest> createUserRequestArgumentCaptor = ArgumentCaptor.forClass(CreateUserRequest.class);

        when(proxyClient.client().createUser(createUserRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createUser(createUserRequestArgumentCaptor.capture());
        assertThat(createUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().username()).isEqualTo(USERNAME);
        assertThat(createUserRequestArgumentCaptor.getValue().password()).isEqualTo(USER_PASSWORD);
        assertThat(createUserRequestArgumentCaptor.getValue().securityProfileIds().size()).isEqualTo(1);
        assertThat(createUserRequestArgumentCaptor.getValue().securityProfileIds().get(0)).isEqualTo(SECURITY_PROFILE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().routingProfileId()).isEqualTo(ROUTING_PROFILE_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().afterContactWorkTimeLimit()).isEqualTo(AFTER_CONTACT_WORK_TIME_LIMIT);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().autoAccept()).isEqualTo(Boolean.TRUE);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().phoneType().toString()).isEqualTo(PHONE_TYPE_DESK);
        assertThat(createUserRequestArgumentCaptor.getValue().phoneConfig().deskPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().firstName()).isEqualTo(FIRST_NAME);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().lastName()).isEqualTo(LAST_NAME);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().email()).isEqualTo(EMAIL);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().secondaryEmail()).isEqualTo(SECONDARY_EMAIL);
        assertThat(createUserRequestArgumentCaptor.getValue().identityInfo().mobile()).isEqualTo(MOBILE);
        assertThat(createUserRequestArgumentCaptor.getValue().directoryUserId()).isEqualTo(DIRECTORY_USER_ID);
        assertThat(createUserRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(HIERARCHY_GROUP_ARN);
        assertThat(createUserRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }
}
