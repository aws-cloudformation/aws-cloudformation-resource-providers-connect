package software.amazon.connect.tasktemplate;

import org.apache.commons.collections4.CollectionUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateTaskTemplateRequest;
import software.amazon.awssdk.services.connect.model.CreateTaskTemplateResponse;
import com.amazonaws.util.StringUtils;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;
import java.util.Objects;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        String instanceArn = model.getInstanceArn();
        final Map<String, String> tags = request.getDesiredResourceTags();

        if(!ArnHelper.isValidInstanceArn(instanceArn)){
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid instance Arn", instanceArn)));
        }

        return proxy.initiate("connect::createTaskTemplate", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateTaskTemplateRequest(resourceModel, tags, logger))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createTaskTemplate, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setTaskTemplateIdentifier(model, response)));

    }

    private CreateTaskTemplateRequest translateToCreateTaskTemplateRequest(final ResourceModel model, final Map<String, String> tags, Logger logger) {
        logger.log(String.format("translateToCreateTaskTemplateRequest for template %s from instance %s", model.getName(), model.getInstanceArn()));
        CreateTaskTemplateRequest.Builder builder = CreateTaskTemplateRequest.builder();
        builder.instanceId(ArnHelper.getIdFromArn(model.getInstanceArn()));
        builder.name(model.getName());
        builder.description(model.getDescription());
        if(!StringUtils.isNullOrEmpty(model.getContactFlowArn())){
            builder.contactFlowId(model.getContactFlowArn());
        }
        builder.clientToken(model.getClientToken());
        if(!Objects.isNull(model.getConstraints())){
            builder.constraints(TaskTemplateMapper.toTaskTemplateConstraints(model.getConstraints()));
        }
        if(!Objects.isNull(model.getDefaults())){
            builder.defaults(TaskTemplateMapper.toTaskTemplateDefaults(model.getDefaults()));
        }
        if(CollectionUtils.isNotEmpty(model.getFields())){
            builder.fields(TaskTemplateMapper.toTaskTemplateFields(model.getFields()));
        }
        builder.status(model.getStatus());
        return builder.build();
    }

    private ResourceModel setTaskTemplateIdentifier(final ResourceModel model,
                                                   final CreateTaskTemplateResponse createTaskTemplateResponse) {
        model.setArn(createTaskTemplateResponse.arn());
        return model;
    }
}
