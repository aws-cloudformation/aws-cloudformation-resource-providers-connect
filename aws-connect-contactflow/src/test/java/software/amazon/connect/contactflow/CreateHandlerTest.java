package software.amazon.connect.contactflow;

import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.awssdk.services.connect.model.CreateContactFlowRequest;
import software.amazon.awssdk.services.connect.model.CreateContactFlowResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_ARN;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_DESCRIPTION;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.CONTACT_FLOW_NAME;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.FLOW_CONTENT;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.TAGS_ONE;
import static software.amazon.connect.contactflow.ContactFlowTestDataProvider.buildContactFlowDesiredStateResourceModel;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Duration;

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
        proxy = mock(AmazonWebServicesClientProxy.class);
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
    void testHandleRequest_Success_TypeContactFlow() {
        final ArgumentCaptor<CreateContactFlowRequest> createContactFlowRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateContactFlowRequest.class);

        final CreateContactFlowResponse createContactFlowResponse = CreateContactFlowResponse.builder()
                .contactFlowArn(CONTACT_FLOW_ARN)
                .build();
        when(proxyClient.client().createContactFlow(createContactFlowRequestArgumentCaptor.capture()))
                .thenReturn(createContactFlowResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getContactFlowArn()).isEqualTo(CONTACT_FLOW_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createContactFlow(createContactFlowRequestArgumentCaptor.capture());

        assertThat(createContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().name()).isEqualTo(CONTACT_FLOW_NAME);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().description()).isEqualTo(CONTACT_FLOW_DESCRIPTION);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().content()).isEqualTo(FLOW_CONTENT);
    }

    @Test
    void testHandleRequest_Exception_CreateContactFlowConnect() {
        final ArgumentCaptor<CreateContactFlowRequest> createContactFlowRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateContactFlowRequest.class);

        when(proxyClient.client().createContactFlow(createContactFlowRequestArgumentCaptor.capture()))
                .thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createContactFlow(createContactFlowRequestArgumentCaptor.capture());
        assertThat(createContactFlowRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().name()).isEqualTo(CONTACT_FLOW_NAME);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().description()).isEqualTo(CONTACT_FLOW_DESCRIPTION);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
        assertThat(createContactFlowRequestArgumentCaptor.getValue().content()).isEqualTo(FLOW_CONTENT);
    }

}
