function openDataJobForm()
{
var steps = $("#steps").val();
    $.ajax(
    {

        url: contextRoot+"session/initDataJobForm",
        data: {steps: steps},
        success: function(result){
            $("#dataJobForm").replaceWith(result);
            $("#dataJobModal").modal();
        }
    });
}