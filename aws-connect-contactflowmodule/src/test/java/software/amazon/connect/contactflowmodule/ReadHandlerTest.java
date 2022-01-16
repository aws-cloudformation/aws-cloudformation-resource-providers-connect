package software.amazon.connect.contactflowmodule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowModuleRequest;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowModuleResponse;
import software.amazon.awssdk.services.connect.model.ContactFlowModule;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.*;

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
        final ArgumentCaptor<DescribeContactFlowModuleRequest> describeContactFlowModuleRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowModuleRequest.class);
        final DescribeContactFlowModuleResponse describeContactFlowModuleResponse = DescribeContactFlowModuleResponse.builder()
                .contactFlowModule(getDescribeContactFlowModuleResponseObject())
                .build();
        when(proxyClient.client().describeContactFlowModule(describeContactFlowModuleRequestArgumentCaptor.capture())).thenReturn(describeContactFlowModuleResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowModuleDesiredStateResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getContactFlowModuleArn()).isEqualTo(CONTACT_FLOW_MODULE_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(CONTACT_FLOW_MODULE_DESCRIPTION);
        assertThat(response.getResourceModel().getName()).isEqualTo(CONTACT_FLOW_MODULE_NAME);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeContactFlowModule(describeContactFlowModuleRequestArgumentCaptor.capture());
        assertThat(describeContactFlowModuleRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeContactFlowModuleRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribeContactFlowModuleRequest> describeContactFlowModuleRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowModuleRequest.class);
        final ResourceModel model = buildContactFlowModuleDesiredStateResourceModel();
        model.setContactFlowModuleArn(INVALID_CONTACT_FLOW_MODULE_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeContactFlowModuleRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }


    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribeContactFlowModuleRequest> describeContactFlowModuleRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowModuleRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowModuleDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describeContactFlowModule(describeContactFlowModuleRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeContactFlowModule(describeContactFlowModuleRequestArgumentCaptor.capture());
        assertThat(describeContactFlowModuleRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeContactFlowModuleRequestArgumentCaptor.getValue().contactFlowModuleId()).isEqualTo(CONTACT_FLOW_MODULE_ARN);

        verify(connectClient, times(1)).serviceName();
    }
    private ContactFlowModule getDescribeContactFlowModuleResponseObject() {
        return ContactFlowModule.builder()
                .arn(CONTACT_FLOW_MODULE_ARN)
                .id(CONTACT_FLOW_MODULE_ID)
                .name(CONTACT_FLOW_MODULE_NAME)
                .description(CONTACT_FLOW_MODULE_DESCRIPTION)
                .content(FLOW_MODULE_CONTENT)
                .state(FLOW_MODULE_STATE_ACTIVE)
                .status(FLOW_MODULE_STATUS_PUBLISHED)
                .tags(TAGS_ONE)
                .build();
    }
}
