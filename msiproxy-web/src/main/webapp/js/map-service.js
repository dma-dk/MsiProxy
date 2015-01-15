
/**
 * MapService factory
 */
angular.module('msiproxy.app')
       .factory('MapService', ['$rootScope', '$http', function ($rootScope, $http) {
        "use strict";

        var proj4326 = new OpenLayers.Projection("EPSG:4326");
        var projmerc = new OpenLayers.Projection("EPSG:900913");

        /**
         * Converts from degrees to radians
         * @param degree the degrees
         * @returns the radians
         */
        function toRad(degree) {
            return degree / 360 * 2 * Math.PI;
        }

        /**
         * Converts from radians to degrees
         * @param rad the radians
         * @returns the degrees
         */
        function toDegree(rad) {
            return rad * 360 / 2 / Math.PI;
        }

        /**
         * Creates an OpenLayer point properly transformed
         * @param lon longitude
         * @param lat latitude
         * @returns the point
         */
        function createPoint(lon, lat) {
            return new OpenLayers.Geometry.Point(lon, lat).transform(proj4326, projmerc);
        }

        /**
         * Constructs a circular ring of transformed points
         * @param lon longitude
         * @param lat latitude
         * @param radius the radius in nm
         * @param noPoints the number of points
         * @returns the list of points that constitutes the ring
         */
        function calculateRing(lon, lat, radius, noPoints) {
            var points = [];
            var lat1 = toRad(lat);
            var lon1 = toRad(lon);
            var R = 6371.0087714; // earths mean radius
            var d = radius * 1852.0 / 1000.0; // nm -> km
            for (var i = 0; i < noPoints; i++) {
                var brng = Math.PI * 2 * i / noPoints;
                var lat2 = Math.asin( Math.sin(lat1)*Math.cos(d/R) +
                Math.cos(lat1)*Math.sin(d/R)*Math.cos(brng) );
                var lon2 = lon1 + Math.atan2(Math.sin(brng)*Math.sin(d/R)*Math.cos(lat1),
                        Math.cos(d/R)-Math.sin(lat1)*Math.sin(lat2));

                points.push(createPoint(toDegree(lon2), toDegree(lat2)));
            }
            return points;
        }


        // Return the public API
        return {

            /**
             * Returns the target Mercator projection
             * @returns the target Mercator projection
             */
            targetProjection: function () {
                return projmerc;
            },

            /**
             * Wraps the lon, lat as an OpenLayers Point transformed to the Mercator projection
             * @param lon the longitude
             * @param lat the latitude
             * @returns the OpenLayers Point
             */
            toPt: function(lon, lat) {
                return createPoint(loc.points[j].lon, loc.points[j].lat);
            },

            /**
             * Wraps the lon, lat as an OpenLayers LonLat transformed to the Mercator projection
             * @param lon the longitude
             * @param lat the latitude
             * @returns the OpenLayers LonLat
             */
            toLonLat: function(lon, lat) {
                return new OpenLayers.LonLat(lon, lat).transform(proj4326, projmerc);
            },

            /**
             * Creates the location OpenLayer feature
             * @param loc the location
             * @param attr the attributes to associate with the feature
             * @param features the feature list to update
             * @returns the updated feature list
             */
            createLocationFeature: function(loc, attr, features) {
                if (!loc || !loc.type) {
                    return features;
                }

                attr.locType = loc.type;

                switch (loc.type) {
                    case 'POINT':
                        for (var j in loc.points) {
                            features.push(new OpenLayers.Feature.Vector(createPoint(loc.points[j].lon, loc.points[j].lat), attr));
                        }
                        break;

                    case "POLYGON":
                    case "POLYLINE":
                        var points = [];
                        for (var j in loc.points) {
                            var p = loc.points[j];
                            points.push(createPoint(p.lon, p.lat));
                        }

                        features.push(new OpenLayers.Feature.Vector(
                            (loc.type == 'POLYGON')
                                ? new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)])
                                : new OpenLayers.Geometry.LineString(points),
                            attr
                        ));

                        if (attr.showVertices) {
                            for (var j in points) {
                                features.push(new OpenLayers.Feature.Vector(points[j], attr));
                            }
                        }
                        break;

                    case 'CIRCLE':
                        var points = calculateRing(loc.points[0].lon, loc.points[0].lat, loc.radius, 40);
                        features.push(new OpenLayers.Feature.Vector(
                            new OpenLayers.Geometry.Polygon([new OpenLayers.Geometry.LinearRing(points)]), attr));
                        break;
                }
                return features;
            },

            /**
             * Zooms the map the the extent of the given layer
             */
            zoomToExtent : function(map, layer) {
                var extent = new OpenLayers.Bounds();

                var e = layer.getDataExtent();
                if (e) {
                    extent.bottom = Math.min(99999999, e.bottom);
                    extent.left = Math.min(99999999, e.left);
                    extent.top = Math.max(-99999999, e.top);
                    extent.right = Math.max(-99999999, e.right);

                    var deltaV = extent.top - extent.bottom;
                    var deltaH =  extent.right - extent.left;

                    // Handle point case
                    if (deltaH < 100 && deltaV < 100) {
                        deltaH = 30000;
                        deltaV = 30000;
                    }

                    extent.bottom -= deltaV * 0.1;
                    extent.left -= deltaH * 0.1;
                    extent.right += deltaH * 0.1;
                    extent.top += deltaV * 0.1;

                    map.zoomToExtent(extent);
                }
            },

            zoomToFeature : function(map, feature) {
                // If the feature is a point (or clost to it), select a greated extent
                var min = 10000;
                var extent = feature.geometry.getBounds();
                if (Math.abs(extent.bottom - extent.top) < 100 && Math.abs(extent.right - extent.left) < 100) {
                    var size = 20000;
                    extent.bottom -= size;
                    extent.left -= size;
                    extent.right += size;
                    extent.top += size;
                }

                map.zoomToExtent(extent);
            }
        }
    }]);

