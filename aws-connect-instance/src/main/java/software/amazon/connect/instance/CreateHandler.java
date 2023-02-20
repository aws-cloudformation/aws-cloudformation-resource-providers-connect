package software.amazon.connect.instance;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateInstanceRequest;
import software.amazon.awssdk.services.connect.model.CreateInstanceResponse;
import software.amazon.awssdk.services.connect.model.DescribeInstanceRequest;
import software.amazon.awssdk.services.connect.model.DescribeInstanceResponse;
import software.amazon.awssdk.services.connect.model.Instance;
import software.amazon.awssdk.services.connect.model.InstanceAttributeType;
import software.amazon.awssdk.services.connect.model.UpdateInstanceAttributeRequest;
import software.amazon.awssdk.services.connect.model.UpdateInstanceAttributeResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    private static final int NUMBER_OF_STATE_POLL_RETRIES = 175;
    private static final int POLL_RETRY_DELAY_IN_MS = 5000;
    private static final String TIMED_OUT_MESSAGE = "Timed out waiting for instance to become available.";

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

        logger.log(String.format("Invoked CreateHandler with accountId:%s, IdentityManagementType:%s, InboundCalls:%s, OutboundCalls:%s",
                request.getAwsAccountId(), model.getIdentityManagementType(), model.getAttributes().getInboundCalls(), model.getAttributes().getOutboundCalls()));

        final CallbackContext currentContext = callbackContext == null || callbackContext.getInstance() == null ?
                CallbackContext.builder().stabilizationRetriesRemaining(NUMBER_OF_STATE_POLL_RETRIES).build() :
                callbackContext;

        return createInstanceAndUpdateProgress(model, currentContext, request.getAwsAccountId());
    }

    private ProgressEvent<ResourceModel, CallbackContext> createInstanceAndUpdateProgress(ResourceModel model, CallbackContext callbackContext, String accountId) {
        // This Lambda will continually be re-invoked with the current state of the instance, finally succeeding when state stabilizes.
        final Instance instance = callbackContext.getInstance();

        if (callbackContext.getStabilizationRetriesRemaining() == 0) {
            throw new RuntimeException(TIMED_OUT_MESSAGE);
        }

        if (instance == null || instance.instanceStatusAsString() == null) { // Create an instance and return IN_PROGRESS event
            CreateInstanceResponse createInstanceResponse = createInstance(model, accountId);
            model.setArn(createInstanceResponse.arn());
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(CallbackContext.builder()
                            .instance(describeInstance(createInstanceResponse.arn()).instance())
                            .stabilizationRetriesRemaining(NUMBER_OF_STATE_POLL_RETRIES)
                            .build())
                    .build();
        } else if (INSTANCE_STATUS_CREATION_FAILED.equals(instance.instanceStatusAsString())) { // Return FAILED event for failed instance creation
            model.setArn(callbackContext.getInstance().arn());
            model.setStatusReason(instance.statusReason().toString());
            model.setInstanceStatus(instance.instanceStatusAsString());
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.FAILED)
                    .message(callbackContext.getInstance().statusReason().message())
                    .build();
        } else if (INSTANCE_STATUS_ACTIVE.equals(instance.instanceStatusAsString())) { // Update instance attributes after successful instance creation
            return processInstanceAttributes(model, callbackContext);
        } else if (INSTANCE_STATUS_CREATION_IN_PROGRESS.equals(instance.instanceStatusAsString())){ // Poll during instance creation IN_PROGRESS until it either succeeds or fails
            try {
                Thread.sleep(POLL_RETRY_DELAY_IN_MS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            DescribeInstanceResponse describeInstanceResponse = describeInstance(callbackContext.getInstance().arn());
            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModel(model)
                    .status(OperationStatus.IN_PROGRESS)
                    .callbackContext(CallbackContext.builder()
                            .instance(describeInstanceResponse.instance())
                            .stabilizationRetriesRemaining(callbackContext.getStabilizationRetriesRemaining() - 1)
                            .build())
                    .build();
        }
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.FAILED)
                .message(callbackContext.getInstance().statusReason().message())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> processInstanceAttributes(ResourceModel model, CallbackContext callbackContext) {
        final Instance instance = callbackContext.getInstance();
        Attributes attributes = model.getAttributes();
        logger.log(String.format("Updating instance attributes with %s", attributes));
        if(attributes.getContactflowLogs() != null) {
            updateInstanceAttribute(instance.arn(), InstanceAttributeType.CONTACTFLOW_LOGS, String.valueOf(attributes.getContactflowLogs()));
            attributes.setContactflowLogs(attributes.getContactflowLogs());
        }
        if(attributes.getContactLens() != null) {
            updateInstanceAttribute(instance.arn(), InstanceAttributeType.CONTACT_LENS, String.valueOf(attributes.getContactLens()));
            attributes.setContactLens(attributes.getContactLens());
        }
        if(attributes.getAutoResolveBestVoices() != null) {
            updateInstanceAttribute(instance.arn(), InstanceAttributeType.AUTO_RESOLVE_BEST_VOICES, String.valueOf(attributes.getAutoResolveBestVoices()));
            attributes.setAutoResolveBestVoices(attributes.getAutoResolveBestVoices());
        }
        if(attributes.getUseCustomTTSVoices() != null) {
            updateInstanceAttribute(instance.arn(), InstanceAttributeType.USE_CUSTOM_TTS_VOICES, String.valueOf(attributes.getUseCustomTTSVoices()));
            attributes.setUseCustomTTSVoices(attributes.getUseCustomTTSVoices());
        }
        if(attributes.getEarlyMedia() != null) {
            updateInstanceAttribute(instance.arn(), InstanceAttributeType.EARLY_MEDIA, String.valueOf(attributes.getEarlyMedia()));
            attributes.setEarlyMedia(attributes.getEarlyMedia());
        }

        model.setArn(instance.arn());
        model.setId(instance.id());
        model.setInstanceStatus(instance.instanceStatusAsString());
        model.setCreatedTime(instance.createdTime().toString());
        model.setServiceRole(instance.serviceRole());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private static CreateInstanceRequest translateToCreateInstanceRequest(final ResourceModel model) {
        final String identityManagementType = model.getIdentityManagementType();
        final boolean inboundCallsEnabled = model.getAttributes().getInboundCalls();
        final boolean outboundCallsEnabled = model.getAttributes().getOutboundCalls();
        if (identityManagementType.equals(SAML) || identityManagementType.equals(CONNECT_MANAGED)) {
            requireNullForType(model.getDirectoryId(), DIRECTORY_ID, identityManagementType);
            requireNotNull(model.getInstanceAlias(), INSTANCE_ALIAS);

            return CreateInstanceRequest.builder()
                    .identityManagementType(identityManagementType)
                    .instanceAlias(model.getInstanceAlias())
                    .inboundCallsEnabled(inboundCallsEnabled)
                    .outboundCallsEnabled(outboundCallsEnabled)
                    .build();
        } else if (identityManagementType.equals(EXISTING_DIRECTORY)) {
            requireNullForType(model.getInstanceAlias(), INSTANCE_ALIAS, identityManagementType);
            requireNotNull(model.getDirectoryId(), DIRECTORY_ID);

            return CreateInstanceRequest.builder()
                    .identityManagementType(identityManagementType)
                    .instanceAlias(model.getInstanceAlias())
                    .inboundCallsEnabled(inboundCallsEnabled)
                    .outboundCallsEnabled(outboundCallsEnabled)
                    .build();
        }
        throw new CfnInvalidRequestException(String.format(INVALID_IDENTITY_MANAGEMENT_TYPE, identityManagementType));
    }

    private CreateInstanceResponse createInstance(ResourceModel model, String accountId) {
        logger.log(String.format("Invoking CreateInstance operation for %s", accountId));
        final CreateInstanceRequest request = translateToCreateInstanceRequest(model);
        return invoke(request, proxyClient, proxyClient.client()::createInstance, logger);
    }

    private DescribeInstanceResponse describeInstance(String instanceArn) {
        logger.log(String.format("Invoking DescribeInstance operation for instance %s", instanceArn));
        final DescribeInstanceRequest request = DescribeInstanceRequest.builder().instanceId(instanceArn).build();
        return invoke(request, proxyClient, proxyClient.client()::describeInstance, logger);
    }

    private UpdateInstanceAttributeResponse updateInstanceAttribute(String instanceArn, InstanceAttributeType attributeType, String value) {
        logger.log(String.format("Invoking UpdateInstanceAttribute operation for %s, %s", instanceArn, attributeType));
        final UpdateInstanceAttributeRequest request = UpdateInstanceAttributeRequest.builder()
                .instanceId(instanceArn)
                .attributeType(attributeType)
                .value(value)
                .build();
        return invoke(request, proxyClient, proxyClient.client()::updateInstanceAttribute, logger);
    }
}
