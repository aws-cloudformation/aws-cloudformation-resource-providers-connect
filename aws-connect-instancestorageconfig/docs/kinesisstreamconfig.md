# AWS::Connect::InstanceStorageConfig KinesisStreamConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#streamarn" title="StreamArn">StreamArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#streamarn" title="StreamArn">StreamArn</a>: <i>String</i>
</pre>

## Properties

#### StreamArn

An ARN is a unique AWS resource identifier.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:kinesis:[-a-z0-9]*:[0-9]{12}:stream/[-a-zA-Z0-9_.]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

