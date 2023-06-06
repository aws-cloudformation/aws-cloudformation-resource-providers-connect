package software.amazon.connect.prompt;

import java.time.Duration;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribePromptRequest;
import software.amazon.awssdk.services.connect.model.DescribePromptResponse;
import software.amazon.awssdk.services.connect.model.Prompt;
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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.INVALID_PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_DESCRIPTION_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ID;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_NAME_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.TAGS_ONE;
import static software.amazon.connect.prompt.PromptTestDataProvider.buildPromptDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {
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
    public void tear_down() {
        verifyNoMoreInteractions(connectClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ArgumentCaptor<DescribePromptRequest> describePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DescribePromptRequest.class);

        final DescribePromptResponse describePromptResponse = DescribePromptResponse.builder()
                .prompt(getDescribePromptResponseObject())
                .build();
        when(proxyClient.client().describePrompt(describePromptRequestArgumentCaptor.capture())).thenReturn(describePromptResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(buildPromptDesiredStateResourceModel())
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).describePrompt(describePromptRequestArgumentCaptor.capture());
        assertThat(describePromptRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describePromptRequestArgumentCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);
        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribePromptRequest> describePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DescribePromptRequest.class);
        final ResourceModel model = buildPromptDesiredStateResourceModel();
        model.setPromptArn(INVALID_PROMPT_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(describePromptRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribePromptRequest> describePromptRequestArgumentCaptor = ArgumentCaptor.forClass(DescribePromptRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPromptDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describePrompt(describePromptRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describePrompt(describePromptRequestArgumentCaptor.capture());
        assertThat(describePromptRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(describePromptRequestArgumentCaptor.getValue().promptId()).isEqualTo(PROMPT_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    private Prompt getDescribePromptResponseObject() {
        return Prompt.builder()
                .name(PROMPT_NAME_ONE)
                .promptARN(PROMPT_ARN)
                .promptId(PROMPT_ID)
                .description(PROMPT_DESCRIPTION_ONE)
                .tags(TAGS_ONE)
                .build();
    }

}
