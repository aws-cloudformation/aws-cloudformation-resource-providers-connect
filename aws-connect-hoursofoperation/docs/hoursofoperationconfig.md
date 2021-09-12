# AWS::Connect::HoursOfOperation HoursOfOperationConfig

Contains information about the hours of operation.

## Syntax

To declare this entity in your AWS CloudFormation template, use the following syntax:

### JSON

<pre>
{
    "<a href="#day" title="Day">Day</a>" : <i>String</i>,
    "<a href="#starttime" title="StartTime">StartTime</a>" : <i><a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a></i>,
    "<a href="#endtime" title="EndTime">EndTime</a>" : <i><a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a></i>
}
</pre>

### YAML

<pre>
<a href="#day" title="Day">Day</a>: <i>String</i>
<a href="#starttime" title="StartTime">StartTime</a>: <i><a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a></i>
<a href="#endtime" title="EndTime">EndTime</a>: <i><a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a></i>
</pre>

## Properties

#### Day

The day that the hours of operation applies to.

_Required_: Yes

_Type_: String

_Allowed Values_: <code>SUNDAY</code> | <code>MONDAY</code> | <code>TUESDAY</code> | <code>WEDNESDAY</code> | <code>THURSDAY</code> | <code>FRIDAY</code> | <code>SATURDAY</code>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### StartTime

The start time or end time for an hours of operation.

_Required_: Yes

_Type_: <a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

#### EndTime

_Required_: Yes

_Type_: <a href="hoursofoperationtimeslice.md">HoursOfOperationTimeSlice</a>

_Update requires_: [No interruption](https://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/using-cfn-updating-stacks-update-behaviors.html#update-no-interrupt)

