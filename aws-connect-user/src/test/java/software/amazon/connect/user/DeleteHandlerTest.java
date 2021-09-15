package software.amazon.connect.user;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteUserRequest;
import software.amazon.awssdk.services.connect.model.DeleteUserResponse;
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
import static org.mockito.Mockito.*;
import static software.amazon.connect.user.UserTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.user.UserTestDataProvider.INVALID_USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.TAGS_ONE;
import static software.amazon.connect.user.UserTestDataProvider.USER_ARN;
import static software.amazon.connect.user.UserTestDataProvider.buildUserDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private DeleteHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new DeleteHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    void testHandleRequest_Success() {
        final ArgumentCaptor<DeleteUserRequest> deleteUserRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserRequest.class);

        final DeleteUserResponse deleteUserResponse = DeleteUserResponse.builder().build();

        when(proxyClient.client().deleteUser(deleteUserRequestArgumentCaptor.capture())).thenReturn(deleteUserResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteUser(deleteUserRequestArgumentCaptor.capture());
        assertThat(deleteUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteUserRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteUser() {
        final ArgumentCaptor<DeleteUserRequest> deleteUserRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserRequest.class);

        when(proxyClient.client().deleteUser(deleteUserRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteUser(deleteUserRequestArgumentCaptor.capture());
        assertThat(deleteUserRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteUserRequestArgumentCaptor.getValue().userId()).isEqualTo(USER_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidUserArn() {
        final ArgumentCaptor<DeleteUserRequest> deleteUserRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserRequest.class);
        final ResourceModel model = buildUserDesiredStateResourceModel();
        model.setUserArn(INVALID_USER_ARN);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(deleteUserRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }
}
