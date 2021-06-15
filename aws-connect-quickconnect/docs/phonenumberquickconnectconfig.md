# AWS::Connect::QuickConnect PhoneNumberQuickConnectConfig

The phone configuration. This is required only if QuickConnectType is PHONE_NUMBER.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#phonenumber" title="PhoneNumber">PhoneNumber</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#phonenumber" title="PhoneNumber">PhoneNumber</a>: <i>String</i>
</pre>

## Properties

#### PhoneNumber

The phone number in E.164 format.

_Required_: Yes

_Type_: String

_Pattern_: <code>^\+[1-9]\d{1,14}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
