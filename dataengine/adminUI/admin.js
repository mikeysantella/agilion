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
        nga.field('progress', 'json'),
        nga.field('inputDatasetIds', 'json'),
        nga.field('outputDatasetIds', 'json'),
    ]);
    admin.addEntity(job);

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
        //     .targetEntity(job)
        //     .targetField(nga.field('id')),

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
        nga.field('requests', 'json'),
    ]);
    admin.addEntity(session);

    var sessions = nga.entity('sessions');
    sessions.listView().fields([
        nga.field('createdTime'),
        nga.field('label').label('Label (Id)')
            .template('{{entry.values.label}} (<a href=\'#session/show/{{ entry.values.id }}\')>{{entry.values.id}}</a>)'),
        nga.field('username'),
        nga.field('requests', 'embedded_list')
            .targetFields([
                nga.field('createdTime'),
                nga.field('id').label('Request Label (Id)')
                    .template('{{entry.values.label}} (<a href=\'#request/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                nga.field('operation.id'),
                nga.field('jobs', 'embedded_list')
                    .targetFields([
                        nga.field('createdTime'),
                        nga.field('id').label('Job Label (Id)')
                            .template('{{entry.values.label}} (<a href=\'#job/show/{{ entry.values.id }}\')>{{ entry.values.id }}</a>)'),
                    ]).sortField('createdTime')
            ]).sortField('createdTime')
    ]).sortField('createdTime');
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