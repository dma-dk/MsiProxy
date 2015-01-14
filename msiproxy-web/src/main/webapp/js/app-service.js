
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

        }]);
