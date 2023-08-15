package software.amazon.connect.trafficdistributiongroup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.AccessDeniedException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.ResourceConflictException;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.ResourceNotReadyException;
import software.amazon.awssdk.services.connect.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BaseHandlerStdTest {

    @Mock
    private ProxyClient<ConnectClient> proxyClient;
    @Mock
    private Function<AwsRequest, AwsResponse> function;
    @Mock
    Logger logger;

    @Test
    public void testInvoke() {
        final AwsRequest request = mock(AwsRequest.class);
        final AwsResponse response = mock(AwsResponse.class);
        when(proxyClient.injectCredentialsAndInvokeV2(request, function)).thenReturn(response);
        AwsResponse actual = BaseHandlerStd.invoke(request, proxyClient, function, logger);
        assertThat(actual).isEqualTo(response);
    }

    @Test
    public void testHandleCommonExceptions_ResourceNotFoundException() {
        Exception ex = ResourceNotFoundException.builder().build();
        assertThrows(CfnNotFoundException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_InvalidRequestException() {
        Exception ex = InvalidRequestException.builder().build();
        assertThrows(CfnInvalidRequestException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_InternalServiceException() {
        Exception ex = InternalServiceException.builder().build();
        assertThrows(CfnServiceInternalErrorException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_ResourceConflictException() {
        Exception ex = ResourceConflictException.builder().build();
        assertThrows(CfnResourceConflictException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_ResourceNotReadyException() {
        Exception ex = ResourceNotReadyException.builder().build();
        assertThrows(CfnResourceConflictException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_ThrottlingException() {
        Exception ex = ThrottlingException.builder().build();
        assertThrows(CfnThrottlingException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_AccessDeniedException() {
        Exception ex = AccessDeniedException.builder().build();
        assertThrows(CfnAccessDeniedException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_GeneralServiceException() {
        Exception ex = new RuntimeException();
        assertThrows(CfnGeneralServiceException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

}
