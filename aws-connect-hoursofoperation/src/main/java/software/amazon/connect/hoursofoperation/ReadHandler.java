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

import java.util.HashSet;
import java.util.List;
import java.util.Set;


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
        model.setName(hoursOfOperation.name());
        model.setDescription(hoursOfOperation.description());
        model.setTimeZone(hoursOfOperation.timeZone());
        model.setTags(convertResourceTagsToSet(hoursOfOperation.tags()));
        model.setConfig(translateToResourceModelConfig(hoursOfOperation.config()));
        return model;
    }

    private Set<HoursOfOperationConfig> translateToResourceModelConfig(final List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfig) {
        final Set<HoursOfOperationConfig> hoursOfOperationConfigSet = new HashSet<>();
        for (software.amazon.awssdk.services.connect.model.HoursOfOperationConfig config : hoursOfOperationConfig) {
            hoursOfOperationConfigSet.add(translateToResourceModelHoursOfOperationConfig(config));
        }
        return hoursOfOperationConfigSet;
    }

    private HoursOfOperationConfig translateToResourceModelHoursOfOperationConfig(final software.amazon.awssdk.services.connect.model.HoursOfOperationConfig config) {
        return HoursOfOperationConfig.builder()
                .day(config.day().toString())
                .startTime(translateToHoursOfOperationTimeSlices(config.startTime()))
                .endTime(translateToHoursOfOperationTimeSlices(config.endTime()))
                .build();
    }
}
