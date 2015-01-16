
/**
 * Converts a div into a search result map.
 *
 * The map directive may be instantiated with a "messages" list, used for maps displaying a list of messages.
 * Alternatively, the map directive can be instantiated with a single "message", used for displaying a single message.
 *
 * In the former case, the map will be interactive, i.e. with tooltip and clickable features. Not so in the latter case.
 */
angular.module('msiproxy.app')
    .directive('msiMap', ['$rootScope', '$location', '$timeout', 'MapService', 'LangService',
        function ($rootScope, $location, $timeout, MapService, LangService) {
        'use strict';

            return {
                restrict: 'A',

                scope: {
                    messages: '=?',
                    message: '=?'
                },

                link: function (scope, element, attrs) {

                    // The map will only be interactive when displaying a list of messages.
                    scope.interactive = (scope.messages !== undefined);

                    // Just used for bootstrapping the map
                    var zoom = 6;
                    var lon  = 11;
                    var lat  = 56;

                    /*********************************/
                    /* Layers                        */
                    /*********************************/

                    var msiContext = {
                        fillOpacity: function(feature) {
                            return (feature.data.locType && (feature.data.locType == 'POLYGON' || feature.data.locType == 'CIRCLE')) ? 0.3 : 1.0;
                        },
                        graphicSize: function(feature) {
                            return 20;
                        },
                        graphicOffset: function(feature) {
                            return -msiContext.graphicSize() / 2;
                        },
                        description: function(feature) {
                            return feature.data.description;
                        },
                        icon: function(feature) {
                            return feature.data.icon;
                        }
                    };

                    var msiLayer  = new OpenLayers.Layer.Vector( "MSI", {
                        displayInLayerSwitcher: true,
                        styleMap: new OpenLayers.StyleMap({
                            "default": new OpenLayers.Style({
                                externalGraphic : "${icon}",
                                graphicWidth : "${graphicSize}",
                                graphicHeight : "${graphicSize}",
                                graphicYOffset : "${graphicOffset}",
                                graphicXOffset : "${graphicOffset}",
                                fillColor: "#ad57a1",
                                fillOpacity: "${fillOpacity}",
                                pointRadius: 8,
                                strokeWidth: 1.5,
                                strokeColor: "#8f2f7b",
                                strokeOpacity: 1.0
                            }, { context: msiContext })
                        })
                    });

                    // Build the array of layers to include
                    var layers = [];

                    // Add the OSM layer
                    layers.push(new OpenLayers.Layer.OSM("OSM", [
                        '//a.tile.openstreetmap.org/${z}/${x}/${y}.png',
                        '//b.tile.openstreetmap.org/${z}/${x}/${y}.png',
                        '//c.tile.openstreetmap.org/${z}/${x}/${y}.png' ], {
                        displayInLayerSwitcher: false
                    }));

                    // NB WMS layer gets proxied via "/wms/" to mask out colors and hide service-name, login and password
                    // For direct access, substitute "/wms/" with: http://kortforsyningen.kms.dk/
                    layers.push(new OpenLayers.Layer.WMS("WMS", "/wms/", {
                            layers : 'cells',
                            transparent : 'true',
                            styles : 'default'
                        }, {
                            isBaseLayer : false,
                            visibility : false,
                            projection : 'EPSG:3857'
                        }));

                    // Add the MSI layer
                    layers.push(msiLayer);

                    /*********************************/
                    /* Map                           */
                    /*********************************/

                    var map = new OpenLayers.Map({
                        div: element[0],
                        theme: null,
                        layers: layers,
                        units : "degrees",
                        projection : MapService.targetProjection(),
                        center: MapService.toLonLat(lon, lat),
                        zoom: zoom
                    });


                    // Show a layer switcher
                    // TODO: Pass the layer switcher id as an attribute to the directive. This is a hack...
                    map.addControl(new OpenLayers.Control.LayerSwitcher({
                        'div' : OpenLayers.Util.getElement((scope.interactive) ? 'msi-layerswitcher' : 'msi-details-layerswitcher')
                    }));

                    // Add zoom buttons
                    map.addControl(new OpenLayers.Control.Zoom());

                    // Trigger an update of the map size
                    // This is needed when using the map directive in a message details dialog...
                    $timeout(function() {
                        map.updateSize();
                    }, 100);

                    /*********************************/
                    /* Interactive Functionality     */
                    /*********************************/

                    /**
                     * Formats the tooltip content. Displays the MSI title line and date
                     * @param feature the MSI feature
                     * @returns the HTML tooltip contents
                     */
                    function formatTooltip(feature) {
                        var msg = feature.data.msi;
                        var desc =
                            '<div class="msi-map-tooltip">' +
                              '<div>' + LangService.messageTitleLine(msg) + '</div>' +
                              '<div><small>' + LangService.messageTime(msg) + '</small></div>' +
                            '</div>';
                        return desc;
                    }

                    /**
                     * When a MSI feature is clicked, open the message details dialog
                     * @param feature the MSI feature
                     */
                    function onMsiSelect(feature) {
                        var message = feature.attributes.msi;
                        var messages = scope.messages;

                        $rootScope.$broadcast('messageDetails', {
                            message: message,
                            messages: messages
                        });
                        hoverControl.unselectAll();
                    }

                    // The map is only interactive when displaying a list of messages, i.e. an overview map.
                    // When used in the message details dialog, the map is not interactive.
                    if (scope.interactive) {

                        var hoverControl = new OpenLayers.Control.SelectFeature(
                            msiLayer, {
                                hover: true,
                                onBeforeSelect: function(feature) {
                                    // add code to create tooltip/popup
                                    feature.popup = new OpenLayers.Popup.FramedCloud(
                                        "",
                                        feature.geometry.getBounds().getCenterLonLat(),
                                        new OpenLayers.Size(100,100),
                                        formatTooltip(feature),
                                        null,
                                        false,
                                        null);

                                    feature.popup.maxSize = new OpenLayers.Size(200,300);

                                    map.addPopup(feature.popup);
                                    return true;
                                },
                                onUnselect: function(feature) {
                                    // remove tooltip
                                    if (feature.popup) {
                                        map.removePopup(feature.popup);
                                        feature.popup.destroy();
                                        feature.popup=null;
                                    }
                                }
                            });

                        map.addControl(hoverControl);
                        hoverControl.activate();

                        var msiSelect = new OpenLayers.Handler.Click(
                            hoverControl, {
                                click: function (evt) {
                                    var feature = this.layer.getFeatureFromEvent(evt);
                                    if (feature) {
                                        onMsiSelect(feature);
                                    }
                                }
                            }, {
                                single: true,
                                double : false
                            });
                        msiSelect.activate();

                    }

                    /*********************************/
                    /* Update MSI and NtM's          */
                    /*********************************/

                    // Check for changes to the message attribute
                    scope.$watch(
                        function () { return scope.message; },
                        function (value) {
                            if (value) {
                                scope.messages = [value];
                            }
                        },
                        true);

                    // Check for changes to the messages list attribute
                    scope.$watch(
                        function () { return scope.messages; },
                        function (value) { addMessageFeatures(value); },
                        true);

                    /**
                     * Add the messages to the map as features
                     * @param messages the list of messages to add to the map
                     */
                    function addMessageFeatures(messages) {
                        msiLayer.removeAllFeatures();
                        if (messages)  {
                            var features = [];

                            for (var i in messages) {
                                var msg = messages[i];
                                var icon = "img/" + msg.seriesIdentifier.mainType.toLowerCase() + ".png";

                                for (var j in msg.locations) {
                                    var loc = msg.locations[j];

                                    // Flick the "showVertices to true to show icons for each vertex
                                    var attr = { id : i, type : "msi", msi : msg, icon: icon, showVertices:false  };
                                    MapService.createLocationFeature(loc, attr, features);
                                }
                            }
                            msiLayer.addFeatures(features);

                            // Zoom to the MSI layer extent
                            MapService.zoomToExtent(map, msiLayer);
                        }
                    }

                }
            }
        }]);

