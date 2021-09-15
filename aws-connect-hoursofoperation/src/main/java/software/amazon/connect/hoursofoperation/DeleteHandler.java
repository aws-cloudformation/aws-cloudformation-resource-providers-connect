package software.amazon.connect.hoursofoperation;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
        final String hoursOfOperationArn = model.getHoursOfOperationArn();

        logger.log(String.format("Invoked DeleteHoursOfOperationHandler with HoursOfOperationArn:%s", hoursOfOperationArn));

        if (!ArnHelper.isValidHoursOfOperationArn(hoursOfOperationArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Hours Of Operation Arn", hoursOfOperationArn)));
        }

        return proxy.initiate("connect::deleteHoursOfOperation", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteHoursOfOperationRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteHoursOfOperation, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteHoursOfOperationRequest translateToDeleteHoursOfOperationRequest(final ResourceModel model) {
        return DeleteHoursOfOperationRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromHoursOfOperationArn(model.getHoursOfOperationArn()))
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .build();
    }
}
