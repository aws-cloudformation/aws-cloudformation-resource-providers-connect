package software.amazon.connect.tasktemplate;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.UpdateTaskTemplateResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
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
public class UpdateHandlerTest extends BaseTest{
    protected software.amazon.connect.tasktemplate.UpdateHandler updateHandler;

    @BeforeEach
    public void setup(){
        super.setup();
        updateHandler = new UpdateHandler();
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleUpdateRequest_Success() {
        final ArgumentCaptor<UpdateTaskTemplateRequest> updateTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateTaskTemplateRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);

        final UpdateTaskTemplateResponse updateTaskTemplateResponse = UpdateTaskTemplateResponse.builder().build();
        when(proxyClient.client().updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture())).thenReturn(updateTaskTemplateResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);


        ResourceModel designedState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        ResourceModel previousState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        previousState.setStatus("Inactive");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(designedState)
                .previousResourceState(previousState)
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();
        final ProgressEvent<ResourceModel, CallbackContext> response = updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();


        verify(proxyClient.client()).updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture());
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).containsAll(TAGS_TWO.keySet());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(connectClient, times(3)).serviceName();

    }

    @Test
    public void handleUpdateRequest_InvalidInstanceArn_CfnInvalidRequestException() {
        ResourceModel designedState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        designedState.setInstanceArn(INSTANCE_ARN_2);
        ResourceModel previousState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        previousState.setStatus("Inactive");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(designedState)
                .previousResourceState(previousState)
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnInvalidRequestException.class, () ->
                updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        verify(connectClient, never()).serviceName();
    }

    @Test
    public void handleUpdateRequest_updateTaskTemplateThrowsException_Exception() {
        final ArgumentCaptor<UpdateTaskTemplateRequest> updateTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateTaskTemplateRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);

        final UpdateTaskTemplateResponse updateTaskTemplateResponse = UpdateTaskTemplateResponse.builder().build();
        when(proxyClient.client().updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        ResourceModel designedState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        ResourceModel previousState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        previousState.setStatus("Inactive");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(designedState)
                .previousResourceState(previousState)
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();
        assertThrows(CfnGeneralServiceException.class, () -> updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client(), times(1)).updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture());
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);

        verify(proxyClient.client(), never()).untagResource(untagResourceRequestArgumentCaptor.capture());

        verify(proxyClient.client(), never()).tagResource(tagResourceRequestArgumentCaptor.capture());

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void handleUpdateRequest_unTagThrowsException_Exception() {
        final ArgumentCaptor<UpdateTaskTemplateRequest> updateTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateTaskTemplateRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);

        final UpdateTaskTemplateResponse updateTaskTemplateResponse = UpdateTaskTemplateResponse.builder().build();
        when(proxyClient.client().updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture())).thenReturn(updateTaskTemplateResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());


        ResourceModel designedState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        ResourceModel previousState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        previousState.setStatus("Inactive");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(designedState)
                .previousResourceState(previousState)
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();
        assertThrows(CfnGeneralServiceException.class, () ->  updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));


        verify(proxyClient.client(), times(1)).updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture());
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);

        verify(proxyClient.client(), times(1)).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).containsAll(TAGS_TWO.keySet());

        verify(proxyClient.client(), never()).tagResource(tagResourceRequestArgumentCaptor.capture());

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void handleUpdateRequest_tagThrowsException_Exception() {
        final ArgumentCaptor<UpdateTaskTemplateRequest> updateTaskTemplateRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateTaskTemplateRequest.class);

        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);

        final UpdateTaskTemplateResponse updateTaskTemplateResponse = UpdateTaskTemplateResponse.builder().build();
        when(proxyClient.client().updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture())).thenReturn(updateTaskTemplateResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());



        ResourceModel designedState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        ResourceModel previousState = buildTaskTemplateDesiredStateResourceModel(TASK_TEMPLATE_ARN);
        previousState.setStatus("Inactive");
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(designedState)
                .previousResourceState(previousState)
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();
        assertThrows(CfnGeneralServiceException.class, () ->  updateHandler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));


        verify(proxyClient.client(), times(1)).updateTaskTemplate(updateTaskTemplateRequestArgumentCaptor.capture());
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateTaskTemplateRequestArgumentCaptor.getValue().taskTemplateId()).isEqualTo(TASK_TEMPLATE_ID);

        verify(proxyClient.client(), times(1)).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).containsAll(TAGS_TWO.keySet());

        verify(proxyClient.client(), times(1)).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(TASK_TEMPLATE_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(connectClient, times(3)).serviceName();
    }
}
