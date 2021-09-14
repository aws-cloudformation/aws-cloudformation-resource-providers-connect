package software.amazon.connect.user;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateUserRequest;
import software.amazon.awssdk.services.connect.model.CreateUserResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;

public class CreateHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final Map<String, String> tags = request.getDesiredResourceTags();

        logger.log(String.format("Invoked CreateUserHandler with InstanceId:%s ", model.getInstanceArn()));

        return proxy.initiate("connect::createUser", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateToCreateUserRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::createUser, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setUserIdentifier(model, response)));

    }

    private CreateUserRequest translateToCreateUserRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateUserRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .username(model.getUsername())
                .password(model.getPassword())
                .securityProfileIds(model.getSecurityProfileArns())
                .routingProfileId(model.getRoutingProfileArn())
                .phoneConfig(translateToUserPhoneConfig(model))
                .identityInfo(translateToUserIdentityInfo(model))
                .directoryUserId(model.getDirectoryUserId())
                .hierarchyGroupId(model.getHierarchyGroupArn())
                .tags(tags)
                .build();
    }

    private ResourceModel setUserIdentifier(final ResourceModel model, final CreateUserResponse createUserResponse) {
        model.setUserArn(createUserResponse.userArn());
        return model;
    }
}
