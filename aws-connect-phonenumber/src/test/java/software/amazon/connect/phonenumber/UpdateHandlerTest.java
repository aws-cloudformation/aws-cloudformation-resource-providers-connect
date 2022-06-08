package software.amazon.connect.phonenumber;

import software.amazon.awssdk.services.connect.model.ClaimedPhoneNumberSummary;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.PhoneNumberStatus;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdatePhoneNumberRequest;
import software.amazon.awssdk.services.connect.model.UpdatePhoneNumberResponse;
import software.amazon.awssdk.services.connect.model.DescribePhoneNumberRequest;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap;


import java.time.Duration;
import java.util.List;
import java.util.Map;

import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INSTANCE_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.INSTANCE_ARN_TWO;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.PHONE_NUMBER_ARN_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_ONE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_THREE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.TAGS_TWO;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.buildPhoneNumberDesiredStateResourceModel;
import static software.amazon.connect.phonenumber.PhoneNumberTestDataProvider.buildPhoneNumberPreviousStateResourceModel;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private ProxyClient<ConnectClient> proxyClient;
    private AmazonWebServicesClientProxy proxy;
    private Logger logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy((LoggerProxy) logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void handleRequest_Success() {
        final ArgumentCaptor<UpdatePhoneNumberRequest> updatePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdatePhoneNumberRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdatePhoneNumberResponse updatePhoneNumberResponse = UpdatePhoneNumberResponse.builder().build();
        when(proxyClient.client().updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture())).thenReturn(updatePhoneNumberResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class)))
                .thenReturn(buildDescribeResponse(INSTANCE_ARN_TWO), buildDescribeResponse(INSTANCE_ARN_ONE));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(request.getDesiredResourceState(), response.getResourceModel());
        assertNull(response.getResourceModels());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(proxyClient.client()).updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(INSTANCE_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().targetArn());
        assertEquals(PHONE_NUMBER_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, tagResourceRequestArgumentCaptor.getValue().resourceArn());
        assertEquals(TAGS_ONE, tagResourceRequestArgumentCaptor.getValue().tags());

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, untagResourceRequestArgumentCaptor.getValue().resourceArn());
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(4)).serviceName();
    }

    @Test
    public void handleRequest_Success_noChanges() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberDesiredStateResourceModel())
                .build();

        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class)))
                .thenReturn(buildDescribeResponse(INSTANCE_ARN_ONE), buildDescribeResponse(INSTANCE_ARN_ONE));

        final ProgressEvent<ResourceModel, CallbackContext> response = handler
                .handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(request.getDesiredResourceState(), response.getResourceModel());
        assertNull(response.getResourceModels());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(connectClient, times(0)).updatePhoneNumber(any(UpdatePhoneNumberRequest.class));
        verify(connectClient, times(0)).tagResource(any(TagResourceRequest.class));
        verify(connectClient, times(0)).untagResource(any(UntagResourceRequest.class));
        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_Success_UpdateTags() {
        when(proxyClient.client().describePhoneNumber(any(DescribePhoneNumberRequest.class)))
                .thenReturn(buildDescribeResponse(INSTANCE_ARN_ONE));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberDesiredStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertNotNull(response);
        assertEquals(OperationStatus.SUCCESS, response.getStatus());
        assertNull(response.getCallbackContext());
        assertEquals(0, response.getCallbackDelaySeconds());
        assertEquals(request.getDesiredResourceState(), response.getResourceModel());
        assertNull(response.getResourceModels());
        assertNull(response.getMessage());
        assertNull(response.getErrorCode());

        verify(connectClient, times(0)).updatePhoneNumber(any(UpdatePhoneNumberRequest.class));
        verify(connectClient, times(1)).tagResource(any(TagResourceRequest.class));
        verify(connectClient, times(1)).untagResource(any(UntagResourceRequest.class));
        verify(connectClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_Exception_UpdateTargetArn() {
        final ArgumentCaptor<UpdatePhoneNumberRequest> updatePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdatePhoneNumberRequest.class);

        when(proxyClient.client().updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(INSTANCE_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().targetArn());
        assertEquals(PHONE_NUMBER_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void handleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdatePhoneNumberRequest> updatePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdatePhoneNumberRequest.class);
        final ArgumentCaptor<DescribePhoneNumberRequest> describePhoneNumberArgumentCaptor =
                ArgumentCaptor.forClass(DescribePhoneNumberRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);
        final UpdatePhoneNumberResponse updatePhoneNumberResponse = UpdatePhoneNumberResponse.builder().build();
        final ClaimedPhoneNumberSummary summary = ClaimedPhoneNumberSummary.builder()
                                                                           .targetArn(INSTANCE_ARN_ONE)
                                                                           .build();
        final DescribePhoneNumberResponse describePhoneNumberResponse = DescribePhoneNumberResponse.builder()
                                                                                                   .claimedPhoneNumberSummary(summary)
                                                                                                   .build();

        when(proxyClient.client().updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture())).thenReturn(updatePhoneNumberResponse);
        when(proxyClient.client().describePhoneNumber(describePhoneNumberArgumentCaptor.capture())).thenReturn(describePhoneNumberResponse);
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberPreviousStateResourceModel())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(INSTANCE_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().targetArn());
        assertEquals(PHONE_NUMBER_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, tagResourceRequestArgumentCaptor.getValue().resourceArn());
        assertEquals(tagsAdded, tagResourceRequestArgumentCaptor.getValue().tags());

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void handleRequest_Exception_UntagResource() {
        final ArgumentCaptor<UpdatePhoneNumberRequest> updatePhoneNumberRequestArgumentCaptor =
                ArgumentCaptor.forClass(UpdatePhoneNumberRequest.class);
        final ArgumentCaptor<DescribePhoneNumberRequest> describePhoneNumberArgumentCaptor =
                ArgumentCaptor.forClass(DescribePhoneNumberRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);
        final UpdatePhoneNumberResponse updatePhoneNumberResponse = UpdatePhoneNumberResponse.builder().build();
        final ClaimedPhoneNumberSummary summary = ClaimedPhoneNumberSummary.builder()
                                                                           .targetArn(INSTANCE_ARN_ONE)
                                                                           .build();
        final DescribePhoneNumberResponse describePhoneNumberResponse = DescribePhoneNumberResponse.builder()
                                                                                                   .claimedPhoneNumberSummary(summary)
                                                                                                   .build();

        when(proxyClient.client().updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture())).thenReturn(updatePhoneNumberResponse);
        when(proxyClient.client().describePhoneNumber(describePhoneNumberArgumentCaptor.capture())).thenReturn(describePhoneNumberResponse);
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildPhoneNumberDesiredStateResourceModel())
                .previousResourceState(buildPhoneNumberPreviousStateResourceModel())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updatePhoneNumber(updatePhoneNumberRequestArgumentCaptor.capture());
        assertEquals(INSTANCE_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().targetArn());
        assertEquals(PHONE_NUMBER_ARN_ONE, updatePhoneNumberRequestArgumentCaptor.getValue().phoneNumberId());

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertEquals(PHONE_NUMBER_ARN_ONE, untagResourceRequestArgumentCaptor.getValue().resourceArn());
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(2)).serviceName();
    }

    private DescribePhoneNumberResponse buildDescribeResponse(final String instance) {
        final PhoneNumberStatus claimedNumberStatus = PhoneNumberStatus.builder()
                                                                       .status("CLAIMED")
                                                                       .build();

        final ClaimedPhoneNumberSummary claimedSummary = ClaimedPhoneNumberSummary.builder()
                                                                                  .phoneNumberStatus(claimedNumberStatus)
                                                                                  .targetArn(instance)
                                                                                  .build();

        return DescribePhoneNumberResponse.builder()
                                          .claimedPhoneNumberSummary(claimedSummary)
                                          .build();
    }
}
