package software.amazon.connect.user;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeUserRequest;
import software.amazon.awssdk.services.connect.model.User;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashSet;

public class ReadHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        final String userArn = model.getUserArn();

        logger.log(String.format("Invoked new ReadUserHandler with User:%s", userArn));

        if (!ArnHelper.isValidUserArn(userArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid User Arn", userArn)));
        }

        return proxy.initiate("connect::describeUser", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeUserRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeUser, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setUserProperties(model, response.user())));

    }

    private DescribeUserRequest translateToDescribeUserRequest(final ResourceModel model) {
        return DescribeUserRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromUserArn(model.getInstanceArn()))
                .userId(model.getUserArn())
                .build();
    }

    private ResourceModel setUserProperties(final ResourceModel model, final User user) {
        final String instanceArn = ArnHelper.getInstanceArnFromUserArn(user.arn());
        model.setInstanceArn(instanceArn);
        model.setDirectoryUserId(user.directoryUserId());
        model.setRoutingProfileArn(user.routingProfileId());
        model.setHierarchyGroupArn(user.hierarchyGroupId());
        model.setSecurityProfileArns(new HashSet<>(user.securityProfileIds()));
        model.setUsername(user.username());
        model.setIdentityInfo(translateToResourceModelUserIdentityInfo(user.identityInfo()));
        model.setPhoneConfig(translateToResourceModelUserPhoneConfig(user.phoneConfig()));
        model.setTags(convertResourceTagsToSet(user.tags()));
        return model;
    }

    private software.amazon.connect.user.UserIdentityInfo translateToResourceModelUserIdentityInfo (final software.amazon.awssdk.services.connect.model.UserIdentityInfo userIdentityInfo){
        final software.amazon.connect.user.UserIdentityInfo resourceModelUserIdentityInfo = new software.amazon.connect.user.UserIdentityInfo();
        resourceModelUserIdentityInfo.setEmail(userIdentityInfo.email());
        resourceModelUserIdentityInfo.setFirstName(userIdentityInfo.firstName());
        resourceModelUserIdentityInfo.setLastName(userIdentityInfo.lastName());
        return resourceModelUserIdentityInfo;
    }

    private software.amazon.connect.user.UserPhoneConfig translateToResourceModelUserPhoneConfig (final software.amazon.awssdk.services.connect.model.UserPhoneConfig userPhoneConfig){
        final software.amazon.connect.user.UserPhoneConfig resourceModelUserPhoneConfig = new software.amazon.connect.user.UserPhoneConfig();
        resourceModelUserPhoneConfig.setAfterContactWorkTimeLimit(userPhoneConfig.afterContactWorkTimeLimit());
        resourceModelUserPhoneConfig.setAutoAccept(userPhoneConfig.autoAccept());
        resourceModelUserPhoneConfig.setDeskPhoneNumber(userPhoneConfig.deskPhoneNumber());
        resourceModelUserPhoneConfig.setPhoneType(userPhoneConfig.phoneTypeAsString());
        return resourceModelUserPhoneConfig;
    }
}
