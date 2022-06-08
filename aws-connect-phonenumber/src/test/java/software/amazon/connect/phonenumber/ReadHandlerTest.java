package software.amazon.connect.phonenumber;

import com.google.common.collect.Sets;
import software.amazon.awssdk.services.connect.model.ClaimedPhoneNumberSummary;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.PhoneNumberStatus;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.Credentials;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mockito.ArgumentCaptor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.ADDRESS;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.DESCRIPTION;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TOLL_FREE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.COUNTRY_CODE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INSTANCE_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INVALID_PHONE_NUMBER_ARN;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_CLAIMED;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.buildPhoneNumberDesiredStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest {
    private ReadHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleRequest_Success() {
        final PhoneNumberStatus claimedStatus = PhoneNumberStatus.builder()
                                                                 .status(PHONE_NUMBER_CLAIMED)
                                                                 .build();

        final ClaimedPhoneNumberSummary summary = ClaimedPhoneNumberSummary.builder()
                                                                           .phoneNumber(ADDRESS)
                                                                           .phoneNumberDescription(DESCRIPTION)
                                                                           .targetArn(INSTANCE_ARN_ONE)
                                                                           .phoneNumberArn(PHONE_NUMBER_ARN_ONE)
                                                                           .phoneNumberType(TOLL_FREE)
                                                                           .phoneNumberCountryCode(COUNTRY_CODE)
                                                                           .tags(TAGS_ONE)
                                                                           .phoneNumberStatus(claimedStatus)
                                                                           .build();

        final ArgumentCaptor<DescribePhoneNumberRequest> describePhoneNumberArgumentCaptor =
                ArgumentCaptor.forClass(DescribePhoneNumberRequest.class);

        final DescribePhoneNumberResponse describePhoneNumberResponse = DescribePhoneNumberResponse.builder()
                                                                                                 .claimedPhoneNumberSummary(summary)
                                                                                                 .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describePhoneNumber(describePhoneNumberArgumentCaptor.capture())).thenReturn(describePhoneNumberResponse);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getResourceModels());
        assertEquals(INSTANCE_ARN_ONE, response.getResourceModel().getTargetArn());
        assertEquals(PHONE_NUMBER_ARN_ONE, response.getResourceModel().getPhoneNumberArn());
        assertEquals(DESCRIPTION, response.getResourceModel().getDescription());
        assertEquals(ADDRESS, response.getResourceModel().getAddress());
        assertEquals(TOLL_FREE, response.getResourceModel().getType());
        assertEquals(COUNTRY_CODE, response.getResourceModel().getCountryCode());
        assertEquals(TAGS_SET_ONE, response.getResourceModel().getTags());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(proxyClient.client()).describePhoneNumber(describePhoneNumberArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, describePhoneNumberArgumentCaptor.getValue().phoneNumberId());

        verify(connectClient, times(1)).serviceName();
    }

    protected static Set<Tag> convertResourceTagsToSet(final Map<String, String> resourceTags) {
        return Optional.ofNullable(resourceTags)
                       .map(tags -> tags.keySet().stream()
                                        .map(key -> Tag.builder().key(key).value(resourceTags.get(key)).build())
                                        .collect(Collectors.toSet()))
                       .orElse(Sets.newHashSet());
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidArn() {
        final ArgumentCaptor<DescribePhoneNumberRequest> describePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribePhoneNumberRequest.class);
        final ResourceModel model = buildPhoneNumberDesiredStateResourceModel();
        model.setPhoneNumberArn(INVALID_PHONE_NUMBER_ARN);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertEquals(0, describePhoneNumberRequestArgumentCaptor.getAllValues().size());

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception() {
        final ArgumentCaptor<DescribePhoneNumberRequest> describePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(DescribePhoneNumberRequest.class);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describePhoneNumber(describePhoneNumberRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describePhoneNumber(describePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, describePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(connectClient, times(1)).serviceName();
    }
}
