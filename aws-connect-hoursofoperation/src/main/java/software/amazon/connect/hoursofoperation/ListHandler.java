package software.amazon.connect.hoursofoperation;

import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.HoursOfOperationSummary;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationsRequest;
import software.amazon.awssdk.services.connect.model.ListHoursOfOperationsResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListHandler extends BaseHandlerStd {
    private Logger logger;
    private ProxyClient<ConnectClient> proxyClient;

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);

    }

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<ConnectClient> proxyClient,
            final Logger logger) {
        this.logger = logger;
        this.proxyClient = proxyClient;
        final ResourceModel model = request.getDesiredResourceState();
        if (model == null) {
            throw new CfnInvalidRequestException("DesiredResourceState is null, unable to get instanceArn to list HoursOfOperation");
        }
        if (!ArnHelper.isValidInstanceArn(model.getInstanceArn())) {
            throw new CfnInvalidRequestException(String.format("The instance Arn provided in the request is not valid"));
        }
        logger.log(String.format("Invoking HoursOfOperation ListHandler with accountId: %s, instanceArn: %s",
                request.getAwsAccountId(), model.getInstanceArn()));
        ListHoursOfOperationsResponse listHoursOfOperationResponse = listHoursOfOperation(request.getNextToken(), model.getInstanceArn());
        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(translateFromListResponse(listHoursOfOperationResponse))
                .nextToken(listHoursOfOperationResponse.nextToken())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private ListHoursOfOperationsResponse listHoursOfOperation(String nextToken, String instanceArn) {
        logger.log(String.format("Invoked listHoursOfOperation operation"));
        ListHoursOfOperationsRequest request = ListHoursOfOperationsRequest.builder()
                .nextToken(nextToken)
                .instanceId(instanceArn)
                .build();
        return invoke(request, proxyClient, proxyClient.client()::listHoursOfOperations, logger);
    }

    private List<ResourceModel> translateFromListResponse(final ListHoursOfOperationsResponse listResponse) {
        return streamOfOrEmpty(listResponse.hoursOfOperationSummaryList())
                .map(hoursOfOperationSummary -> translateToResourceModel(hoursOfOperationSummary))
                .collect(Collectors.toList());
    }

    private ResourceModel translateToResourceModel(final HoursOfOperationSummary hoursOfOperationSummary) {
        return ResourceModel.builder()
                .hoursOfOperationArn(hoursOfOperationSummary.arn())
                .name(hoursOfOperationSummary.name())
                .build();
    }
}