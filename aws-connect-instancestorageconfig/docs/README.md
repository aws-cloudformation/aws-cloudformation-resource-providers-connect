# AWS::Connect::InstanceStorageConfig

Resource Type definition for AWS::Connect::InstanceStorageConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Connect::InstanceStorageConfig",
    "Properties" : {
        "<a href="#instancearn" title="InstanceArn">InstanceArn</a>" : <i>String</i>,
        "<a href="#resourcetype" title="ResourceType">ResourceType</a>" : <i>String</i>,
        "<a href="#storagetype" title="StorageType">StorageType</a>" : <i>String</i>,
        "<a href="#s3config" title="S3Config">S3Config</a>" : <i><a href="s3config.md">S3Config</a></i>,
        "<a href="#kinesisvideostreamconfig" title="KinesisVideoStreamConfig">KinesisVideoStreamConfig</a>" : <i><a href="kinesisvideostreamconfig.md">KinesisVideoStreamConfig</a></i>,
        "<a href="#kinesisstreamconfig" title="KinesisStreamConfig">KinesisStreamConfig</a>" : <i><a href="kinesisstreamconfig.md">KinesisStreamConfig</a></i>,
        "<a href="#kinesisfirehoseconfig" title="KinesisFirehoseConfig">KinesisFirehoseConfig</a>" : <i><a href="kinesisfirehoseconfig.md">KinesisFirehoseConfig</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Connect::InstanceStorageConfig
Properties:
    <a href="#instancearn" title="InstanceArn">InstanceArn</a>: <i>String</i>
    <a href="#resourcetype" title="ResourceType">ResourceType</a>: <i>String</i>
    <a href="#storagetype" title="StorageType">StorageType</a>: <i>String</i>
    <a href="#s3config" title="S3Config">S3Config</a>: <i><a href="s3config.md">S3Config</a></i>
    <a href="#kinesisvideostreamconfig" title="KinesisVideoStreamConfig">KinesisVideoStreamConfig</a>: <i><a href="kinesisvideostreamconfig.md">KinesisVideoStreamConfig</a></i>
    <a href="#kinesisstreamconfig" title="KinesisStreamConfig">KinesisStreamConfig</a>: <i><a href="kinesisstreamconfig.md">KinesisStreamConfig</a></i>
    <a href="#kinesisfirehoseconfig" title="KinesisFirehoseConfig">KinesisFirehoseConfig</a>: <i><a href="kinesisfirehoseconfig.md">KinesisFirehoseConfig</a></i>
</pre>

## Properties

#### InstanceArn

Connect Instance ID with which the storage config will be associated

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ResourceType

Specifies the type of storage resource available for the instance

_Required_: Yes

_Type_: String

_Allowed Values_: <code>CHAT_TRANSCRIPTS</code> | <code>CALL_RECORDINGS</code> | <code>SCHEDULED_REPORTS</code> | <code>MEDIA_STREAMS</code> | <code>CONTACT_TRACE_RECORDS</code> | <code>AGENT_EVENTS</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### StorageType

Specifies the storage type to be associated with the instance

_Required_: Yes

_Type_: String

_Allowed Values_: <code>S3</code> | <code>KINESIS_VIDEO_STREAM</code> | <code>KINESIS_STREAM</code> | <code>KINESIS_FIREHOSE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### S3Config

_Required_: No

_Type_: <a href="s3config.md">S3Config</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KinesisVideoStreamConfig

_Required_: No

_Type_: <a href="kinesisvideostreamconfig.md">KinesisVideoStreamConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KinesisStreamConfig

_Required_: No

_Type_: <a href="kinesisstreamconfig.md">KinesisStreamConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KinesisFirehoseConfig

_Required_: No

_Type_: <a href="kinesisfirehoseconfig.md">KinesisFirehoseConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### AssociationId

An associationID is automatically generated when a storage config is associated with an instance

