package software.amazon.connect.contactflow;

import software.amazon.awssdk.services.connect.model.DeleteContactFlowRequest;
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

        logger.log(String.format("Invoked DeleteContactFlowHandler with ContactFlow:%s", model.getContactFlowArn()));

        return proxy.initiate("connect::deleteContactFlow", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteContactFlowRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteContactFlow, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteContactFlowRequest translateToDeleteContactFlowRequest(final ResourceModel model) {
        return DeleteContactFlowRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromContactFlowArn(model.getContactFlowArn()))
                .contactFlowId(model.getContactFlowArn())
                .build();
    }
}
