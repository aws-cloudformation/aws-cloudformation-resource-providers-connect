package software.amazon.connect.contactflowmodule;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.awssdk.services.connect.model.CreateContactFlowModuleRequest;
import software.amazon.awssdk.services.connect.model.CreateContactFlowModuleResponse;

import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Map;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();

        logger.log(String.format("Invoked CreateCreateContactFlowModuleHandler with Instance:%s, ModuleName:%s "
                , model.getInstanceArn(), model.getName()));

        return proxy.initiate("connect::createContactFlowModule", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateContactFlowModuleRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createContactFlowModule, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setContactFlowModuleIdentifier(model, response)));

    }

    private CreateContactFlowModuleRequest translateToCreateContactFlowModuleRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateContactFlowModuleRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription())
                .content(model.getContent())
                .tags(tags)
                .build();
    }

    private ResourceModel setContactFlowModuleIdentifier(final ResourceModel model, final CreateContactFlowModuleResponse createContactFlowModuleResponse) {
        model.setContactFlowModuleArn(createContactFlowModuleResponse.arn());
        return model;
    }
}
