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
      operation: null,
      topLevelSubOpType: null
    },

    // Define the methods for the model/view
    methods:
    {
        // This method is invoked when the user wants to add another target deck entry
        addSubOperationTopLevel: function()
        {
            if (this.operation.subOperations == null)
                Vue.set(app.operation, "subOperations",  {});

            var newSubOp = {
                key: this.topLevelSubOpType,
                subOperations: {}
            };

            Vue.set(app.operation.subOperations, this.topLevelSubOpType,  newSubOp);
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
            return allSubOperations
        },

        getAllChildrenRecursive: function(subOperation, level, list)
        {
            var newLevel = level + 1;
            for (var key in subOperation.subOperations)
            {
                var subOp = subOperation.subOperations[key];
                subOp.level = newLevel;
                list.push(subOp);

                for (var childSubop in subOp.subOperations)
                {
                    getAllChildrenRecursive(childSubop, level, list);
                }
            }
        },

        addSubOperation: function(subOperation)
        {
            if (subOperation.subOperations == null)
                Vue.set(subOperation, "subOperations",  {});

            var newSubOp = {
                key: subOperation.subOpLevelType,
                subOperations: {}
            };

            Vue.set(subOperation.subOperations,subOperation.subOpLevelType,  newSubOp);
        },

        operationTypeChanged: function(e)
        {
            this.operation = {}
            this.operation.index = e.target.value;
        }
    },

    computed: {
    },

    // Run this when Vue is ready
    mounted() {
        initBootstrapFilePickers();
    },
});

/**

getChildrenStart: function(subOperation)
        {
            return getChildren(subOperation, 0, []);
        },

        getChildren: function(subOperation, count, allChildren)
        {
            allChildren.append(subOperation);
            if (subOperation.subOperations == null || subOperation.subOperations.length == 0)
                return allChildren;
            else
            {
                for (var i = 0; i < subOperation.subOperations.length; i++)
                {
                    getChildren(subOperation.subOperations[i], (count + 1), allChildren);
                }
            }
        },

        */