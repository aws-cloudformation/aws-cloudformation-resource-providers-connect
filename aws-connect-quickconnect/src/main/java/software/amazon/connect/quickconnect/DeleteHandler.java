package software.amazon.connect.quickconnect;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteQuickConnectRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String quickConnectArn = model.getQuickConnectARN();

        logger.log(String.format("Invoked DeleteQuickConnectHandler with QuickConnect:%s", quickConnectArn));

        return proxy.initiate("connect::deleteQuickConnect", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteQuickConnectRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteQuickConnect, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteQuickConnectRequest translateToDeleteQuickConnectRequest(final ResourceModel model) {
        return DeleteQuickConnectRequest
                .builder()
                .instanceId(model.getInstanceId())
                .quickConnectId(model.getQuickConnectARN())
                .build();
    }
}
