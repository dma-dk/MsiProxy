
/**
 * Services that retrieves MSI messages from the backend
 */
angular.module('msiproxy.app')

    /********************************
     * Interface for calling the application server
     ********************************/
    .factory('MsiProxyService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        /**
         * Scans through the search result and marks all messages that should potentially display an area head line
         * @param messages the messages to group by area
         * @param maxLevels the number of root areas to include in the area headings
         */
        function checkGroupByArea(messages, maxLevels) {
            var lastAreaId = undefined;
            if (messages) {
                for (var m in messages) {
                    var msg = messages[m];
                    var areas = [];
                    for (var area = msg.area; area !== undefined; area = area.parent) {
                        areas.unshift(area);
                    }
                    if (areas.length > 0) {
                        // If the message has a new sub-area at the maxLevels'th root level,
                        // flag it by setting the areaHeading message attribute to this area.
                        var index = Math.min(areas.length - 1, maxLevels - 1);
                        area = areas[index];
                        if (!lastAreaId || area.id != lastAreaId) {
                            lastAreaId = area.id;
                            msg.areaHeading = area;
                        }

                        // Flag that the area is the current area heading
                        area.areaHeading = true;
                    }
                }
            }
        }

        return {

            /**
             * Fetches a list of messages from the server
             * @param provider the MSI provider
             * @param lang the language
             * @param success called if the request is successful
             * @param error called if the request fails
             */
            messages: function(provider, lang, success, error) {
                $http.get('/rest/' + provider + '/v1/service/messages?lang=' + lang)
                    .success(function (messages) {
                        // First, group the messages by area
                        checkGroupByArea(messages, 2);

                        // Hand over the messages to the callee
                        success(messages);
                    })
                    .error(error);
            }

        };
    }])

    /********************************
     * The language service is used for changing language,
     * and formatting various MSI specific features in
     * a language specific manner.
     ********************************/
    .service('LangService', ['$rootScope', '$translate',
        function ($rootScope, $translate) {
            'use strict';

            /**
             * Registers the current language
             * @param lang the language
             */
            this.changeLanguage = function(lang) {
                $translate.use(lang);
                $rootScope.language = lang;
            };

            /**
             * look for a description entity with the given language
             * @param elm the localized entity
             * @param lang the language
             * @returns a description entity with the given language
             */
            this.descForLanguage = function(elm, lang) {
                if (elm && elm.descs) {
                    for (var d in elm.descs) {
                        if (elm.descs[d].lang == lang) {
                            return elm.descs[d];
                        }
                    }
                }
                return undefined;
            };

            /**
             * look for a description entity for the current language
             * @param elm the localized entity
             * @returns a description entity for the current language
             */
            this.desc = function(elm) {
                return this.descForLanguage(elm, $rootScope.language);
            };

            /**
             * Returns the area lineage description for the area using the current language
             * @param area the area lineage description for the area
             * @param vicinity an optional vicinity parameter
             * @param excludeAreaHeading whether to exclude the area heading or not
             */
            this.areaLineage = function(area, vicinity, excludeAreaHeading) {
                var divider = " - ";
                var areas = (vicinity) ? vicinity : '';
                while (area && (!excludeAreaHeading || !area.areaHeading)) {
                    var desc = this.desc(area);
                    var areaName = (desc) ? desc.name : '';
                    areas = areaName + ((areas.length > 0 && areaName.length > 0) ? divider : '') + areas;
                    area = area.parent;
                }
                return areas;
            };

            /**
             * Returns the message area lineage description for the area using the current language.
             * This is composed of the area lineage + the vicinity of the message
             * @param msg the message to return the area lineage description for
             * @param excludeAreaHeading whether to exclude the area heading or not
             */
            this.messageAreaLineage = function(msg, excludeAreaHeading) {
                var desc = this.desc(msg);
                return this.areaLineage(msg.area, (desc) ? desc.vicinity : undefined, excludeAreaHeading);
            };

            /**
             * Returns the message title line using the current language.
             * This is composed of the area lineage + the vicinity of the message + title of the message
             * @param msg the message to return the title line for
             * @param excludeAreaHeading whether to exclude the area heading or not
             */
            this.messageTitleLine = function(msg, excludeAreaHeading) {
                var desc = this.desc(msg);
                var title = this.messageAreaLineage(msg, excludeAreaHeading);
                if (desc && desc.title) {
                    title = title + ' - ' + desc.title;
                }
                return title;
            };

            /**
             * Returns the message time description using the current language.
             * If the textual time description is defined, this is returned,
             * otherwise, the validFrom - validTo date interval is used.
             * @param msg the message to return the time description for
             */
            this.messageTime = function(msg) {
                var desc = this.desc(msg);
                if (desc && desc.time) {
                    return plain2html(desc.time);
                }

                var lang = $rootScope.language;
                var from = moment(msg.validFrom);
                var time = from.locale(lang).format("lll");
                if (msg.validTo) {
                    var to = moment(msg.validTo);
                    var fromDate = from.locale(lang).format("ll");
                    var toDate = to.locale(lang).format("ll");
                    var toDateTime = to.locale(lang).format("lll");
                    if (fromDate == toDate) {
                        // Same dates
                        time += " - " + toDateTime.replace(toDate, '');
                    } else {
                        time += " - " + toDateTime;
                    }
                }
                return time;
            }

        }]);
