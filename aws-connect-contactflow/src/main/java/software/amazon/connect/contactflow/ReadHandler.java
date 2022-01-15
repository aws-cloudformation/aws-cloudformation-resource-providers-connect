package software.amazon.connect.contactflow;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ContactFlow;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowRequest;
import software.amazon.awssdk.services.connect.model.DescribeContactFlowResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(final AmazonWebServicesClientProxy proxy,
                                                                          final ResourceHandlerRequest<ResourceModel> request,
                                                                          final CallbackContext callbackContext,
                                                                          final ProxyClient<ConnectClient> proxyClient,
                                                                          final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String contactFlowArn = model.getContactFlowArn();

        logger.log(String.format("Invoked new ReadContactFlowHandler with ContactFlow:%s", contactFlowArn));

        if (!ArnHelper.isValidContactFlowArn(contactFlowArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid contact flow Arn", contactFlowArn)));
        }
        return proxy.initiate("connect::describeContactFlow", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeContactFlowRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeContactFlow, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setContactFlowProperties(model, response.contactFlow())));
    }

    private DescribeContactFlowRequest translateToDescribeContactFlowRequest(final ResourceModel model) {
        return DescribeContactFlowRequest
                .builder()
                .instanceId(ArnHelper.getInstanceArnFromContactFlowArn(model.getContactFlowArn()))
                .contactFlowId(model.getContactFlowArn())
                .build();
    }

    private ResourceModel setContactFlowProperties(final ResourceModel model, final ContactFlow contactFlow) {
        final String instanceArn = ArnHelper.getInstanceArnFromContactFlowArn(contactFlow.arn());
        model.setInstanceArn(instanceArn);
        model.setName(contactFlow.name());
        model.setDescription(contactFlow.description());
        model.setTags(convertResourceTagsToSet(contactFlow.tags()));
        model.setState(contactFlow.state().toString());
        model.setType(contactFlow.type().toString());
        model.setContent(contactFlow.content());
        return model;
    }

}
