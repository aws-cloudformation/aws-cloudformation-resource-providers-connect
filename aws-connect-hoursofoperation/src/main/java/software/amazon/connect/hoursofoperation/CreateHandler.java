package software.amazon.connect.hoursofoperation;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequestOverrideConfiguration;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationOverrideResponse;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateHandler extends BaseHandlerStd {
    public static final String RESOURCE_ID_HEADER = "x-amz-resource-id";

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();

        if (model == null) {
            throw new CfnInvalidRequestException("DesiredResourceState is null, unable to get instanceArn to create HoursOfOperation");
        }

        logger.log(String.format("Invoked CreateHoursOfOperationHandler with InstanceArn:%s ", model.getInstanceArn()));
        ProgressEvent<ResourceModel, CallbackContext> hoopProgressEvent = ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> createHoursOfOperation(proxy, proxyClient, model, tags, callbackContext, logger));

        List<software.amazon.connect.hoursofoperation.HoursOfOperationOverride> overrideList = model.getHoursOfOperationOverrides();
        if(overrideList != null) {
            for (int i = 0 ; i < overrideList.size(); i++) {
                int index = i;
                hoopProgressEvent = hoopProgressEvent.then(progress -> createHoursOfOperationOverride(proxy, proxyClient,
                        model, callbackContext, index, overrideList.get(index), logger));
            }
        }
        return hoopProgressEvent.then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createHoursOfOperation(final AmazonWebServicesClientProxy proxy,
                                                                                 final ProxyClient<ConnectClient> proxyClient,
                                                                                 final ResourceModel model,
                                                                                 final Map<String, String> tags,
                                                                                 final CallbackContext callbackContext,
                                                                                 final Logger logger){
        logger.log("Invoking CreateHoursOfOperation operation");
        return proxy.initiate("connect::createHoursOfOperation", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateHoursOfOperationRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createHoursOfOperation, logger))
                .done(response -> ProgressEvent.progress(setHoursOfOperationIdentifier(model, response), callbackContext));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createHoursOfOperationOverride(final AmazonWebServicesClientProxy proxy,
                                                                                         final ProxyClient<ConnectClient> proxyClient,
                                                                                         final ResourceModel model,
                                                                                         final CallbackContext callbackContext,
                                                                                         final int index,
                                                                                         final HoursOfOperationOverride override,
                                                                                         final Logger logger) {
        logger.log("Invoking CreateHoursOfOperationOverride operation");
        return proxy.initiate("connect::createHoursOfOperationOverride", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateHoursOfOperationOverrideRequest(resourceModel, override, logger))
                .backoffDelay(Constant.of()
                        .delay(Duration.ofMillis(500))
                        .timeout(Duration.ofSeconds(2))
                        .build())
                .makeServiceCall((req, clientProxy) -> {
                    CreateHoursOfOperationOverrideResponse response = null;
                    try {
                        response = clientProxy.injectCredentialsAndInvokeV2(req, clientProxy.client()::createHoursOfOperationOverride);
                    } catch (Exception ex) {
                        if (isThrottlingException(ex)) {
                            throw ex;
                        } else {
                            handleCommonExceptions(ex, logger);
                        }
                    }
                    return response;
                })
                .retryErrorFilter((_req, _ex, _client, _model, _cxt) -> isThrottlingException(_ex))
                .progress();
    }

    private CreateHoursOfOperationRequest translateToCreateHoursOfOperationRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateHoursOfOperationRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription())
                .config(translateToHoursOfOperationConfig(model))
                .timeZone(model.getTimeZone())
                .tags(tags)
                .build();
    }

    private CreateHoursOfOperationOverrideRequest translateToCreateHoursOfOperationOverrideRequest(final ResourceModel model,
                                                                                                   final HoursOfOperationOverride override,
                                                                                                   final Logger logger) {
        logger.log(String.format("Translating request for override id: %s", override.getHoursOfOperationOverrideId()));
        Map<String, List<String>> overrideHeader = new HashMap<>();
        if (override.getHoursOfOperationOverrideId() != null) {
            overrideHeader.put(RESOURCE_ID_HEADER, new ArrayList<>(Collections.singletonList
                    (override.getHoursOfOperationOverrideId())));
        }
        CreateHoursOfOperationOverrideRequest createRequest = CreateHoursOfOperationOverrideRequest.builder()
                .instanceId(model.getInstanceArn())
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .name(override.getOverrideName())
                .description(override.getOverrideDescription())
                .effectiveFrom(override.getEffectiveFrom())
                .effectiveTill(override.getEffectiveTill())
                .config(translateToHOOPOverrideConfigModel(override))
                .overrideConfiguration(AwsRequestOverrideConfiguration.builder()
                        .headers(overrideHeader).build())
                .build();
        return createRequest;
    }

    private ResourceModel setHoursOfOperationIdentifier(final ResourceModel model, final CreateHoursOfOperationResponse createHoursOfOperationResponse) {
        model.setHoursOfOperationArn(createHoursOfOperationResponse.hoursOfOperationArn());
        return model;
    }

    private boolean isThrottlingException(Exception ex) {
        return (ex instanceof ConnectException && StringUtils.equals(THROTTLING_ERROR_CODE, ((ConnectException) ex).awsErrorDetails().errorCode()));
    }
}
