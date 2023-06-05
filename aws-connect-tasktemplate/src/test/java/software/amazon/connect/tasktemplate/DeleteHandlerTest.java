package software.amazon.connect.tasktemplate;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.model.DeleteTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.DeleteTaskTemplateResponse;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends BaseTest{
    protected software.amazon.connect.tasktemplate.DeleteHandler deleteHandler;

    @BeforeEach
    public void setup(){
        super.setup();
        deleteHandler = new DeleteHandler();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleDeleteRequest_success() {
        final ArgumentCaptor<DeleteTaskTemplateRequest> deleteTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(proxyClient.client().deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture())).thenReturn(DeleteTaskTemplateResponse.builder().build());
        when(proxyClient.client().getTaskTemplate(any(GetTaskTemplateRequest.class))).thenReturn(GetTaskTemplateResponse.builder().build());
        final ProgressEvent<ResourceModel, CallbackContext> response = deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture());
        verify(proxyClient.client(), times(1)).getTaskTemplate(any(GetTaskTemplateRequest.class));
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void handleDeleteRequest_InvalidTaskTemplateArn_CfnNotFoundException() {
        final ArgumentCaptor<DeleteTaskTemplateRequest> deleteTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(INVALID_TASK_TEMPLATE_ARN)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getAllValues().size()).isEqualTo(0);

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void handleDeleteRequest_clientThrowsResourceNotFoundExceptionOnGetTaskTemplate_notCallDeleteTaskTemplate() {
        final ArgumentCaptor<DeleteTaskTemplateRequest> deleteTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();

        when(proxyClient.client().getTaskTemplate(any(GetTaskTemplateRequest.class))).thenThrow(ResourceNotFoundException.builder().build());;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client(), times(1)).getTaskTemplate(any(GetTaskTemplateRequest.class));
        verify(proxyClient.client(), never()).deleteTaskTemplate(any(DeleteTaskTemplateRequest.class));
        verify(connectClient, times(1)).serviceName();
    }
    @Test
    public void handleDeleteRequest_clientThrowsThrottlingExceptionOnGetTaskTemplate_callDeleteTaskTemplate() {
        final ArgumentCaptor<DeleteTaskTemplateRequest> deleteTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();

        when(proxyClient.client().deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture())).thenReturn(DeleteTaskTemplateResponse.builder().build());
        when(proxyClient.client().getTaskTemplate(any(GetTaskTemplateRequest.class))).thenReturn(GetTaskTemplateResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        verify(proxyClient.client()).deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture());
        verify(proxyClient.client(), times(1)).getTaskTemplate(any(GetTaskTemplateRequest.class));
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void handleDeleteRequest_clientThrowsExceptionOnDeleteTaskTemplate_Exception() {
        final ArgumentCaptor<DeleteTaskTemplateRequest> deleteTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTaskTemplateRequest.class);
        final ResourceModel model = ResourceModel.builder()
                .arn(TASK_TEMPLATE_ARN)
                .build();

        when(proxyClient.client().deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());
        when(proxyClient.client().getTaskTemplate(any(GetTaskTemplateRequest.class))).thenReturn(GetTaskTemplateResponse.builder().build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                deleteHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).deleteTaskTemplate(deleteTaskTemplateRequestArgumentCaptor.capture());
        verify(proxyClient.client(), times(1)).getTaskTemplate(any(GetTaskTemplateRequest.class));
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(deleteTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);
        verify(connectClient, times(2)).serviceName();
    }
}
