package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.model.AvailableNumberSummary;
import software.amazon.awssdk.services.connect.model.ClaimPhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.ClaimPhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.SearchAvailablePhoneNumbersRequest;
import software.amazon.awssdk.services.connect.model.SearchAvailablePhoneNumbersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CreateHandler extends BaseHandlerStd {
    private static final Integer MAX_PHONE_NUMBER_RESULTS = 10;
    private static final Integer NUMBER_LOOPS = 2;
    private static final Integer START_DELAY_MS = 2000;
    protected static final String PHONE_NUMBER_CLAIMED = "CLAIMED";
    protected static final String PHONE_NUMBER_FAILED = "FAILED";

    private static final BiFunction<ResourceModel, ProxyClient<ConnectClient>, ResourceModel>
            EMPTY_CALL = (model, proxyClient) -> model;

    /**
     * Using the specified resource model, calls SearchAvailablePhoneNumbers API to get a list of available numbers.
     * One of the numbers is randomly selected and claimed using ClaimPhoneNumber API. Only returns successful
     * progress event when that resource is stabilized (correctly claimed to the account).
     * @param proxy
     * @param request
     * @param callbackContext
     * @param proxyClient
     * @param logger
     * @return ProgressEvent<ResourceModel, CallbackContext>
     * */
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest (
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();
        String numberToClaim = "";

        logger.log(String.format("Invoked CreatePhoneNumberHandler with TargetArn: %s, Country Code: %s, Type: %s, Prefix: %s",
                                 model.getTargetArn(), model.getCountryCode(), model.getType(), model.getPrefix()));

        // Check if ARN (read only property) is set for idempotency
        if (model.getPhoneNumberArn() == null) {
            List<AvailableNumberSummary> availableNumbers = buildAvailableNumbersList(logger, model, proxyClient);
            if (availableNumbers.isEmpty()) {
                logger.log(String.format("No available numbers were found with the given request configuration. Target: %s, " +
                                         "Country Code: %s, Type: %s, Prefix: %s",
                                         model.getTargetArn(), model.getCountryCode(), model.getType(), model.getPrefix()));
                HandlerErrorCode notFoundException = HandlerErrorCode.NotFound;
                return ProgressEvent.<ResourceModel, CallbackContext>builder()
                        .resourceModel(model)
                        .status(OperationStatus.FAILED)
                        .errorCode(notFoundException)
                        .build();
            }

            SecureRandom random = new SecureRandom();
            final int index = random.nextInt(availableNumbers.size());
            numberToClaim = availableNumbers.get(index).phoneNumber();
        } else {
            numberToClaim = model.getAddress();
        }

        // Local lambda variables must be final
        final String finalNumberToClaim = numberToClaim;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            .then(progress ->
               proxy.initiate("connect::claimPhoneNumber", proxyClient, request.getDesiredResourceState(), callbackContext)
                    .translateToServiceRequest((resourceModel) -> translateToClaimPhoneNumberRequest(model, finalNumberToClaim, tags))
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::claimPhoneNumber, logger))
                    .done(this::setPhoneNumberArn)
            )
            .then(progress -> stabilize(logger, proxy, proxyClient, progress, "connect::postCreateStabilize"))
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Builds available number list by building Search API request and making multiple calls.
     * @param
     * @return SearchAvailablePhoneNumbersRequest
     * */
    List<AvailableNumberSummary> buildAvailableNumbersList(final Logger logger,
                                                           final ResourceModel model,
                                                           final ProxyClient<ConnectClient> proxyClient) {

        logger.log(String.format("Calling SearchAvailableNumbers API, request configuration Target: %s, " +
                                 "Country Code: %s, Type: %s, Prefix: %s",
                                 model.getTargetArn(), model.getCountryCode(), model.getType(), model.getPrefix()));

        SearchAvailablePhoneNumbersResponse searchResponse = buildAndCallSearchAvailableNumbers(proxyClient, logger,
                                                                                                model, null);
        List<AvailableNumberSummary> availableNumbers = new ArrayList<>(searchResponse.availableNumbersList());
        int counter = 0;
        String nextToken = searchResponse.nextToken();

        while (nextToken != null && counter++ < NUMBER_LOOPS) {
            logger.log(String.format("Calling SearchAvailableNumbers API, request configuration Target: %s, " +
                                     "Country Code: %s, Type: %s, Prefix: %s",
                                     model.getTargetArn(), model.getCountryCode(), model.getType(), model.getPrefix()));
            searchResponse = buildAndCallSearchAvailableNumbers(proxyClient, logger, model, nextToken);
            availableNumbers.addAll(searchResponse.availableNumbersList());
            nextToken = searchResponse.nextToken();
        }

        return availableNumbers;
    }

    /**
     * Returns a SearchAvailablePhoneNumberRequest built from the provided resource model. If a prefix is specified and/or a nextToken
     * is provided, they will also be built into the request.
     * @param model
     * @param nextToken
     * @return SearchAvailablePhoneNumbersRequest
     * */
    private SearchAvailablePhoneNumbersRequest buildSearchAvailablePhoneNumbersRequestFromModel(ResourceModel model,
                                                                                                String nextToken) {

        SearchAvailablePhoneNumbersRequest.Builder requestBuilder = SearchAvailablePhoneNumbersRequest.builder()
                                                                                                      .targetArn(model.getTargetArn())
                                                                                                      .phoneNumberCountryCode(model.getCountryCode())
                                                                                                      .phoneNumberType(model.getType())
                                                                                                      .maxResults(MAX_PHONE_NUMBER_RESULTS);
        if (model.getPrefix() != null) {
            requestBuilder.phoneNumberPrefix(model.getPrefix());
        }
        if (nextToken != null) {
            requestBuilder.nextToken(nextToken);
        }
        return requestBuilder.build();
    }

    /**
     * Translates a resource model, number to claim, and tags into a ClaimPhoneNumberRequest.
     * @param model
     * @param numberToClaim
     * @param tags
     * @return ClaimPhoneNumberRequest
     * */
    private ClaimPhoneNumberRequest translateToClaimPhoneNumberRequest(final ResourceModel model,
                                                                       final String numberToClaim,
                                                                       final Map<String, String> tags) {

        return ClaimPhoneNumberRequest.builder()
                                      .targetArn(model.getTargetArn())
                                      .phoneNumber(numberToClaim)
                                      .phoneNumberDescription(model.getDescription())
                                      .tags(tags)
                                      .build();
    }

    /**
     * Sets resource model phone number ARN and returns progress event.
     * @param model
     * @param claimResponse
     * @return ProgressEvent
     * */
    private ProgressEvent<ResourceModel, CallbackContext> setPhoneNumberArn(final ClaimPhoneNumberRequest claimRequest,
                                                                            final ClaimPhoneNumberResponse claimResponse,
                                                                            final ProxyClient<ConnectClient> proxyClient,
                                                                            final ResourceModel model,
                                                                            final CallbackContext callbackContext) {
        model.setPhoneNumberArn(claimResponse.phoneNumberArn());
        return ProgressEvent.progress(model, callbackContext);
    }

    /**
     * Builds a SearchAvailablePhoneNumbersRequest from the provided resource model, calls and handles exceptions from
     * SearchAvailablePhoneNumbers API, and returns the response.
     * @param proxyClient
     * @param logger
     * @param model
     * @param nextToken
     * @return SearchAvailablePhoneNumbersResponse
     * */
    private SearchAvailablePhoneNumbersResponse buildAndCallSearchAvailableNumbers(final ProxyClient<ConnectClient> proxyClient,
                                                                                   final Logger logger,
                                                                                   ResourceModel model,
                                                                                   String nextToken) {

        SearchAvailablePhoneNumbersRequest request = buildSearchAvailablePhoneNumbersRequestFromModel(model, nextToken);
        return invoke(request, proxyClient, proxyClient.client()::searchAvailablePhoneNumbers, logger);
    }

    /**
     * Check for stabilization and return progressEvent with current progress state.
     * @param logger
     * @param proxy
     * @param proxyClient
     * @param progress
     * @param callGraph
     * @return ProgressEvent
     * */
    private ProgressEvent<ResourceModel, CallbackContext> stabilize(final Logger logger,
                                                                    final AmazonWebServicesClientProxy proxy,
                                                                    final ProxyClient<ConnectClient> proxyClient,
                                                                    final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                    final String callGraph) {
        return proxy.initiate(callGraph, proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Function.identity())
                    .backoffDelay(STABILIZATION_DELAY)
                    .makeServiceCall(EMPTY_CALL)
                    .stabilize((request, response, proxyInvocation, model, callbackContext) ->
                                       isStabilized(model, proxyClient, logger)).progress();
    }

    /**
     * Checks for resource stabilization by calling DescribePhoneNumber API and seeing if the number is claimed.
     * @param model
     * @param proxyClient
     * @param logger
     * @return boolean
     * */
    private boolean isStabilized(final ResourceModel model,
                                 final ProxyClient<ConnectClient> proxyClient,
                                 final Logger logger) {

        // Need a delay before stabilization call to make sure ClaimNumber workflow has started.
        try {
            Thread.sleep(START_DELAY_MS);
        } catch (Exception e) {
            logger.log("Sleep for stabilization delay was stopped early.");
        }

        logger.log(String.format("Describing number: %s", model.getPhoneNumberArn()));
        final DescribePhoneNumberRequest request = DescribePhoneNumberRequest.builder()
                                                                             .phoneNumberId(model.getPhoneNumberArn())
                                                                             .build();

        DescribePhoneNumberResponse response = invoke(request, proxyClient, proxyClient.client()::describePhoneNumber, logger);
        final String status = response.claimedPhoneNumberSummary()
                                      .phoneNumberStatus()
                                      .statusAsString();

        model.setAddress(response.claimedPhoneNumberSummary().phoneNumber());
        if (status.equals(PHONE_NUMBER_FAILED)) {
            throw new CfnNotStabilizedException(ResourceModel.TYPE_NAME, model.getPhoneNumberArn());
        }

        boolean stabilized = status.equals(PHONE_NUMBER_CLAIMED);
        logger.log(String.format("%s create has stabilized: %s", ResourceModel.TYPE_NAME, stabilized));
        return stabilized;
    }
}
