package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ListTrafficDistributionGroupsRequest;
import software.amazon.awssdk.services.connect.model.ListTrafficDistributionGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ListHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Invoked ListTrafficDistributionGroupsHandler for Instance: %s",
                model.getInstanceArn()));

        return proxy.initiate("connect::listTrafficDistributionGroups", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel ->
                        translateCfnModelToListTrafficDistributionGroupsRequest(resourceModel, request))
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::listTrafficDistributionGroups, logger))
                .done(response ->
                        ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .resourceModels(translateListTrafficDistributionGroupsResponseToCfnModel(response))
                            .status(OperationStatus.SUCCESS)
                            .nextToken(response.nextToken())
                            .build());
    }

    private ListTrafficDistributionGroupsRequest translateCfnModelToListTrafficDistributionGroupsRequest(
            final ResourceModel model,
            final ResourceHandlerRequest<ResourceModel> request) {

        return ListTrafficDistributionGroupsRequest.builder()
                        .instanceId(model.getInstanceArn())
                        .nextToken(request.getNextToken())
                        .build();
    }

    private List<ResourceModel> translateListTrafficDistributionGroupsResponseToCfnModel(
            final ListTrafficDistributionGroupsResponse response) {
        final List<ResourceModel> responseModels = new ArrayList<>();
        response.trafficDistributionGroupSummaryList()
                .forEach(trafficDistributionGroupSummary -> {
                    ResourceModel responseModel = ResourceModel.builder()
                            .trafficDistributionGroupArn(trafficDistributionGroupSummary.arn())
                            .instanceArn(trafficDistributionGroupSummary.instanceArn())
                            .name(trafficDistributionGroupSummary.name())
                            .status(trafficDistributionGroupSummary.statusAsString())
                            .isDefault(Optional.ofNullable(trafficDistributionGroupSummary.isDefault())
                                    .orElse(false))
                            .build();
                    responseModels.add(responseModel);
                });
        return responseModels;
    }
}
