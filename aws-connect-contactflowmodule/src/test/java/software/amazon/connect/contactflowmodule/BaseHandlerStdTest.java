package software.amazon.connect.contactflowmodule;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.connect.ConnectClient;
import software.amazon.awssdk.services.connect.model.ConnectException;
import software.amazon.awssdk.services.connect.model.DuplicateResourceException;
import software.amazon.awssdk.services.connect.model.InternalServiceException;
import software.amazon.awssdk.services.connect.model.InvalidParameterException;
import software.amazon.awssdk.services.connect.model.InvalidRequestException;
import software.amazon.awssdk.services.connect.model.LimitExceededException;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.VALID_TAG_KEY_ONE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.VALID_TAG_VALUE_ONE;
import static software.amazon.connect.contactflowmodule.ContactFlowModuleTestDataProvider.VALID_TAG_VALUE_TWO;

@ExtendWith(MockitoExtension.class)
public class BaseHandlerStdTest {
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";
    private static final String THROTTLING_ERROR_CODE = "TooManyRequestsException";
    private static final String CONNECT_EXCEPTION_ERROR_CODE = "ConnectException";

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
    public void testHandleCommonExceptions_InvalidParameterException() {
        Exception ex = InvalidParameterException.builder().build();
        assertThrows(CfnInvalidRequestException.class, () ->
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
    public void testHandleCommonExceptions_ThrottlingException() {
        Exception ex = ConnectException.builder()
                .statusCode(429)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(THROTTLING_ERROR_CODE)
                        .build())
                .build();
        assertThrows(CfnThrottlingException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_DuplicateResourceException() {
        Exception ex = DuplicateResourceException.builder().build();
        assertThrows(CfnAlreadyExistsException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_RuntimeException() {
        Exception ex = new RuntimeException();
        assertThrows(CfnGeneralServiceException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_LimitExceededException() {
        Exception ex = LimitExceededException.builder().build();
        assertThrows(CfnServiceLimitExceededException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_ConnectException() {
        Exception ex = ConnectException.builder()
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(CONNECT_EXCEPTION_ERROR_CODE)
                        .build())
                .statusCode(403)
                .build();
        assertThrows(CfnGeneralServiceException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testHandleCommonExceptions_AccessDeniedException() {
        Exception ex = ConnectException.builder()
                .statusCode(403)
                .awsErrorDetails(AwsErrorDetails.builder()
                        .errorCode(ACCESS_DENIED_ERROR_CODE)
                        .build())
                .build();
        assertThrows(CfnAccessDeniedException.class, () ->
                BaseHandlerStd.handleCommonExceptions(ex, logger));
    }

    @Test
    public void testConvertResourceTagsToSet() {
        final ImmutableMap<String, String> tagMap = ImmutableMap.of(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE, VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO);
        final Set<Tag> tagSet = ImmutableSet.of(new Tag(VALID_TAG_KEY_ONE, VALID_TAG_VALUE_ONE), new Tag(VALID_TAG_KEY_TWO, VALID_TAG_VALUE_TWO));
        assertThat(BaseHandlerStd.convertResourceTagsToSet(tagMap)).isEqualTo(tagSet);
    }
}
