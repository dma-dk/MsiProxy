
/**
 * Translations.
 * Specific site implementations should add their own translations
 */
angular.module('msiproxy.app')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {
            'LANG_EN' : 'English'

        });

        $translateProvider.translations('da', {
            'LANG_EN' : 'Engelsk'

        });

        $translateProvider.preferredLanguage('en');

    }]);

