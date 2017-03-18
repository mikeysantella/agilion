function toggleShowHideDisplay(element)
{
    var e = $(element);
    var icon = e.find(".showhide-icon");
    var text = e.find(".showhide-text");

    if (icon.hasClass("show-icon"))
    {
        icon.removeClass("show-icon");
        icon.addClass("hide-icon")
    }
    else
    {
        icon.removeClass("hide-icon");
        icon.addClass("show-icon")
    }

    if (text.hasClass("show-text"))
    {
        text.removeClass("show-text");
        text.addClass("hide-text")
    }
    else
    {
        text.removeClass("hide-text");
        text.addClass("show-text")
    }
}