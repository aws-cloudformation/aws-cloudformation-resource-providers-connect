package software.amazon.connect.user;

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
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
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

    protected static software.amazon.awssdk.services.connect.model.UserPhoneConfig translateToUserPhoneConfig(final ResourceModel model) {

        return software.amazon.awssdk.services.connect.model.UserPhoneConfig.builder()
                .afterContactWorkTimeLimit(model.getPhoneConfig().getAfterContactWorkTimeLimit())
                .autoAccept(model.getPhoneConfig().getAutoAccept())
                .deskPhoneNumber(model.getPhoneConfig().getDeskPhoneNumber())
                .phoneType(model.getPhoneConfig().getPhoneType())
                .build();
    }

    protected static software.amazon.awssdk.services.connect.model.UserIdentityInfo translateToUserIdentityInfo(final ResourceModel model) {

        return software.amazon.awssdk.services.connect.model.UserIdentityInfo.builder()
                .email(model.getIdentityInfo().getEmail())
                .firstName(model.getIdentityInfo().getFirstName())
                .lastName(model.getIdentityInfo().getLastName())
                .build();
    }

    protected static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        return Optional.ofNullable(resourceTags)
                .map(tags -> tags.keySet().stream()
                        .map(key -> Tag.builder().key(key).value(resourceTags.get(key)).build())
                        .collect(Collectors.toSet()))
                .orElse(Sets.newHashSet());
    }
}
