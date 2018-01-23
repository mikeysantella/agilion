Vue.component('subop-template', {
  template: '#subop-template',
  props: {
    suboperationlist: {
        type: Object
    }
  }
});

var app = new Vue({
    el: '#app',

    // Define the data model. We're going to submit this to the server
    data: {
      datamodel: JSON.parse($("#listOperationsJson").text()),
      operation: {},
      topLevelSubOpType: null
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

            if (StringUtils.isNotBlank(this.operation.index))
            {
                // Build a stub of the operation using the datamodel
                for (var i = 0; i < this.datamodel[this.operation.index].params.length; i++)
                {
                    var param = this.datamodel[this.operation.index].params[i];

                    // Set the value to the default if one exists, null otherwise. We want this value to be "watched" for UI changes
                    var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                    Vue.set(this.operation, param.key, defaultValue);

                    // Set flags for the operation. These dont need to be "watched" for UI-initiated changes
                    this.operation[param.key]
                }
            }
        },

        // This method is invoked when the user adds a "top-level" sub operation (i.e. a sub-op that has no parents).
        addSubOperationTopLevel: function()
        {
            if (this.operation.subOperations == null)
                Vue.set(app.operation, "subOperations",  {});

            var newSubOp = {
                key: this.topLevelSubOpType,
                subOperations: {}
            };

            for (var i = 0; i < this.datamodel[this.operation.index].subOperations[this.topLevelSubOpType].params.length; i++)
            {
                var param = this.datamodel[this.operation.index].subOperations[this.topLevelSubOpType].params[i];

                // Set the value to the default if one exists, null otherwise
                var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                Vue.set(newSubOp, param.key, defaultValue);
            }


            Vue.set(app.operation.subOperations, this.topLevelSubOpType,  newSubOp);
        },


        addSubOperation: function(subOperation)
        {
            if (subOperation.subOperations == null)
                Vue.set(subOperation, "subOperations",  {});

            var newSubOp = {
                key: subOperation.subOpLevelType,
                subOperations: {}
            };

            // Build a stub of the operation using the datamodel
            for (var i = 0; i < this.datamodel[this.operation.index].subOperations[subOperation.subOpLevelType].params.length; i++)
            {
                var param = this.datamodel[this.operation.index].subOperations[subOperation.subOpLevelType].params[i];

                // Set the value to the default if one exists, null otherwise
                var defaultValue = StringUtils.isNotBlank(param.defaultValue) ? param.defaultValue : '';
                Vue.set(newSubOp, param.key, defaultValue);
            }

            Vue.set(subOperation.subOperations,subOperation.subOpLevelType,  newSubOp);
        },

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
    },

    computed: {
    },

    // Run this when Vue is ready
    mounted() {
        initBootstrapFilePickers();
    },
});