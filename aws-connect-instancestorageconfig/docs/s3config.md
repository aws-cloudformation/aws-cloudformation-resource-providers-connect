# AWS::Connect::InstanceStorageConfig S3Config

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#bucketname" title="BucketName">BucketName</a>" : <i>String</i>,
    "<a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>" : <i>String</i>,
    "<a href="#encryptionconfig" title="EncryptionConfig">EncryptionConfig</a>" : <i><a href="encryptionconfig.md">EncryptionConfig</a></i>
}
</pre>

### YAML

<pre>
<a href="#bucketname" title="BucketName">BucketName</a>: <i>String</i>
<a href="#bucketprefix" title="BucketPrefix">BucketPrefix</a>: <i>String</i>
<a href="#encryptionconfig" title="EncryptionConfig">EncryptionConfig</a>: <i><a href="encryptionconfig.md">EncryptionConfig</a></i>
</pre>

## Properties

#### BucketName

A name for the S3 Bucket

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### BucketPrefix

Prefixes are used to infer logical hierarchy

_Required_: Yes

_Type_: String

_Minimum_: <code>1</code>

_Maximum_: <code>128</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EncryptionConfig

_Required_: No

_Type_: <a href="encryptionconfig.md">EncryptionConfig</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

