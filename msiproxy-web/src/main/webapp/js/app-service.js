
/**
 * Services that retrieves MSI messages from the backend
 */
angular.module('msiproxy.app')

    /********************************
     * Interface for calling the application server
     ********************************/
    .factory('MsiProxyService', [ '$http', '$rootScope', function($http, $rootScope) {
        'use strict';

        return {

            messages: function(provider, lang, success, error) {
                $http.get('/rest/' + provider + '/v1/service/messages?lang=' + lang)
                    .success(success)
                    .error(error);
            }

        };
    }])

    /********************************
     * The language service is used for changing language, etc.
     ********************************/
    .service('LangService', ['$rootScope', '$translate',
        function ($rootScope, $translate) {
            'use strict';

            this.changeLanguage = function(lang) {
                $translate.use(lang);
                $rootScope.language = lang;
            };

            // look for a description entity with the given language
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

            // look for a description entity with the given language - falls back to using the first description
            this.descForLangOrDefault = function(elm, lang) {
                var desc = this.descForLanguage(elm, (lang) ? lang : $rootScope.language);
                if (!desc && elm && elm.descs && elm.descs.length > 0) {
                    desc = elm.descs[0];
                }
                return desc;
            };

            // look for a description entity with the current language
            this.desc = function(elm) {
                return this.descForLanguage(elm, $rootScope.language);
            };

            /**
             * Returns the area lineage description for the area using the current language
             * @param area the area lineage description for the area
             * @param vicinity an optional vicinity parameter
             */
            this.areaLineage = function(area, vicinity) {
                var divider = " - ";
                var areas = (vicinity) ? vicinity : '';
                while (area) {
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
             */
            this.messageAreaLineage = function(msg) {
                var desc = this.desc(msg);
                return this.areaLineage(msg.area, (desc) ? desc.vicinity : undefined);
            };

            /**
             * Returns the message title line using the current language.
             * This is composed of the area lineage + the vicinity of the message + title of the message
             * @param msg the message to return the title line for
             */
            this.messageTitleLine = function(msg) {
                var desc = this.desc(msg);
                var title = this.messageAreaLineage(msg);
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
