package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AccessDeniedException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.ResourceConflictException;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.ResourceNotReadyException;
import software.amazon.awssdk.services.connect.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.function.Function;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected static final int CALLBACK_DELAY_SECONDS = 30;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        ConnectClient connectClient = ClientBuilder.getClient(Region.of(request.getRegion()));
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(() -> connectClient),
                logger);
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger);

    protected static void handleCommonExceptions(
            final Exception ex,
            final Logger logger) {
        if (ex instanceof ResourceNotFoundException) {
            throw new CfnNotFoundException(ex);
        } else if (ex instanceof InvalidRequestException ) {
            throw new CfnInvalidRequestException(ex);
        } else if (ex instanceof InternalServiceException) {
            throw new CfnServiceInternalErrorException(ex);
        } else if (ex instanceof ResourceConflictException || ex instanceof ResourceNotReadyException) {
            throw new CfnResourceConflictException(ex);
        } else if (ex instanceof ThrottlingException) {
            throw new CfnThrottlingException(ex);
        } else if (ex instanceof AccessDeniedException) {
            throw new CfnAccessDeniedException(ex);
        }
        logger.log(String.format("Exception in handler: %s", ex));
        throw new CfnGeneralServiceException(ex);
    }

    protected static <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT invoke(
            final RequestT request,
            final ProxyClient<ConnectClient> proxyClient,
            final Function<RequestT, ResponseT> requestFunction,
            final Logger logger) {
        ResponseT response = null;
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, requestFunction);
        } catch (final Exception e) {
            handleCommonExceptions(e, logger);
        }
        return response;
    }
}
