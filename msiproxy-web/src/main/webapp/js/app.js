/**
 * The main MSI-proxy app module
 *
 * @type {angular.Module}
 */


var app = angular.module('msiproxy.app', [ 'ngRoute', 'ngSanitize', 'pascalprecht.translate' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/:provider/:lang/details', {
            templateUrl: 'partials/message-details.html'
        }).when('/:provider/:lang/grid', {
            templateUrl: 'partials/message-grid.html'
        }).when('/:provider/:lang/map', {
            templateUrl: 'partials/message-map.html'
        }).otherwise({
            redirectTo: '/dkmsi/da/details'
        });
    }]);
