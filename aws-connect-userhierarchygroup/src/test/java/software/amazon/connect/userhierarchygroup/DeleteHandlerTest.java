package software.amazon.connect.userhierarchygroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteUserHierarchyGroupRequest;
import software.amazon.awssdk.services.connect.model.DeleteUserHierarchyGroupResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.buildUserHierarchyGroupResourceModel;

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
    public void handleRequest_SimpleDeleteSuccess() {
        final ArgumentCaptor<DeleteUserHierarchyGroupRequest> deleteUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserHierarchyGroupRequest.class);

        final DeleteUserHierarchyGroupResponse deleteUserHierarchyGroupResponse = DeleteUserHierarchyGroupResponse.builder()
                .build();
        when(proxyClient.client().deleteUserHierarchyGroup(deleteUserHierarchyGroupRequestArgumentCaptor.capture())).thenReturn(deleteUserHierarchyGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserHierarchyGroupResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteUserHierarchyGroup(deleteUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(deleteUserHierarchyGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeleteUserHierarchyGroup() {
        final ArgumentCaptor<DeleteUserHierarchyGroupRequest> deleteUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserHierarchyGroupRequest.class);

        when(proxyClient.client().deleteUserHierarchyGroup(deleteUserHierarchyGroupRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserHierarchyGroupResourceModel())
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteUserHierarchyGroup(deleteUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(deleteUserHierarchyGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteUserHierarchyGroupRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(USER_HIERARCHY_GROUP_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    void testHandleRequest_CfnNotFoundException_InvalidUHGArn() {
        final ArgumentCaptor<DeleteUserHierarchyGroupRequest> deleteUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteUserHierarchyGroupRequest.class);

        final ResourceModel model = buildUserHierarchyGroupResourceModel();
        model.setUserHierarchyGroupArn("INVALID ARN");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        assertThat(deleteUserHierarchyGroupRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);
    }
}
