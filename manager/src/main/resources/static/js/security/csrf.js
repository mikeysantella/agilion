/**
 * This file automatically adds the CSRF token as an input for every form submission, and as a header for every ajax
 * request
 **/

$(document).ready(function()
{
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");
    var parameter = $("meta[name='_csrf_parameter']").attr("content");

    $(document).ajaxSend(function(e, xhr, options) {
        xhr.setRequestHeader(header, token);
    });
})