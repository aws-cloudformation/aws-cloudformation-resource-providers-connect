package software.amazon.connect.userhierarchygroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyGroupNameRequest;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyGroupNameResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_UPDATED_NAME;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.buildUserHierarchyGroupNameResourceModel;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.buildUserHierarchyGroupResourceModel;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;
    private ResourceModel currentModel;
    private ResourceModel desiredModel;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
        currentModel = buildUserHierarchyGroupResourceModel();
        desiredModel = buildUserHierarchyGroupNameResourceModel();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleRequest_SimpleUpdateSuccess() {
        final ArgumentCaptor<UpdateUserHierarchyGroupNameRequest> updateUserHierarchyGroupNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyGroupNameRequest.class);

        final UpdateUserHierarchyGroupNameResponse updateUserHierarchyGroupNameResponse = UpdateUserHierarchyGroupNameResponse.builder().build();
        when(proxyClient.client().updateUserHierarchyGroupName(updateUserHierarchyGroupNameRequestArgumentCaptor.capture())).thenReturn(updateUserHierarchyGroupNameResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(desiredModel)
            .previousResourceState(currentModel)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).updateUserHierarchyGroupName(updateUserHierarchyGroupNameRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(USER_HIERARCHY_GROUP_ARN);
        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getValue().name()).isEqualTo(USER_HIERARCHY_GROUP_UPDATED_NAME);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_NoUpdateSuccess() {
        final ArgumentCaptor<UpdateUserHierarchyGroupNameRequest> updateUserHierarchyGroupNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyGroupNameRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(currentModel)
                .previousResourceState(currentModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);
    }

    @Test
    public void testHandleRequest_Exception_UpdateUserHierarchyGroupName() {
        final ArgumentCaptor<UpdateUserHierarchyGroupNameRequest> updateUserHierarchyGroupNameRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateUserHierarchyGroupNameRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(currentModel)
                .desiredResourceState(desiredModel)
                .build();

        when(proxyClient.client().updateUserHierarchyGroupName(updateUserHierarchyGroupNameRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateUserHierarchyGroupName(updateUserHierarchyGroupNameRequestArgumentCaptor.capture());
        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateUserHierarchyGroupNameRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(USER_HIERARCHY_GROUP_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }
}
