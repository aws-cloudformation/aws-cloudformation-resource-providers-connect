# AWS::Connect::Instance

Resource Type definition for AWS::Connect::Instance

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Connect::Instance",
    "Properties" : {
        "<a href="#identitymanagementtype" title="IdentityManagementType">IdentityManagementType</a>" : <i>String</i>,
        "<a href="#instancealias" title="InstanceAlias">InstanceAlias</a>" : <i>String</i>,
        "<a href="#directoryid" title="DirectoryId">DirectoryId</a>" : <i>String</i>,
        "<a href="#clienttoken" title="ClientToken">ClientToken</a>" : <i>String</i>,
        "<a href="#attributes" title="Attributes">Attributes</a>" : <i><a href="attributes.md">Attributes</a></i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Connect::Instance
Properties:
    <a href="#identitymanagementtype" title="IdentityManagementType">IdentityManagementType</a>: <i>String</i>
    <a href="#instancealias" title="InstanceAlias">InstanceAlias</a>: <i>String</i>
    <a href="#directoryid" title="DirectoryId">DirectoryId</a>: <i>String</i>
    <a href="#clienttoken" title="ClientToken">ClientToken</a>: <i>String</i>
    <a href="#attributes" title="Attributes">Attributes</a>: <i><a href="attributes.md">Attributes</a></i>
</pre>

## Properties

#### IdentityManagementType

Specifies the type of directory integration for new instance.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>SAML</code> | <code>CONNECT_MANAGED</code> | <code>EXISTING_DIRECTORY</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### InstanceAlias

Alias of the new directory created as part of new instance creation.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>62</code>

_Pattern_: <code>^(?!d-)([\da-zA-Z]+)([-]*[\da-zA-Z])*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### DirectoryId

Existing directoryId user wants to map to the new Connect instance.

_Required_: No

_Type_: String

_Minimum_: <code>12</code>

_Maximum_: <code>12</code>

_Pattern_: <code>^d-[0-9a-f]{10}$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### ClientToken

Client Token

_Required_: No

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Attributes

_Required_: Yes

_Type_: <a href="attributes.md">Attributes</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the Arn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### Id

An instanceId is automatically generated on creation and assigned as the unique identifier.

#### Arn

An instanceArn is automatically generated on creation based on instanceId.

#### ServiceRole

Service linked role created as part of instance creation.

#### CreatedTime

Timestamp of instance creation logged as part of instance creation.

#### InstanceStatus

Specifies the creation status of new instance.

#### StatusReason

Specifies the reason if the instance creation status is failed.

