package software.amazon.connect.contactflowmodule;

import software.amazon.awssdk.services.connect.model.CreateContactFlowRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.awssdk.services.connect.model.CreateContactFlowModuleRequest;
import software.amazon.awssdk.services.connect.model.CreateContactFlowModuleResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_ARN;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_DESCRIPTION;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.CONTACT_FLOW_MODULE_NAME;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.FLOW_MODULE_CONTENT;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.TAGS_ONE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.buildContactFlowModuleDesiredStateResourceModel;

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
    void testHandleRequest_Success_TypeContactFlowModule() {
        final ArgumentCaptor<CreateContactFlowModuleRequest> createContactFlowModuleRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateContactFlowModuleRequest.class);

        final CreateContactFlowModuleResponse createContactFlowModuleResponse = CreateContactFlowModuleResponse.builder()
                .arn(CONTACT_FLOW_MODULE_ARN)
                .build();
        when(proxyClient.client().createContactFlowModule(createContactFlowModuleRequestArgumentCaptor.capture()))
                .thenReturn(createContactFlowModuleResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowModuleDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getContactFlowModuleArn()).isEqualTo(CONTACT_FLOW_MODULE_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createContactFlowModule(createContactFlowModuleRequestArgumentCaptor.capture());

        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().name()).isEqualTo(CONTACT_FLOW_MODULE_NAME);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().description()).isEqualTo(CONTACT_FLOW_MODULE_DESCRIPTION);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().content()).isEqualTo(FLOW_MODULE_CONTENT);
    }

    @Test
    void testHandleRequest_Exception_CreateContactFlowModuleConnect() {
        final ArgumentCaptor<CreateContactFlowModuleRequest> createContactFlowModuleRequestArgumentCaptor =
                ArgumentCaptor.forClass(CreateContactFlowModuleRequest.class);

        when(proxyClient.client().createContactFlowModule(createContactFlowModuleRequestArgumentCaptor.capture()))
                .thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildContactFlowModuleDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createContactFlowModule(createContactFlowModuleRequestArgumentCaptor.capture());
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().name()).isEqualTo(CONTACT_FLOW_MODULE_NAME);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().description()).isEqualTo(CONTACT_FLOW_MODULE_DESCRIPTION);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
        assertThat(createContactFlowModuleRequestArgumentCaptor.getValue().content()).isEqualTo(FLOW_MODULE_CONTENT);
    }
}
