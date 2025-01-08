package software.amazon.connect.hoursofoperation;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.HoursOfOperationOverride;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesRequest;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesResponse;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.DuplicateResourceException;
import software.amazon.awssdk.services.connect.model.LimitExceededException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.services.connect.model.InvalidParameterException;
import software.amazon.awssdk.services.connect.model.HoursOfOperationOverrideConfig;
import software.amazon.awssdk.services.connect.model.OverrideTimeSlice;

import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;


public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    public static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    public static final String THROTTLING_ERROR_CODE = "TooManyRequestsException";


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger);

    protected static void handleCommonExceptions(final Exception ex, final Logger logger) {
        if (ex instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(ex);
        } else if (ex instanceof InvalidParameterException || ex instanceof InvalidRequestException) {
            throw new CfnInvalidRequestException(ex);
        } else if (ex instanceof InternalServiceException) {
            throw new CfnServiceInternalErrorException(ex);
        } else if (ex instanceof DuplicateResourceException) {
            throw new CfnAlreadyExistsException(ex);
        } else if (ex instanceof LimitExceededException) {
            throw new CfnServiceLimitExceededException(ex);
        } else if (ex instanceof ConnectException && StringUtils.equals(THROTTLING_ERROR_CODE, ((ConnectException) ex).awsErrorDetails().errorCode())) {
            throw new CfnThrottlingException(ex);
        } else if (ex instanceof ConnectException && StringUtils.equals(ACCESS_DENIED_ERROR_CODE, ((ConnectException) ex).awsErrorDetails().errorCode())) {
            throw new CfnAccessDeniedException(ex);
        }
        logger.log(String.format("Exception in handler:%s", ex));
        throw new CfnGeneralServiceException(ex);
    }

    protected static <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT invoke(final RequestT request,
                                                                                                   final ProxyClient<ConnectClient> proxyClient,
                                                                                                   final Function<RequestT, ResponseT> requestFunction,
                                                                                                   final Logger logger) {
        ResponseT response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, requestFunction);
        } catch (Exception e) {
            handleCommonExceptions(e, logger);
        }
        return response;
    }

    protected static List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> translateToHoursOfOperationConfig(final ResourceModel model) {
        final List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfigList = new ArrayList<>();
        for (software.amazon.connect.hoursofoperation.HoursOfOperationConfig hoursOfOperationConfig : model.getConfig()) {
            hoursOfOperationConfigList.add(
                    software.amazon.awssdk.services.connect.model.HoursOfOperationConfig.builder()
                            .startTime(translateToHoursOfOperationTimeSlice(hoursOfOperationConfig.getStartTime()))
                            .endTime(translateToHoursOfOperationTimeSlice(hoursOfOperationConfig.getEndTime()))
                            .day(hoursOfOperationConfig.getDay())
                            .build()
            );
        }
        return hoursOfOperationConfigList;
    }

    protected static software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice translateToHoursOfOperationTimeSlices(final software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice time) {
        return software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice.builder()
                .hours(time.hours())
                .minutes(time.minutes())
                .build();
    }

    protected static software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice translateToHoursOfOperationTimeSlice(final software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice time) {
        return software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice.builder()
                .hours(time.getHours())
                .minutes(time.getMinutes())
                .build();
    }

    protected static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        return Optional.ofNullable(resourceTags)
                .map(tags -> tags.keySet().stream()
                        .map(key -> Tag.builder().key(key).value(resourceTags.get(key)).build())
                        .collect(Collectors.toSet()))
                .orElse(Sets.newHashSet());
    }

    public ListHoursOfOperationOverridesResponse listHoursOfOperationOverrides(final ResourceModel model,
                                                                               final ProxyClient<ConnectClient> proxyClient,
                                                                               String nextToken) {
        ListHoursOfOperationOverridesRequest request = ListHoursOfOperationOverridesRequest.builder()
                .instanceId(ArnHelper.getInstanceArnFromHoursOfOperationArn(model.getHoursOfOperationArn()))
                .hoursOfOperationId(model.getHoursOfOperationArn())
                .nextToken(nextToken)
                .build();
        return proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listHoursOfOperationOverrides);
    }

    protected static List<software.amazon.connect.hoursofoperation.HoursOfOperationOverride> translateToHOOPOverridesList(final List<HoursOfOperationOverride> hoursOfOperationOverrideList) {
        final List<software.amazon.connect.hoursofoperation.HoursOfOperationOverride> overridesList = new ArrayList<>();
        for (HoursOfOperationOverride hoursOfOperationOverride : hoursOfOperationOverrideList) {
            overridesList.add(software.amazon.connect.hoursofoperation.HoursOfOperationOverride.builder()
                    .hoursOfOperationOverrideId(hoursOfOperationOverride.hoursOfOperationOverrideId())
                    .overrideName(hoursOfOperationOverride.name())
                    .overrideDescription(hoursOfOperationOverride.description())
                    .effectiveFrom(hoursOfOperationOverride.effectiveFrom())
                    .effectiveTill(hoursOfOperationOverride.effectiveTill())
                    .overrideConfig(translateFromHOOPOverrideConfigModel(hoursOfOperationOverride))
                    .build()
            );
        }
        return overridesList;
    }

    protected static List<HoursOfOperationOverrideConfig> translateToHOOPOverrideConfigModel(final software.amazon.connect.hoursofoperation.HoursOfOperationOverride override) {
        final List<HoursOfOperationOverrideConfig> overrideConfigModelList = new ArrayList<>();
        for (software.amazon.connect.hoursofoperation.HoursOfOperationOverrideConfig hoopOverrideConfig : override.getOverrideConfig()) {
            overrideConfigModelList.add(HoursOfOperationOverrideConfig
                    .builder()
                    .startTime(translateToOverrideTimeSliceModel(hoopOverrideConfig.getStartTime()))
                    .endTime(translateToOverrideTimeSliceModel(hoopOverrideConfig.getEndTime()))
                    .day(hoopOverrideConfig.getDay())
                    .build()
            );
        }
        return overrideConfigModelList;
    }

    protected static Set<software.amazon.connect.hoursofoperation.HoursOfOperationOverrideConfig> translateFromHOOPOverrideConfigModel(final HoursOfOperationOverride hoursOfOperationOverride) {
        final Set<software.amazon.connect.hoursofoperation.HoursOfOperationOverrideConfig> overrideConfigSet= new HashSet<>();
        for (HoursOfOperationOverrideConfig hoopOverrideConfig : hoursOfOperationOverride.config()) {
            overrideConfigSet.add(software.amazon.connect.hoursofoperation.HoursOfOperationOverrideConfig.builder()
                    .day(hoopOverrideConfig.day().toString())
                    .startTime(translateFromOverrideTimeSliceModel(hoopOverrideConfig.startTime()))
                    .endTime(translateFromOverrideTimeSliceModel(hoopOverrideConfig.endTime()))
                    .build()
            );
        }
        return overrideConfigSet;
    }

    protected static software.amazon.connect.hoursofoperation.HoursOfOperationOverride translateFromSingleHOOPOverrideModel(final HoursOfOperationOverride override) {
        Set<software.amazon.connect.hoursofoperation.HoursOfOperationOverrideConfig> overrideConfig = translateFromHOOPOverrideConfigModel(override);
        return software.amazon.connect.hoursofoperation.HoursOfOperationOverride
                .builder()
                .hoursOfOperationOverrideId(override.hoursOfOperationOverrideId())
                .overrideName(override.name())
                .overrideDescription(override.description())
                .overrideConfig(overrideConfig)
                .effectiveFrom(override.effectiveFrom())
                .effectiveTill(override.effectiveTill())
                .build();
    }

    protected static OverrideTimeSlice translateToOverrideTimeSliceModel(final software.amazon.connect.hoursofoperation
            .OverrideTimeSlice overrideTime) {
        return OverrideTimeSlice.builder()
                .hours(overrideTime.getHours())
                .minutes(overrideTime.getMinutes())
                .build();
    }

    protected static software.amazon.connect.hoursofoperation.OverrideTimeSlice translateFromOverrideTimeSliceModel(final OverrideTimeSlice overrideTime) {
        return software.amazon.connect.hoursofoperation.OverrideTimeSlice.builder()
                .hours(overrideTime.hours())
                .minutes(overrideTime.minutes())
                .build();
    }
}
