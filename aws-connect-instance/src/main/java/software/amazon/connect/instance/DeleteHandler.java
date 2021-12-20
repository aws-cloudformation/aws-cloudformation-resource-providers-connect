package software.amazon.connect.instance;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteInstanceRequest;
import software.amazon.awssdk.services.connect.model.DeleteInstanceResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

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

        logger.log(String.format("Invoked DeleteHandler with Instance Arn:%s", instanceArn));

        if (!ArnHelper.isValidInstanceArn(instanceArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Instance Arn", instanceArn)));
        }

        deleteInstance(instanceArn);
        return ProgressEvent.defaultSuccessHandler(null);
    }

    private DeleteInstanceResponse deleteInstance(String arn) {
        logger.log(String.format("Invoking DeleteInstance operation for %s", arn));
        final DeleteInstanceRequest request = DeleteInstanceRequest.builder().instanceId(arn).build();
        return invoke(request, proxyClient, proxyClient.client()::deleteInstance, logger);
    }
}
