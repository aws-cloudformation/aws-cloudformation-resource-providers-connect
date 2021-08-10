package software.amazon.connect.hoursofoperation;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.HoursOfOperation;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                          final ResourceHandlerRequest<ResourceModel> request,
                                                                          final CallbackContext callbackContext,
                                                                          final ProxyClient<ConnectClient> proxyClient,
                                                                          final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String hoursOfOperationArn = model.getHoursOfOperationArn();

        logger.log(String.format("Invoked new ReadHoursOfOperationHandler with HoursOfOperation:%s", hoursOfOperationArn));

        if (!ArnHelper.isValidHoursOfOperationArn(hoursOfOperationArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Hours of operation Arn", hoursOfOperationArn)));
        }

        return proxy.initiate("connect::describeHoursOfOperation", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeHoursOfOperationRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeHoursOfOperation, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setHoursOfOperationProperties(model, response.hoursOfOperation())));
    }

    private DescribeHoursOfOperationRequest translateToDescribeHoursOfOperationRequest(final ResourceModel model) {
        return DescribeHoursOfOperationRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromHoursOfOperationArn(model.getHoursOfOperationArn()))
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .build();

    }

    private ResourceModel setHoursOfOperationProperties(final ResourceModel model, final HoursOfOperation hoursOfOperation) {
        final String instanceArn = ArnHelper.getInstanceArnFromHoursOfOperationArn(hoursOfOperation.hoursOfOperationArn());
        model.setInstanceArn(instanceArn);
        model.setName(model.getName());
        model.setDescription(model.getDescription());
        model.setTags(convertResourceTagsToSet(hoursOfOperation.tags()));
        model.setConfig(toHoursOfOperationConfig(hoursOfOperation.config()));
        return model;
    }

}
