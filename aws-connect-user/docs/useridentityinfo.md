# AWS::Connect::User UserIdentityInfo

Contains information about the identity of a user.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#firstname" title="FirstName">FirstName</a>" : <i>String</i>,
    "<a href="#lastname" title="LastName">LastName</a>" : <i>String</i>,
    "<a href="#email" title="Email">Email</a>" : <i>String</i>,
    "<a href="#secondaryemail" title="SecondaryEmail">SecondaryEmail</a>" : <i>String</i>,
    "<a href="#mobile" title="Mobile">Mobile</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#firstname" title="FirstName">FirstName</a>: <i>String</i>
<a href="#lastname" title="LastName">LastName</a>: <i>String</i>
<a href="#email" title="Email">Email</a>: <i>String</i>
<a href="#secondaryemail" title="SecondaryEmail">SecondaryEmail</a>: <i>String</i>
<a href="#mobile" title="Mobile">Mobile</a>: <i>String</i>
</pre>

## Properties

#### FirstName

The first name. This is required if you are using Amazon Connect or SAML for identity management.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastName

The last name. This is required if you are using Amazon Connect or SAML for identity management.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Email

The email address. If you are using SAML for identity management and include this parameter, an error is returned.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SecondaryEmail

The secondary email address. If you provide a secondary email, the user receives email notifications -- other than password reset notifications -- to this email address instead of to their primary email address.

_Required_: No

_Type_: String

_Pattern_: <code>(?=^.{0,265}$)[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Mobile

The mobile phone number.

_Required_: No

_Type_: String

_Pattern_: <code>^\+[1-9]\d{1,14}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)
