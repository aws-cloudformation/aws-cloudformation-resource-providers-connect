package software.amazon.connect.hoursofoperation;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.HoursOfOperation;
import software.amazon.awssdk.services.connect.model.HoursOfOperationOverride;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesRequest;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesResponse;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.utils.StringUtils;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReadHandler extends BaseHandlerStd {
    final ResourceModel model = null;

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
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("The Hours of operation Arn provided in the request is not valid")));
        }
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> describeHoursOfOperation(proxy, proxyClient, model, callbackContext, logger))
                .then(progress -> listHoursOfOperationOverrides(proxy, proxyClient, request, model, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeHoursOfOperation(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel model,
                                                                                 final CallbackContext callbackContext,
                                                                                 final Logger logger){
        logger.log(String.format("Invoking DescribeHoursOfOperation for HoursOfOperation %s", model.getHoursOfOperationArn()));
        return proxy.initiate("connect::describeHoursOfOperation", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeHoursOfOperationRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeHoursOfOperation, logger))
                .done(response -> ProgressEvent.progress(setHoursOfOperationProperties(model, response.hoursOfOperation()), callbackContext));
    }

    private ProgressEvent<ResourceModel, CallbackContext> listHoursOfOperationOverrides(final AmazonWebServicesClientProxy proxy,
                                                                                        final ProxyClient<ConnectClient> proxyClient,
                                                                                        final ResourceHandlerRequest<ResourceModel> request,
                                                                                        final ResourceModel model,
                                                                                        final CallbackContext callbackContext,
                                                                                        final Logger logger){
        logger.log(String.format("Invoking ListHoursOfOperationOverrides operation for HoursOfOperation %s", model.getHoursOfOperationArn()));

        return proxy.initiate("connect::listHoursOfOperationOverrides", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToListHoursOfOperationOverridesRequest)
                .makeServiceCall((req, clientProxy) -> {
                    List<HoursOfOperationOverride> hoursOfOperationOverrideList = new ArrayList<>();
                    String nextToken = null;
                    try {
                        do {
                            ListHoursOfOperationOverridesResponse response = listHoursOfOperationOverrides(model, proxyClient, nextToken);
                            if (response == null || response.hoursOfOperationOverrideList() == null) {
                                return hoursOfOperationOverrideList;
                            }
                            nextToken = response.nextToken();
                            hoursOfOperationOverrideList.addAll(response.hoursOfOperationOverrideList());
                        } while (nextToken != null);
                    } catch (Exception ex) {
                        if (ex instanceof ConnectException && StringUtils.equals(ACCESS_DENIED_ERROR_CODE, ((ConnectException) ex).awsErrorDetails().errorCode())) {
                            return null;
                        } else {
                            handleCommonExceptions(ex, logger);
                        }
                    }
                    return hoursOfOperationOverrideList;
                }).done(response -> {
                    if (response != null) {
                        return ProgressEvent.progress(setHoursOfOperationOverrides(model, response), callbackContext);
                    } else {
                        return ProgressEvent.progress(model, callbackContext);
                    }
                });
    }

    private ListHoursOfOperationOverridesRequest translateToListHoursOfOperationOverridesRequest(final ResourceModel model) {
        return ListHoursOfOperationOverridesRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromHoursOfOperationArn(model.getHoursOfOperationArn()))
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .nextToken(null)
                .build();
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

    private ResourceModel setHoursOfOperationOverrides(final ResourceModel model, final List<HoursOfOperationOverride> overrideList) {
        model.setHoursOfOperationOverrides(translateToHOOPOverridesList(overrideList));
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
