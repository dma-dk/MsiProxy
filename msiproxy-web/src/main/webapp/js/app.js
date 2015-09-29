/**
 * The main MSI-proxy app module definition.
 *
 * Define the routes of the single page application.
 */

var app = angular.module('msiproxy.app', [ 'ngRoute', 'ngSanitize', 'ui.bootstrap', 'pascalprecht.translate' ])
    .config(['$routeProvider', function ($routeProvider) {
        'use strict';

        $routeProvider.when('/:provider/:lang/details', {
            templateUrl: 'partials/message-details.html'
        }).when('/:provider/:lang/details/:messageId', {
            templateUrl: 'partials/message-details.html'
        }).when('/:provider/:lang/map', {
            templateUrl: 'partials/message-map.html'
        }).when('/:provider/:lang/map/:messageId', {
            templateUrl: 'partials/message-map.html'
        }).otherwise({
            redirectTo: '/dkmsi/da/details'
        });
    }]);


/**
 * The view mode bar and filter bar are always visible, but the filter bar can
 * have varying height and may change height when the window is re-sized.
 * Compute the correct top position of the message lists
 */
$( window ).resize(function() {
    adjustMessageListTopPosition();
});

function adjustMessageListTopPosition() {
    var filterBar = $('.filter-bar');
    if (filterBar.length) {
        var offset = 40 + filterBar.height();
        var msiDetails = $(".msi-details");
        if (msiDetails.length) msiDetails.css("margin-top", offset + "px");
        var msiMap = $(".msi-map");
        if (msiMap.length) msiMap.css("top", offset + "px");
    }
}