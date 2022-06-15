package de.hirola.sportsapplications.util;

import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.model.LocationData;
import io.jenetics.jpx.*;
import io.jenetics.jpx.geom.Geoid;
import javax.validation.constraints.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A util class for managing GPX files-
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 *
 */
public final class GPXManager {
    /**
     * Import a track in GPX-Format with version 1.1 into the local datastore.
     * If the GPX file contains several tracks, these are imported individually.
     *
     * @param sportsLibrary in which the track import should become
     * @param importFile with data in GPX format
     * @throws IOException if the gpx file not read or the data could not be imported.
     */
    public static void importGPX(@NotNull SportsLibrary sportsLibrary, @NotNull File importFile) throws IOException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        if (importFile.exists()) {
            if (importFile.isFile() && importFile.canRead()) {
                // reading the gpx file using the jpx-library
                GPX gpx = GPX.read(importFile.getPath());
                // read metadata - create track remarks
                Optional<Metadata> optionalMetadata = gpx.getMetadata();
                StringBuilder trackRemarks = new StringBuilder();
                if (optionalMetadata.isPresent()) {
                    Metadata gpxMetadata = optionalMetadata.get();
                    if (!gpxMetadata.isEmpty()) {
                        Optional<String> optionalName = gpxMetadata.getName();
                        Optional<Person> optionalAuthor = gpxMetadata.getAuthor();
                        Optional<String> optionalDescription = gpxMetadata.getDescription();
                        List<Link> links = gpxMetadata.getLinks();
                        // build simple remarks
                        trackRemarks.append(optionalName.orElse(" "));
                        if (optionalAuthor.isPresent()) {
                            Person author = optionalAuthor.get();
                            if (!author.isEmpty()) {
                                Optional<String> optionalAuthorName = author.getName();
                                trackRemarks.append(optionalAuthorName.orElse(" "));
                                Optional<Link> optionalAuthorLink = author.getLink();
                                if (optionalAuthorLink.isPresent()) {
                                    Link authorLink = optionalAuthorLink.get();
                                    trackRemarks.append(authorLink.getHref().toString());
                                }
                                Optional<Email> optionalAuthorEmail = author.getEmail();
                                if (optionalAuthorEmail.isPresent()) {
                                    Email authorEmail = optionalAuthorEmail.get();
                                    trackRemarks.append(authorEmail.getAddress());
                                }
                            }
                        }
                        trackRemarks.append(optionalDescription.orElse(" "));
                        for (Link link: links) {
                            trackRemarks.append(link.getHref().toString());
                            trackRemarks.append(" ");
                        }
                    }
                }
                // read the tracks
                LocalDate now  = LocalDate.now(ZoneId.systemDefault());
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
                List<Track> gpxTracks = gpx.getTracks();
                for (Track gpxTrack : gpxTracks) {
                    Optional<String> optionalName = gpxTrack.getName();
                    String trackName = optionalName.orElse(
                            applicationResources.getString("track.default.name")
                                    + now.format(dateTimeFormatter));
                    Optional<String> optionalDescription = gpxTrack.getDescription();
                    String trackDescription = optionalDescription.orElse(
                            applicationResources.getString("track.default.description"));
                    // track segment: list of track points which are logically connected in order.
                    // while a track is recording, gps signal can be lost and comes back, a new segment begins
                    List<TrackSegment> trackSegments = gpxTrack.getSegments();
                    List<LocationData> locationDataList = new ArrayList<>();
                    double trackDistance = 0.0; // distance over all segments, calculate by jpx library
                    for (TrackSegment trackSegment: trackSegments) {
                        List<WayPoint> wayPoints = trackSegment.getPoints();
                        // add the distance of the segment
                        Length pathLength = wayPoints.stream().collect(Geoid.WGS84.toPathLength());
                        trackDistance += pathLength.doubleValue();
                        // create location data
                        for (WayPoint wayPoint: wayPoints) {
                            Latitude latitude = wayPoint.getLatitude();
                            Longitude longitude = wayPoint.getLongitude();
                            // create a simple locationData object
                            LocationData locationData = new LocationData(latitude.doubleValue(), longitude.doubleValue());
                            // add optional values to the locationData object
                            Optional<Length> optionalElevation = wayPoint.getElevation();
                            if (optionalElevation.isPresent()) {
                                Length elevation = optionalElevation.get();
                                locationData.setElevation(elevation.doubleValue());
                            }
                            Optional<ZonedDateTime> timeOptional = wayPoint.getTime();
                            if (timeOptional.isPresent()) {
                                ZonedDateTime zonedDateTime = timeOptional.get();
                                locationData.setTimeStamp(zonedDateTime.toEpochSecond());
                            }
                            Optional<Speed> optionalSpeed = wayPoint.getSpeed();
                            if (optionalSpeed.isPresent()) {
                                Speed speed = optionalSpeed.get();
                                locationData.setSpeed(speed.doubleValue());
                            }
                            Optional<Fix> optionalFix = wayPoint.getFix();
                            if (optionalFix.isPresent()) {
                                Fix fix = optionalFix.get();
                                locationData.setGpsFix(fix.getValue());
                            }

                            // add locationData to the list
                            locationDataList.add(locationData);
                        }
                    }
                    // create the track for the local datastore
                    de.hirola.sportsapplications.model.Track track = new de.hirola.sportsapplications.model.Track();
                    track.setName(trackName);
                    track.setDescription(trackDescription);
                    track.setRemarks(trackRemarks.toString());
                    track.setLocations(locationDataList);
                    track.setDistance(trackDistance);
                    // add to the local datastore
                    try {
                        sportsLibrary.add(track);
                    } catch (SportsLibraryException exception) {
                        throw new IOException("Error while adding track to the local datastore.", exception);
                    }
                }
            } else {
                throw new IOException("The file " + importFile + " is not a file or could not be read.");
            }
        } else {
            throw new IOException("The file " + importFile + " does not exist.");
        }
    }

    /**
     * Export a given Track from the sports library to a gpx file with version 1.1.
     *
     * @param track to be exported
     * @param exportFile for the track
     * @throws IOException if the export failed
     */
    public static void exportGPX(@NotNull de.hirola.sportsapplications.model.Track track,
                                 @NotNull File exportFile) throws IOException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        File parentDirectory = exportFile.getParentFile();
        if (parentDirectory.exists()) {
            if (parentDirectory.isDirectory() && parentDirectory.canWrite()) {
                // create a list of waypoints
                final List<WayPoint> wayPoints = new ArrayList<>();
                for (LocationData locationData : track.getLocations()) {
                    final WayPoint wayPoint = WayPoint.builder()
                            .lat(locationData.getLatitude())
                            .lon(locationData.getLongitude())
                            .time(locationData.getTimeStamp())
                            .speed(locationData.getSpeed())
                            .fix(locationData.getGpsFix().orElse("none"))
                            .build();
                    wayPoints.add(wayPoint);
                }
                // create a segment with waypoints
                final TrackSegment gpxTrackSegment = TrackSegment.of(wayPoints);
                // create a track with the segment
                final Track gpxTrack = Track.builder()
                        .name(track.getName())
                        .desc(track.getDescription().orElse(applicationResources.getString("track.default.description")))
                        .addSegment(gpxTrackSegment)
                        .build();
                // create a list of links for the metadata
                final List<Link> links = new ArrayList<>();
                Link link1 = Link.of(
                        URI.create(applicationResources.getString("gpx.export.link.1.href")),
                        applicationResources.getString("gpx.export.link.1.text"),
                        Global.Defaults.GPX_LINK_TYPE);
                links.add(link1);
                Link link2 = Link.of(
                        URI.create(applicationResources.getString("gpx.export.link.2.href")),
                        applicationResources.getString("gpx.export.link.2.text"),
                        Global.Defaults.GPX_LINK_TYPE);
                links.add(link2);
                // create metadata
                final Metadata metadata = Metadata.builder()
                        .author(applicationResources.getString("gpx.export.author"))
                        .desc(applicationResources.getString("gpx.export.description"))
                        .links(links)
                        .build();
                // create a gpx object
                final GPX gpx = GPX.builder()
                        .metadata(metadata)
                        .addTrack(gpxTrack)
                        .build();
                // export the gpx to a file
                GPX.write(gpx, exportFile.getPath());
            } else {
                throw new IOException("The file " + exportFile + " is not a file or is not writeable.");
            }
        } else {
            throw new IOException("The file " + exportFile + " does not exist.");
        }
    }
}
