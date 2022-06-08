package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AvailableNumberSummary;
import software.amazon.awssdk.services.connect.model.ClaimedPhoneNumberSummary;
import software.amazon.awssdk.services.connect.model.PhoneNumberStatus;
import software.amazon.awssdk.services.connect.model.ClaimPhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.ClaimPhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.SearchAvailablePhoneNumbersRequest;
import software.amazon.awssdk.services.connect.model.SearchAvailablePhoneNumbersResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Credentials;

import java.time.Duration;
import java.util.List;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ID_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.buildCreatePhoneNumberResourceModel;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.ADDRESS;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.COUNTRY_CODE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.DID;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_CLAIMED;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_FAILED;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_IN_PROGRESS;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {
    private CreateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @Test
    public void handleRequest_simpleSuccess() {
        final AvailableNumberSummary summary = buildAvailableNumberSummary(ADDRESS, COUNTRY_CODE, DID);
        final List<AvailableNumberSummary> numberList = Arrays.asList(new AvailableNumberSummary[]{summary});
        final SearchAvailablePhoneNumbersResponse searchResponse = buildSearchPhoneNumberResponse(numberList, null);

        when(proxyClient.client().searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class))).thenReturn(searchResponse);
        final ClaimPhoneNumberResponse claimPhoneNumberResponse =
                buildClaimPhoneNumberResponse(PHONE_NUMBER_ID_ONE, PHONE_NUMBER_ARN_ONE);

        when(proxyClient.client().claimPhoneNumber(any(ClaimPhoneNumberRequest.class))).thenReturn(claimPhoneNumberResponse);

        final DescribePhoneNumberResponse claimedResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_CLAIMED);
        final DescribePhoneNumberResponse waitingResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_IN_PROGRESS);
        AtomicInteger attempt = new AtomicInteger(2);
        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class))).then((m) -> {
            switch (attempt.getAndDecrement()) {
                case 2:
                    return waitingResponse;
                default:
                    return claimedResponse;
            }
        });

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreatePhoneNumberResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(response.getCallbackDelaySeconds(), 0);
        assertEquals(PHONE_NUMBER_ARN_ONE, response.getResourceModel().getPhoneNumberArn());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(proxyClient.client()).searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class));
        verify(proxyClient.client()).claimPhoneNumber(any(ClaimPhoneNumberRequest.class));
        verify(proxyClient.client(), times(3)).describePhoneNumber(any(DescribePhoneNumberRequest.class));
    }

    @Test
    public void handleRequest_successPagination() {
        final AvailableNumberSummary summary = buildAvailableNumberSummary(ADDRESS, COUNTRY_CODE, DID);
        final List<AvailableNumberSummary> numberList = Arrays.asList(new AvailableNumberSummary[]{summary});
        final SearchAvailablePhoneNumbersResponse searchResponse = buildSearchPhoneNumberResponse(numberList,
                                                                                                  "nextToken");
        final SearchAvailablePhoneNumbersResponse lastResponse = buildSearchPhoneNumberResponse(numberList,null);

        when(proxyClient.client().searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class))).thenReturn(searchResponse,
                                                                                                                         searchResponse,
                                                                                                                         lastResponse);
        final ClaimPhoneNumberResponse claimPhoneNumberResponse =
                buildClaimPhoneNumberResponse(PHONE_NUMBER_ID_ONE, PHONE_NUMBER_ARN_ONE);

        when(proxyClient.client().claimPhoneNumber(any(ClaimPhoneNumberRequest.class))).thenReturn(claimPhoneNumberResponse);

        final DescribePhoneNumberResponse claimedResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_CLAIMED);
        final DescribePhoneNumberResponse waitingResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_IN_PROGRESS);
        AtomicInteger attempt = new AtomicInteger(2);
        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class))).then((m) -> {
            switch (attempt.getAndDecrement()) {
                case 2:
                    return waitingResponse;
                default:
                    return claimedResponse;
            }
        });

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreatePhoneNumberResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(PHONE_NUMBER_ARN_ONE, response.getResourceModel().getPhoneNumberArn());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(proxyClient.client(), times(3)).searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class));
        verify(proxyClient.client()).claimPhoneNumber(any(ClaimPhoneNumberRequest.class));
        verify(proxyClient.client(), times(3)).describePhoneNumber(any(DescribePhoneNumberRequest.class));
    }

    @Test
    public void handleRequest_failure_noAvailableNumbers() {
        final List<AvailableNumberSummary> numberList = Arrays.asList(new AvailableNumberSummary[]{});
        final SearchAvailablePhoneNumbersResponse searchResponse = buildSearchPhoneNumberResponse(numberList, null);

        when(proxyClient.client().searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class))).thenReturn(searchResponse);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreatePhoneNumberResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.FAILED, response.getStatus());
        assertEquals(HandlerErrorCode.NotFound, response.getErrorCode());

        verify(proxyClient.client()).searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class));
    }

    @Test
    void testHandleRequest_failure_claimException() {
        final AvailableNumberSummary summary = buildAvailableNumberSummary(ADDRESS, COUNTRY_CODE, DID);
        final List<AvailableNumberSummary> numberList = Arrays.asList(new AvailableNumberSummary[]{summary});
        final SearchAvailablePhoneNumbersResponse searchResponse = buildSearchPhoneNumberResponse(numberList, null);

        when(proxyClient.client().searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class))).thenReturn(searchResponse);
        when(proxyClient.client().claimPhoneNumber(any(ClaimPhoneNumberRequest.class))).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreatePhoneNumberResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).claimPhoneNumber(any(ClaimPhoneNumberRequest.class));
    }

    @Test
    public void handleRequest_stabilizationFailure() {
        final AvailableNumberSummary summary = buildAvailableNumberSummary(ADDRESS, COUNTRY_CODE, DID);
        final List<AvailableNumberSummary> numberList = Arrays.asList(new AvailableNumberSummary[]{summary});
        final SearchAvailablePhoneNumbersResponse searchResponse = buildSearchPhoneNumberResponse(numberList, null);

        when(proxyClient.client().searchAvailablePhoneNumbers(any(SearchAvailablePhoneNumbersRequest.class))).thenReturn(searchResponse);
        final ClaimPhoneNumberResponse claimPhoneNumberResponse =
                buildClaimPhoneNumberResponse(PHONE_NUMBER_ID_ONE, PHONE_NUMBER_ARN_ONE);

        when(proxyClient.client().claimPhoneNumber(any(ClaimPhoneNumberRequest.class))).thenReturn(claimPhoneNumberResponse);

        final DescribePhoneNumberResponse failedResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_FAILED);
        final DescribePhoneNumberResponse waitingResponse = buildClaimNumberDescribeResponse(PHONE_NUMBER_IN_PROGRESS);
        AtomicInteger attempt = new AtomicInteger(2);
        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class))).then((m) -> {
            switch (attempt.getAndDecrement()) {
                case 2:
                    return waitingResponse;
                default:
                    return failedResponse;
            }
        });

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildCreatePhoneNumberResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnNotStabilizedException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });

        verify(proxyClient.client()).claimPhoneNumber(any(ClaimPhoneNumberRequest.class));
    }


    private ClaimPhoneNumberResponse buildClaimPhoneNumberResponse(String phoneNumberId,
                                                                   String phoneNumberArn) {
         return ClaimPhoneNumberResponse.builder()
                                        .phoneNumberId(phoneNumberId)
                                        .phoneNumberArn(phoneNumberArn)
                                        .build();
    }

    private SearchAvailablePhoneNumbersResponse buildSearchPhoneNumberResponse(List<AvailableNumberSummary> numbersList,
                                                                               String nextToken) {
        return SearchAvailablePhoneNumbersResponse.builder()
                                                  .availableNumbersList(numbersList)
                                                  .nextToken(nextToken)
                                                  .build();

    }

    private AvailableNumberSummary buildAvailableNumberSummary(String address,
                                                               String countryCode,
                                                               String numberType) {
        return AvailableNumberSummary.builder()
                                     .phoneNumber(address)
                                     .phoneNumberCountryCode(countryCode)
                                     .phoneNumberType(numberType)
                                     .build();
    }

    private DescribePhoneNumberResponse buildClaimNumberDescribeResponse(final String describeStatus) {
        final PhoneNumberStatus claimedNumberStatus = PhoneNumberStatus.builder()
                                                                       .status(describeStatus)
                                                                       .build();

        final ClaimedPhoneNumberSummary claimedSummary = ClaimedPhoneNumberSummary.builder()
                                                                                  .phoneNumberStatus(claimedNumberStatus)
                                                                                  .build();

        return DescribePhoneNumberResponse.builder()
                                          .claimedPhoneNumberSummary(claimedSummary)
                                          .build();
    }
}

