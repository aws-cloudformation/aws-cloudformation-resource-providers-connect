# AWS::Connect::User

Resource Type definition for AWS::Connect::User

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Connect::User",
    "Properties" : {
        "<a href="#instancearn" title="InstanceArn">InstanceArn</a>" : <i>String</i>,
        "<a href="#directoryuserid" title="DirectoryUserId">DirectoryUserId</a>" : <i>String</i>,
        "<a href="#hierarchygrouparn" title="HierarchyGroupArn">HierarchyGroupArn</a>" : <i>String</i>,
        "<a href="#username" title="Username">Username</a>" : <i>String</i>,
        "<a href="#password" title="Password">Password</a>" : <i>String</i>,
        "<a href="#routingprofilearn" title="RoutingProfileArn">RoutingProfileArn</a>" : <i>String</i>,
        "<a href="#identityinfo" title="IdentityInfo">IdentityInfo</a>" : <i><a href="useridentityinfo.md">UserIdentityInfo</a></i>,
        "<a href="#phoneconfig" title="PhoneConfig">PhoneConfig</a>" : <i><a href="userphoneconfig.md">UserPhoneConfig</a></i>,
        "<a href="#securityprofilearns" title="SecurityProfileArns">SecurityProfileArns</a>" : <i>[ String, ... ]</i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Connect::User
Properties:
    <a href="#instancearn" title="InstanceArn">InstanceArn</a>: <i>String</i>
    <a href="#directoryuserid" title="DirectoryUserId">DirectoryUserId</a>: <i>String</i>
    <a href="#hierarchygrouparn" title="HierarchyGroupArn">HierarchyGroupArn</a>: <i>String</i>
    <a href="#username" title="Username">Username</a>: <i>String</i>
    <a href="#password" title="Password">Password</a>: <i>String</i>
    <a href="#routingprofilearn" title="RoutingProfileArn">RoutingProfileArn</a>: <i>String</i>
    <a href="#identityinfo" title="IdentityInfo">IdentityInfo</a>: <i><a href="useridentityinfo.md">UserIdentityInfo</a></i>
    <a href="#phoneconfig" title="PhoneConfig">PhoneConfig</a>: <i><a href="userphoneconfig.md">UserPhoneConfig</a></i>
    <a href="#securityprofilearns" title="SecurityProfileArns">SecurityProfileArns</a>: <i>
      - String</i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### InstanceArn

The identifier of the Amazon Connect instance.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### DirectoryUserId

The identifier of the user account in the directory used for identity management.

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### HierarchyGroupArn

The identifier of the hierarchy group for the user.

_Required_: No

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/agent-group/[-a-zA-Z0-9]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Username

The user name for the account.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>64</code>

_Pattern_: <code>[a-zA-Z0-9\_\-\.\@]+</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Password

The password for the user account. A password is required if you are using Amazon Connect for identity management. Otherwise, it is an error to include a password.

_Required_: No

_Type_: String

_Pattern_: <code>^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d\S]{8,64}$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### RoutingProfileArn

The identifier of the routing profile for the user.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/routing-profile/[-a-zA-Z0-9]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### IdentityInfo

Contains information about the identity of a user.

_Required_: No

_Type_: <a href="useridentityinfo.md">UserIdentityInfo</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### PhoneConfig

Contains information about the phone configuration settings for a user.

_Required_: Yes

_Type_: <a href="userphoneconfig.md">UserPhoneConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### SecurityProfileArns

One or more security profile arns for the user

_Required_: Yes

_Type_: List of String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

One or more tags.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the UserArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### UserArn

The Amazon Resource Name (ARN) for the user.

