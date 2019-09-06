/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.reader.postgis;

import com.graphhopper.reader.DataReader;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphHopperStorage;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.NodeAccess;
import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * PostgisReader takes care of reading a PostGIS table and writing it to a road network graph
 *
 * @author Vikas Veshishth
 * @author Philip Welch
 * @author Mario Basa
 * @author Robin Boldt
 */
public abstract class PostgisReader implements DataReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgisReader.class);

    private final GraphStorage graphStorage;
    private final NodeAccess nodeAccess;
    protected final Graph graph;
    protected EncodingManager encodingManager;

    private Map<String, String> postgisParams;

    public PostgisReader(GraphHopperStorage ghStorage,
                         Map<String, String> postgisParams) {

        this.graphStorage = ghStorage;
        this.graph = ghStorage;
        this.nodeAccess = graph.getNodeAccess();
        this.encodingManager = ghStorage.getEncodingManager();

        this.postgisParams = postgisParams;
    }

    @Override
    public void readGraph() {
        graphStorage.create(1000);
        processJunctions();
        processRoads();
    }

    abstract void processJunctions();

    abstract void processRoads();

    protected FeatureIterator<SimpleFeature> getFeatureIterator(
            DataStore dataStore, String tableName) {

        if (dataStore == null)
            throw new IllegalArgumentException("DataStore cannot be null for getFeatureIterator");

        LOGGER.info("Getting the feature iterator for " + tableName);

        try {
            FeatureSource<SimpleFeatureType, SimpleFeature> source =
                    dataStore.getFeatureSource(tableName);
            Filter filter = Filter.INCLUDE;
            FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

            FeatureIterator<SimpleFeature> features = collection.features();
            return features;

        } catch (Exception e) {
            throw Utils.asUnchecked(e);
        }
    }

    protected DataStore openPostGisStore() {
        try {
            LOGGER.info("Opening DB connection to " + this.postgisParams.get("dbtype") + " " + this.postgisParams.get("host") + ":" + this.postgisParams.get("port") + " to database " + this.postgisParams.get("database") + " schema " + this.postgisParams.get("schema"));
            DataStore ds = DataStoreFinder.getDataStore(this.postgisParams);
            if (ds == null)
                throw new IllegalArgumentException("Error Connecting to Database ");
            return ds;

        } catch (Exception e) {
            throw Utils.asUnchecked(e);
        }
    }

    /*
     * Get longitude using the current long-lat order convention
     */
    protected double lng(Coordinate coordinate) {
        return coordinate.getOrdinate(0);
    }

    /*
     * Get latitude using the current long-lat order convention
     */
    protected double lat(Coordinate coordinate) {
        return coordinate.getOrdinate(1);
    }

    protected void saveTowerPosition(int nodeId, Coordinate point) {
        nodeAccess.setNode(nodeId, lat(point), lng(point));
    }
}