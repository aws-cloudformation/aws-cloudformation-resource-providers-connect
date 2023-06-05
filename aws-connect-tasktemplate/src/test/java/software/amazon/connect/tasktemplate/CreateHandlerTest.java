package software.amazon.connect.tasktemplate;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.model.CreateTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.CreateTaskTemplateResponse;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends BaseTest{
    protected software.amazon.connect.tasktemplate.CreateHandler createHandler;

    @BeforeEach
    public void setup(){
        super.setup();
        createHandler = new CreateHandler();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleCreateRequest_Success() {

        final ArgumentCaptor<CreateTaskTemplateRequest> createTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(CreateTaskTemplateRequest.class);
        final CreateTaskTemplateResponse createTaskTemplateResponse = CreateTaskTemplateResponse.builder()
                .id(TASK_TEMPLATE_ID)
                .arn(TASK_TEMPLATE_ARN)
                .build();
        when(proxyClient.client().createTaskTemplate(createTaskTemplateRequestArgumentCaptor.capture())).thenReturn(createTaskTemplateResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildTaskTemplateDesiredStateResourceModel())
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = createHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createTaskTemplate(createTaskTemplateRequestArgumentCaptor.capture());
        assertThat(createTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ID);
        verify(connectClient, times(1)).serviceName();
    }
}
