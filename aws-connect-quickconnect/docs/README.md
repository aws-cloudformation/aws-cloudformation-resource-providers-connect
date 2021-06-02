# AWS::Connect::QuickConnect

Resource Type definition for AWS::Connect::QuickConnect

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Connect::QuickConnect",
    "Properties" : {
        "<a href="#instancearn" title="InstanceArn">InstanceArn</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>,
        "<a href="#description" title="Description">Description</a>" : <i>String</i>,
        "<a href="#quickconnectconfig" title="QuickConnectConfig">QuickConnectConfig</a>" : <i><a href="quickconnectconfig.md">QuickConnectConfig</a></i>,
        "<a href="#tags" title="Tags">Tags</a>" : <i>[ <a href="tag.md">Tag</a>, ... ]</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Connect::QuickConnect
Properties:
    <a href="#instancearn" title="InstanceArn">InstanceArn</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
    <a href="#description" title="Description">Description</a>: <i>String</i>
    <a href="#quickconnectconfig" title="QuickConnectConfig">QuickConnectConfig</a>: <i><a href="quickconnectconfig.md">QuickConnectConfig</a></i>
    <a href="#tags" title="Tags">Tags</a>: <i>
      - <a href="tag.md">Tag</a></i>
</pre>

## Properties

#### InstanceArn

The identifier of the Amazon Connect instance.

_Required_: Yes

_Type_: String

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Name

The name of the quick connect.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>127</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Description

The description of the quick connect.

_Required_: No

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>250</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### QuickConnectConfig

Configuration settings for the quick connect.

_Required_: Yes

_Type_: <a href="quickconnectconfig.md">QuickConnectConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### Tags

One or more tags.

_Required_: No

_Type_: List of <a href="tag.md">Tag</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the QuickConnectArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### QuickConnectArn

The Amazon Resource Name (ARN) for the quick connect.
