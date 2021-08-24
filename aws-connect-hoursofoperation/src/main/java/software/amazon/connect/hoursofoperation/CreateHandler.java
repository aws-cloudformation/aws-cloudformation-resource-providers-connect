package software.amazon.connect.hoursofoperation;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationResponse;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;

import java.util.Map;

public class CreateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();

        logger.log(String.format("Invoked CreateHoursOfOperationHandler with InstanceId:%s ", model.getInstanceArn()));

        return proxy.initiate("connect::createHoursOfOperation", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateHoursOfOperationRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createHoursOfOperation, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setHoursOfOperationIdentifier(model, response)));

    }

    private CreateHoursOfOperationRequest translateToCreateHoursOfOperationRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateHoursOfOperationRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription())
                .tags(tags)
                .config(translateToHoursOfOperationConfig(model))
                .timeZone(model.getTimeZone())
                .build();
    }

    private ResourceModel setHoursOfOperationIdentifier(final ResourceModel model, final CreateHoursOfOperationResponse createHoursOfOperationResponse) {
        model.setHoursOfOperationArn(createHoursOfOperationResponse.hoursOfOperationArn());
        return model;
    }
}
