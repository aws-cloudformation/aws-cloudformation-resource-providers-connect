# AWS::Connect::User UserPhoneConfig

Contains information about the phone configuration settings for a user.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#aftercontactworktimelimit" title="AfterContactWorkTimeLimit">AfterContactWorkTimeLimit</a>" : <i>Integer</i>,
    "<a href="#autoaccept" title="AutoAccept">AutoAccept</a>" : <i>Boolean</i>,
    "<a href="#deskphonenumber" title="DeskPhoneNumber">DeskPhoneNumber</a>" : <i>String</i>,
    "<a href="#phonetype" title="PhoneType">PhoneType</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#aftercontactworktimelimit" title="AfterContactWorkTimeLimit">AfterContactWorkTimeLimit</a>: <i>Integer</i>
<a href="#autoaccept" title="AutoAccept">AutoAccept</a>: <i>Boolean</i>
<a href="#deskphonenumber" title="DeskPhoneNumber">DeskPhoneNumber</a>: <i>String</i>
<a href="#phonetype" title="PhoneType">PhoneType</a>: <i>String</i>
</pre>

## Properties

#### AfterContactWorkTimeLimit

The After Call Work (ACW) timeout setting, in seconds.

_Required_: No

_Type_: Integer

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### AutoAccept

The Auto accept setting.

_Required_: No

_Type_: Boolean

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DeskPhoneNumber

The phone number for the user's desk phone.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PhoneType

The phone type.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>SOFT_PHONE</code> | <code>DESK_PHONE</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
