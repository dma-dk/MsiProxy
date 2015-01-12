/**
 * The main MSI-proxy app module
 *
 * @type {angular.Module}
 */


var app = angular.module('msiproxy.app', [ 'ngRoute', 'ngSanitize', 'pascalprecht.translate' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/:provider/:lang', {
            templateUrl: 'partials/app.html'
        }).otherwise({
            redirectTo: '/dkmsi/da'
        });
    }]);
