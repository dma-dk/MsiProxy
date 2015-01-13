
/**
 * Translations.
 * Specific site implementations should add their own translations
 */
angular.module('msiproxy.app')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {

            'FIELD_REFERENCE' : 'Reference',
            'FIELD_TIME' : 'Time',
            'FIELD_LOCATION' : 'Location',
            'FIELD_DETAILS' : 'Details',
            'FIELD_ATTACHMENTS' : 'Attachments',
            'FIELD_NOTE' : 'Note',
            'FIELD_CHARTS' : 'Charts',
            'FIELD_PUBLICATION' : 'Publication',
            'REF_REPITITION' : '(repitition)',
            'REF_CANCELLED' : '(cancelled)',
            'REF_UPDATED' : '(updated)'

    });

        $translateProvider.translations('da', {

            'FIELD_REFERENCE' : 'Reference',
            'FIELD_TIME' : 'Tid',
            'FIELD_LOCATION' : 'Placering',
            'FIELD_DETAILS' : 'Detaljer',
            'FIELD_ATTACHMENTS' : 'Vedhæftninger',
            'FIELD_NOTE' : 'Note',
            'FIELD_CHARTS' : 'Søkort',
            'FIELD_PUBLICATION' : 'Publikation',
            'REF_REPITITION' : '(gentagelse)',
            'REF_CANCELLED' : '(udgår)',
            'REF_UPDATED' : '(ajourført)'

    });

        $translateProvider.preferredLanguage('en');

    }]);

