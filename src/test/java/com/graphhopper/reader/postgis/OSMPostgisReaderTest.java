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

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.GraphHopperConfig;
import com.graphhopper.config.Profile;
import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.shapes.GHPoint;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * To be able to use this test class, you need to define access to your postgis DB in the environment variables
 * in your ~/.profile ~/.bashrc or similar
 * <p>
 * The current test uses a shape file from Geofabrik, this one: http://download.geofabrik.de/europe/andorra-201215-free.shp.zip
 * I uploaded the file "gis_osm_roads_free_1.shp" to https://qgiscloud.com/ using qgis.
 * <p>
 * If you use a different shape file, you might need to change the test values.
 *
 * @author Robin Boldt
 */
public class OSMPostgisReaderTest {

    private final String dir = "./target/tmp/test-db";

    // Note: Change the number of expected edges to your database
    private final int NR_EXPECTED_EDGES = 8538;

    @Before
    public void setUp() {
        new File(dir).mkdirs();
    }

    @After
    public void tearDown() {
        Helper.removeDir(new File(dir));
    }

    @Test
    public void testBasicGraphCreation() {

        if (System.getenv("GH_DB_HOST") == null)
            throw new IllegalStateException("You need to define the environment variables before running the test");


        StopWatch stopWatch = new StopWatch().start();
        GraphHopper graphHopper = new GraphHopperPostgis().forServer();

        GraphHopperConfig graphHopperConfig = new GraphHopperConfig();
        graphHopperConfig.putObject("db.host", System.getenv("GH_DB_HOST"));
        graphHopperConfig.putObject("db.port", System.getenv("GH_DB_PORT"));
        graphHopperConfig.putObject("db.database", System.getenv("GH_DB_DATABASE"));
        graphHopperConfig.putObject("db.schema", System.getenv("GH_DB_SCHEMA"));
        graphHopperConfig.putObject("db.user", System.getenv("GH_DB_USER"));
        graphHopperConfig.putObject("db.passwd", System.getenv("GH_DB_PASSWD"));
        graphHopperConfig.putObject("db.tags_to_copy", "name");

        //TODO this should be fixed at some point, probably it would be better to have this in the args as well
        graphHopperConfig.putObject("datareader.file", System.getenv("GH_DB_TABLE"));
        graphHopperConfig.putObject("graph.location", dir);
        graphHopperConfig.putObject("graph.flag_encoders", "car");

        graphHopperConfig.setProfiles(Collections.singletonList(new Profile("my_car").setVehicle("car").setWeighting("fastest")));

        graphHopper.init(graphHopperConfig);
        //graphHopper.setCHEnabled(false);
        graphHopper.importOrLoad();

        assertEquals("The number of expected edges does not match", graphHopper.getGraphHopperStorage().getAllEdges().length(), NR_EXPECTED_EDGES);
        stopWatch.stop();
        System.out.println("Importing the database took: " + stopWatch.getSeconds());

        GHRequest request = new GHRequest();
        request.addPoint(new GHPoint(42.476655, 1.490536));
        request.addPoint(new GHPoint(42.537271, 1.589928));
        request.setProfile("my_car");
        GHResponse response = graphHopper.route(request);

        assertTrue(13000 < response.getBest().getDistance());
        assertTrue(14000 > response.getBest().getDistance());

        assertTrue(800000 < response.getBest().getTime());
        assertTrue(900000 > response.getBest().getTime());
    }

}
