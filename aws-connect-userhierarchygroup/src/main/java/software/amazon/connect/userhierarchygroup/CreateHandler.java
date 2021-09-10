package software.amazon.connect.userhierarchygroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateUserHierarchyGroupRequest;
import software.amazon.awssdk.services.connect.model.CreateUserHierarchyGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Invoked CreateUserHierarchyGroupHandler with InstanceArn:%s, UserHierarchyName:%s, ParentGroupArn:%s", model.getInstanceArn(), model.getName(), model.getParentGroupArn()));

        return proxy.initiate("connect::createUserHierarchyGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateUserHierarchyGroupRequest(resourceModel))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createUserHierarchyGroup, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setUserHierarchyGroupIdentifier(model, response)));
    }

    private CreateUserHierarchyGroupRequest translateToCreateUserHierarchyGroupRequest(final ResourceModel model) {
        return CreateUserHierarchyGroupRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .parentGroupId(model.getParentGroupArn())
                .build();
    }

    private ResourceModel setUserHierarchyGroupIdentifier(final ResourceModel model, final CreateUserHierarchyGroupResponse createuserHierarchyGroupResponse) {
        model.setUserHierarchyGroupArn(createuserHierarchyGroupResponse.hierarchyGroupArn());
        return model;
    }
}
