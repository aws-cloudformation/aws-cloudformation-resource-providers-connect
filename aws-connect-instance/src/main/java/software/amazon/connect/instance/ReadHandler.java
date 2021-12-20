package software.amazon.connect.instance;

import com.google.common.collect.Lists;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.*;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.*;

import java.util.List;

public class ReadHandler extends BaseHandlerStd {

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

        logger.log(String.format("Invoked DescribeHandler with Instance Arn:%s", instanceArn));

        if (!ArnHelper.isValidInstanceArn(instanceArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", instanceArn)));
        }

        DescribeInstanceResponse response = describeInstance(instanceArn);
        translateToResourceModel(model, response.instance());
        return ProgressEvent.defaultSuccessHandler(model);
    }

    private void translateToResourceModel(ResourceModel model, Instance instance) {
        model.setArn(instance.arn());
        model.setId(instance.id());
        model.setIdentityManagementType(instance.identityManagementTypeAsString());
        model.setInstanceAlias(instance.instanceAlias());
        model.setCreatedTime(instance.createdTime().toString());
        model.setServiceRole(instance.serviceRole());
        model.setInstanceStatus(instance.instanceStatusAsString());

        Attributes attributes = Attributes.builder().build();
        attributes.setInboundCalls(instance.inboundCallsEnabled());
        attributes.setOutboundCalls(instance.outboundCallsEnabled());
        model.setAttributes(attributes);

        if(!INSTANCE_STATUS_ACTIVE.equals(instance.instanceStatusAsString())) {
            return;
        }
        List<Attribute> instanceAttributes = listInstanceAttributes(instance.arn());
        for(Attribute attribute: instanceAttributes) {
            InstanceAttributeType attributeType = attribute.attributeType();
            if(attributeType == InstanceAttributeType.CONTACTFLOW_LOGS) {
                model.getAttributes().setContactflowLogs(Boolean.parseBoolean(attribute.value()));
            } else if(attributeType == InstanceAttributeType.CONTACT_LENS) {
                model.getAttributes().setContactLens(Boolean.parseBoolean(attribute.value()));
            } else if(attributeType == InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES) {
                model.getAttributes().setAutoResolveBestVoices(Boolean.parseBoolean(attribute.value()));
            } else if(attributeType == InstanceAttributeType.USE_CUSTOM_TTS_VOICES) {
                model.getAttributes().setUseCustomTTSVoices(Boolean.parseBoolean(attribute.value()));
            } else if(attributeType == InstanceAttributeType.EARLY_MEDIA) {
                model.getAttributes().setEarlyMedia(Boolean.parseBoolean(attribute.value()));
            }
        }
    }

    private DescribeInstanceResponse describeInstance(String arn) {
        logger.log(String.format("Invoking DescribeInstance operation for instance %s", arn));
        final DescribeInstanceRequest request = DescribeInstanceRequest.builder().instanceId(arn).build();
        return invoke(request, proxyClient, proxyClient.client()::describeInstance, logger);
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
