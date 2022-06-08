package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.model.ReleasePhoneNumberRequest;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    /**
     * Using the specified resource model, calls ReleasePhoneNumber API to release a phone number.
     * @param proxy
     * @param request
     * @param callbackContext
     * @param proxyClient
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String phoneNumberArn = model.getPhoneNumberArn();

        logger.log(String.format("Invoked DeletePhoneNumberHandler with PhoneNumber:%s", phoneNumberArn));

        if (!ArnHelper.isValidPhoneNumberArn(phoneNumberArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid PhoneNumber ARN", phoneNumberArn)));
        }

        return proxy.initiate("connect::releasePhoneNumber", proxyClient, model, callbackContext)
                    .translateToServiceRequest(this::translateToReleasePhoneNumberRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::releasePhoneNumber, logger))
                    .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    /**
     * Returns a ReleasePhoneNumberRequest built from the provided resource model.
     * @param model
     * @return ReleasePhoneNumberRequest
     * */
    private ReleasePhoneNumberRequest translateToReleasePhoneNumberRequest(final ResourceModel model) {
        return ReleasePhoneNumberRequest
                .builder()
                .phoneNumberId(model.getPhoneNumberArn())
                .build();
    }
}
