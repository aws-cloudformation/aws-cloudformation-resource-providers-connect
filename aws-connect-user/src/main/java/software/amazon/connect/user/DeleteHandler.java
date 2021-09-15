package software.amazon.connect.user;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteUserRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String userArn = model.getUserArn();

        logger.log(String.format("Invoked DeleteUserHandler with User:%s", userArn));

        if (!ArnHelper.isValidUserArn(userArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid User Arn", userArn)));
        }

        return proxy.initiate("connect::deleteUser", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteUserRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteUser, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteUserRequest translateToDeleteUserRequest(final ResourceModel model) {
        return DeleteUserRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromUserArn(model.getUserArn()))
                .userId(model.getUserArn())
                .build();
    }
}
