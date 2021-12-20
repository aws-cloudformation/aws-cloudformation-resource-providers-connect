# AWS::Connect::Instance Attributes

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#inboundcalls" title="InboundCalls">InboundCalls</a>" : <i>Boolean</i>,
    "<a href="#outboundcalls" title="OutboundCalls">OutboundCalls</a>" : <i>Boolean</i>,
    "<a href="#contactflowlogs" title="ContactflowLogs">ContactflowLogs</a>" : <i>Boolean</i>,
    "<a href="#contactlens" title="ContactLens">ContactLens</a>" : <i>Boolean</i>,
    "<a href="#autoresolvebestvoices" title="AutoResolveBestVoices">AutoResolveBestVoices</a>" : <i>Boolean</i>,
    "<a href="#usecustomttsvoices" title="UseCustomTTSVoices">UseCustomTTSVoices</a>" : <i>Boolean</i>,
    "<a href="#earlymedia" title="EarlyMedia">EarlyMedia</a>" : <i>Boolean</i>
}
</pre>

### YAML

<pre>
<a href="#inboundcalls" title="InboundCalls">InboundCalls</a>: <i>Boolean</i>
<a href="#outboundcalls" title="OutboundCalls">OutboundCalls</a>: <i>Boolean</i>
<a href="#contactflowlogs" title="ContactflowLogs">ContactflowLogs</a>: <i>Boolean</i>
<a href="#contactlens" title="ContactLens">ContactLens</a>: <i>Boolean</i>
<a href="#autoresolvebestvoices" title="AutoResolveBestVoices">AutoResolveBestVoices</a>: <i>Boolean</i>
<a href="#usecustomttsvoices" title="UseCustomTTSVoices">UseCustomTTSVoices</a>: <i>Boolean</i>
<a href="#earlymedia" title="EarlyMedia">EarlyMedia</a>: <i>Boolean</i>
</pre>

## Properties

#### InboundCalls

Mandatory element which enables inbound calls on new instance.

_Required_: Yes

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### OutboundCalls

Mandatory element which enables outbound calls on new instance.

_Required_: Yes

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ContactflowLogs

Boolean flag which enables CONTACTFLOW_LOGS on an instance.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ContactLens

Boolean flag which enables CONTACT_LENS on an instance.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoResolveBestVoices

Boolean flag which enables AUTO_RESOLVE_BEST_VOICES on an instance.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UseCustomTTSVoices

Boolean flag which enables USE_CUSTOM_TTS_VOICES on an instance.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EarlyMedia

Boolean flag which enables EARLY_MEDIA on an instance.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

