/**
 * The MSI-Proxy controller
 */
angular.module('msiproxy.app')
    .controller('MsiProxyCtrl', ['$scope', '$rootScope', '$routeParams', 'MsiProxyService', 'LangService',
        function ($scope, $rootScope, $routeParams, MsiProxyService, LangService) {
            'use strict';

            $scope.messages = [];
            $scope.provider = $routeParams.provider;
            $scope.lang = $routeParams.lang;

            $scope.init = function () {

                LangService.changeLanguage($scope.lang);

                MsiProxyService.messages(
                    $scope.provider,
                    $scope.lang,
                    function(data) {
                        $scope.messages = data;
                        $scope.checkGroupByArea(2);
                    },
                    function () {
                        console.error("Error fetching messages");
                    }
                )
            };

            // Scans through the search result and marks all messages that should potentially display an area head line
            $scope.checkGroupByArea = function (maxLevels) {
                var lastAreaId = undefined;
                if ($scope.messages) {
                    for (var m in $scope.messages) {
                        var msg = $scope.messages[m];
                        var areas = [];
                        for (var area = msg.area; area !== undefined; area = area.parent) {
                            areas.unshift(area);
                        }
                        if (areas.length > 0) {
                            area = areas[Math.min(areas.length - 1, maxLevels - 1)];
                            if (!lastAreaId || area.id != lastAreaId) {
                                lastAreaId = area.id;
                                msg.areaHeading = area;
                            }
                        }
                    }
                }
            };

        }]);
