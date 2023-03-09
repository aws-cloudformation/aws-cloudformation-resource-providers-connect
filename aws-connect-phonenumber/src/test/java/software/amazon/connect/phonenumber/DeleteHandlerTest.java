package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.model.ReleasePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.ReleasePhoneNumberResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.Duration;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INVALID_PHONE_NUMBER_ARN;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.buildPhoneNumberDesiredStateResourceModel;


@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest {

    private DeleteHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new DeleteHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleRequest_Success() {
        final ArgumentCaptor<ReleasePhoneNumberRequest> releasePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(ReleasePhoneNumberRequest.class);

        final ReleasePhoneNumberResponse releasePhoneNumberResponse = ReleasePhoneNumberResponse.builder().build();

        when(proxyClient.client().releasePhoneNumber(releasePhoneNumberRequestArgumentCaptor.capture())).thenReturn(releasePhoneNumberResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertNull(response.getResourceModel());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(proxyClient.client()).releasePhoneNumber(releasePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(releasePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId(), PHONE_NUMBER_ARN_ONE);

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void handleRequest_Exception_releasePhoneNumber() {
        final ArgumentCaptor<ReleasePhoneNumberRequest> releasePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(ReleasePhoneNumberRequest.class);

        when(proxyClient.client().releasePhoneNumber(releasePhoneNumberRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).releasePhoneNumber(releasePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, releasePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(connectClient, times(1)).serviceName();
    }

    @Test
    public void handleRequest_CfnNotFoundException_InvalidPhoneNumberArn() {
        final ArgumentCaptor<ReleasePhoneNumberRequest> releasePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(ReleasePhoneNumberRequest.class);
        final ResourceModel model = buildPhoneNumberDesiredStateResourceModel();
        model.setPhoneNumberArn(INVALID_PHONE_NUMBER_ARN);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
        assertEquals(0, releasePhoneNumberRequestArgumentCaptor.getAllValues().size());

        verify(connectClient, never()).serviceName();
    }
}
