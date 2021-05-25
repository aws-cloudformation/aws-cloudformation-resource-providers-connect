package software.amazon.connect.quickconnect;

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
import software.amazon.awssdk.services.connect.model.QuickConnectType;
import software.amazon.awssdk.services.connect.model.ResourceNotFoundException;
import software.amazon.awssdk.services.connect.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceInternalErrorException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProxyClient;

import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.CONTACT_FLOW_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.PHONE_NUMBER;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.QUEUE_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.TAGS_SET_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.USER_ID;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_KEY_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_KEY_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_VALUE_ONE;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.VALID_TAG_VALUE_TWO;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeQueue;
import static software.amazon.connect.quickconnect.QuickConnectTestDataProvider.buildQuickConnectResourceModelWithQuickConnectTypeUser;

@ExtendWith(MockitoExtension.class)
public class BaseHandlerStdTest {

    private static final String PARAMETER_NAME = "QuickConnectConfig";
    private static final String MISSING_MANDATORY_PARAMETER_EXCEPTION_MESSAGE = "Invalid request provided: Required parameter missing " + PARAMETER_NAME;
    private static final String INVALID_QUICK_CONNECT_TYPE = "InvalidQuickConnectType";
    private static final String ACCESS_DENIED_ERROR_CODE = "AccessDeniedException";

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
        Exception ex = ThrottlingException.builder().build();
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

    @Test
    public void testRequireNotNull() {
        CfnInvalidRequestException cfnInvalidRequestException = assertThrows(CfnInvalidRequestException.class, () ->
                BaseHandlerStd.requireNotNull(null, PARAMETER_NAME));
        assertThat(MISSING_MANDATORY_PARAMETER_EXCEPTION_MESSAGE).isEqualTo(cfnInvalidRequestException.getMessage());
    }

    @Test
    public void testTranslateToQuickConnectConfig_TypePhoneNumber() {
        software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig phoneNumberQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.PhoneNumberQuickConnectConfig
                        .builder()
                        .phoneNumber(PHONE_NUMBER)
                        .build();
        software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypePhoneNumber = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.PHONE_NUMBER.toString())
                .phoneConfig(phoneNumberQuickConnectConfig)
                .build();
        assertThat(BaseHandlerStd.translateToQuickConnectConfig(buildQuickConnectResourceModelWithQuickConnectTypePhoneNumber())).isEqualTo(quickConnectConfigTypePhoneNumber);
    }

    @Test
    public void testTranslateToQuickConnectConfig_TypeUser() {
        software.amazon.awssdk.services.connect.model.UserQuickConnectConfig userQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.UserQuickConnectConfig
                        .builder()
                        .userId(USER_ID)
                        .contactFlowId(CONTACT_FLOW_ID)
                        .build();
        software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypeUser = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.USER.toString())
                .userConfig(userQuickConnectConfig)
                .build();
        assertThat(BaseHandlerStd.translateToQuickConnectConfig(buildQuickConnectResourceModelWithQuickConnectTypeUser())).isEqualTo(quickConnectConfigTypeUser);
    }

    @Test
    public void testTranslateToQuickConnectConfig_TypeQueue() {
        software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig queueQuickConnectConfig =
                software.amazon.awssdk.services.connect.model.QueueQuickConnectConfig
                        .builder()
                        .queueId(QUEUE_ID)
                        .contactFlowId(CONTACT_FLOW_ID)
                        .build();
        software.amazon.awssdk.services.connect.model.QuickConnectConfig quickConnectConfigTypeQueue = software.amazon.awssdk.services.connect.model.QuickConnectConfig
                .builder()
                .quickConnectType(QuickConnectType.QUEUE.toString())
                .queueConfig(queueQuickConnectConfig)
                .build();
        assertThat(BaseHandlerStd.translateToQuickConnectConfig(buildQuickConnectResourceModelWithQuickConnectTypeQueue())).isEqualTo(quickConnectConfigTypeQueue);
    }

    @Test
    public void testTranslateToQuickConnectConfig_InvalidType() {
        QuickConnectConfig quickConnectConfigTypeInvalid = QuickConnectConfig
                .builder()
                .quickConnectType(INVALID_QUICK_CONNECT_TYPE)
                .build();
        ResourceModel resourceModel = ResourceModel.builder()
                .quickConnectConfig(quickConnectConfigTypeInvalid)
                .tags(TAGS_SET_TWO)
                .build();
        assertThrows(CfnInvalidRequestException.class, () ->
                BaseHandlerStd.translateToQuickConnectConfig(resourceModel));
    }
}
