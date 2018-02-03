var statusTimer = null;

var app = new Vue({
    el: '#app',

    // Define the data model. We're going to submit this to the server
    data: {
      datamodel: JSON.parse($("#listOperationsJson").text()),
      operation: {},
      topLevelSubOpType: null,
      sessionID: null,
      operationStatus: null
    },

    // Define the methods for the model/view
    methods:
    {
        // This method is called when the Operation type is selected. It initializes the operation data with default values,
        // as well as important ui-related flags
        operationTypeChanged: function(e)
        {
            this.operation = {}
            this.operation.index = e.target.value;
            Vue.set(this.operation, "params", {});

            if (StringUtils.isNotBlank(this.operation.index))
            {
                this.operation.id = this.datamodel[this.operation.index].id

                if (StringUtils.isNotBlank(this.operation.index))
                {
                    // Build a stub of the operation using the datamodel
                    for (var i = 0; i < this.datamodel[this.operation.index].params.length; i++)
                    {
                        var param = this.datamodel[this.operation.index].params[i];

                        // Set the value to the default if one exists, null otherwise. We want this value to be "watched" for UI changes
                        var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                        Vue.set(this.operation.params, param.key, defaultValue);
                    }
                }
            }
        },

        // This method is called when a sub-operation is added to a parent operation.
        addSubOperation: function(parentOperation, opType)
        {
            // If this is true, then we are adding a "top-level" sub-operation. In other words, this sub-op has no parent.
            if (parentOperation == null)
            {
                if (app.operation.subOperations == null)
                    Vue.set(app.operation, "subOperations",  {});

                parentOperation = this.operation;
            }
            else // Otherwise, we are adding a sub-operation to an existing sub-operation. My head....ow...
            {
                if (parentOperation.subOperations == null)
                    Vue.set(subOperation, "subOperations",  {});
            }

            var path = (parentOperation.path != null) ? parentOperation.path + "."+opType : opType;

            // Initialize the object that represents the new sub-operation
            var newSubOp = {
                key: opType,
                path: path,
                params: {},
                subOperations: {}
            };

            // Build a stub of the operation using the datamodel, initializing it using any default values provided
            for (var i = 0; i < this.datamodel[this.operation.index].subOperations[opType].params.length; i++)
            {
                var param = this.datamodel[this.operation.index].subOperations[opType].params[i];

                // Set the value to the default if one exists, null otherwise
                var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                Vue.set(newSubOp.params, param.key, defaultValue);
            }

            // Explicitly tell Vue to watch for changes (without this, the model is not updated when changes occur
            Vue.set(parentOperation.subOperations,opType, newSubOp);
        },

        // This method returns all sub operations in the view. It returns a flattened list, but assigns each
        // sub-op a "level" attribute that indicates its depth so that the display can reflect parent-child relationships
        getAllSubOperations: function()
        {
            var allSubOperations = [];
            for (var key in this.operation.subOperations)
            {
                var subOp = this.operation.subOperations[key];
                subOp.level = 0;
                allSubOperations.push(subOp);

                this.getAllChildrenRecursive(subOp, 0, allSubOperations);

            }
            return allSubOperations;
        },

        // Internal method, do not call.
        getAllChildrenRecursive: function(subOperation, level, list)
        {
            var newLevel = level + 1;
            for (var key in subOperation.subOperations)
            {
                var subOp = subOperation.subOperations[key];
                subOp.level = newLevel;
                list.push(subOp);

                this.getAllChildrenRecursive(subOp, newLevel, list);
            }
        },

        // This method deletes a sub operation, given it's path.
        deleteOperation: function(parentOperation, pathForDeletion)
        {
            // If the parent operation is null, it means that we need to start our search at the top
            if (parentOperation == null)
                parentOperation = this.operation;

            for (var opKey in parentOperation.subOperations)
            {
                var subop = parentOperation.subOperations[opKey];
                var subopPath = subop.path;
                if (subopPath == pathForDeletion)
                {
                    Vue.delete(parentOperation.subOperations, opKey);
                    break;
                }
                else
                    this.deleteOperation(subop, pathForDeletion);
            }
        },

        // This method submits the data to the manager component, so that it can send it to the DataEngine.
        submit: function()
        {
            var vue = this;
            $.ajax({
                url: contextRoot+"admin/dataengine/submit",
                method: 'POST',
                data: JSON.stringify(this.operation),
                contentType: "application/json",
                success: function(data)
                {
                    vue.sessionID = data;

                },
                error: function()
                {
                    alert("damn something broke");
                }
            });
        },

        status: function()
        {
            if (this.sessionID != null)
            {
                vue = this;
                $.ajax({
                    url: contextRoot+"admin/dataengine/status",
                    method: 'POST',
                    data: this.sessionID,
                    contentType: "application/json",
                    success: function(data)
                    {
                        vue.operationStatus = data;
                    },
                    error: function()
                    {
                        alert("damn something broke");
                    }
                });
            }

        }
    },

    computed:
    {

    },

    // Run this when Vue is ready
    mounted() {
        initBootstrapFilePickers();
        var a = this;
        statusTimer = setInterval(function(){ a.status(); }, 1000);
    },
});