package software.amazon.connect.instance;

import com.google.common.collect.Lists;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.Attribute;
import software.amazon.awssdk.services.connect.model.InstanceAttributeType;
import software.amazon.awssdk.services.connect.model.InstanceSummary;
import software.amazon.awssdk.services.connect.model.ListInstanceAttributesRequest;
import software.amazon.awssdk.services.connect.model.ListInstanceAttributesResponse;
import software.amazon.awssdk.services.connect.model.ListInstancesRequest;
import software.amazon.awssdk.services.connect.model.ListInstancesResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListHandler extends BaseHandlerStd {

    private ProxyClient<ConnectClient> proxyClient;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        this.proxyClient = proxyClient;
        this.logger = logger;

        logger.log("Invoked ListHandler");

        ListInstancesResponse listInstancesResponse = listInstances(request.getNextToken());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(translateFromListResponse(listInstancesResponse))
                .nextToken(listInstancesResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private List<ResourceModel> translateFromListResponse(final ListInstancesResponse listInstancesResponse) {
        return streamOfOrEmpty(listInstancesResponse.instanceSummaryList())
                .map(summary -> translateToResourceModel(summary))
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    private ResourceModel translateToResourceModel(InstanceSummary instance) {
        Attributes attributes = Attributes.builder().build();
        attributes.setInboundCalls(instance.inboundCallsEnabled());
        attributes.setOutboundCalls(instance.outboundCallsEnabled());
        if(INSTANCE_STATUS_ACTIVE.equals(instance.instanceStatusAsString())) {
            List<Attribute> instanceAttributes = listInstanceAttributes(instance.arn());
            for(Attribute attribute: instanceAttributes) {
                InstanceAttributeType attributeType = attribute.attributeType();
                if(attributeType == InstanceAttributeType.CONTACTFLOW_LOGS) {
                    attributes.setContactflowLogs(Boolean.parseBoolean(attribute.value()));
                } else if(attributeType == InstanceAttributeType.CONTACT_LENS) {
                    attributes.setContactLens(Boolean.parseBoolean(attribute.value()));
                } else if(attributeType == InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES) {
                    attributes.setAutoResolveBestVoices(Boolean.parseBoolean(attribute.value()));
                } else if(attributeType == InstanceAttributeType.USE_CUSTOM_TTS_VOICES) {
                    attributes.setUseCustomTTSVoices(Boolean.parseBoolean(attribute.value()));
                } else if(attributeType == InstanceAttributeType.EARLY_MEDIA) {
                    attributes.setEarlyMedia(Boolean.parseBoolean(attribute.value()));
                }
            }
        }
        return ResourceModel.builder()
                .arn(instance.arn())
                .id(instance.id())
                .identityManagementType(instance.identityManagementTypeAsString())
                .instanceAlias(instance.instanceAlias())
                .createdTime(instance.createdTime().toString())
                .serviceRole(instance.serviceRole())
                .instanceStatus(instance.instanceStatusAsString())
                .attributes(attributes)
                .build();
    }

    private ListInstancesResponse listInstances(String nextToken) {
        logger.log("Invoking ListInstances operation");
        ListInstancesRequest request = ListInstancesRequest.builder().nextToken(nextToken).build();
        return invoke(request, proxyClient, proxyClient.client()::listInstances, logger);
    }

    private List<Attribute> listInstanceAttributes(String arn) {
        logger.log(String.format("Invoking ListInstanceAttributes operation for instance %s", arn));
        ListInstanceAttributesRequest request = ListInstanceAttributesRequest.builder().instanceId(arn).build();
        ListInstanceAttributesResponse response = invoke(request, proxyClient, proxyClient.client()::listInstanceAttributes, logger);
        final List<Attribute> attributes = Lists.newArrayList(response.attributes());

        String nextToken = response.nextToken();
        while (nextToken != null){
            request = ListInstanceAttributesRequest.builder().instanceId(arn).nextToken(nextToken).build();
            response = invoke(request, proxyClient, proxyClient.client()::listInstanceAttributes, logger);
            attributes.addAll(response.attributes());
            nextToken = response.nextToken();
        }
        return attributes;
    }
}
