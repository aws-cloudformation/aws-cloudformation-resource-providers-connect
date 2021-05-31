# AWS::Connect::QuickConnect QuickConnectConfig

Configuration settings for the quick connect.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#quickconnecttype" title="QuickConnectType">QuickConnectType</a>" : <i>String</i>,
    "<a href="#phoneconfig" title="PhoneConfig">PhoneConfig</a>" : <i><a href="phonenumberquickconnectconfig.md">PhoneNumberQuickConnectConfig</a></i>,
    "<a href="#queueconfig" title="QueueConfig">QueueConfig</a>" : <i><a href="queuequickconnectconfig.md">QueueQuickConnectConfig</a></i>,
    "<a href="#userconfig" title="UserConfig">UserConfig</a>" : <i><a href="userquickconnectconfig.md">UserQuickConnectConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#quickconnecttype" title="QuickConnectType">QuickConnectType</a>: <i>String</i>
<a href="#phoneconfig" title="PhoneConfig">PhoneConfig</a>: <i><a href="phonenumberquickconnectconfig.md">PhoneNumberQuickConnectConfig</a></i>
<a href="#queueconfig" title="QueueConfig">QueueConfig</a>: <i><a href="queuequickconnectconfig.md">QueueQuickConnectConfig</a></i>
<a href="#userconfig" title="UserConfig">UserConfig</a>: <i><a href="userquickconnectconfig.md">UserQuickConnectConfig</a></i>
</pre>

## Properties

#### QuickConnectType

The type of quick connect. In the Amazon Connect console, when you create a quick connect, you are prompted to assign one of the following types: Agent (USER), External (PHONE_NUMBER), or Queue (QUEUE).

_Required_: Yes

_Type_: String

_Allowed Values_: <code>PHONE_NUMBER</code> | <code>QUEUE</code> | <code>USER</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PhoneConfig

The phone configuration. This is required only if QuickConnectType is PHONE_NUMBER.

_Required_: No

_Type_: <a href="phonenumberquickconnectconfig.md">PhoneNumberQuickConnectConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### QueueConfig

The queue configuration. This is required only if QuickConnectType is QUEUE.

_Required_: No

_Type_: <a href="queuequickconnectconfig.md">QueueQuickConnectConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### UserConfig

The user configuration. This is required only if QuickConnectType is USER.

_Required_: No

_Type_: <a href="userquickconnectconfig.md">UserQuickConnectConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

