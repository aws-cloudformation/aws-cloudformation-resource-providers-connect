package software.amazon.connect.contactflowmodule;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowModuleRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.awssdk.services.connect.model.ContactFlowModule;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                          final ResourceHandlerRequest<ResourceModel> request,
                                                                          final CallbackContext callbackContext,
                                                                          final ProxyClient<ConnectClient> proxyClient,
                                                                          final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String contactFlowModuleArn = model.getContactFlowModuleArn();

        logger.log(String.format("Invoked new ReadContactFlowModuleHandler with Module:%s", contactFlowModuleArn));

        if (!ArnHelper.isValidContactFlowModuleArn(contactFlowModuleArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid contact flow module Arn", contactFlowModuleArn)));
        }
        return proxy.initiate("connect::describeContactFlowModule", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeContactFlowModuleRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeContactFlowModule, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setContactFlowModuleProperties(model, response.contactFlowModule())));
    }

    private DescribeContactFlowModuleRequest translateToDescribeContactFlowModuleRequest(final ResourceModel model) {
        return DescribeContactFlowModuleRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromContactFlowModuleArn(model.getContactFlowModuleArn()))
                .contactFlowModuleId(model.getContactFlowModuleArn())
                .build();
    }

    private ResourceModel setContactFlowModuleProperties(final ResourceModel model, final ContactFlowModule contactFlowModule) {
        final String instanceArn = ArnHelper.getInstanceArnFromContactFlowModuleArn(contactFlowModule.arn());
        model.setInstanceArn(instanceArn);
        model.setContent(contactFlowModule.content());
        model.setName(contactFlowModule.name());
        model.setDescription(contactFlowModule.description());
        model.setTags(convertResourceTagsToSet(contactFlowModule.tags()));
        model.setState(contactFlowModule.state().toString());
        model.setStatus(contactFlowModule.status().toString());
        return model;
    }

}
