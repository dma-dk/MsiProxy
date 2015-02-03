
/**
 * Translations.
 */
angular.module('msiproxy.app')

    .config(['$translateProvider', function ($translateProvider) {

        $translateProvider.translations('en', {

            'TITLE' : 'Maritime Notifications',
            'MENU_DETAILS' : 'Details',
            'MENU_GRID' : 'Gallery',
            'MENU_MAP' : 'Map',
            'MENU_PDF' : 'PDF',
            'BTN_CLOSE' : 'Close',
            'GENERAL_MSGS' : 'General Notifications',
            'FIELD_REFERENCE' : 'Reference',
            'FIELD_TIME' : 'Time',
            'FIELD_LOCATION' : 'Location',
            'FIELD_DETAILS' : 'Details',
            'FIELD_ATTACHMENTS' : 'Attachments',
            'FIELD_NOTE' : 'Note',
            'FIELD_CHARTS' : 'Charts',
            'FIELD_PUBLICATION' : 'Publication',
            'REF_REPETITION' : '(repetition)',
            'REF_CANCELLED' : '(cancelled)',
            'REF_UPDATED' : '(updated)',
            'ACTIVE_NOW' : 'Active Now',
            'SHOW_POS' : 'Show positions',
            'HIDE_POS' : 'Hide positions'
        });

        $translateProvider.translations('da', {

            'TITLE' : 'Maritime Notifikationer',
            'MENU_DETAILS' : 'Detaljer',
            'MENU_GRID' : 'Galleri',
            'MENU_MAP' : 'Kort',
            'MENU_PDF' : 'PDF',
            'BTN_CLOSE' : 'Luk',
            'GENERAL_MSGS' : 'Generelle Notifikationer',
            'FIELD_REFERENCE' : 'Reference',
            'FIELD_TIME' : 'Tid',
            'FIELD_LOCATION' : 'Placering',
            'FIELD_DETAILS' : 'Detaljer',
            'FIELD_ATTACHMENTS' : 'Vedhæftninger',
            'FIELD_NOTE' : 'Note',
            'FIELD_CHARTS' : 'Søkort',
            'FIELD_PUBLICATION' : 'Publikation',
            'REF_REPETITION' : '(gentagelse)',
            'REF_CANCELLED' : '(udgår)',
            'REF_UPDATED' : '(ajourført)',
            'ACTIVE_NOW' : 'Aktive nu',
            'SHOW_POS' : 'Vis positioner',
            'HIDE_POS' : 'Skjul positioner'
        });

        $translateProvider.preferredLanguage('en');

    }]);

