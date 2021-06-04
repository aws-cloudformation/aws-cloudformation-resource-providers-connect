package software.amazon.connect.quickconnect;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.DescribeQuickConnectRequest;
import software.amazon.awssdk.services.connect.model.QuickConnect;
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ReadHandler extends BaseHandlerStd {

    private static final String USER_ARN_FORMAT = "%s/agent/%s";
    private static final String CONTACT_FLOW_ARN_FORMAT = "%s/contact-flow/%s";
    private static final String QUEUE_ARN_FORMAT = "%s/queue/%s";


    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final String quickConnectArn = model.getQuickConnectArn();

        logger.log(String.format("Invoked new ReadQuickConnectHandler with QuickConnect:%s", quickConnectArn));

        if (!isValidQuickConnectArn(quickConnectArn)) {
            throw new CfnNotFoundException(new CfnInvalidRequestException(String.format("%s is not a valid Quick Connect Arn", quickConnectArn)));
        }

        return proxy.initiate("connect::describeQuickConnect", proxyClient, model, callbackContext)
                .translateToServiceRequest(this::translateToDescribeQuickConnectRequest)
                .makeServiceCall((req, clientProxy) -> invoke(req, clientProxy, clientProxy.client()::describeQuickConnect, logger))
                .done(response -> ProgressEvent.defaultSuccessHandler(setQuickConnectProperties(model, response.quickConnect())));
    }

    private DescribeQuickConnectRequest translateToDescribeQuickConnectRequest(final ResourceModel model) {
        return DescribeQuickConnectRequest
                .builder()
                .instanceId(getInstanceArnFromQuickConnectArn(model.getQuickConnectArn()))
                .quickConnectId(model.getQuickConnectArn())
                .build();
    }

    private software.amazon.connect.quickconnect.QuickConnectConfig translateToResourceModelQuickConnectConfig
            (final String instanceArn, final software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfig) {
        final String quickConnectType = quickConnectConfig.quickConnectType().toString();
        final software.amazon.connect.quickconnect.QuickConnectConfig resourceModelQuickConnectConfig = new software.amazon.connect.quickconnect.QuickConnectConfig();
        resourceModelQuickConnectConfig.setQuickConnectType(quickConnectType);

        if (quickConnectType.equals(QuickConnectType.USER.toString())) {
            final software.amazon.connect.quickconnect.UserQuickConnectConfig userQuickConnectConfig = new software.amazon.connect.quickconnect.UserQuickConnectConfig();
            userQuickConnectConfig.setUserArn(String.format(USER_ARN_FORMAT, instanceArn, quickConnectConfig.userConfig().userId()));
            userQuickConnectConfig.setContactFlowArn(String.format(CONTACT_FLOW_ARN_FORMAT, instanceArn, quickConnectConfig.userConfig().contactFlowId()));
            resourceModelQuickConnectConfig.setUserConfig(userQuickConnectConfig);
        }

        if (quickConnectType.equals(QuickConnectType.QUEUE.toString())) {
            final software.amazon.connect.quickconnect.QueueQuickConnectConfig queueQuickConnectConfig = new software.amazon.connect.quickconnect.QueueQuickConnectConfig();
            queueQuickConnectConfig.setQueueArn(String.format(QUEUE_ARN_FORMAT, instanceArn, quickConnectConfig.queueConfig().queueId()));
            queueQuickConnectConfig.setContactFlowArn(String.format(CONTACT_FLOW_ARN_FORMAT, instanceArn, quickConnectConfig.queueConfig().contactFlowId()));

            resourceModelQuickConnectConfig.setQueueConfig(queueQuickConnectConfig);
        }

        if (quickConnectType.equals(QuickConnectType.PHONE_NUMBER.toString())) {
            final software.amazon.connect.quickconnect.PhoneNumberQuickConnectConfig phoneNumberQuickConnectConfig = new software.amazon.connect.quickconnect.PhoneNumberQuickConnectConfig();
            phoneNumberQuickConnectConfig.setPhoneNumber(quickConnectConfig.phoneConfig().phoneNumber());
            resourceModelQuickConnectConfig.setPhoneConfig(phoneNumberQuickConnectConfig);
        }
        return resourceModelQuickConnectConfig;
    }

    private ResourceModel setQuickConnectProperties(final ResourceModel model, final QuickConnect quickConnect) {
        final String instanceArn = getInstanceArnFromQuickConnectArn(quickConnect.quickConnectARN());
        model.setInstanceArn(instanceArn);
        model.setName(quickConnect.name());
        model.setDescription(quickConnect.description());
        model.setQuickConnectConfig(translateToResourceModelQuickConnectConfig(instanceArn, quickConnect.quickConnectConfig()));
        model.setTags(convertResourceTagsToSet(quickConnect.tags()));
        return model;
    }
}
