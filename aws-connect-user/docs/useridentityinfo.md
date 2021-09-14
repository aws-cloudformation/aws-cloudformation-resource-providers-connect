# AWS::Connect::User UserIdentityInfo

Contains information about the identity of a user.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#email" title="Email">Email</a>" : <i>String</i>,
    "<a href="#firstname" title="FirstName">FirstName</a>" : <i>String</i>,
    "<a href="#lastname" title="LastName">LastName</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#email" title="Email">Email</a>: <i>String</i>
<a href="#firstname" title="FirstName">FirstName</a>: <i>String</i>
<a href="#lastname" title="LastName">LastName</a>: <i>String</i>
</pre>

## Properties

#### Email

The email address. If you are using SAML for identity management and include this parameter, an error is returned.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### FirstName

The first name. This is required if you are using Amazon Connect or SAML for identity management.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### LastName

The last name. This is required if you are using Amazon Connect or SAML for identity management.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

