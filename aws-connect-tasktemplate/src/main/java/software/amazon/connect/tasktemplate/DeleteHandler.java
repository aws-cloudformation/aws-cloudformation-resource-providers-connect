package software.amazon.connect.tasktemplate;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        String taskTemplateArn = model.getArn();

        if(!ArnHelper.isValidTaskTemplateArn(taskTemplateArn)){
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid task template Arn", taskTemplateArn)));
        }

        // Since TaskTemplatesService does not return ResourceNotFound for Delete request on deleted task template, we will check if task template was
        // deleted and throw ResourceNotFoundException before sending the Delete request
        try {
            proxy.initiate("connect::GetTaskTemplate", proxyClient, model, callbackContext)
                    .translateToServiceRequest(this::translateToGetTaskTemplateRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::getTaskTemplate, logger))
                    .done(response -> ProgressEvent.defaultSuccessHandler(null));
        }
        catch (CfnNotFoundException e){
            throw e;
        }
        catch (Exception e){
            // Just ignore other exceptions from GetTaskTemplate.
            logger.log(e.getMessage());

        }

        return proxy.initiate("connect::deleteTaskTemplate", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteTaskTemplateRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteTaskTemplate, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteTaskTemplateRequest translateToDeleteTaskTemplateRequest(final ResourceModel model) {
        return DeleteTaskTemplateRequest
                .builder()
                .taskTemplateId(ArnHelper.getIdFromArn(model.getArn()))
                .instanceId(ArnHelper.getInstanceArnFromTaskTemplateArn(model.getArn()))
                .build();
    }

    private GetTaskTemplateRequest translateToGetTaskTemplateRequest(final ResourceModel model) {
        return GetTaskTemplateRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromTaskTemplateArn(model.getArn()))
                .taskTemplateId(model.getArn())
                .build();
    }

}