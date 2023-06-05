package software.amazon.connect.tasktemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends BaseTest{

    protected software.amazon.connect.tasktemplate.ReadHandler readHandler;
    @BeforeEach
    public void setup(){
        super.setup();
        readHandler = new ReadHandler();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleReadRequest_Success() {
        final ArgumentCaptor<GetTaskTemplateRequest> getTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(GetTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();
        GetTaskTemplateResponse getTaskTemplateResponse = buildGetTaskTemplateResponse();
        when(proxyClient.client().getTaskTemplate(getTaskTemplateRequestArgumentCaptor.capture())).thenReturn(getTaskTemplateResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getResourceModel().getInstanceArn()).isEqualTo(INSTANCE_ARN);
        assertThat(response.getResourceModel().getArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(response.getResourceModel().getFields().size()).isEqualTo(getTaskTemplateResponse.fields().size());
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).getTaskTemplate(getTaskTemplateRequestArgumentCaptor.capture());
        assertThat(getTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(getTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ARN);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void handleReadRequest_InvalidTaskTemplateArn_CfnNotFoundException() {
        final ArgumentCaptor<GetTaskTemplateRequest> getTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(GetTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(INVALID_TASK_TEMPLATE_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(getTaskTemplateRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }


    @Test
    public void handleReadRequest_clientThrowException_Exception() {
        final ArgumentCaptor<GetTaskTemplateRequest> getTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(GetTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();

        when(proxyClient.client().getTaskTemplate(getTaskTemplateRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                readHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).getTaskTemplate(getTaskTemplateRequestArgumentCaptor.capture());
        assertThat(getTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(getTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ARN);
        verify(connectClient, times(1)).serviceName();
    }
}
