package software.amazon.connect.instance;

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
import software.amazon.awssdk.services.connect.model.ServiceQuotaExceededException;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;

import java.util.Objects;
import java.util.function.Function;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    public static final String SAML = "SAML";
    public static final String CONNECT_MANAGED = "CONNECT_MANAGED";
    public static final String EXISTING_DIRECTORY = "EXISTING_DIRECTORY";

    public static final String INSTANCE_STATUS_CREATION_IN_PROGRESS = "CREATION_IN_PROGRESS";
    public static final String INSTANCE_STATUS_ACTIVE = "ACTIVE";
    public static final String INSTANCE_STATUS_CREATION_FAILED = "CREATION_FAILED";

    public static final String INSTANCE_ALIAS = "InstanceAlias";
    public static final String DIRECTORY_ID = "DIRECTORY_ID";

    private static final String MISSING_MANDATORY_PARAMETER = "Required parameter missing %s";
    private static final String INVALID_PARAMETER_FOR_TYPE = "Invalid Parameter %s for type %s";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    private static final String THROTTLING_ERROR_CODE = "TooManyRequestsException";
    public static final String INVALID_IDENTITY_MANAGEMENT_TYPE = "Invalid IdentityManagementType: %s";

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
        } else if (ex instanceof LimitExceededException || ex instanceof ServiceQuotaExceededException) {
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
}
