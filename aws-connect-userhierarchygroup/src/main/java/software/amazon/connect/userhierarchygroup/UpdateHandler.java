package software.amazon.connect.userhierarchygroup;

import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.UpdateUserHierarchyGroupNameRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel desiredStateModel = request.getDesiredResourceState();
        final ResourceModel previousStateModel = request.getPreviousResourceState();

        logger.log(String.format("Invoked UpdateUserHierarchyGroupHandler with UserHierarchyGroup:%s", desiredStateModel.getUserHierarchyGroupArn()));

        if (StringUtils.isNotEmpty(desiredStateModel.getInstanceArn()) && !desiredStateModel.getInstanceArn().equals(previousStateModel.getInstanceArn())) {
            throw new CfnInvalidRequestException("InstanceArn cannot be updated.");
        }

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateUserHierarchyGroupName(proxy, proxyClient, desiredStateModel, previousStateModel, progress, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(desiredStateModel));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateUserHierarchyGroupName(final AmazonWebServicesClientProxy proxy,
                                                                                       final ProxyClient<ConnectClient> proxyClient,
                                                                                       final ResourceModel desiredStateModel,
                                                                                       final ResourceModel previousStateModel,
                                                                                       final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                                       final CallbackContext context,
                                                                                       final Logger logger) {

        final boolean updateUserHierarchyGroupName = !StringUtils.equals(desiredStateModel.getName(), previousStateModel.getName());

        if (updateUserHierarchyGroupName) {
            logger.log(String.format("Calling UpdateUserHierarchyGroupName API for UserHierarchyGroup:%s", desiredStateModel.getUserHierarchyGroupArn()));
            return proxy.initiate("connect::updateUserHierarchyGroupName", proxyClient, desiredStateModel, context)
                    .translateToServiceRequest(UpdateHandler::translateToUpdateUserHierarchyGroupNameRequest)
                    .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::updateUserHierarchyGroupName, logger))
                    .progress();
        } else {
            logger.log(String.format("UserHierarchyGroup name field is unchanged from in the update operation, " +
                    "skipping UpdateUserHierarchyGroupName API call for UserHierarchyGroup:%s", desiredStateModel.getUserHierarchyGroupArn()));
            return progress;
        }
    }

    private static UpdateUserHierarchyGroupNameRequest translateToUpdateUserHierarchyGroupNameRequest(final ResourceModel model) {
        return UpdateUserHierarchyGroupNameRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .hierarchyGroupId(model.getUserHierarchyGroupArn())
                .name(model.getName())
                .build();
    }

}
