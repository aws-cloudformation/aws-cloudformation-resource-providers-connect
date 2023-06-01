package software.amazon.connect.instancestorageconfig;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DisassociateInstanceStorageConfigRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<ConnectClient> proxyClient,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        this.logger = logger;

        logger.log(String.format("Invoked DeleteHandler with accountId:%s, instanceArn:%s, resourceType:%s, associationId:%s",
                request.getAwsAccountId(), model.getInstanceArn(), model.getResourceType(), model.getAssociationId()));

        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", model.getInstanceArn())));
        }
        requireNotNull(model.getResourceType(), RESOURCE_TYPE);
        requireNotNull(model.getAssociationId(), ASSOCIATION_ID);

        return proxy.initiate("connect::disassociateInstanceStorageConfig", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToDisassociateInstanceStorageConfigRequest(resourceModel))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::disassociateInstanceStorageConfig, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DisassociateInstanceStorageConfigRequest translateToDisassociateInstanceStorageConfigRequest(ResourceModel model) {
        return DisassociateInstanceStorageConfigRequest.builder()
                .instanceId(model.getInstanceArn())
                .resourceType(model.getResourceType())
                .associationId(model.getAssociationId())
                .build();
    }
}
