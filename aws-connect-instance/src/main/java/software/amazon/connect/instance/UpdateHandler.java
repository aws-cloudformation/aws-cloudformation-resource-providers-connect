package software.amazon.connect.instance;

import com.google.common.collect.Lists;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.Attribute;
import software.amazon.awssdk.services.connect.model.InstanceAttributeType;
import software.amazon.awssdk.services.connect.model.ListInstanceAttributesRequest;
import software.amazon.awssdk.services.connect.model.ListInstanceAttributesResponse;
import software.amazon.awssdk.services.connect.model.UpdateInstanceAttributeRequest;
import software.amazon.awssdk.services.connect.model.UpdateInstanceAttributeResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class UpdateHandler extends BaseHandlerStd {
    private ProxyClient<ConnectClient> proxyClient;
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        this.proxyClient = proxyClient;
        this.logger = logger;

        final String instanceArn = model.getArn();

        logger.log(String.format("Invoked UpdateHandler with instanceArn:%s", instanceArn));

        if (!ArnHelper.isValidInstanceArn(instanceArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", instanceArn)));
        }

        return updateInstanceAttributes(model);
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateInstanceAttributes(ResourceModel model) {
        final String instanceArn = model.getArn();
        final List<Attribute> existingAttributes = listInstanceAttributes(instanceArn);
        final Attributes attributes = model.getAttributes();

        boolean inboundCalls = attributes.getInboundCalls() != null && attributes.getInboundCalls();
        if(inboundCalls != getExistingAttributeValue(existingAttributes, InstanceAttributeType.INBOUND_CALLS)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.INBOUND_CALLS, String.valueOf(attributes.getInboundCalls()));
        }
        boolean outboundCalls = attributes.getOutboundCalls() != null && attributes.getOutboundCalls();
        if(outboundCalls != getExistingAttributeValue(existingAttributes, InstanceAttributeType.OUTBOUND_CALLS)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.OUTBOUND_CALLS, String.valueOf(attributes.getOutboundCalls()));
        }
        boolean contactflowLogs = attributes.getContactflowLogs() != null && attributes.getContactflowLogs();
        if(contactflowLogs != getExistingAttributeValue(existingAttributes, InstanceAttributeType.CONTACTFLOW_LOGS)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.CONTACTFLOW_LOGS, String.valueOf(contactflowLogs));
        }
        boolean contactLens = attributes.getContactLens() != null && attributes.getContactLens();
        if(contactLens != getExistingAttributeValue(existingAttributes, InstanceAttributeType.CONTACT_LENS)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.CONTACT_LENS, String.valueOf(contactLens));
        }
        boolean autoResolveBestVoices = attributes.getAutoResolveBestVoices() != null && attributes.getAutoResolveBestVoices();
        if(autoResolveBestVoices != getExistingAttributeValue(existingAttributes, InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES, String.valueOf(autoResolveBestVoices));
        }
        boolean useCustomTTSVoices = attributes.getUseCustomTTSVoices() != null && attributes.getUseCustomTTSVoices();
        if(useCustomTTSVoices != getExistingAttributeValue(existingAttributes, InstanceAttributeType.USE_CUSTOM_TTS_VOICES)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.USE_CUSTOM_TTS_VOICES, String.valueOf(useCustomTTSVoices));
        }
        boolean earlyMedia = attributes.getEarlyMedia() != null && attributes.getEarlyMedia();
        if(earlyMedia != getExistingAttributeValue(existingAttributes, InstanceAttributeType.EARLY_MEDIA)) {
            updateInstanceAttribute(instanceArn, InstanceAttributeType.EARLY_MEDIA, String.valueOf(earlyMedia));
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private Boolean getExistingAttributeValue(List<Attribute> attributes, InstanceAttributeType type) {
        for(Attribute attribute: attributes) {
            InstanceAttributeType attributeType = attribute.attributeType();
            if(attributeType == type) {
                return Boolean.parseBoolean(attribute.value());
            }
        }
        return null;
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

    private UpdateInstanceAttributeResponse updateInstanceAttribute(String instanceArn, InstanceAttributeType attributeType, String value) {
        logger.log(String.format("Invoking UpdateInstanceAttribute operation for instance %s, attribute %s", instanceArn, attributeType));
        final UpdateInstanceAttributeRequest request = UpdateInstanceAttributeRequest.builder()
                .instanceId(instanceArn)
                .attributeType(attributeType)
                .value(value)
                .build();
        return invoke(request, proxyClient, proxyClient.client()::updateInstanceAttribute, logger);
    }
}
