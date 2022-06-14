package software.amazon.connect.tasktemplate;

import com.amazonaws.util.StringUtils;
import org.apache.commons.collections4.CollectionUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.GetTaskTemplateResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Objects;

public class ReadHandler extends BaseHandlerStd{

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

        return proxy.initiate("connect::getTaskTemplate", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToGetTaskTemplateRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::getTaskTemplate, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setTaskTemplateProperties(model, response, logger)));
    }

    private GetTaskTemplateRequest translateToGetTaskTemplateRequest(final ResourceModel model) {
        return GetTaskTemplateRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromTaskTemplateArn(model.getArn()))
                .taskTemplateId(model.getArn())
                .build();
    }

    private ResourceModel setTaskTemplateProperties(final ResourceModel model, final GetTaskTemplateResponse response, Logger logger) {
        model.setInstanceArn(ArnHelper.getInstanceArnFromTaskTemplateArn(response.arn()));
        model.setName(response.name());
        model.setDescription(response.description());
        if(StringUtils.isNullOrEmpty(response.contactFlowId())){
            model.setContactFlowArn(response.contactFlowId());
        }
        if(!Objects.isNull(response.constraints())){
            model.setConstraints(ResourceModelMapper.toResourceModelConstraint(response.constraints()));
        }
        if(!Objects.isNull(response.defaults())) {
            model.setDefaults(ResourceModelMapper.toResourceModelDefaults(response.defaults()));
        }
        if(CollectionUtils.isNotEmpty(response.fields())) {
            model.setFields(ResourceModelMapper.toResourceModelFields(response.fields()));
        }
        model.setStatus(response.statusAsString());
        model.setTags(convertResourceTagsToSet(response.tags()));
        return model;
    }
}