package de.hirola.sportsapplications.tables;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The schema for the location data table. The table is in relation to
 * the table of tracks.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public interface TrackLocationColumns {

    String TABLE_NAME = "locations";
    String ID = "id"; // primary key in sqlite
    String TRACK_ID = "trackid"; // id of the related track
    // data columns
    String TIME_STAMP = "timeStamp";
    String PROVIDER = "provider";
    String LONGITUDE = "longitude";
    String LATITUDE = "latitude";
    String ALTITUDE = "altitude";
    String SPEED = "speed";

    String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + TRACK_ID + " INTEGER NOT NULL, "
            + TIME_STAMP + " INTEGER, "
            + PROVIDER + " TEXT, "
            + LONGITUDE + " FLOAT, "
            + LATITUDE + " FLOAT, "
            + ALTITUDE + " FLOAT, "
            + SPEED + " FLOAT, "
            + "FOREIGN KEY (" + TRACK_ID + ") REFERENCES " + TrackColumns.TABLE_NAME +
            "(" + TrackColumns.ID + ") ON UPDATE CASCADE ON DELETE CASCADE)";

    String CREATE_TABLE_INDEX = "CREATE INDEX " + TABLE_NAME + "_" + TRACK_ID + "_index ON " + TABLE_NAME + "(" + TRACK_ID + ")";

}
