$(document).ready(function(){
    $(".jobState").each(function(){
        var text = $(this).text();
        $(this).text(StringUtils.capsUnderscoreToTitleCase(text));
    });
});