package software.amazon.connect.userhierarchygroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteUserHierarchyGroupRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        final String userHierarchyGroupArn = model.getUserHierarchyGroupArn();

        logger.log(String.format("Invoked DeleteUserHierarchyGroupHandler with UserHierarchyGroup:%s", userHierarchyGroupArn));

        if (!ArnHelper.isValidUserHierarchyGroupArn(userHierarchyGroupArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid UserHierarchyGroup Arn", userHierarchyGroupArn)));
        }

        return proxy.initiate("connect::deleteUserHierarchyGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDeleteUserHierarchyGroupRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::deleteUserHierarchyGroup, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteUserHierarchyGroupRequest translateToDeleteUserHierarchyGroupRequest(final ResourceModel model) {
        return DeleteUserHierarchyGroupRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromUserHierarchyGroupArn(model.getUserHierarchyGroupArn()))
                .hierarchyGroupId(model.getUserHierarchyGroupArn())
                .build();
    }
}
