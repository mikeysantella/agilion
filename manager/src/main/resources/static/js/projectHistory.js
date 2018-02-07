$(document).ready(function(){
    $(".jobState").each(function(){
        var text = $(this).text();
        $(this).text(StringUtils.capsUnderscoreToTitleCase(text));
    });
});

function refreshProjectTable()
{
    var btn = $("#refreshButton");
    var buttonText = $("#buttonText");
    var spinIcon = $("#spinningButtonIcon");
    var icon = $("#buttonIcon");

    updateButton(true, btn, buttonText, icon, spinIcon);
    $.ajax({
        url: contextRoot+"project/reloadHistory",
        method: 'GET',
        success: function(data)
        {
            $("#tableContainer").html(data);
             updateButton(false, btn, buttonText, icon, spinIcon);
        },
        error: function()
        {
            alert("FUCK");
             updateButton(false, btn, buttonText, icon, spinIcon);
        }
    });
}

function updateButton(isLoading, btn, buttonText, icon, spinIcon)
{
    if (isLoading)
    {
        buttonText.text("Refreshing...");
        btn.addClass("disabled");
        icon.addClass("hidden");
        spinIcon.removeClass("hidden");
    }
    else
    {
        buttonText.text("Refresh");
        btn.removeClass("disabled");
        icon.removeClass("hidden");
        spinIcon.addClass("hidden");
    }
}