/**
 * String manipulation functions
 **/
var StringUtils = {}
StringUtils.isBlank = function(str)
{
    return (str == null || str.length === 0 || (str instanceof String && !str.trim()));
};

StringUtils.isNotBlank = function(str)
{
    return !StringUtils.isBlank(str);
};

StringUtils.toTitleCase = function(s)
{
    return s.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
};

StringUtils.capsUnderscoreToTitleCase = function(s)
{
    var text =  s.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
    text = text.replace(/_/g, " ");
    return text;
};