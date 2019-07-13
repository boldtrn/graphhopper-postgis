# GraphHopper Postgis reader

This repository is based on the work by [mbasa](https://github.com/mbasa/graphhopper/tree/postgis) and by [PGWelch](https://github.com/graphhopper/graphhopper/pull/874).

Please note, this is a module for GraphHopper, you will need to combine it with [GraphHopper](https://github.com/graphhopper/graphhopper).

To get an idea of how to use it, have a look at the [OSMPostgisReaderTest](https://github.com/boldtrn/graphhopper-postgis/blob/master/src/main/test/java/com/graphhopper/reader/postgis/OSMPostgisReaderTest.java).

## Setup Postgres 

Create a Table or View in the specifed database and schema that has these necessary columns (refer to OSM documentation for the proper values of these columns) : 

``` 
osm_id, maxspeed, oneway, fclass, name, geom 
```

Furthermore you can add more attributes and refer them in the config key `db.tagsToCopy`. These tags will be copied to the ReaderWay that you can use in your FlagEncoder. 

For example:

``` 
 create view philview 
   (osm_id,maxspeed,oneway,fclass,name,geom ) 
   as select id,0,oneway,'tertiary'::text,name,geom from phil;
```

## Start GraphHopper

Please note, these instructions are only valid if you add this module to the main GraphHopper. I'd recommend to set it up along the lines of the [GraphHopper MapMatching Repository](https://github.com/graphhopper/map-matching). 

Start the GraphHopper server by adding the parameter ``/<path where graph will reside>/<table or view name>``. The example below will create a graph, philview-gh, in /Users/mbasa/t for the philveiw view of PostgreSQL:

``` 
./graphhopper.sh web /Users/mbasa/t/philview
```

## Updating the Graph

If the data in PostgreSQL changes and the graph has to be updated, just delete the created graph directory and restart GraphHopper using the above method.