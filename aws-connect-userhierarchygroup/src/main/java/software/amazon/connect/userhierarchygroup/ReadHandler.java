package software.amazon.connect.userhierarchygroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeUserHierarchyGroupRequest;
import software.amazon.awssdk.services.connect.model.HierarchyGroup;
import software.amazon.awssdk.services.connect.model.HierarchyPath;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String userHierarchyGroupArn = model.getUserHierarchyGroupArn();

        logger.log(String.format("Invoked new ReadUserHierarchyGroupHandler with UserHierarchyGroup:%s", userHierarchyGroupArn));

        if (!ArnHelper.isValidUserHierarchyGroupArn(userHierarchyGroupArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid UserHierarchyGroupArn", userHierarchyGroupArn)));
        }

        return proxy.initiate("connect::describeUserHierarchyGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeUserHierarchyGroupRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeUserHierarchyGroup, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setUserHierarchyGroup(model, response.hierarchyGroup())));
    }

    private DescribeUserHierarchyGroupRequest translateToDescribeUserHierarchyGroupRequest(final ResourceModel model) {
        return DescribeUserHierarchyGroupRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromUserHierarchyGroupArn(model.getUserHierarchyGroupArn()))
                .hierarchyGroupId(model.getUserHierarchyGroupArn())
                .build();
    }

    private ResourceModel setUserHierarchyGroup(final ResourceModel model, final HierarchyGroup group) {
        final String instanceArn = ArnHelper.getInstanceArnFromUserHierarchyGroupArn(group.arn());
        model.setInstanceArn(instanceArn);
        model.setName(group.name());
        HierarchyPath parents = group.hierarchyPath();
        int levelId = Integer.parseInt(group.levelId());

        // find the parent group arn
        String parentArn = findParentArnWithLevelId(levelId, parents);
        model.setParentGroupArn(parentArn);
        return model;
    }

    private String findParentArnWithLevelId(int levelId, HierarchyPath path) {
        switch (levelId) {
            case 1: return null;
            case 2: return path.levelOne().arn();
            case 3: return path.levelTwo().arn();
            case 4: return path.levelThree().arn();
            case 5: return path.levelFour().arn();
            default:
                throw new CfnInvalidRequestException("The group is not contained in any hierarchy level");
        }
    }
}
