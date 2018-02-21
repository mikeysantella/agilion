var app = new Vue({
    el: '#app',

    // Define the data model. We're going to submit this to the server
    data: {
      form : {
          projectName: "",
          targetDeck: {
              targetDeckEntryList:[{
                  selectorList: "",
                  selectorType: "",
              }],
              selectorFile: {
                file: null,
                display: ""
              }
          },
          fromDate: "",
          toDate: "",
          dataSources: [],
          dataSets: [{
            nodelist: {
                file: null,
                display: ""
            },
            edgelist: {
                file: null,
                display: ""
            }
          }]
      },

      formErrors: [],

      submitted: false,

      // A little hacky, but it's very convenient to get server data like this by rendering this page as a Thymeleaf Template
      serverData: {
          selectorTypes: SERVERDATA.selectorTypes, // This is defined in newProject.html
          dataSources: SERVERDATA.dataSources // So is this.
      },
    },

    // Define the methods for the model/view
    methods:
    {
        // This method is invoked when the user wants to add another target deck entry
        addTargetDeck: function()
        {
            this.form.targetDeck.targetDeckEntryList.push({
                selectorList: null,
                selectorType: "",
            });
        },

        // This method is invoked when the user presses the "Remove" button
        removeTargetDeck: function(row)
        {
            var index = this.form.targetDeck.targetDeckEntryList.indexOf(row);
            this.form.targetDeck.targetDeckEntryList.splice(index, 1);
        },

        swapDates: function()
        {
            var temp = this.form.fromDate;
            this.form.fromDate = this.form.toDate;
            this.form.toDate = temp;
        },

        addDataset: function()
        {
            var dict = {
               nodelist: {
                   file: null,
                   display: ""
               },
               edgelist: {
                   file: null,
                   display: ""
               }
            }
            this.form.dataSets.push(dict);
        },

        removeDataset: function(index)
        {
            this.form.dataSets.splice(index, 1);
        },

        datasetFileChanged: function(set, event)
        {
            set.file = event.target.files[0];
            set.display = event.target.value.replace(/\\/g, '/').replace(/.*\//, '');
        },

        selectorFileChanged: function(event)
        {
            this.form.targetDeck.selectorFile.file = event.target.files[0];
            this.form.targetDeck.selectorFile.display = event.target.value.replace(/\\/g, '/').replace(/.*\//, '');
        },

        formIsClientSideValid: function()
        {
            return this.validProjectName && this.validTargetDeck && this.validDates;
        },

        // This method submits the model to the server if validation succeeds
        submitNetworkBuild: function(form)
        {
            var vue = this;
            var btn = $("#submitButton");
            this.submitted = true;
            if (this.formIsClientSideValid())
            {
                btn.button('Working...');
                $.ajax({
                    url: contextRoot+"project/submit",
                    method: 'POST',
                    data: buildFormSubmissionObject(this.form),
                    processData: false,
                    contentType: false,
                    success: function(data)
                    {
                        btn.button('reset')
                        window.location.href="/project/history?submittedNetwork=true";
                    },
                    error: function(data)
                    {
                        var validationResult = data.responseJSON;
                        btn.button('reset');
                        if (validationResult["OBJ_ID"] && validationResult["OBJ_ID"] == "VALIDATION_RESULT")
                        {
                            vue.formErrors = validationResult.errors;
                        }
                    }
                });
            }
        },

        selectAllDatasources: function(){ this.form.dataSources = this.serverData.dataSources;},
        deselectAllDatasources: function(){ this.form.dataSources = [];}
    },

    computed: {
        // Valid if the project name is not blank (or the client has not yet tried to submit the form
        validProjectName: function() { return !this.submitted || StringUtils.isNotBlank(this.form.projectName); },

        validDates: function() { return !this.submitted || (StringUtils.isNotBlank(this.form.fromDate) && StringUtils.isNotBlank(this.form.toDate)); },

        // Valid if each target deck entry has values for the list and the type, OR if there's only one BLANK target deck entry
        // but the user has at least one file.
        validTargetDeck: function()
        {
            var validTargetDeckEntries = true;
            for (var i = 0; i < this.form.targetDeck.targetDeckEntryList.length; i++)
            {
                var entry = this.form.targetDeck.targetDeckEntryList[i];
                if (!(StringUtils.isNotBlank(entry.selectorList) && StringUtils.isNotBlank(entry.selectorType)))
                {
                    validTargetDeckEntries = false;
                    break;
                }
            }

            var hasFile = this.form.targetDeck.targetDeckEntryList.length == 1 && StringUtils.isNotBlank(this.form.targetDeck.selectorFiles);
            return !this.submitted || (validTargetDeckEntries || hasFile);
        }
    },

    // Run this when Vue is ready
    mounted() {
        // Vue doesn't play nicely with bootstrap datepickers....
        $("#fromDate").datepicker().on(
            "changeDate", () => {this.form.fromDate = $('#fromDate').val()}
        );
        $("#toDate").datepicker().on(
            "changeDate", () => {this.form.toDate = $('#toDate').val()}
        );

    },
});

function buildFormSubmissionObject(model, selectorFile, dataFile)
{
    // Add all of the simple fields first
    var formData = new FormData();
    formData.append("projectName", model.projectName);
    formData.append("fromDate", model.fromDate);
    formData.append("toDate", model.toDate);
    formData.append("dataSources", model.dataSources);

    // Add the target deck object
    $.each(model.targetDeck.targetDeckEntryList, function(i, entry)
    {
        formData.append("targetDeck.targetDeckEntryList["+i+"].selectorList", entry.selectorList);
        formData.append("targetDeck.targetDeckEntryList["+i+"].selectorType", entry.selectorType);
    });

    // Vue doesn't do files in its model... leaky abstractions, yay!
    if (model.targetDeck.selectorFile.file != null)
        formData.append("targetDeck.selectorFile", model.targetDeck.selectorFile.file);

    $.each(model.dataSets,  function(i, dataset)
    {
        if (dataset.nodelist.file != null)
            formData.append("dataSets["+i+"].nodelist", dataset.nodelist.file);

        if (dataset.edgelist.file != null)
            formData.append("dataSets["+i+"].edgelist", dataset.edgelist.file);
    });
    return formData;
}
