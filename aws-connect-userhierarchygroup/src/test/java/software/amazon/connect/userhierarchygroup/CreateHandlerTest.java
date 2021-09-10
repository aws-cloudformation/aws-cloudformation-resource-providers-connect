package software.amazon.connect.userhierarchygroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateUserHierarchyGroupRequest;
import software.amazon.awssdk.services.connect.model.CreateUserHierarchyGroupResponse;
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
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.PARENT_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_ID;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_NAME;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.buildUserHierarchyGroupResourceModel;

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
        verify(connectClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    void handleRequest_SimpleCreateSuccess() {
        final ArgumentCaptor<CreateUserHierarchyGroupRequest> createUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(CreateUserHierarchyGroupRequest.class);

        final CreateUserHierarchyGroupResponse createUserHierarchyGroupResponse = CreateUserHierarchyGroupResponse.builder()
                .hierarchyGroupArn(USER_HIERARCHY_GROUP_ARN)
                .hierarchyGroupId(USER_HIERARCHY_GROUP_ID)
                .build();
        when(proxyClient.client().createUserHierarchyGroup(createUserHierarchyGroupRequestArgumentCaptor.capture())).thenReturn(createUserHierarchyGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserHierarchyGroupResourceModel())
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

        verify(proxyClient.client()).createUserHierarchyGroup(createUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().name()).isEqualTo(USER_HIERARCHY_GROUP_NAME);
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().parentGroupId()).isEqualTo(PARENT_HIERARCHY_GROUP_ARN);
    }

    @Test
    void testHandleRequest_Exception_CreateUserHierarchyGroup() {
        final ArgumentCaptor<CreateUserHierarchyGroupRequest> createUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(CreateUserHierarchyGroupRequest.class);

        when(proxyClient.client().createUserHierarchyGroup(createUserHierarchyGroupRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildUserHierarchyGroupResourceModel())
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createUserHierarchyGroup(createUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().name()).isEqualTo(USER_HIERARCHY_GROUP_NAME);
        assertThat(createUserHierarchyGroupRequestArgumentCaptor.getValue().parentGroupId()).isEqualTo(PARENT_HIERARCHY_GROUP_ARN);
    }
}
