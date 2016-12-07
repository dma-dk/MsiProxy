
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
                        var serId = (msg.seriesIdentifier.number) ? ' (' + msg.seriesIdentifier.shortId + ')' : '';
                        var title = LangService.messageTitleLine(msg) + serId;

                        // Hack alert:
                        if (title.indexOf('Danmark - ') == 0 || title.indexOf('Denmark - ') == 0) {
                            title = title.substr('Danmark - '.length);
                        }

                        var desc =
                            '<div class="msi-map-tooltip">' +
                              '<div><strong>' + title + '</strong></div>' +
                              '<div><small>' + LangService.messageTime(msg) + '</small></div>' +
                            '</div>';
                        return desc;
                    }

                    /**
                     * Estimates the size of the tooltip popup
                     * @param html the html content
                     * @returns the estimated size
                     */
                    function computePopupSize(html) {
                        var titleLineNo = html.occurrences("msi-map-tooltip");
                        var timeLineNo = html.occurrences("<br />");
                        return new OpenLayers.Size(300, 10 + 40 * titleLineNo + 14 *  timeLineNo);
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

                    /**
                     * Checks if point is within the bounds of the given feature
                     * @param f the feature
                     * @param pt the point
                     * @return if the point is within the bounds of the given feature
                     */
                    function featureAtPoint(f, pt, mousePos) {
                        if (f.data.locType && f.data.locType == 'POINT') {
                            var featurePos = map.getViewPortPxFromLonLat(new OpenLayers.LonLat(f.geometry.x, f.geometry.y));
                            return featurePos.distanceTo(mousePos) < 12;
                        }
                        return f.atPoint(pt, 0, 0);
                    }

                    // The map is only interactive when displaying a list of messages, i.e. an overview map.
                    // When used in the message details dialog, the map is not interactive.
                    if (scope.interactive) {

                        var hoverControl = new OpenLayers.Control.SelectFeature(
                            msiLayer, {
                                hover: true,
                                onBeforeSelect: function(feature) {
                                    if (map.popup) {
                                        return;
                                    }

                                    var b = feature.geometry.getBounds();
                                    var html = formatTooltip(feature);

                                    // add code to create tooltip/popup
                                    map.popup = new OpenLayers.Popup.Anchored(
                                        "tooltip",
                                        new OpenLayers.LonLat(b.left, b.bottom),
                                        computePopupSize(html),
                                        html,
                                        {'size': new OpenLayers.Size(0,0), 'offset': new OpenLayers.Pixel(170, 12)},
                                        false,
                                        null);

                                    map.popup.backgroundColor = '#eeeeee';
                                    map.popup.calculateRelativePosition = function () {
                                        return 'bl';
                                    };


                                    map.addPopup(map.popup);
                                    return true;
                                },
                                onUnselect: function(feature) {
                                    // remove tooltip
                                    if (map.popup) {
                                        map.removePopup(map.popup);
                                        map.popup.destroy();
                                        map.popup=null;
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

                        // Overlapping polygon features poses a problem, in that the top-most polygon
                        // will hide the ones below.
                        // Attempt to fix this by tracking mouse moved events. Whenever a popup is being
                        // displayed, update the content to show the text for all features below the cursor.
                        map.events.register("mousemove", map, function (e) {
                            var mousePos = this.events.getMousePosition(e);
                            var point = map.getLonLatFromPixel(mousePos);
                            if (map.popup) {
                                var includedMessages = [];
                                var html = '';
                                for (var i in msiLayer.features) {
                                    var f = msiLayer.features[i];
                                    var msg = f.data.msi;
                                    if (featureAtPoint(f, point, mousePos) && $.inArray(msg.id, includedMessages) == -1) {
                                        includedMessages.push(msg.id);
                                        html += formatTooltip(f);
                                    }
                                }
                                if (map.popup.contentHTML != html) {
                                    map.popup.setContentHTML(html);
                                    map.popup.setSize(computePopupSize(html));
                                }
                            }
                        });

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

