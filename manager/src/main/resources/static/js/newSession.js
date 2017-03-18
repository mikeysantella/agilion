function toggleDataFileUpload(element)
{
    var type = $(element).val();
    if (type == "FILE_UPLOAD")
        $("#dataFileUpload").removeClass("hidden");
    else
        $("#dataFileUpload").addClass("hidden");
}

function openAdditionalJobForm()
{
    var steps = $("#steps").val();
    $.ajax(
    {
        url: contextRoot+"session/initAdditionalJobForm",
        success: function(result){
            $("#additionalDataJobForm").replaceWith(result);
            $("#additionalJobModal").modal();
        }
    });
}

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

function addNewDataJob()
{
    var dataJobForm = $("#dataJobForm");
    var dataSourcename = $("#dataSourceSelect").val();
    var jobType = $("input[name='ingestJobType']:checked").val();
    var jobTypeDisplay = $("input[name='ingestJobType']:checked").siblings("[name='jobTypeText']").text();
    var fileInput = $('#uploadedFile');
    var fileInputClone = fileInput.clone();
    $("#dataFileUpload").append(fileInputClone);

    var steplist = []
    $("#stepContainer").find('input:checked').each(function(){
        steplist.push($(this).val());
    });

    var params = {};
    $("#paramContainer").find("label").each(function(){
        var pKey = $(this).attr("name");
        var value = $(this).siblings("input").val();
        params[pKey] = value;
    });

    // Add new row for display purposes
    var tr = $("<tr>");
    tr.append("<td>"+dataSourcename+"</td>");
    tr.append("<td>"+mapToString(params)+"</td>");
    tr.append("<td>"+jobTypeDisplay+"</td>");
    tr.append("<td>"+steplist+"</td>");

    // Add new hidden row for form submission
    var hidden = $("<td>");
    hidden.append(createFormInput("dataIngestJobs[0]", "dataSourceName", dataSourcename));
    hidden.append(createFormInput("dataIngestJobs[0]", "executeForSteps", steplist));
    hidden.append(createFormInput("dataIngestJobs[0]", "jobType", steplist));
    fileInput.attr("name", "dataIngestJobs[0].uploadedFile");
    hidden.append(fileInput);
    for(var k in params)
    {
        hidden.append(createFormInput("dataIngestJobs[0]", "params["+k+"]", params[k]));
    }
    hidden.addClass("hidden");
    tr.append(hidden);
    $("#dataJobTable").find("tbody").append(tr);

    reorderIndices("dataJobTable");
}

function addNewAdditionalDataJob()
{
    var dataJobForm = $("#additionalDataJobForm");
    var jobName = $("#jobName").val();
    var jobType = $("#jobType").val();
    var executionOrder = $("#executionOrder").val();
    var executionStep = $("#executionStep").val();

    var params = {};
    $("#jobParamContainer").find("label").each(function(){
        var pKey = $(this).attr("name");
        var value = $(this).siblings("input").val();
        params[pKey] = value;
    });

    // Add new row for display purposes
    var tr = $("<tr>");
    tr.append("<td>"+jobName+"</td>");
    tr.append("<td>"+jobType+"</td>");
    tr.append("<td>"+mapToString(params)+"</td>");
    tr.append("<td>"+executionOrder+" Step "+executionStep+"</td>");

    // Add new hidden row for form submission
    var hidden = $("<td>");
    hidden.append(createFormInput("dataEngineJobs[0]", "jobName", jobName));
    hidden.append(createFormInput("dataEngineJobs[0]", "jobType", jobType));
    hidden.append(createFormInput("dataEngineJobs[0]", "executionConfig.order", executionOrder));
    hidden.append(createFormInput("dataEngineJobs[0]", "executionConfig.step", executionStep));
    for(var k in params)
    {
        hidden.append(createFormInput("dataIngestJobs[0]", "params["+k+"]", params[k]));
    }
    hidden.addClass("hidden");
    tr.append(hidden);
    $("#additionalJobTable").find("tbody").append(tr);

    reorderIndices("additionalJobTable");
}

function createFormInput(objName, fieldPath, fieldValue)
{
    var e = $("<input>");
    e.attr("id", objName+"__"+fieldPath);
    e.attr("name", objName+"."+fieldPath);
    e.attr("value", fieldValue);

    return e;
}

function reorderIndices(tableID)
{
    var table = $("#"+tableID);
    var count = 0;
    table.find("td.hidden").each(function(){
        $(this).find("input").each(function(){
            var input = $(this);
            var oldname = input.attr("name");
            var newName = oldname.replace(/\[\d+\]/, "["+count+"]");
            input.attr("name", newName);
        });
        count++;
    });
}

function mapToString(map)
{
    var str = "";
    var keys = [];
    for(var k in map) keys.push(k);
    for (var i = 0; i < keys.length; i++)
    {
        var key = keys[i];
        var val = map[key];
        str += key+": "+val+"<br/>";
    }
    return str;

}
