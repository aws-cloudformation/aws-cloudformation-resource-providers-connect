# AWS::Connect::InstanceStorageConfig EncryptionConfig

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#encryptiontype" title="EncryptionType">EncryptionType</a>" : <i>String</i>,
    "<a href="#keyid" title="KeyId">KeyId</a>" : <i>String</i>
}
</pre>

### YAML

<pre>
<a href="#encryptiontype" title="EncryptionType">EncryptionType</a>: <i>String</i>
<a href="#keyid" title="KeyId">KeyId</a>: <i>String</i>
</pre>

## Properties

#### EncryptionType

Specifies default encryption using AWS KMS-Managed Keys

_Required_: Yes

_Type_: String

_Allowed Values_: <code>KMS</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### KeyId

Specifies the encryption key id

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

