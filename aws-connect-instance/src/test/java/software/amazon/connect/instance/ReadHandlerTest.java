package software.amazon.connect.instance;

import org.assertj.core.util.Lists;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.Credentials;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static software.amazon.connect.instance.InstanceTestDataProvider.*;
import static software.amazon.connect.instance.InstanceTestDataProvider.INSTANCE_STATUS_ACTIVE;

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

    @Test
    public void testHandleRequest_SimpleSuccess() {
        mockDescribeInstanceRequest_Success();
        mockListInstanceAttributesRequest_Success();

        final ResourceModel model = ResourceModel.builder()
                .arn(INSTANCE_ARN)
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
    }

    @Test
    public void testHandleRequest_Exception() {
        final ResourceModel model = ResourceModel.builder().arn(INSTANCE_ARN).build();
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ArgumentCaptor<DescribeInstanceRequest> describeInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceRequest.class);
        when(proxyClient.client().describeInstance(describeInstanceRequestArgumentCaptor.capture())).thenThrow(new RuntimeException());

        assertThrows(CfnGeneralServiceException.class, () ->
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger));

        verify(proxyClient.client()).describeInstance(describeInstanceRequestArgumentCaptor.capture());
        assertThat(describeInstanceRequestArgumentCaptor.getValue().instanceId()).isEqualTo(INSTANCE_ARN);
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

    private void mockDescribeInstanceRequest_Success() {
        final ArgumentCaptor<DescribeInstanceRequest> describeInstanceRequestArgumentCaptor = ArgumentCaptor.forClass(DescribeInstanceRequest.class);
        Instance instance = Instance.builder()
                .arn(INSTANCE_ARN)
                .id(INSTANCE_ID)
                .instanceAlias(INSTANCE_ALIAS)
                .instanceStatus(INSTANCE_STATUS_ACTIVE)
                .createdTime(Instant.now())
                .build();

        final DescribeInstanceResponse describeInstanceResponse = DescribeInstanceResponse.builder()
                .instance(instance)
                .build();
        when(proxyClient.client().describeInstance(describeInstanceRequestArgumentCaptor.capture())).thenReturn(describeInstanceResponse);
    }

    private void mockListInstanceAttributesRequest_Success() {
        final ArgumentCaptor<ListInstanceAttributesRequest> listInstanceAttributesRequestArgumentCaptor = ArgumentCaptor.forClass(ListInstanceAttributesRequest.class);

        List<Attribute> attributes = Lists.newArrayList();
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.INBOUND_CALLS).value("true").build());
        attributes.add(Attribute.builder().attributeType(InstanceAttributeType.OUTBOUND_CALLS).value("true").build());

        final ListInstanceAttributesResponse listInstanceAttributesResponse = ListInstanceAttributesResponse.builder()
                .attributes(attributes)
                .build();
        when(proxyClient.client().listInstanceAttributes(listInstanceAttributesRequestArgumentCaptor.capture())).thenReturn(listInstanceAttributesResponse);
    }
}
