
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
                    messages: "="
                },
                link: function(scope, element, attrs) {

                    element.bind('click', function() {
                        $rootScope.$broadcast('messageDetails', {
                            message: scope.message,
                            messages: scope.messages
                        });
                    });
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
                format: "@?"
            },
            link: function(scope, element, attrs) {
                scope.language = $rootScope.language;
                scope.format = scope.format || 'list';
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
                var msg = scope.msiMessageId;
                if (msg.seriesIdentifier.number) {
                    var id = msg.seriesIdentifier.fullId;
                    if (msg.type == 'TEMPORARY_NOTICE') {
                        id += '(T)';
                    } else if (msg.type == 'PRELIMINARY_NOTICE') {
                        id += '(P)';
                    }
                    id += '.';
                    element.html(id);
                }
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
                msiMessageTitle: "="
            },
            link: function(scope, element, attrs) {
                element.html(LangService.messageTitleLine(scope.msiMessageTitle));
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
                msiMessageArea: "="
            },
            link: function(scope, element, attrs) {
                element.html(LangService.messageAreaLineage(scope.msiMessageArea));
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
                areaDivider: "@"
            },
            link: function(scope, element, attrs) {
                element.html(LangService.areaLineage(scope.msiArea, undefined));
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
                element.html(LangService.messageTime(scope.msg));
            }
        };
    }]);


