package software.amazon.connect.prompt;

import org.junit.jupiter.api.AfterEach;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ListPromptsRequest;
import software.amazon.awssdk.services.connect.model.ListPromptsResponse;
import software.amazon.awssdk.services.connect.model.PromptSummary;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.prompt.PromptTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ARN;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_ID;
import static software.amazon.connect.prompt.PromptTestDataProvider.PROMPT_NAME_ONE;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    private ListHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ListHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void tear_down() {
        verifyNoMoreInteractions(connectClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ArgumentCaptor<ListPromptsRequest> listPromptsRequestArgumentCaptor = ArgumentCaptor.forClass(ListPromptsRequest.class);
        ListPromptsResponse listPromptsResponse = buildListPromptResponse(null);
        when(proxyClient.client().listPrompts(listPromptsRequestArgumentCaptor.capture())).thenReturn(listPromptsResponse);

        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response =
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).listPrompts(listPromptsRequestArgumentCaptor.capture());
    }

    @Test
    public void handleListRequest_Exception() {
        final ArgumentCaptor<ListPromptsRequest> listPromptsRequestArgumentCaptor = ArgumentCaptor.forClass(ListPromptsRequest.class);
        when(proxyClient.client().listPrompts(listPromptsRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceModel model = ResourceModel.builder()
                .instanceArn(INSTANCE_ARN)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(proxyClient.client()).listPrompts(listPromptsRequestArgumentCaptor.capture());
    }

    @Test
    public void handleRequest_NullResourceModel() {
        final ResourceHandlerRequest<ResourceModel> request =  ResourceHandlerRequest.<ResourceModel>builder()
                .build();
        assertThrows(CfnInvalidRequestException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, never()).serviceName();
    }

    @Test
    public void handleRequest_NullInstanceArn() {
        final ResourceModel model = ResourceModel
                .builder()
                .instanceArn(null)
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        assertThrows(CfnInvalidRequestException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, never()).serviceName();
    }

    protected static ListPromptsResponse buildListPromptResponse(String nextToken){
        PromptSummary promptSummary = PromptSummary.builder()
                .arn(PROMPT_ARN)
                .id(PROMPT_ID)
                .name(PROMPT_NAME_ONE)
                .build();
        ListPromptsResponse.Builder builder = ListPromptsResponse.builder();
        builder.promptSummaryList(promptSummary);
        if(StringUtils.isNotBlank(nextToken)){
            builder.nextToken(nextToken);
        }
        return builder.build();
    }
}
