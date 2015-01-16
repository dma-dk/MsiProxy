
/**
 * Directives used by the MSI-proxy app
 */
angular.module('msiproxy.app')

    /****************************************************************
     * Binds a click event that will open the message details dialog
     ****************************************************************/
    .directive('msiMessageDetails', ['$rootScope',
        function ($rootScope) {
            'use strict';

            return {
                restrict: 'A',
                scope: {
                    message: "=",
                    messages: "=",
                    disabled: "=?"
                },
                link: function(scope, element, attrs) {

                    if (!scope.disabled) {
                        element.css('cursor', 'pointer');
                        element.bind('click', function() {
                            $rootScope.$broadcast('messageDetails', {
                                message: scope.message,
                                messages: scope.messages
                            });
                        });
                    }
                }
            };
        }])


    /********************************
     * Renders the message details
     ********************************/
    .directive('msiRenderMessageDetails', [ '$rootScope', function ($rootScope) {
        'use strict';

        return {
            restrict: 'A',
            templateUrl: '/partials/render-message-details.html',
            replace: false,
            scope: {
                msg: "=",
                messages: "=",
                format: "@",
                excludeAreaHeading: "=?"
            },
            link: function(scope, element, attrs) {
                scope.language = $rootScope.language;
                scope.format = scope.format || 'list';
                scope.excludeAreaHeading = (scope.format == 'list');
            }
        };
    }])

    /********************************
     * Displays the composite identifier of the message
     ********************************/
    .directive('msiMessageId', [function () {
        return {
            restrict: 'A',
            scope: {
                msiMessageId: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch(
                    function() { return scope.msiMessageId; },
                    function (msg) { element.html(formatSeriesIdentifier(msg)); },
                    true);
            }
        };
    }])


    /********************************
     * Replaces the content of the element with the title line of the message
     ********************************/
    .directive('msiMessageTitle', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiMessageTitle: "=",
                excludeAreaHeading: "=?"
            },
            link: function(scope, element, attrs) {
                scope.$watch(
                    function() { return scope.msiMessageTitle; },
                    function (msg) { element.html(LangService.messageTitleLine(scope.msiMessageTitle, scope.excludeAreaHeading)); },
                    true);
            }
        };
    }])

    /********************************
     * Replaces the content of the element with the area description of the message
     ********************************/
    .directive('msiMessageArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiMessageArea: "=",
                excludeAreaHeading: "=?"
            },
            link: function(scope, element, attrs) {
                element.html(LangService.messageAreaLineage(scope.msiMessageArea, scope.excludeAreaHeading));
            }
        };
    }])

    /********************************
     * Replaces the content of the element with the area description
     ********************************/
    .directive('msiArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiArea: "=",
                areaDivider: "@",
                excludeAreaHeading: "=?"
            },
            link: function(scope, element, attrs) {
                element.html(LangService.areaLineage(scope.msiArea, undefined, scope.excludeAreaHeading));
            }
        };
    }])

    /********************************
     * Prints the message time interval
     ********************************/
    .directive('msiValidFromTo', ['LangService', function (LangService) {
        return {
            restrict: 'E',
            scope: {
                msg: "="
            },
            link: function(scope, element, attrs) {
                scope.$watch(
                    function() { return scope.msg; },
                    function (msg) { element.html(LangService.messageTime(scope.msg)); },
                    true);
            }
        };
    }]);


