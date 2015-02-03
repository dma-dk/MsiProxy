/**
 * The main MSI-Proxy controller
 */
angular.module('msiproxy.app')
    .controller('MsiProxyCtrl', ['$scope', '$rootScope', '$routeParams', '$location', '$window', '$modal', '$timeout', 'MsiProxyService', 'LangService',
        function ($scope, $rootScope, $routeParams, $location, $window, $modal, $timeout, MsiProxyService, LangService) {
            'use strict';

            $scope.allMessages = [];        // The total list of messages
            $scope.messages = [];           // The filtered list of messages
            $scope.generalMessages = [];    // The filtered list of messages without a location

            $scope.provider = $routeParams.provider;
            $scope.lang = $routeParams.lang;
            $scope.viewMode = 'details';

            // Filtering support
            $scope.areaHeadings = [];
            $scope.filterNow = { filtered: true };

            /**
             * Called to initialize the controller with view mode.
             * @param viewMode the view mode, one of "details", "grid" or "map"
             */
            $scope.init = function (viewMode) {
                $scope.viewMode = viewMode;

                // Register the current language
                LangService.changeLanguage($scope.lang);

                // Load messages
                MsiProxyService.messages(
                    $scope.provider,
                    $scope.lang,
                    $scope.updateMessages,
                    function () {
                        console.error("Error fetching messages");
                    }
                )
            };

            /**
             * Called when the list of messages has been updated
             * @param messages the new list of messages
             */
            $scope.updateMessages = function (messages) {
                $scope.allMessages = messages;

                // Reset the filter
                $scope.resetFilter();

                // Hook up watchers on filter controls
                $scope.$watch(
                    function() { return $scope.areaHeadings },
                    function(data) { $scope.updateFilter(); },
                    true);
                $scope.$watch(
                    function() { return $scope.filterNow },
                    function(data) { $scope.updateFilter(); },
                    true);
            };

            /**
             * Resets the message filter
             */
            $scope.resetFilter = function () {
                $scope.messages = $scope.allMessages;
                $scope.filterNow.filtered = false;
                $scope.areaHeadings = [];

                // Get hold of the area headings
                // Every time the two root-most areas of a message in the messages list changes,
                // the message will be stamped with an "areaHeading" attribute, so that, in effect,
                // the areaHeading attribute serves as a grouping of the messages.
                for (var m in $scope.allMessages) {
                    if ($scope.allMessages[m].areaHeading) {
                        $scope.areaHeadings.push($scope.allMessages[m].areaHeading);
                        $scope.allMessages[m].areaHeading.filtered = false;
                    }
                }

                // Since the height of the filter bar may have changed height,
                // recompute the top of the message list
                $timeout(adjustMessageListTopPosition, 100);

            };

            /**
             * Updates the filtered list of message based on the current filter state
             */
            $scope.updateFilter = function () {
                $scope.messages = [];
                $scope.generalMessages = [];

                // Compute the area headings that should be filtered by
                var areas = [];
                for (var a in $scope.areaHeadings) {
                    if ($scope.areaHeadings[a].filtered) {
                        areas.push($scope.areaHeadings[a]);
                    }
                }

                // Special case: If no area heading is selected, filter by all area headings
                if (areas.length == 0) {
                    areas = $scope.areaHeadings;
                }

                // Filter the list of messages by their area heading and date
                var includeAreaMessages = false;
                for (var m in $scope.allMessages) {
                    var msg = $scope.allMessages[m];
                    if (msg.areaHeading) {
                        includeAreaMessages = $.inArray(msg.areaHeading, areas) != -1;
                    }
                    var includeActive = !$scope.filterNow.filtered || msg.validFrom < new Date();
                    if (includeAreaMessages && includeActive) {
                        $scope.messages.push(msg);
                        // If the message does not have a location, add it as a "general message"
                        if (!msg.locations || msg.locations.length == 0) {
                            $scope.generalMessages.push(msg);
                        }
                    }
                }
            };

            /**
             * Change the view mode of the message list
             * @param viewMode the new view mode
             */
            $scope.go = function(viewMode) {
                $location.path( '/' + $scope.provider + '/' + $scope.lang + '/' + viewMode );
            };

            /**
             * Export a PDF of the message list
             */
            $scope.pdf = function () {
                var params = 'provider=' + $scope.provider + '&lang=' + $scope.lang;
                params += '&activeNow=' + $scope.filterNow.filtered;
                var areas = '';
                for (var a in $scope.areaHeadings) {
                    if ($scope.areaHeadings[a].filtered) {
                        if (areas.length > 0) {
                            areas += ',';
                        }
                        areas += $scope.areaHeadings[a].id;
                    }
                }
                params += '&areaHeadings=' + areas;
                $window.open('/details.pdf?' + params, '_blank');
            };

            /**
             * Register for 'messageDetails' events, and launch the message details dialog
             */
            $scope.$on('messageDetails', function (event, data) {
                $modal.open({
                    controller: "MessageDialogCtrl",
                    templateUrl: "/partials/message-details-dialog.html",
                    size: 'lg',
                    resolve: {
                        message: function () { return data.message; },
                        messages: function () { return data.messages; }
                    }
                });
            });

        }])


    /*******************************************************************
     * Controller that handles displaying message details in a dialog
     *******************************************************************/
    .controller('MessageDialogCtrl', ['$scope', '$window', 'message', 'messages',
        function ($scope, $window, message, messages) {
            'use strict';

            $scope.messages = messages;
            $scope.msg = message;
            $scope.index = $.inArray(message, messages);


            /**
             * Navigate to the previous message in the message list
             */
            $scope.selectPrev = function() {
                if ($scope.index > 0) {
                    $scope.index--;
                    $scope.msg = $scope.messages[$scope.index];
                }
            };

            /**
             * Navigate to the next message in the message list
             */
            $scope.selectNext = function() {
                if ($scope.index >= 0 && $scope.index < $scope.messages.length - 1) {
                    $scope.index++;
                    $scope.msg= $scope.messages[$scope.index];
                }
            };

            /**
             * Export a PDF with the current message
             */
            $scope.pdf = function () {
                $window.open('/details.pdf?provider=' + $scope.msg.provider
                    + '&lang=' + $scope.language
                    + '&messageId=' + $scope.msg.id,
                    '_blank');
            };

        }]);
