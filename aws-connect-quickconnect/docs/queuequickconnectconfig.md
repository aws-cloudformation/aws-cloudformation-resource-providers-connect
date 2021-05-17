# AWS::Connect::QuickConnect QueueQuickConnectConfig

The queue configuration. This is required only if QuickConnectType is QUEUE.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#contactflowid" title="ContactFlowId">ContactFlowId</a>" : <i>String</i>,
    "<a href="#queueid" title="QueueId">QueueId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#contactflowid" title="ContactFlowId">ContactFlowId</a>: <i>String</i>
<a href="#queueid" title="QueueId">QueueId</a>: <i>String</i>
</pre>

## Properties

#### ContactFlowId

The identifier of the contact flow.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### QueueId

The identifier for the queue.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
