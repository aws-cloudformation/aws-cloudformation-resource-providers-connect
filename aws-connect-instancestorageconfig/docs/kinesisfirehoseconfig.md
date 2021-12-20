# AWS::Connect::InstanceStorageConfig KinesisFirehoseConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#firehosearn" title="FirehoseArn">FirehoseArn</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#firehosearn" title="FirehoseArn">FirehoseArn</a>: <i>String</i>
</pre>

## Properties

#### FirehoseArn

An ARN is a unique AWS resource identifier.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:firehose:[-a-z0-9]*:[0-9]{12}:deliverystream/[-a-zA-Z0-9_.]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

