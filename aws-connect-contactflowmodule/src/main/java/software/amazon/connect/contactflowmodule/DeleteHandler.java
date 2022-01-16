package software.amazon.connect.contactflowmodule;

import software.amazon.awssdk.services.connect.model.DeleteContactFlowModuleRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.awssdk.services.connect.ConnectClient;
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
        ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Invoked DeleteContactFlowModuleHandler with Module:%s", model.getContactFlowModuleArn()));

        return proxy.initiate("connect::deleteContactFlowModule", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteContactFlowModuleRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteContactFlowModule, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteContactFlowModuleRequest translateToDeleteContactFlowModuleRequest(final ResourceModel model) {
        return DeleteContactFlowModuleRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromContactFlowModuleArn(model.getContactFlowModuleArn()))
                .contactFlowModuleId(model.getContactFlowModuleArn())
                .build();
    }
}
