/**
 * The MSI-Proxy controller
 */
angular.module('msiproxy.app')
    .controller('MsiProxyCtrl', ['$scope', '$rootScope', '$routeParams', '$location', '$window', 'MsiProxyService', 'LangService',
        function ($scope, $rootScope, $routeParams, $location, $window, MsiProxyService, LangService) {
            'use strict';

            $scope.messages = [];
            $scope.provider = $routeParams.provider;
            $scope.lang = $routeParams.lang;
            $scope.viewMode = 'details';

            // Called to initialize the controller
            $scope.init = function (viewMode) {
                $scope.viewMode = viewMode;

                // Register the current language
                LangService.changeLanguage($scope.lang);

                // Load messages
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

            // Change the view mode of the message list
            $scope.go = function(viewMode) {
                $location.path( '/' + $scope.provider + '/' + $scope.lang + '/' + viewMode );
            };

            // Export a PDF of the message list
            $scope.pdf = function () {
                $window.open('/details.pdf?provider=' + $scope.provider + '&lang=' + $scope.lang, '_blank');
            };

        }]);
