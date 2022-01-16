package software.amazon.connect.contactflow;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.connect.contactflow.CallbackContext;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.*;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    private software.amazon.connect.contactflow.ReadHandler handler;
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
        final ArgumentCaptor<DescribeContactFlowRequest> describeContactFlowRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowRequest.class);
        final DescribeContactFlowResponse describeContactFlowResponse = DescribeContactFlowResponse.builder()
                .contactFlow(getDescribeContactFlowResponseObject())
                .build();
        when(proxyClient.client().describeContactFlow(describeContactFlowRequestArgumentCaptor.capture())).thenReturn(describeContactFlowResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getContactFlowArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(response.getResourceModel().getDescription()).isEqualTo(CONTACT_FLOW_DESCRIPTION);
        assertThat(response.getResourceModel().getName()).isEqualTo(CONTACT_FLOW_NAME);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describeContactFlow(describeContactFlowRequestArgumentCaptor.capture());
        assertThat(describeContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeContactFlowRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribeContactFlowRequest> describeContactFlowRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowRequest.class);
        final ResourceModel model = buildContactFlowDesiredStateResourceModel();
        model.setContactFlowArn(INVALID_CONTACT_FLOW_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describeContactFlowRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }


    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribeContactFlowRequest> describeContactFlowRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeContactFlowRequest.class);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describeContactFlow(describeContactFlowRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeContactFlow(describeContactFlowRequestArgumentCaptor.capture());
        assertThat(describeContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describeContactFlowRequestArgumentCaptor.getValue().contactFlowId()).isEqualTo(CONTACT_FLOW_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    private ContactFlow getDescribeContactFlowResponseObject() {
        return ContactFlow.builder()
                .arn(CONTACT_FLOW_ARN)
                .id(CONTACT_FLOW_ID)
                .name(CONTACT_FLOW_NAME)
                .description(CONTACT_FLOW_DESCRIPTION)
                .content(FLOW_CONTENT)
                .state(FLOW_STATE_ACTIVE)
                .tags(TAGS_ONE)
                .type(FLOW_TYPE)
                .build();
    }



}
