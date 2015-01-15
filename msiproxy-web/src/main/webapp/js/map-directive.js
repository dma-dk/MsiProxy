
/**
 * Converts a div into a search result map
 */
angular.module('msiproxy.app')
    .directive('msiMap', ['$rootScope', '$location', 'MapService', 'LangService',
        function ($rootScope, $location, MapService, LangService) {
        'use strict';

            return {
                restrict: 'A',

                scope: {
                    msiMap: '='
                },

                link: function (scope, element, attrs) {

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

                    var msiLayer  = new OpenLayers.Layer.Vector( "Msi-Nm", {
                        displayInLayerSwitcher: false,
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
                                /**
                                label : "${description}",
                                fontFamily: "Courier New, monospace",
                                fontWeight: "bold",
                                fontSize: "11px",
                                fontColor: "#8f2f7b",
                                labelOutlineColor: "white",
                                labelOutlineWidth : 2,
                                labelYOffset: -20
                                **/
                            }, { context: msiContext })
                        })
                    });

                    // Build the array of layers to include
                    var layers = [];

                    // Add the OSM layer
                    layers.push(new OpenLayers.Layer.OSM("OSM", [
                        '//a.tile.openstreetmap.org/${z}/${x}/${y}.png',
                        '//b.tile.openstreetmap.org/${z}/${x}/${y}.png',
                        '//c.tile.openstreetmap.org/${z}/${x}/${y}.png' ], null));

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

/*
                    map.addControl(new OpenLayers.Control.LayerSwitcher({
                        'div' : OpenLayers.Util.getElement('search-layerswitcher')
                    }));
*/

                    /*********************************/
                    /* Hover Tooltip                 */
                    /*********************************/

                    function formatTooltip(feature) {
                        var msg = feature.data.msi;
                        var desc =
                            '<div class="msi-map-tooltip">' +
                            '<div>' + LangService.messageTitleLine(msg) + '</div>' +
                            '<div>' + LangService.messageTime(msg) + '</div>' +
                            '</div>';
                        return desc;
                    }

                    var hoverControl = new OpenLayers.Control.SelectFeature(
                        msiLayer, {
                            hover: true,
                            onBeforeSelect: function(feature) {
                                // add code to create tooltip/popup
                                var popup = new OpenLayers.Popup.FramedCloud(
                                    "",
                                    feature.geometry.getBounds().getCenterLonLat(),
                                    new OpenLayers.Size(100,100),
                                    formatTooltip(feature),
                                    null,
                                    true,
                                    null);

                                feature.popup = popup;

                                map.addPopup(popup);
                                // return false to disable selection and redraw
                                // or return true for default behaviour
                                return true;
                            },
                            onUnselect: function(feature) {
                                // remove tooltip
                                map.removePopup(feature.popup);
                                feature.popup.destroy();
                                feature.popup=null;
                            }
                        });

                    map.addControl(hoverControl);
                    hoverControl.activate();

                    /*********************************/
                    /* Update MSI and NtM's          */
                    /*********************************/

                    // Crop the text to at most len characters
                    function cropTxt(txt, len) {
                        if (txt && txt.length > len) {
                            txt = txt.substring(0, len) + "\u2026";
                        }
                        return txt;
                    }

                    // Check for changes to the messages list
                    scope.$watch(
                        function () { return scope.msiMap; },
                        function (value) { addMessageFeatures(value); },
                        true);

                    // Add the messages to the map as features
                    function addMessageFeatures(messages) {
                        msiLayer.removeAllFeatures();
                        if (messages)  {
                            var features = [];

                            for (var i in messages) {
                                var msg = messages[i];
                                var icon = "img/" + msg.seriesIdentifier.mainType.toLowerCase() + ".png";

                                for (var j in msg.locations) {
                                    var loc = msg.locations[j];

                                    var title = (msg.descs && msg.descs.length > 0) ? msg.descs[0].title : "N/A";

                                    // Flick the "showVertices to true to show icons for each vertex
                                    var attr = { id : i, description: cropTxt(title, 20), type : "msi", msi : msg, icon: icon, showVertices:false  };
                                    MapService.createLocationFeature(loc, attr, features);
                                }
                            }
                            msiLayer.addFeatures(features);

                            // Zoom to the MSI layer extent
                            MapService.zoomToExtent(map, msiLayer);
                        }
                    }

                    /*********************************/
                    /* Pop-ups for the features      */
                    /*********************************/

/*
                    var msiSelect = new OpenLayers.Control.SelectFeature(msiLayer);
                    msiLayer.events.on({
                        "featureselected": onMsiSelect
                    });
                    map.addControl(msiSelect);
                    msiSelect.activate();

                    function onMsiSelect(event) {
                        var messageId, messages;
                        if (event.feature.cluster) {
                            // Cluster clicked
                            messages = getClusterMessages(event.feature);
                            messageId = messages[0].id;

                        } else {
                            // Actual message clicked
                            messageId = event.feature.attributes.msi.id;
                            messages = scope.searchResult.messages;
                        }

                        $rootScope.$broadcast('messageDetails', {
                            messageId: messageId,
                            messages: messages
                        });
                        msiSelect.unselectAll();
                    }
*/

                }
            }
        }]);

