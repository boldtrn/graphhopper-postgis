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

import com.graphhopper.GraphHopper;
import com.graphhopper.util.CmdArgs;
import com.graphhopper.util.Helper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * To be able to use this test class, you need to define access to your postgis DB in the environment variables
 * in your ~/.profile ~/.bashrc or similar
 *
 * @author Robin Boldt
 */
public class OSMPostgisReaderTest {

    private final String dir = "./target/tmp/test-db";

    // Note: Change the number of expected edges to your database
    private final int NR_EXPECTED_EDGES = 1000000;

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

        if(System. getenv("GH_DB_HOST") == null)
            throw new IllegalStateException("You need to define the environment variables before running the test");


        GraphHopper graphHopper = new GraphHopperPostgis().forServer();

        CmdArgs args = new CmdArgs();
        args.put("db.host", System. getenv("GH_DB_HOST"));
        args.put("db.port", System. getenv("GH_DB_PORT"));
        args.put("db.database", System. getenv("GH_DB_DATABASE"));
        args.put("db.schema", System. getenv("GH_DB_SCHEMA"));
        args.put("db.user", System. getenv("GH_DB_USER"));
        args.put("db.passwd", System. getenv("GH_DB_PASSWD"));

        //TODO this should be fixed at some point, probably it would be nicer to have this in the args as well
        args.put("datareader.file", System. getenv("GH_DB_TABLE"));
        args.put("graph.location", dir);
        args.put("graph.flag_encoders", "car");
        graphHopper.init(args);
        graphHopper.setCHEnabled(false);
        graphHopper.importOrLoad();

        assertTrue("Not enough edges created", graphHopper.getGraphHopperStorage().getAllEdges().length()>NR_EXPECTED_EDGES);
    }

}
