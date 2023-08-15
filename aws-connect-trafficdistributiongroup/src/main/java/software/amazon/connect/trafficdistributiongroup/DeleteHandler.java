package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DeleteTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroupStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    /**
     Handles the process of deleting a Traffic Distribution Group (TDG) and manages the status
     transition during the deletion operation. The method uses a stabilization mechanism to handle
     the status of the ongoing TDG deletion.
     The stabilization mechanism is responsible for checking the status of the TDG deletion operation
     and retrying until the TDG is successfully deleted or an error occurs. The handler will call
     the isTrafficDistributionGroupDeleted method to check if the TDG has been deleted or not.
     If the TDG is not deleted, the stabilization mechanism will retry until the deletion is
     successful or a DELETION_FAILED status is encountered.
     The transition from IN_PROGRESS to SUCCESS happens when the TDG deletion operation is successful
     and the TDG no longer exists. The handler will return a successful response indicating the
     completion of the AWS CloudFormation stack operation.
     If the deletion operation encounters a DELETION_FAILED status, the handler will throw a
     CfnNotStabilizedException, indicating that the deletion operation failed and the stack
     operation cannot be completed.
     @param proxy AmazonWebServicesClientProxy to make AWS SDK calls
     @param request ResourceHandlerRequest containing the desired resource state
     @param callbackContext The context object passed between AWS Lambda invocations
     @param proxyClient ProxyClient to inject credentials and invoke AWS SDK
     @param logger Logger to log information and errors
     @return ProgressEvent with the updated resource model and status
     */
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();

        logger.log(String.format("Invoked DeleteTrafficDistributionGroup with Traffic Distribution Group:%s",
                model.getTrafficDistributionGroupArn()));

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> checkIfTrafficDistributionGroupExists(proxy, callbackContext, proxyClient, logger, model))
                .then(progress -> deleteTrafficDistributionGroup(proxy, callbackContext, proxyClient, logger, model))
                .then(progress -> {
                    if (!callbackContext.getCallBackDelay()) {
                        callbackContext.enableCallBackDelay(true);
                        logger.log ("In Delete, Initiate a CallBack Delay of " + CALLBACK_DELAY_SECONDS + " seconds");
                        progress = ProgressEvent
                                .defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, model);
                    }
                    return progress;
                })
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private ProgressEvent<ResourceModel, CallbackContext> checkIfTrafficDistributionGroupExists(
            final AmazonWebServicesClientProxy proxy,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger,
            final ResourceModel model) {
        return proxy.initiate("connect::describeTrafficDistributionGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateCfnModelToDescribeTrafficDistributionGroupRequest)
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::describeTrafficDistributionGroup, logger))
                .progress();
    }

    private DescribeTrafficDistributionGroupRequest translateCfnModelToDescribeTrafficDistributionGroupRequest(
            final ResourceModel model) {
        return DescribeTrafficDistributionGroupRequest.builder()
                .trafficDistributionGroupId(model.getTrafficDistributionGroupArn())
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteTrafficDistributionGroup(
            final AmazonWebServicesClientProxy proxy,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger,
            final ResourceModel model) {
        return proxy.initiate("connect::deleteTrafficDistributionGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateCfnModelToDeleteTrafficDistributionGroupRequest)
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::deleteTrafficDistributionGroup, logger))
                .stabilize((_request, _response, _client, _model, _callback) ->
                        isTrafficDistributionGroupDeleted(proxyClient, model))
                .progress();
    }

    private DeleteTrafficDistributionGroupRequest translateCfnModelToDeleteTrafficDistributionGroupRequest(
            final ResourceModel model) {
        return DeleteTrafficDistributionGroupRequest.builder()
                .trafficDistributionGroupId(model.getTrafficDistributionGroupArn())
                .build();
    }
    private boolean isTrafficDistributionGroupDeleted(
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel model) {
        final DescribeTrafficDistributionGroupRequest request = DescribeTrafficDistributionGroupRequest.builder()
                .trafficDistributionGroupId(model.getTrafficDistributionGroupArn())
                .build();
        DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse;
        try {
            describeTrafficDistributionGroupResponse = proxyClient
                    .injectCredentialsAndInvokeV2(request, proxyClient.client()::describeTrafficDistributionGroup);
        } catch (final ResourceNotFoundException e) {
            return true;
        }
        String status = describeTrafficDistributionGroupResponse.trafficDistributionGroup().status().toString();
        if (TrafficDistributionGroupStatus.DELETION_FAILED.toString().equals(status)) {
            throw new CfnNotStabilizedException("Traffic Distribution Group deletion failed :", model.getTrafficDistributionGroupArn());
        }
        return false;
    }
}
