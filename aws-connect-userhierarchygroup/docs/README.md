# AWS::Connect::UserHierarchyGroup

Resource Type definition for AWS::Connect::UserHierarchyGroup

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "Type" : "AWS::Connect::UserHierarchyGroup",
    "Properties" : {
        "<a href="#instancearn" title="InstanceArn">InstanceArn</a>" : <i>String</i>,
        "<a href="#parentgrouparn" title="ParentGroupArn">ParentGroupArn</a>" : <i>String</i>,
        "<a href="#name" title="Name">Name</a>" : <i>String</i>
    }
}
</pre>

### YAML

<pre>
Type: AWS::Connect::UserHierarchyGroup
Properties:
    <a href="#instancearn" title="InstanceArn">InstanceArn</a>: <i>String</i>
    <a href="#parentgrouparn" title="ParentGroupArn">ParentGroupArn</a>: <i>String</i>
    <a href="#name" title="Name">Name</a>: <i>String</i>
</pre>

## Properties

#### InstanceArn

The identifier of the Amazon Connect instance.

_Required_: Yes

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*$</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### ParentGroupArn

The Amazon Resource Name (ARN) for the user hierarchy group.

_Required_: No

_Type_: String

_Pattern_: <code>^arn:aws[-a-z0-9]*:connect:[-a-z0-9]*:[0-9]{12}:instance/[-a-zA-Z0-9]*/agent-group/[-a-zA-Z0-9]*$</code>

_Update requires_: [Replacement](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-replacement)

#### Name

The name of the user hierarchy group.

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>100</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

## Return Values

### Ref

When you pass the logical ID of this resource to the intrinsic `Ref` function, Ref returns the UserHierarchyGroupArn.

### Fn::GetAtt

The `Fn::GetAtt` intrinsic function returns a value for a specified attribute of this type. The following are the available attributes and sample return values.

For more information about using the `Fn::GetAtt` intrinsic function, see [Fn::GetAtt](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/intrinsic-function-reference-getatt.html).

#### UserHierarchyGroupArn

The Amazon Resource Name (ARN) for the user hierarchy group.
