// declare a new module called 'myApp', and make it require the `ng-admin` module as a dependency
var myApp = angular.module('myApp', ['ng-admin']);
// declare a function to run when the module bootstraps (during the 'config' phase)
myApp.config(['NgAdminConfigurationProvider', function (nga) {
    // create an admin application
    var admin = nga.application('DataEngine Admin UI')
      .baseApiUrl('http://localhost:9090/main/DataEngine/0.0.3/'); // main API endpoint

    var dataset = nga.entity('dataset');
    dataset.showView().fields([
        nga.field('id'),
        nga.field('label'),
        nga.field('state'),
        nga.field('createdTime'),
        nga.field('deletedTime'),
        nga.field('dataFormat'),
        nga.field('dataSchema'),
        nga.field('uri'),
        nga.field('stats'),
    ]);
    admin.addEntity(dataset);

    var job = nga.entity('job');
    job.showView().fields([
        nga.field('requestId').label('parent request')
            .template('<a href=\'#request/show/{{ entry.values.requestId }}\')>{{ entry.values.requestId }}</a>'),
            nga.field('id'),
            nga.field('label'),
            nga.field('type'),
            nga.field('createdTime'),
            nga.field('state'),
            nga.field('params', 'json'),
            nga.field('progress', 'json'),
            nga.field('inputDatasetIds', 'json')
                .template('<li ng-repeat="(dsLabel, dsId) in entry.values.inputDatasetIds">'+
                    ' {{dsLabel}} (<a href=\'#dataset/show/{{ dsId }}\')>{{ dsId }}</a>)</li>'),
            nga.field('outputDatasetIds', 'json')
                .template('<li ng-repeat="(dsLabel, dsId) in entry.values.outputDatasetIds">'+
                    '{{dsLabel}} (<a href=\'#dataset/show/{{ dsId }}\')>{{ dsId }}</a>)</li>'),
        ]);
    admin.addEntity(job);

    // Very useful:
    // nga.field('outputDatasetIds', 'json')
    //   .template('<table><tr ng-repeat="(key, value) in entry.values">'+
    //   '<td> {{key}} </td> <td> {{ value }} </td></tr></table>')

    var request = nga.entity('request');
    // file:///datadrive/dlam/dev/ng-admin/index.html#/request/show/88a11bf3-44c4-49b6-834d-fcbe6a3d72da
    request.showView().fields([
        nga.field('sessionId').label('parent session')
            .template('<a href=\'#session/show/{{ entry.values.sessionId }}\')>{{entry.values.sessionId}}</a>'),
        nga.field('id'),
        nga.field('label'),
        nga.field('createdTime'),
        nga.field('state'),
        nga.field('priorRequestIds', 'reference_many')
            .targetEntity(request)
            .targetField(nga.field('label')),
        //.singleApiCall(ids => ({'id': ids })),
        nga.field('operation', 'json'),
        //     .targetEntity(operationSel)
        //     .targetField(nga.field('id')),
        nga.field('jobs', 'json')
            .template('<li ng-repeat="job in entry.values.jobs">'+
                '<i>{{job.state}}</i>: "{{job.label}}" (<a href=\'#job/show/{{ job.id }}\')>{{ job.id }}</a>)'+
                '<ul><li ng-repeat="(dsLabel, dsId) in job.inputDatasetIds">'+
                '<i>input</i>: {{dsLabel}} (<a href=\'#dataset/show/{{ dsId }}\')>{{ dsId }}</a>)</li>'+
                '<li ng-repeat="(dsLabel, dsId) in job.outputDatasetIds">'+
                '<i>output</i>: {{dsLabel}} (<a href=\'#dataset/show/{{ dsId }}\')>{{ dsId }}</a>)</li>'+
                '</ul></li>'),
        //DEBUG: nga.field('jobs', 'json')
    ]);
    admin.addEntity(request);

    var session = nga.entity('session');
    // file:///datadrive/dlam/dev/ng-admin/index.html#/session/show/newSess
    session.showView().fields([
        nga.field('id'),
        nga.field('label'),
        nga.field('username'),
        nga.field('createdTime'),
        nga.field('defaults', 'json'),
        nga.field('requests', 'json')
            .template('<li ng-repeat="req in entry.values.requests">'+
                '<i>{{req.state}}</i>: "{{req.label}}" (<a href=\'#req/show/{{ req.id }}\')>{{ req.id }}</a>)'+
                '<ul><li ng-repeat="job in req.jobs">'+
                '<i>job {{job.state}}</i>: "{{job.label}}" (<a href=\'#job/show/{{ job.id }}\')>{{ job.id }}</a>)'+
                '</ul></li>'),
        nga.field('requests', 'embedded_list')
            .targetFields([
                nga.field('createdTime'),
                nga.field('id').label('Request Label (Operation, Id)')
                    .template('{{entry.values.label}} <br/>'+
                    '({{entry.values["operation.id"]}}, <br/>'+
                    '<a href=\'#request/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                nga.field('jobs', 'embedded_list')
                    .targetFields([
                        nga.field('createdTime'),
                        nga.field('id').label('Job Label (Id)')
                            .template('{{entry.values.label}} <br/>'+
                            '(<a href=\'#job/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                    ]).sortField('createdTime')
            ]).sortField('createdTime'),
        nga.field('requests', 'json'),
    ]);
    admin.addEntity(session);

    var sessions = nga.entity('sessions');
    //sessions.readOnly();
    sessions.listView().fields([
        nga.field('createdTime'),
        nga.field('label').label('Label (Id)')
            .template('{{entry.values.label}} <br/>'+
            '(<a href=\'#session/show/{{ entry.values.id }}\')>{{entry.values.id}}</a>)'),
        nga.field('username'),
        nga.field('requests', 'embedded_list')
            .targetFields([
                nga.field('createdTime'),
                nga.field('id').label('Request Label (Operation, Id)')
                    .template('{{entry.values.label}} <br/>'+
                    '({{entry.values["operation.id"]}}, <br/>'+
                    '<a href=\'#request/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                nga.field('jobs', 'embedded_list')
                    .targetFields([
                        nga.field('createdTime'),
                        nga.field('id').label('Job Label (Id)')
                            .template('{{entry.values.label}} <br/>'+
                            '(<a href=\'#job/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                    ]).sortField('createdTime')
            ]).sortField('createdTime')
    ]).sortField('createdTime')
    .filters([
        nga.field('username')
            .pinned(true)
    ]);
    admin.addEntity(sessions);

    var operations = nga.entity('operations');
    // set the fields of the user entity list view
    operations.listView().fields([
        nga.field('id'),
        nga.field('description'),
        nga.field('level'),
        nga.field('params', 'json'),
        nga.field('subOperations', 'json'),
    ]);
    admin.addEntity(operations);


    // attach the admin application to the DOM and execute it
    nga.configure(admin);
}]);