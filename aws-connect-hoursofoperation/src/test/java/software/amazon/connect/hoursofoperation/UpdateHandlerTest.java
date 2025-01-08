package software.amazon.connect.hoursofoperation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.CreateHoursOfOperationOverrideResponse;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.DeleteHoursOfOperationOverrideResponse;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesRequest;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationOverridesResponse;
import software.amazon.awssdk.services.connect.model.OverrideDays;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.TagResourceResponse;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceResponse;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationOverrideRequest;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationOverrideResponse;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationRequest;
import software.amazon.awssdk.services.connect.model.UpdateHoursOfOperationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.DAY_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_CONFIG_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_DESCRIPTION_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_NAME_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.HOURS_OF_OPERATION_OVERRIDE_ID_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.INSTANCE_ARN_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.NEXT_TOKEN;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_EFFECTIVE_FROM;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_EFFECTIVE_TILL;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_NAME_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_NAME_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_TIMESLICE_HOUR_13;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_TIMESLICE_HOUR_17;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_TIMESLICE_HOUR_9;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.OVERRIDE_TIMESLICE_MIN_0;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_SET_ONE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TAGS_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.TUESDAY;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_KEY_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.VALID_TAG_VALUE_THREE;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHOOPEmptyOverrideDesiredStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHOOPOverrideDesiredStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHOOPOverrideDesiredStateResourceModelThree;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHOOPOverridePreviousStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHOOPOverridesPreviousStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationDesiredStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.buildHoursOfOperationPreviousStateResourceModel;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getConfig;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getHoursOfOperationOverrideOne;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getHoursOfOperationOverrideThree;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getHoursOfOperationOverrideTwo;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getListHOOPOverridesResponse;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.getOverride;
import static software.amazon.connect.hoursofoperation.HoursOfOperationTestDataProvider.validateConfig;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    private UpdateHandler handler;
    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<ConnectClient> proxyClient;
    private LoggerProxy logger;

    @Mock
    private ConnectClient connectClient;

    @BeforeEach
    public void setup() {
        final Credentials MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = proxy.newProxy(() -> connectClient);
    }

    @AfterEach
    public void post_execute() {
        verifyNoMoreInteractions(proxyClient.client());
    }

    @Test
    public void testHandleRequestWithoutOverrides_Success() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        final TagResourceResponse tagResourceResponse = TagResourceResponse.builder().build();
        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenReturn(tagResourceResponse);

        final UntagResourceResponse untagResourceResponse = UntagResourceResponse.builder().build();
        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenReturn(untagResourceResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_TWO)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(TAGS_ONE);

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequest_Success_UpdateDescriptionNull() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .instanceArn(INSTANCE_ARN)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(null)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .tags(TAGS_SET_ONE)
                .build();

        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_TWO);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo("");
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());
        
        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateInstanceArn() {
        final ResourceModel desiredResourceModel = ResourceModel.builder()
                .hoursOfOperationArn(HOURS_OF_OPERATION_ARN)
                .instanceArn(INSTANCE_ARN_TWO)
                .name(HOURS_OF_OPERATION_NAME_TWO)
                .description(HOURS_OF_OPERATION_DESCRIPTION_TWO)
                .config(getConfig(HOURS_OF_OPERATION_CONFIG_ONE, HOURS_OF_OPERATION_CONFIG_TWO))
                .tags(TAGS_SET_ONE)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredResourceModel)
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnInvalidRequestException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(connectClient, never()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UpdateHoursOfOperation() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(connectClient, atMostOnce()).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_TagResource() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<TagResourceRequest> tagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(TagResourceRequest.class);

        final Map<String, String> tagsAdded = ImmutableMap.of(VALID_TAG_KEY_THREE, VALID_TAG_VALUE_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        when(proxyClient.client().tagResource(tagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(TAGS_THREE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).tagResource(tagResourceRequestArgumentCaptor.capture());
        assertThat(tagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(tagResourceRequestArgumentCaptor.getValue().tags()).isEqualTo(tagsAdded);

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequest_Exception_UnTagResource() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<UntagResourceRequest> untagResourceRequestArgumentCaptor = ArgumentCaptor.forClass(UntagResourceRequest.class);
        final List<String> unTagKeys = Lists.newArrayList(VALID_TAG_KEY_TWO, VALID_TAG_KEY_THREE);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        when(proxyClient.client().untagResource(untagResourceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHoursOfOperationDesiredStateResourceModel())
                .previousResourceState(buildHoursOfOperationPreviousStateResourceModel())
                .desiredResourceTags(ImmutableMap.of())
                .previousResourceTags(TAGS_TWO)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(proxyClient.client()).untagResource(untagResourceRequestArgumentCaptor.capture());
        assertThat(untagResourceRequestArgumentCaptor.getValue().resourceArn()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(untagResourceRequestArgumentCaptor.getValue().tagKeys()).hasSameElementsAs(unTagKeys);

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequestWithNewOverrides_Success() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<CreateHoursOfOperationOverrideRequest> createHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationOverrideRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideTwo()))
                .nextToken(null)
                .build();

        final CreateHoursOfOperationOverrideResponse createHoursOfOperationOverrideResponse = CreateHoursOfOperationOverrideResponse.builder().build();
        final DeleteHoursOfOperationOverrideResponse deleteHoursOfOperationOverrideResponse = DeleteHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationOverrideResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationOverrideResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHOOPOverrideDesiredStateResourceModel())
                .previousResourceState(buildHOOPOverridePreviousStateResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override related fields
        verify(proxyClient.client()).createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture());
        CreateHoursOfOperationOverrideRequest createOverrideRequest = createHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateRequest(createOverrideRequest, TUESDAY, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_17, OVERRIDE_NAME_ONE, HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION);

        verify(proxyClient.client()).deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture());
        DeleteHoursOfOperationOverrideRequest overrideDeleteRequest = deleteHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideDeleteRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideDeleteRequest.hoursOfOperationId());
        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequestWithExistingOverrides_Success() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<CreateHoursOfOperationOverrideRequest> createHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationOverrideRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponsePageOne = getListHOOPOverridesResponse(NEXT_TOKEN, getHoursOfOperationOverrideOne());
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponsePageTwo = getListHOOPOverridesResponse(null, getHoursOfOperationOverrideTwo());
        final CreateHoursOfOperationOverrideResponse createHoursOfOperationOverrideResponse = CreateHoursOfOperationOverrideResponse.builder().build();
        final DeleteHoursOfOperationOverrideResponse deleteHoursOfOperationOverrideResponse = DeleteHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponsePageOne)
                .thenReturn(listHoursOfOperationOverridesResponsePageTwo);
        when(proxyClient.client().createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationOverrideResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationOverrideResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHOOPOverrideDesiredStateResourceModelThree())
                .previousResourceState(buildHOOPOverridesPreviousStateResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        InOrder inorder = inOrder(proxyClient.client());

        inorder.verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        inorder.verify(proxyClient.client(), times(2)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());
        assertNull(listOverrideRequest.get(0).nextToken());
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(1).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(1).hoursOfOperationId());
        assertEquals(NEXT_TOKEN, listOverrideRequest.get(1).nextToken());

        inorder.verify(proxyClient.client(), times(2)).deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture());
        DeleteHoursOfOperationOverrideRequest overrideDeleteRequest = deleteHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideDeleteRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideDeleteRequest.hoursOfOperationId());

        inorder.verify(proxyClient.client()).createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture());
        CreateHoursOfOperationOverrideRequest createOverrideRequest = createHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateRequest(createOverrideRequest, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_THREE, HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION);

        verify(connectClient, times(4)).serviceName();
    }

    @Test
    public void testHandleRequestWithAnOldOverrideUpdatedById() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<UpdateHoursOfOperationOverrideRequest> updateHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideThree()))
                .nextToken(null)
                .build();
        final UpdateHoursOfOperationOverrideResponse updateHoursOfOperationOverrideResponse = UpdateHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().updateHoursOfOperationOverride(updateHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationOverrideResponse);

        // we update the description of override-one as part of the desired state
        ResourceModel previousModel = buildHOOPOverrideDesiredStateResourceModelThree();
        previousModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null); // Override id is not persisted in previous state
        ResourceModel desiredModel = buildHOOPOverrideDesiredStateResourceModelThree();
        desiredModel.getHoursOfOperationOverrides().get(0).setOverrideDescription("Override description updated");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        // HOOP should be updated
        verify(proxyClient.client(), times(1)).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());

        // Create and Delete override apis should not be called.
        verify(proxyClient.client(), never()).createHoursOfOperationOverride(any(CreateHoursOfOperationOverrideRequest.class));
        verify(proxyClient.client(), never()).deleteHoursOfOperationOverride(any(DeleteHoursOfOperationOverrideRequest.class));

        // override related fields
        verify(proxyClient.client(), times(1)).updateHoursOfOperationOverride(updateHoursOfOperationOverrideRequestArgumentCaptor.capture());
        UpdateHoursOfOperationOverrideRequest updateOverrideRequest = updateHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateUpdateOverrideRequest(updateOverrideRequest, HOURS_OF_OPERATION_OVERRIDE_ID_THREE, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_THREE, "Override description updated");
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequestWithAnOldOverrideUpdatedByName() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<UpdateHoursOfOperationOverrideRequest> updateHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideThree()))
                .nextToken(null)
                .build();
        final UpdateHoursOfOperationOverrideResponse updateHoursOfOperationOverrideResponse = UpdateHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().updateHoursOfOperationOverride(updateHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationOverrideResponse);

        // we update the description of override-one as part of the desired state
        ResourceModel previousModel = buildHOOPOverrideDesiredStateResourceModelThree();
        previousModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null);
        ResourceModel desiredModel = buildHOOPOverrideDesiredStateResourceModelThree();
        desiredModel.getHoursOfOperationOverrides().get(0).setOverrideDescription("Override description updated");
        desiredModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        // HOOP should be updated
        verify(proxyClient.client(), times(1)).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());

        // Create and Delete override apis should not be called.
        verify(proxyClient.client(), never()).createHoursOfOperationOverride(any(CreateHoursOfOperationOverrideRequest.class));
        verify(proxyClient.client(), never()).deleteHoursOfOperationOverride(any(DeleteHoursOfOperationOverrideRequest.class));

        // override related fields
        verify(proxyClient.client(), times(1)).updateHoursOfOperationOverride(updateHoursOfOperationOverrideRequestArgumentCaptor.capture());
        UpdateHoursOfOperationOverrideRequest updateOverrideRequest = updateHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateUpdateOverrideRequest(updateOverrideRequest, HOURS_OF_OPERATION_OVERRIDE_ID_THREE, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_THREE, "Override description updated");
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testUpdateOverrideWithDifferentOverrideId() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<CreateHoursOfOperationOverrideRequest> createHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationOverrideRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideThree()))
                .nextToken(null)
                .build();
        final CreateHoursOfOperationOverrideResponse createHoursOfOperationOverrideResponse = CreateHoursOfOperationOverrideResponse.builder().build();
        final DeleteHoursOfOperationOverrideResponse deleteHoursOfOperationOverrideResponse = DeleteHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationOverrideResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationOverrideResponse);

        // we update the description of override-one as part of the desired state
        ResourceModel previousModel = buildHOOPOverrideDesiredStateResourceModelThree();
        previousModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null);
        ResourceModel desiredModel = buildHOOPOverrideDesiredStateResourceModelThree();
        desiredModel.getHoursOfOperationOverrides().get(0).setOverrideDescription("Override description updated");
        // provide a different override id for override-three
        desiredModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId("override-four");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        // HOOP should be updated
        verify(proxyClient.client(), times(1)).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());

        // a new override will be created with id as 'override-four'
        verify(proxyClient.client()).createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture());
        CreateHoursOfOperationOverrideRequest createOverrideRequest = createHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateRequest(createOverrideRequest, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_THREE, "Override description updated");

        // override delete call
        verify(proxyClient.client(), times(1)).deleteHoursOfOperationOverride(any(DeleteHoursOfOperationOverrideRequest.class));
        DeleteHoursOfOperationOverrideRequest overrideDeleteRequest = deleteHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideDeleteRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideDeleteRequest.hoursOfOperationId());
        assertEquals(HOURS_OF_OPERATION_OVERRIDE_ID_THREE, overrideDeleteRequest.hoursOfOperationOverrideId());

        // Update api will not be invoked.
        verify(proxyClient.client(), never()).updateHoursOfOperationOverride(any(UpdateHoursOfOperationOverrideRequest.class));

        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testUpdateOverrideWithConfigUnchangedButWithDifferentId() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<CreateHoursOfOperationOverrideRequest> createHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationOverrideRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final CreateHoursOfOperationOverrideResponse createHoursOfOperationOverrideResponse = CreateHoursOfOperationOverrideResponse.builder().build();
        final DeleteHoursOfOperationOverrideResponse deleteHoursOfOperationOverrideResponse = DeleteHoursOfOperationOverrideResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideThree()))
                .nextToken(null)
                .build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationOverrideResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationOverrideResponse);

        ResourceModel previousModel = buildHOOPOverrideDesiredStateResourceModelThree();
        previousModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null);
        ResourceModel desiredModel = buildHOOPOverrideDesiredStateResourceModelThree();
        // provide a different override id for override-three, rest of the override fields remain the same
        // this should lead to skipping the api calls since there is essentially no change to HOOP as well
        // as it's associated overrides.
        desiredModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId("override-four");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        // Update HOOP
        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());
        assertNull(listOverrideRequest.get(0).nextToken());

        // override create call
        verify(proxyClient.client(), times(1)).createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture());
        CreateHoursOfOperationOverrideRequest createOverrideRequest = createHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateRequest(createOverrideRequest, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_THREE, HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION);

        verify(proxyClient.client(), times(1)).deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture());
        DeleteHoursOfOperationOverrideRequest overrideDeleteRequest = deleteHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideDeleteRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideDeleteRequest.hoursOfOperationId());

        // No api call for HOOP as well overrides should be made in this case
        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequestWithEmptyOverrides_Success() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = getListHOOPOverridesResponse(null, getHoursOfOperationOverrideTwo());
        final DeleteHoursOfOperationOverrideResponse deleteHoursOfOperationOverrideResponse = DeleteHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(deleteHoursOfOperationOverrideResponse);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHOOPEmptyOverrideDesiredStateResourceModel())
                .previousResourceState(buildHOOPOverridePreviousStateResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override related fields
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        ListHoursOfOperationOverridesRequest overrideListRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideListRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideListRequest.hoursOfOperationId());

        verify(proxyClient.client()).deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture());
        DeleteHoursOfOperationOverrideRequest overrideDeleteRequest = deleteHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        assertEquals(INSTANCE_ARN, overrideDeleteRequest.instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, overrideDeleteRequest.hoursOfOperationId());
        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testHandleRequestWithOverrides_Exception_UpdateHoursOfOperation() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<DeleteHoursOfOperationOverrideRequest> deleteHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder().hoursOfOperationOverrideList(getHoursOfOperationOverrideTwo()).build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().deleteHoursOfOperationOverride(deleteHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(buildHOOPEmptyOverrideDesiredStateResourceModel())
                .previousResourceState(buildHOOPOverridePreviousStateResourceModel())
                .desiredResourceTags(TAGS_ONE)
                .previousResourceTags(TAGS_ONE)
                .build();

        assertThrows(CfnGeneralServiceException.class, () -> handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        verify(connectClient, times(2)).serviceName();
    }

    @Test
    public void testUpdateOverrideWithIdAndNameSwapped() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final ArgumentCaptor<CreateHoursOfOperationOverrideRequest> createHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(CreateHoursOfOperationOverrideRequest.class);
        final ArgumentCaptor<ListHoursOfOperationOverridesRequest> listHoursOfOperationOverridesRequestArgumentCaptor = ArgumentCaptor.forClass(ListHoursOfOperationOverridesRequest.class);
        final ArgumentCaptor<UpdateHoursOfOperationOverrideRequest> updateHoursOfOperationOverrideRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationOverrideRequest.class);

        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();
        final ListHoursOfOperationOverridesResponse listHoursOfOperationOverridesResponse = ListHoursOfOperationOverridesResponse.builder()
                .hoursOfOperationOverrideList(ImmutableList.of(getHoursOfOperationOverrideThree()))
                .nextToken(null)
                .build();
        final CreateHoursOfOperationOverrideResponse createHoursOfOperationOverrideResponse = CreateHoursOfOperationOverrideResponse.builder().build();
        final UpdateHoursOfOperationOverrideResponse updateHoursOfOperationOverrideResponse = UpdateHoursOfOperationOverrideResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);
        when(proxyClient.client().listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture())).thenReturn(listHoursOfOperationOverridesResponse);
        when(proxyClient.client().createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(createHoursOfOperationOverrideResponse);
        when(proxyClient.client().updateHoursOfOperationOverride(updateHoursOfOperationOverrideRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationOverrideResponse);

        // we update the description of override-one as part of the desired state
        ResourceModel previousModel = buildHOOPOverrideDesiredStateResourceModelThree();
        previousModel.getHoursOfOperationOverrides().get(0).setHoursOfOperationOverrideId(null);

        ResourceModel desiredModel = buildHOOPOverrideDesiredStateResourceModelThree();
        desiredModel.getHoursOfOperationOverrides().get(0).setOverrideName(OVERRIDE_NAME_ONE);
        desiredModel.getHoursOfOperationOverrides().add(getOverride(HOURS_OF_OPERATION_OVERRIDE_CONFIG_ONE, null, OVERRIDE_NAME_THREE));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(previousModel)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        // HOOP should be updated
        verify(proxyClient.client(), times(1)).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // override list call
        verify(proxyClient.client(), times(1)).listHoursOfOperationOverrides(listHoursOfOperationOverridesRequestArgumentCaptor.capture());
        List<ListHoursOfOperationOverridesRequest> listOverrideRequest = listHoursOfOperationOverridesRequestArgumentCaptor.getAllValues();
        assertEquals(INSTANCE_ARN, listOverrideRequest.get(0).instanceId());
        assertEquals(HOURS_OF_OPERATION_ARN, listOverrideRequest.get(0).hoursOfOperationId());

        // a new override will be created with name as OVERRIDE_NAME_THREE
        verify(proxyClient.client(), times(1)).createHoursOfOperationOverride(createHoursOfOperationOverrideRequestArgumentCaptor.capture());
        CreateHoursOfOperationOverrideRequest createOverrideRequest = createHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateRequest(createOverrideRequest, TUESDAY, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_17, OVERRIDE_NAME_THREE, HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION);

        // override with id HOURS_OF_OPERATION_OVERRIDE_ID_THREE will be updated with name changed to OVERRIDE_NAME_ONE
        verify(proxyClient.client(), times(1)).updateHoursOfOperationOverride(any(UpdateHoursOfOperationOverrideRequest.class));
        UpdateHoursOfOperationOverrideRequest overrideUpdateRequest = updateHoursOfOperationOverrideRequestArgumentCaptor.getValue();
        validateUpdateOverrideRequest(overrideUpdateRequest, HOURS_OF_OPERATION_OVERRIDE_ID_THREE, DAY_ONE, OVERRIDE_TIMESLICE_HOUR_9, OVERRIDE_TIMESLICE_HOUR_13, OVERRIDE_NAME_ONE, HOURS_OF_OPERATION_OVERRIDE_DESCRIPTION);

        // Delete api will not be invoked.
        verify(proxyClient.client(), never()).deleteHoursOfOperationOverride(any(DeleteHoursOfOperationOverrideRequest.class));

        verify(connectClient, times(3)).serviceName();
    }

    @Test
    public void testHandleRequest_NullOverrides() {
        final ArgumentCaptor<UpdateHoursOfOperationRequest> updateHoursOfOperationRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateHoursOfOperationRequest.class);
        final UpdateHoursOfOperationResponse updateHoursOfOperationResponse = UpdateHoursOfOperationResponse.builder().build();

        when(proxyClient.client().updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture())).thenReturn(updateHoursOfOperationResponse);

        ResourceModel desiredModel = buildHOOPEmptyOverrideDesiredStateResourceModel();
        desiredModel.setHoursOfOperationOverrides(null);
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(desiredModel)
                .previousResourceState(buildHOOPOverridePreviousStateResourceModel())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        validateResponse(request, response);

        verify(proxyClient.client()).updateHoursOfOperation(updateHoursOfOperationRequestArgumentCaptor.capture());
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().hoursOfOperationId()).isEqualTo(HOURS_OF_OPERATION_ARN);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().name()).isEqualTo(HOURS_OF_OPERATION_NAME_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().description()).isEqualTo(HOURS_OF_OPERATION_DESCRIPTION_ONE);
        assertThat(updateHoursOfOperationRequestArgumentCaptor.getValue().config()).isNotNull();
        validateConfig(updateHoursOfOperationRequestArgumentCaptor.getValue().config());

        // Update and Delete override apis should not be called.
        verify(proxyClient.client(), never()).createHoursOfOperationOverride(any(CreateHoursOfOperationOverrideRequest.class));
        verify(proxyClient.client(), never()).updateHoursOfOperationOverride(any(UpdateHoursOfOperationOverrideRequest.class));

        verify(connectClient, times(1)).serviceName();
    }

    private void validateRequest(CreateHoursOfOperationOverrideRequest createOverrideRequest, String day, Integer startTime, Integer endTime, String overrideName, String description) {
        assertEquals(description, createOverrideRequest.description());
        assertEquals(overrideName, createOverrideRequest.name());
        assertEquals(OVERRIDE_EFFECTIVE_FROM, createOverrideRequest.effectiveFrom());
        assertEquals(OVERRIDE_EFFECTIVE_TILL, createOverrideRequest.effectiveTill());
        assertNotNull(createOverrideRequest.config());
        assertEquals(1, createOverrideRequest.config().size());
        assertEquals(OverrideDays.fromValue(day), createOverrideRequest.config().get(0).day());
        assertEquals(startTime, createOverrideRequest.config().get(0).startTime().hours());
        assertEquals(OVERRIDE_TIMESLICE_MIN_0, createOverrideRequest.config().get(0).startTime().minutes());
        assertEquals(endTime, createOverrideRequest.config().get(0).endTime().hours());
        assertEquals(OVERRIDE_TIMESLICE_MIN_0, createOverrideRequest.config().get(0).endTime().minutes());
    }

    private void validateUpdateOverrideRequest(UpdateHoursOfOperationOverrideRequest updateOverrideRequest, String overrideId, String day, Integer startTime, Integer endTime, String overrideName, String description) {
        assertEquals(description, updateOverrideRequest.description());
        assertEquals(overrideId, updateOverrideRequest.hoursOfOperationOverrideId());
        assertEquals(overrideName, updateOverrideRequest.name());
        assertEquals(OVERRIDE_EFFECTIVE_FROM, updateOverrideRequest.effectiveFrom());
        assertEquals(OVERRIDE_EFFECTIVE_TILL, updateOverrideRequest.effectiveTill());
        assertNotNull(updateOverrideRequest.config());
        assertEquals(1, updateOverrideRequest.config().size());
        assertEquals(OverrideDays.fromValue(day), updateOverrideRequest.config().get(0).day());
        assertEquals(startTime, updateOverrideRequest.config().get(0).startTime().hours());
        assertEquals(OVERRIDE_TIMESLICE_MIN_0, updateOverrideRequest.config().get(0).startTime().minutes());
        assertEquals(endTime, updateOverrideRequest.config().get(0).endTime().hours());
        assertEquals(OVERRIDE_TIMESLICE_MIN_0, updateOverrideRequest.config().get(0).endTime().minutes());
    }

    private void validateResponse(ResourceHandlerRequest<ResourceModel> request, ProgressEvent<ResourceModel, CallbackContext> response) {
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
