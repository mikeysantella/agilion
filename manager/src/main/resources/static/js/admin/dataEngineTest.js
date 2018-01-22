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
      selected: {
        operation: -1,
        params: {},
        params: {},
        subOperations: []
      }
    },

    // Define the methods for the model/view
    methods:
    {
        // This method is invoked when the user wants to add another target deck entry
        addSubOperation: function()
        {
            var newSubOp = {
                key: null,
            };
            this.selected.subOperations.push(newSubOp);
        },

        getChildren: function(subOperation)
        {
            return subOperations
        }
    },

    computed: {
    },

    // Run this when Vue is ready
    mounted() {
        initBootstrapFilePickers();
    },
});