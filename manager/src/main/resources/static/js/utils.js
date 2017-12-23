var StringUtils = {}

StringUtils.isBlank = function(str)
{
    return (str == null || str.length === 0 || !str.trim());
}

StringUtils.isNotBlank = function(str)
{
    return !StringUtils.isBlank(str);
}