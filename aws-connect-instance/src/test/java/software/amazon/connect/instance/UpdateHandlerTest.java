package software.amazon.connect.instance;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.*;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.connect.instance.InstanceTestDataProvider.INSTANCE_ARN;
import static software.amazon.connect.instance.InstanceTestDataProvider.INVALID_INSTANCE_ARN;

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

    @Test
    public void testHandleRequest_SimpleSuccess() {
        mockListInstanceAttributesRequest_Success();

        final ArgumentCaptor<UpdateInstanceAttributeRequest> updateInstanceAttributeRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateInstanceAttributeRequest.class);
        final UpdateInstanceAttributeResponse updateInstanceAttributeResponse = UpdateInstanceAttributeResponse.builder().build();
        when(proxyClient.client().updateInstanceAttribute(updateInstanceAttributeRequestArgumentCaptor.capture())).thenReturn(updateInstanceAttributeResponse);

        final ResourceModel model = ResourceModel.builder()
                .arn(INSTANCE_ARN)
                .attributes(Attributes.builder()
                        .inboundCalls(true)
                        .outboundCalls(false)
                        .contactflowLogs(true)
                        .contactLens(true)
                        .autoResolveBestVoices(true)
                        .useCustomTTSVoices(true)
                        .earlyMedia(true)
                        .build())
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        assertThat(updateInstanceAttributeRequestArgumentCaptor.getValue().attributeType()).isEqualTo(InstanceAttributeType.OUTBOUND_CALLS);
        assertThat(updateInstanceAttributeRequestArgumentCaptor.getValue().value()).isEqualTo("false");
    }

    @Test
    public void testHandleRequest_Exception() {
        final ResourceModel model = ResourceModel.builder()
                .arn(INSTANCE_ARN)
                .attributes(Attributes.builder()
                        .inboundCalls(true)
                        .outboundCalls(false)
                        .contactflowLogs(true)
                        .contactLens(true)
                        .autoResolveBestVoices(true)
                        .useCustomTTSVoices(true)
                        .earlyMedia(true)
                        .build())
                .build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        mockListInstanceAttributesRequest_Success();

        final ArgumentCaptor<UpdateInstanceAttributeRequest> updateInstanceAttributeRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateInstanceAttributeRequest.class);
        when(proxyClient.client().updateInstanceAttribute(updateInstanceAttributeRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).updateInstanceAttribute(updateInstanceAttributeRequestArgumentCaptor.capture());
        assertThat(updateInstanceAttributeRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
    }

    @Test
    public void testHandleRequest_CfnNotFoundException_InvalidInstanceArn() {
        final ResourceModel model = ResourceModel.builder().arn(INVALID_INSTANCE_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));
    }

    private void mockListInstanceAttributesRequest_Success() {
        final ArgumentCaptor<ListInstanceAttributesRequest> listInstanceAttributesRequestArgumentCaptor = ArgumentCaptor.forClass(ListInstanceAttributesRequest.class);
        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.INBOUND_CALLS).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.OUTBOUND_CALLS).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.CONTACTFLOW_LOGS).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.CONTACT_LENS).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.USE_CUSTOM_TTS_VOICES).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.EARLY_MEDIA).value("true").build());

        final ListInstanceAttributesResponse listInstanceAttributesResponse = ListInstanceAttributesResponse.builder()
                .attributes(attributes)
                .build();
        when(proxyClient.client().listInstanceAttributes(listInstanceAttributesRequestArgumentCaptor.capture())).thenReturn(listInstanceAttributesResponse);
    }
}
