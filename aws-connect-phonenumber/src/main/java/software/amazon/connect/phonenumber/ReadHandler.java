package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.ClaimedPhoneNumberSummary;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProxyClient;

public class ReadHandler extends BaseHandlerStd {

    /**
     * Using the specified resource model, calls DescribePhoneNumber API to describe a phone number.
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

        logger.log(String.format("Invoked ReadPhoneNumberHandler with PhoneNumber:%s", phoneNumberArn));
        if (!ArnHelper.isValidPhoneNumberArn(phoneNumberArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid PhoneNumber Arn", phoneNumberArn)));
        }

        return proxy.initiate("connect::describePhoneNumber", proxyClient, model, callbackContext)
                    .translateToServiceRequest(this::translateToDescribePhoneNumberRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describePhoneNumber, logger))
                    .done(response -> ProgressEvent.defaultSuccessHandler(setPhoneNumberProperties(model, response.claimedPhoneNumberSummary())));
    }

    /**
     * Returns a DescribePhoneNumberRequest built from the provided resource model.
     * @param model
     * @return DescribePhoneNumberRequest
     * */
    private DescribePhoneNumberRequest translateToDescribePhoneNumberRequest(final ResourceModel model) {
        return DescribePhoneNumberRequest
                .builder()
                .phoneNumberId(model.getPhoneNumberArn())
                .build();
    }

    /**
     * Returns a resource model built from a ClaimedPhoneNumberSummary.
     * @param model
     * @return ResourceModel
     * */
    private ResourceModel setPhoneNumberProperties(final ResourceModel model, final ClaimedPhoneNumberSummary phoneNumber) {
        model.setTargetArn(phoneNumber.targetArn());
        model.setDescription(phoneNumber.phoneNumberDescription());
        model.setType(phoneNumber.phoneNumberTypeAsString());
        model.setTags(convertResourceTagsToSet(phoneNumber.tags()));
        model.setAddress(phoneNumber.phoneNumber());
        model.setCountryCode(phoneNumber.phoneNumberCountryCodeAsString());
        return model;
    }
}
