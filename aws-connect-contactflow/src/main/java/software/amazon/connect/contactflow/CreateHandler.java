package software.amazon.connect.contactflow;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ContactFlowType;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.awssdk.services.connect.model.CreateContactFlowRequest;
import software.amazon.awssdk.services.connect.model.CreateContactFlowResponse;

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

        logger.log(String.format("Invoked CreateContactFlowHandler with Instance:%s, ContactFlowName:%s "
                , model.getInstanceArn(), model.getName()));

        return proxy.initiate("connect::createContactFlow", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateContactFlowRequest(resourceModel, tags, logger))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createContactFlow, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setContactFlowIdentifier(model, response)));

    }

    private CreateContactFlowRequest translateToCreateContactFlowRequest(final ResourceModel model, final Map<String, String> tags, Logger logger) {
        return CreateContactFlowRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .type(model.getType())
                .name(model.getName())
                .description(model.getDescription())
                .content(model.getContent())
                .tags(tags)
                .build();
    }

    private ResourceModel setContactFlowIdentifier(final ResourceModel model,
                                                  final CreateContactFlowResponse createContactFlowResponse) {
        model.setContactFlowArn(createContactFlowResponse.contactFlowArn());
        return model;
    }

}
