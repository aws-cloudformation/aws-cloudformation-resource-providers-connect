package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.CreateTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.CreateTrafficDistributionGroupResponse;

import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupRequest;
import software.amazon.awssdk.services.connect.model.DescribeTrafficDistributionGroupResponse;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.TrafficDistributionGroupStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Map;

public class CreateHandler extends BaseHandlerStd {

    /**
     Handles the process of creating a Traffic Distribution Group (TDG) and manages the status
     transition during the creation operation. The method uses an inProgressHandler to handle
     the status of the ongoing TDG creation.
     The inProgressHandler is responsible for checking the status of the TDG creation operation
     and retrying until the status becomes ACTIVE or an error occurs. The handler will be called
     multiple times if the operation is still in progress (status is PENDING). This method relies
     on the built-in backoff mechanism of the AWS CloudFormation Custom Resource framework, which
     helps manage the retry logic.
     The transition from IN_PROGRESS to SUCCESS happens when the TDG creation operation reaches
     the ACTIVE status. The handler will return a successful response with the ACTIVE status, and
     the AWS CloudFormation stack operation will be marked as complete.
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
        final Map<String, String> tags = request.getDesiredResourceTags();

        logger.log(String.format("Invoked CreateTrafficDistributionGroupHandler with Instance:%s, Traffic Distribution Group Name:%s",
                model.getInstanceArn(), model.getName()));

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> createTrafficDistributionGroup(proxy, callbackContext, proxyClient, logger, model, tags))
                .then(progress -> {
                    if (!callbackContext.getCallBackDelay()) {
                        callbackContext.enableCallBackDelay(true);
                        logger.log ("In Create, Initiate a CallBack Delay of " + CALLBACK_DELAY_SECONDS + " seconds");
                        progress = ProgressEvent.
                                defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, model);
                    }
                    return progress;
                })
                .then(progress -> ProgressEvent.defaultSuccessHandler(model));
    }

    /**
     * Stabilize method is used for polling the status of an asynchronous operation.
     * In this case, the method is used to keep checking if the Traffic Distribution Group
     * is active before transitioning to the next step.
     */
    private ProgressEvent<ResourceModel, CallbackContext> createTrafficDistributionGroup(
            final AmazonWebServicesClientProxy proxy,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger,
            final ResourceModel model,
            final Map<String, String> tags) {
        return proxy.initiate("connect::createTrafficDistributionGroup", proxyClient, model, callbackContext)
                .translateToServiceRequest(resourceModel -> translateCfnModelToCreateTrafficDistributionGroupRequest(resourceModel, tags))
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::createTrafficDistributionGroup, logger))
                .stabilize((_request, _response, _client, _model, _callback) ->
                        isTrafficDistributionGroupActive(proxyClient,
                                translateCreateTrafficDistributionGroupResponseToCfnModel(_model, _response), logger))
                .progress();
    }

    private CreateTrafficDistributionGroupRequest translateCfnModelToCreateTrafficDistributionGroupRequest(
            final ResourceModel model,
            final Map<String, String> tags) {
        return CreateTrafficDistributionGroupRequest
                .builder()
                .instanceId(model.getInstanceArn())
                .name(model.getName())
                .description(model.getDescription())
                .tags(tags)
                .build();
    }

    private ResourceModel translateCreateTrafficDistributionGroupResponseToCfnModel(
            final ResourceModel model,
            final CreateTrafficDistributionGroupResponse createTrafficDistributionGroupResponse) {
        model.setTrafficDistributionGroupArn(createTrafficDistributionGroupResponse.arn());
        return model;
    }

    /**
     * We are using injectCredentialsAndInvokeV2 instead of invoke to automatically
     * handle injecting the necessary credentials required for making the service call.
     */
    private boolean isTrafficDistributionGroupActive(
            final ProxyClient<ConnectClient> proxyClient,
            final ResourceModel model,
            final Logger logger) {
        final DescribeTrafficDistributionGroupRequest request = DescribeTrafficDistributionGroupRequest.builder()
                .trafficDistributionGroupId(model.getTrafficDistributionGroupArn())
                .build();
        DescribeTrafficDistributionGroupResponse describeTrafficDistributionGroupResponse = null;
        try {
            describeTrafficDistributionGroupResponse = proxyClient
                    .injectCredentialsAndInvokeV2(request, proxyClient.client()::describeTrafficDistributionGroup);
        } catch (final ResourceNotFoundException e) {
            return false;
        } catch (final Exception e) {
            handleCommonExceptions(e, logger);
        }
        String status = describeTrafficDistributionGroupResponse.trafficDistributionGroup().status().toString();
        if (TrafficDistributionGroupStatus.CREATION_FAILED.toString().equals(status)) {
            throw new CfnNotStabilizedException("Traffic Distribution Group creation failed :", model.getTrafficDistributionGroupArn());
        }
        return TrafficDistributionGroupStatus.ACTIVE.toString().equals(status);
    }
}
