package software.amazon.connect.quickconnect;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateQuickConnectRequest;
import software.amazon.awssdk.services.connect.model.CreateQuickConnectResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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

        logger.log(String.format("Invoked CreateQuickConnectHandler with InstanceId:%s ,QuickConnectName:%s, " +
                "QuickConnectType:%s", model.getInstanceArn(), model.getName(), model.getQuickConnectConfig().getQuickConnectType()));

        return proxy.initiate("connect::createQuickConnect", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateQuickConnectRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createQuickConnect, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setQuickConnectIdentifiers(model, response)));
    }

    private CreateQuickConnectRequest translateToCreateQuickConnectRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateQuickConnectRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription())
                .tags(tags)
                .quickConnectConfig(translateToQuickConnectConfig(model))
                .build();
    }

    private ResourceModel setQuickConnectIdentifiers(final ResourceModel model, final CreateQuickConnectResponse createQuickConnectResponse) {
        model.setQuickConnectArn(createQuickConnectResponse.quickConnectARN());
        return model;
    }
}
