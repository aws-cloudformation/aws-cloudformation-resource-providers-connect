package software.amazon.connect.hoursofoperation;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.DuplicateResourceException;
import software.amazon.awssdk.services.connect.model.LimitExceededException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.services.connect.model.InvalidParameterException;
import software.amazon.awssdk.services.connect.model.HoursOfOperationTimeSlice;
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

import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


public abstract class BaseHandlerStd extends BaseHandler<CallbackContext>  {
    private static final String MISSING_MANDATORY_PARAMETER = "Required parameter missing %s";
    private static final String INVALID_PARAMETER_FOR_TYPE = "Invalid Parameter %s for type %s";
    private static final String HOURS_OF_OPERATION_ID = "HoursOfOperationId";

    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    private static final String THROTTLING_ERROR_CODE = "TooManyRequestsException";


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
        for (HoursOfOperationConfig hoursOfOperationConfig : model.getConfig()) {
            hoursOfOperationConfigList.add(
                    software.amazon.awssdk.services.connect.model.HoursOfOperationConfig.builder()
                    .startTime(toHoursOfOperationTimeSlice(hoursOfOperationConfig.getStartTime()))
                    .endTime(toHoursOfOperationTimeSlice(hoursOfOperationConfig.getEndTime()))
                    .day(hoursOfOperationConfig.getDay())
                    .build()
            );
        }
        return hoursOfOperationConfigList;
    }

    protected Set<HoursOfOperationConfig> translateToResourceModelConfig(final List<software.amazon.awssdk.services.connect.model.HoursOfOperationConfig> hoursOfOperationConfig) {
        final Set<HoursOfOperationConfig> hoursOfOperationConfigSet = new HashSet<>();
        for (software.amazon.awssdk.services.connect.model.HoursOfOperationConfig  config : hoursOfOperationConfig){
            hoursOfOperationConfigSet.add(translateToResourceModelHoursOfOperationConfig(config));

        }
        return hoursOfOperationConfigSet;
    }

    protected static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        return Optional.ofNullable(resourceTags)
                .map(tags -> tags.keySet().stream()
                        .map(key -> Tag.builder().key(key).value(resourceTags.get(key)).build())
                        .collect(Collectors.toSet()))
                .orElse(Sets.newHashSet());
    }

    protected static void requireNotNull(final Object object, final String parameterName) {
        if (Objects.isNull(object)) {
            throw new CfnInvalidRequestException(String.format(MISSING_MANDATORY_PARAMETER, parameterName));
        }
    }

    protected static void requireNullForType(final Object object, final String parameterName, final String type) {
        if (Objects.nonNull(object)) {
            throw new CfnInvalidRequestException(String.format(INVALID_PARAMETER_FOR_TYPE, parameterName, type));
        }
    }

    private HoursOfOperationConfig translateToResourceModelHoursOfOperationConfig(final software.amazon.awssdk.services.connect.model.HoursOfOperationConfig config) {
        return HoursOfOperationConfig.builder()
                .day(config.day().toString())
                .startTime(toHoursOfOperationTimeSlices(config.startTime()))
                .endTime(toHoursOfOperationTimeSlices(config.endTime()))
                .build();
    }

    private software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice toHoursOfOperationTimeSlices(final HoursOfOperationTimeSlice time) {
        return software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice.builder()
                .hours(time.hours())
                .minutes(time.minutes())
                .build();
    }

    private static HoursOfOperationTimeSlice toHoursOfOperationTimeSlice(final software.amazon.connect.hoursofoperation.HoursOfOperationTimeSlice time) {
        HoursOfOperationTimeSlice hoursOfOperationTimeSlice = HoursOfOperationTimeSlice.builder()
                .hours(time.getHours())
                .minutes(time.getMinutes())
                .build();
        return hoursOfOperationTimeSlice;
    }
}
