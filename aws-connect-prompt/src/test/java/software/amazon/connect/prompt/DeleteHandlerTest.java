package software.amazon.connect.prompt;

import java.time.Duration;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeletePromptRequest;
import software.amazon.awssdk.services.connect.model.DeletePromptResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.INVALID_PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.buildPromptDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

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
    public void tear_down() {
        verifyNoMoreInteractions(connectClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ArgumentCaptor<DeletePromptRequest> deletePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DeletePromptRequest.class);

        final DeletePromptResponse deletePromptResponse = DeletePromptResponse.builder().build();
        when(proxyClient.client().deletePrompt(deletePromptRequestArgumentCaptor.capture())).thenReturn(deletePromptResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildPromptDesiredStateResourceModel())
            .desiredResourceTags(TAGS_ONE)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deletePrompt(deletePromptRequestArgumentCaptor.capture());
        assertThat(deletePromptRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deletePromptRequestArgumentCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    void testHandleRequest_Exception_DeletePrompt() {
        final ArgumentCaptor<DeletePromptRequest> deletePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DeletePromptRequest.class);

        when(proxyClient.client().deletePrompt(deletePromptRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new software.amazon.connect.prompt.CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deletePrompt(deletePromptRequestArgumentCaptor.capture());
        assertThat(deletePromptRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deletePromptRequestArgumentCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);

        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidPromptArn() {
        final ArgumentCaptor<DeletePromptRequest> deletePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DeletePromptRequest.class);
        final ResourceModel model = buildPromptDesiredStateResourceModel();
        model.setPromptArn(INVALID_PROMPT_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new software.amazon.connect.prompt.CallbackContext(), proxyClient, logger));
        assertThat(deletePromptRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }
}
