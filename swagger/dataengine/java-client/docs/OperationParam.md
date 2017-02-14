
# OperationParam

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**key** | **String** |  | 
**description** | **String** |  | 
**valuetype** | [**ValuetypeEnum**](#ValuetypeEnum) | map of type for each parameter | 
**required** | **Boolean** | whether this parameter is required | 
**isMultivalued** | **Boolean** | if true, then a list of values is expected | 
**defaultValue** | **Object** |  | 
**possibleValues** | **List&lt;Object&gt;** | possible values from which to choose (if applicable) |  [optional]


<a name="ValuetypeEnum"></a>
## Enum: ValuetypeEnum
Name | Value
---- | -----
STRING | &quot;string&quot;
INT | &quot;int&quot;
LONG | &quot;long&quot;
FLOAT | &quot;float&quot;
DOUBLE | &quot;double&quot;
BOOLEAN | &quot;boolean&quot;
ENUM | &quot;enum&quot;



