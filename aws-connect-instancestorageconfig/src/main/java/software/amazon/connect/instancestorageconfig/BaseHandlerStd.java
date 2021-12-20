package software.amazon.connect.instancestorageconfig;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.*;
import software.amazon.cloudformation.proxy.*;

import java.util.Objects;
import java.util.function.Function;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

    public static final String CHAT_TRANSCRIPTS = "CHAT_TRANSCRIPTS";
    public static final String CALL_RECORDINGS = "CALL_RECORDINGS";
    public static final String SCHEDULED_REPORTS = "SCHEDULED_REPORTS";
    public static final String MEDIA_STREAMS = "MEDIA_STREAMS";
    public static final String CONTACT_TRACE_RECORDS = "CONTACT_TRACE_RECORDS";
    public static final String AGENT_EVENTS = "AGENT_EVENTS";

    public static final String RESOURCE_TYPE = "ResourceType";
    public static final String ASSOCIATION_ID = "AssociationId";
    public static final String STORAGE_TYPE = "StorageType";
    public static final String S3 = "S3";
    public static final String KINESIS_VIDEO_STREAM = "KINESIS_VIDEO_STREAM";
    public static final String KINESIS_STREAM = "KINESIS_STREAM";
    public static final String KINESIS_FIREHOSE = "KINESIS_FIREHOSE";
    public static final String S3_CONFIG = "S3Config";
    public static final String KINESIS_VIDEO_STREAM_CONFIG = "KinesisVideoStreamConfig";
    public static final String ENCRYPTION_CONFIG = "EncryptionConfig";
    public static final String KINESIS_STREAM_CONFIG = "KinesisStreamConfig";
    public static final String KINESIS_FIREHOSE_CONFIG = "KinesisFirehoseConfig";

    private static final String MISSING_MANDATORY_PARAMETER = "Required parameter missing %s";
    private static final String INVALID_PARAMETER_FOR_RESOURCE_TYPE = "Invalid Parameter %s for resource type %s";
    protected static final String INVALID_RESOURCE_TYPE = "Invalid resource type %s";
    private static final String INVALID_PARAMETER_FOR_STORAGE_TYPE = "Invalid Parameter %s for storage type %s";
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
        } else if (ex instanceof DuplicateResourceException || ex instanceof ResourceConflictException) {
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

    protected static void validateStorageTypesForResourceType(String resourceType, String requestStorageType, String... requiredStorageType) {
        for(String storageType: requiredStorageType){
            if(storageType.equals(requestStorageType)) {
                return;
            }
        }
        throw new CfnInvalidRequestException(String.format(INVALID_PARAMETER_FOR_RESOURCE_TYPE, requestStorageType, resourceType));
    }

    protected static void requireNullForStorageType(final Object object, final String parameterName, final String type) {
        if (Objects.nonNull(object)) {
            throw new CfnInvalidRequestException(String.format(INVALID_PARAMETER_FOR_STORAGE_TYPE, parameterName, type));
        }
    }
}
