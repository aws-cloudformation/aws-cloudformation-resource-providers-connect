package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroup;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Optional;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Invoked DescribeTrafficDistributionGroupHandler for Instance: %s and "
                        + "TDG: %s", model.getInstanceArn(), model.getTrafficDistributionGroupArn()));

        return proxy.initiate("connect::describeTrafficDistributionGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateCfnModelToDescribeTrafficDistributionGroupRequest)
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::describeTrafficDistributionGroup, logger))
                .done(response ->
                        ProgressEvent.defaultSuccessHandler(
                                translateDescribeTrafficDistributionGroupResponseToCfnModel(response)));
    }

    private DescribeTrafficDistributionGroupRequest translateCfnModelToDescribeTrafficDistributionGroupRequest(
            final ResourceModel model) {
        return DescribeTrafficDistributionGroupRequest.builder()
                .trafficDistributionGroupId(model.getTrafficDistributionGroupArn())
                .build();
    }

    private ResourceModel translateDescribeTrafficDistributionGroupResponseToCfnModel(
            final DescribeTrafficDistributionGroupResponse response) {
        final TrafficDistributionGroup trafficDistributionGroup = response.trafficDistributionGroup();
        return ResourceModel.builder()
                .trafficDistributionGroupArn(trafficDistributionGroup.arn())
                .name(trafficDistributionGroup.name())
                .status(trafficDistributionGroup.statusAsString())
                .description(trafficDistributionGroup.description())
                .instanceArn(trafficDistributionGroup.instanceArn())
                .tags(TagHelper.convertToSet(trafficDistributionGroup.tags()))
                .isDefault(Optional.ofNullable(trafficDistributionGroup.isDefault())
                        .orElse(false))
                .build();
    }
}
