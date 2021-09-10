package software.amazon.connect.userhierarchygroup;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeUserHierarchyGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeUserHierarchyGroupResponse;
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
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_FOUR_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ID_FIVE;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ID_FOUR;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ID_ONE;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ID_THREE;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ID_TWO;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_ONE_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_THREE_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.LEVEL_TWO_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_ARN;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.USER_HIERARCHY_GROUP_NAME;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.buildUserHierarchyGroupResourceModel;
import static software.amazon.connect.userhierarchygroup.UserHierarchyGroupTestDataProvider.generateGroupBasedOnLevelId;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {

    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;
    private ResourceModel model;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
        model = buildUserHierarchyGroupResourceModel();
    }


    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @ParameterizedTest
    @ValueSource(strings = {LEVEL_ID_ONE, LEVEL_ID_TWO, LEVEL_ID_THREE, LEVEL_ID_FOUR, LEVEL_ID_FIVE})
    public void handleRequest_ReadSuccess(String levelId) {
        final ArgumentCaptor<DescribeUserHierarchyGroupRequest> describeUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserHierarchyGroupRequest.class);

        final DescribeUserHierarchyGroupResponse describeUserHierarchyGroupResponse = DescribeUserHierarchyGroupResponse.builder()
                .hierarchyGroup(generateGroupBasedOnLevelId(levelId)).build();

        when(proxyClient.client().describeUserHierarchyGroup(describeUserHierarchyGroupRequestArgumentCaptor.capture())).thenReturn(describeUserHierarchyGroupResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
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

        verify(proxyClient.client()).describeUserHierarchyGroup(describeUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getName()).isEqualTo(USER_HIERARCHY_GROUP_NAME);
        assertThat(response.getResourceModel().getUserHierarchyGroupArn()).isEqualTo(USER_HIERARCHY_GROUP_ARN);

        switch (levelId) {
            case LEVEL_ID_ONE: assertThat(response.getResourceModel().getParentGroupArn()).isEqualTo(null); break;
            case LEVEL_ID_TWO: assertThat(response.getResourceModel().getParentGroupArn()).isEqualTo(LEVEL_ONE_HIERARCHY_GROUP_ARN); break;
            case LEVEL_ID_THREE: assertThat(response.getResourceModel().getParentGroupArn()).isEqualTo(LEVEL_TWO_HIERARCHY_GROUP_ARN); break;
            case LEVEL_ID_FOUR: assertThat(response.getResourceModel().getParentGroupArn()).isEqualTo(LEVEL_THREE_HIERARCHY_GROUP_ARN); break;
            case LEVEL_ID_FIVE: assertThat(response.getResourceModel().getParentGroupArn()).isEqualTo(LEVEL_FOUR_HIERARCHY_GROUP_ARN); break;
        }

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_DescribeUserHierarchyGroup() {
        final ArgumentCaptor<DescribeUserHierarchyGroupRequest> describeUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserHierarchyGroupRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().describeUserHierarchyGroup(describeUserHierarchyGroupRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeUserHierarchyGroup(describeUserHierarchyGroupRequestArgumentCaptor.capture());
        assertThat(describeUserHierarchyGroupRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeUserHierarchyGroupRequestArgumentCaptor.getValue().hierarchyGroupId()).isEqualTo(USER_HIERARCHY_GROUP_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_DescribeUserHierarchyGroup() {
        final ArgumentCaptor<DescribeUserHierarchyGroupRequest> describeUserHierarchyGroupRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeUserHierarchyGroupRequest.class);
        model.setUserHierarchyGroupArn("InvalidUHGArn");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        assertThat(describeUserHierarchyGroupRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);
    }
}
