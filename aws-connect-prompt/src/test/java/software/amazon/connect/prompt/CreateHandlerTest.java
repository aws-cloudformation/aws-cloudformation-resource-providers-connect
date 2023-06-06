package software.amazon.connect.prompt;

import java.time.Duration;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreatePromptRequest;
import software.amazon.awssdk.services.connect.model.CreatePromptResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import static org.mockito.Mockito.when;
import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_DESCRIPTION_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ID;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_NAME_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_S3URI_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.buildPromptDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {
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
    public void tear_down() {
        verify(connectClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ArgumentCaptor<CreatePromptRequest> createPromptRequestArgumentCaptor = ArgumentCaptor.forClass(CreatePromptRequest.class);
        final CreatePromptResponse createPromptResponseResponse = CreatePromptResponse.builder()
                .promptId(PROMPT_ID)
                .promptARN(PROMPT_ARN)
                .build();
        when(proxyClient.client().createPrompt(createPromptRequestArgumentCaptor.capture())).thenReturn(createPromptResponseResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildPromptDesiredStateResourceModel())
            .desiredResourceTags(TAGS_ONE)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createPrompt(createPromptRequestArgumentCaptor.capture());
        createPromptValidationHelper(createPromptRequestArgumentCaptor);
    }

    @Test
    void testHandleRequest_Exception_CreatePrompt() {
        final ArgumentCaptor<CreatePromptRequest> createPromptRequestArgumentCaptor = ArgumentCaptor.forClass(CreatePromptRequest.class);

        when(proxyClient.client().createPrompt(createPromptRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).createPrompt(createPromptRequestArgumentCaptor.capture());
        createPromptValidationHelper(createPromptRequestArgumentCaptor);
    }

    private void createPromptValidationHelper(ArgumentCaptor<CreatePromptRequest> createPromptCaptor) {
        assertThat(createPromptCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(createPromptCaptor.getValue().name()).isEqualTo(PROMPT_NAME_ONE);
        assertThat(createPromptCaptor.getValue().description()).isEqualTo(PROMPT_DESCRIPTION_ONE);
        assertThat(createPromptCaptor.getValue().s3Uri()).isEqualTo(PROMPT_S3URI_ONE);
        assertThat(createPromptCaptor.getValue().tags()).isEqualTo(TAGS_ONE);
    }
}
