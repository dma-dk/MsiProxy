
/**
 * Directives used by the MSI-proxy app
 */
angular.module('msiproxy.app')

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
                messages: "="
            },
            link: function(scope, element, attrs) {
                scope.language = $rootScope.language;
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
                var id = msg.seriesIdentifier.fullId;
                if (msg.type == 'TEMPORARY_NOTICE') {
                    id += '(T)';
                } else if (msg.type == 'PRELIMINARY_NOTICE') {
                    id += '(P)';
                }
                element.html(id);
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
                var desc = LangService.desc(scope.msiMessageArea);
                var areas = (desc && desc.vicinity) ? desc.vicinity : '';
                for (var area = scope.msiMessageArea.area; area; area = area.parent) {
                    desc = LangService.desc(area);
                    var areaName = (desc) ? desc.name : '';
                    areas = areaName + ((areas.length > 0 && areaName.length > 0) ? ' - ' : '') + areas;
                }
                element.html(areas);
            }
        };
    }])

    /**
     * Replaces the content of the element with the area description
     */
    .directive('msiArea', ['LangService', function (LangService) {
        return {
            restrict: 'A',
            scope: {
                msiArea: "=",
                areaDivider: "@"
            },
            link: function(scope, element, attrs) {
                var divider = (attrs.areaDivider) ? attrs.areaDivider : " - ";
                scope.$watch(
                    function() { return scope.msiArea; },
                    function (newValue) {
                        var areas = '';
                        for (var area = scope.msiArea; area; area = area.parent) {
                            desc = LangService.desc(area);
                            var areaName = (desc) ? desc.name : '';
                            areas = areaName + ((areas.length > 0 && areaName.length > 0) ? divider : '') + areas;
                        }
                        element.html(areas);
                    });
            }
        };
    }])
