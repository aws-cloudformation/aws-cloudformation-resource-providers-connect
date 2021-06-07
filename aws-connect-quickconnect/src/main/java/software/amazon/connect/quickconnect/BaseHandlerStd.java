package software.amazon.connect.quickconnect;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.services.connect.model.DuplicateResourceException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.InvalidParameterException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.LimitExceededException;
import software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig;
import software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig;
import software.amazon.awssdk.services.connect.model.QuickConnectConfig;
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.UserQuickConnectConfig;
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    private static final String MISSING_MANDATORY_PARAMETER = "Required parameter missing %s";
    private static final String QUICK_CONNECT_USER_CONFIG = "UserConfig";
    private static final String QUICK_CONNECT_QUEUE_CONFIG = "QueueConfig";
    private static final String QUICK_CONNECT_PHONE_CONFIG = "PhoneConfig";
    private static final String USER_ID = "UserId";
    private static final String QUEUE_ID = "QueueId";
    private static final String CONTACT_FLOW_ID = "ContactFlowId";
    private static final String PHONE_NUMBER = "PhoneNumber";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    private static final String THROTTLING_ERROR_CODE = "TooManyRequestsException";
    private static final String INVALID_QUICK_CONNECT_TYPE = "Invalid QuickConnectType: %s";

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

    protected static software.amazon.awssdk.services.connect.model.QuickConnectConfig translateToQuickConnectConfig(final ResourceModel model) {
        final String quickConnectType = model.getQuickConnectConfig().getQuickConnectType();
        if (quickConnectType.equals(QuickConnectType.USER.toString())) {
            requireNotNull(model.getQuickConnectConfig().getUserConfig(), QUICK_CONNECT_USER_CONFIG);
            requireNotNull(model.getQuickConnectConfig().getUserConfig().getUserArn(), USER_ID);
            requireNotNull(model.getQuickConnectConfig().getUserConfig().getContactFlowArn(), CONTACT_FLOW_ID);
            final software.amazon.awssdk.services.connect.model.UserQuickConnectConfig userQuickConnectConfig = UserQuickConnectConfig.builder()
                    .userId(model.getQuickConnectConfig().getUserConfig().getUserArn())
                    .contactFlowId(model.getQuickConnectConfig().getUserConfig().getContactFlowArn())
                    .build();
            return software.amazon.awssdk.services.connect.model.QuickConnectConfig.builder()
                    .quickConnectType(quickConnectType)
                    .userConfig(userQuickConnectConfig)
                    .build();
        } else if (quickConnectType.equals(QuickConnectType.QUEUE.toString())) {
            requireNotNull(model.getQuickConnectConfig().getQueueConfig(), QUICK_CONNECT_QUEUE_CONFIG);
            requireNotNull(model.getQuickConnectConfig().getQueueConfig().getQueueArn(), QUEUE_ID);
            requireNotNull(model.getQuickConnectConfig().getQueueConfig().getContactFlowArn(), CONTACT_FLOW_ID);
            final software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig queueQuickConnectConfig = QueueQuickConnectConfig.builder()
                    .queueId(model.getQuickConnectConfig().getQueueConfig().getQueueArn())
                    .contactFlowId(model.getQuickConnectConfig().getQueueConfig().getContactFlowArn())
                    .build();
            return software.amazon.awssdk.services.connect.model.QuickConnectConfig.builder()
                    .quickConnectType(quickConnectType)
                    .queueConfig(queueQuickConnectConfig)
                    .build();
        } else if (quickConnectType.equals(QuickConnectType.PHONE_NUMBER.toString())) {
            requireNotNull(model.getQuickConnectConfig().getPhoneConfig(), QUICK_CONNECT_PHONE_CONFIG);
            requireNotNull(model.getQuickConnectConfig().getPhoneConfig().getPhoneNumber(), PHONE_NUMBER);
            final software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig phoneNumberQuickConnectConfig = PhoneNumberQuickConnectConfig.builder()
                    .phoneNumber(model.getQuickConnectConfig().getPhoneConfig().getPhoneNumber())
                    .build();
            return QuickConnectConfig.builder()
                    .quickConnectType(quickConnectType)
                    .phoneConfig(phoneNumberQuickConnectConfig)
                    .build();
        }
        throw new CfnInvalidRequestException(String.format(INVALID_QUICK_CONNECT_TYPE, quickConnectType));
    }

    protected static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        return Optional.ofNullable(resourceTags)
                .map(tags -> tags.keySet().stream()
                        .map(key -> Tag.builder().key(key).value(resourceTags.get(key)).build())
                        .collect(Collectors.toSet()))
                .orElse(Sets.newHashSet());
    }

    protected static void requireNotNull(final Object object, final String parameterName) {
        if (object == null) {
            throw new CfnInvalidRequestException(String.format(MISSING_MANDATORY_PARAMETER, parameterName));
        }
    }
}
