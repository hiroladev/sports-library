package de.hirola.sportslibrary.tables;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The schema for the track table.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.1
 */
public interface TrackColumns {

    String TABLE_NAME = "tracks"; // table name
    // columns from kinto object
    String ID = "id"; // the local (sqlite) id
    // data columns
    String NAME = "name"; // track name
    String DESCRIPTION = "description"; // short track description
    String AVGSPEED = "avgspeed"; // average speed
    String DISTANCE = "distance"; // distance
    String STARTTIME = "starttime"; // track start time
    String STOPTIME = "stoptime"; // track stop time
    String ACTIVE = "active"; // is a track recording

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NAME + " TEXT, "
            + DESCRIPTION + " TEXT, "
            + AVGSPEED + " FLOAT, "
            + DISTANCE + " FLOAT, "
            + STARTTIME + " INTEGER, "
            + STOPTIME + " INTEGER, "
            + ACTIVE + " INTEGER)";

    String CREATE_TABLE_INDEX = "CREATE UNIQUE INDEX " + TABLE_NAME + "_" + ID + "_index ON " + TABLE_NAME + "(" + ID + ")";

}
