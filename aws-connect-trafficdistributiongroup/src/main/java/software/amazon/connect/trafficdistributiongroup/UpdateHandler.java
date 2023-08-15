package software.amazon.connect.trafficdistributiongroup;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.TagResourceRequest;
import software.amazon.awssdk.services.connect.model.UntagResourceRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Map;

public class UpdateHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> updateResource(proxy, proxyClient, progress.getResourceModel(), request, callbackContext, logger))
                .then(progress -> ProgressEvent.defaultSuccessHandler(request.getDesiredResourceState()));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<ConnectClient> serviceClient,
            final ResourceModel model,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger
    ) {
        logger.log(String.format("Invoked UpdateTrafficDistributionGroupHandler for Instance:%s and TDG:%s",
                model.getInstanceArn(), model.getTrafficDistributionGroupArn()));

        ProgressEvent<ResourceModel,
                CallbackContext> progressEvent =
                ProgressEvent.progress(model, callbackContext);
        if (TagHelper.shouldUpdateTags(request)) {
            progressEvent =  progressEvent
                    .then(progress -> untagResource(proxy, serviceClient, model, request, callbackContext, logger))
                    .then(progress -> tagResource(proxy, serviceClient, model, request, callbackContext, logger));
        }
        return progressEvent;
    }

    private ProgressEvent<ResourceModel, CallbackContext> untagResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<ConnectClient> serviceClient,
            final ResourceModel model,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger
    ) {
        logger.log(String.format("[UPDATE][IN PROGRESS] Going to remove tags for TDG with arn: %s",
                model.getTrafficDistributionGroupArn()));
        final Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
        final Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
        final Map<String, String> tagsToRemove = TagHelper.generateTagsToRemove(previousTags, desiredTags);
        if (tagsToRemove.isEmpty()) {
            logger.log(String.format("No tags to remove for TDG with arn: %s.",
                    model.getTrafficDistributionGroupArn()));
            return ProgressEvent.progress(model, callbackContext);
        }
        logger.log(String.format("Tags to be updated for TDG with arn: %s.",
                model.getTrafficDistributionGroupArn()));
        return proxy.initiate("AWS-Connect-TrafficDistributionGroup::UntagResource",
                        serviceClient,
                        model,
                        callbackContext)
                .translateToServiceRequest(resourceModel -> UntagResourceRequest.builder()
                        .resourceArn(resourceModel.getTrafficDistributionGroupArn())
                        .tagKeys(tagsToRemove.keySet())
                        .build())
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::untagResource, logger))
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> tagResource(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<ConnectClient> serviceClient,
            final ResourceModel model,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger
    ) {
        logger.log(String.format("[UPDATE][IN PROGRESS] Going to add tags for TDG with arn: %s.",
                model.getTrafficDistributionGroupArn()));
        final Map<String, String> previousTags = TagHelper.getPreviouslyAttachedTags(request);
        final Map<String, String> desiredTags = TagHelper.getNewDesiredTags(request);
        final Map<String, String> tagsToAdd = TagHelper.generateTagsToAdd(previousTags, desiredTags);
        if (tagsToAdd.isEmpty()) {
            logger.log(String.format("No tags to add for TDG with arn: %s.",
                    model.getTrafficDistributionGroupArn()));
            return ProgressEvent.progress(model, callbackContext);
        }
        return proxy.initiate("AWS-Connect-TrafficDistributionGroup::TagResource",
                        serviceClient,
                        model,
                        callbackContext)
                .translateToServiceRequest(resourceModel -> TagResourceRequest.builder()
                        .resourceArn(resourceModel.getTrafficDistributionGroupArn())
                        .tags(tagsToAdd)
                        .build())
                .makeServiceCall((req, clientProxy) ->
                        invoke(req, clientProxy, clientProxy.client()::tagResource, logger))
                .progress();
    }
}
