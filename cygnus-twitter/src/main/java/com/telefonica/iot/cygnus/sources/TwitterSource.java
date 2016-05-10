
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.telefonica.iot.cygnus.sources;

import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.EventDrivenSource;
import org.apache.flume.channel.ChannelProcessor;
import org.apache.flume.conf.Configurable;
import org.apache.flume.event.EventBuilder;
import org.apache.flume.source.AbstractSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TwitterSource extends AbstractSource
        implements EventDrivenSource, Configurable {

    private TwitterStream twitterStream;
    private List<Event> eventBatch = new ArrayList<Event>();

    private boolean have_filters = false;
    private boolean have_coordinate_filter = false;
    private boolean have_keyword_filter = false;

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private double[][] bounding_box;
    private String[] splitKeywords;


    private long documentCount = 0;
    private long startTime = 0;
    private long exceptionCount = 0;
    private long totalTextIndexed = 0;
    private long skippedDocs = 0;
    private long batchEndTime = 0;


    private int maxBatchSize = 1000;
    private int maxBatchDurationMillis = 1000;

    // Fri May 14 02:52:55 +0000 2010

    private DecimalFormat numFormatter = new DecimalFormat("###,###.###");

    private static int REPORT_INTERVAL = 100;
    private static int STATS_INTERVAL = REPORT_INTERVAL * 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSource.class);


    String getConsumerKey(){
        return consumerKey;
    }

    String getConsumerSecret(){
        return consumerSecret;
    }

    String getAccessToken() {
        return accessToken;
    }

    String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    double[][] getBoundingBox() {
        return bounding_box;
    }

    String[] getKeywords() {
        return splitKeywords;
    }

    @Override
    public void configure(Context context) {
        consumerKey = context.getString("consumerKey");
        consumerSecret = context.getString("consumerSecret");
        accessToken = context.getString("accessToken");
        accessTokenSecret = context.getString("accessTokenSecret");

        LOGGER.info("Consumer Key:        '" + consumerKey + "'");
        LOGGER.info("Consumer Secret:     '" + consumerSecret + "'");
        LOGGER.info("Access Token:        '" + accessToken + "'");
        LOGGER.info("Access Token Secret: '" + accessTokenSecret + "'");


        String south_west_latitude;
        String south_west_longitude;

        String north_east_latitude;
        String north_east_longitude;

        String keywords;


        //Top-left coordinate
        south_west_latitude = context.getString("south_west_latitude");
        south_west_longitude = context.getString("south_west_longitude");
        LOGGER.info("South-West coordinate: '" + south_west_latitude + " " + south_west_longitude + "'");

        //Bottom-right coordinate
        north_east_latitude = context.getString("north_east_latitude");
        north_east_longitude = context.getString("north_east_longitude");
        LOGGER.info("North-East coordinate: '" + north_east_latitude + " " + north_east_longitude + "'");

        keywords = context.getString("keywords");
        LOGGER.info("Keywords:            '" + keywords + "'");

        if (south_west_latitude != null && south_west_longitude != null && north_east_latitude != null && north_east_longitude != null) {
            double latitude1 = Double.parseDouble(south_west_latitude);
            double longitude1 = Double.parseDouble(south_west_longitude);

            double latitude2 = Double.parseDouble(north_east_latitude);
            double longitude2 = Double.parseDouble(north_east_longitude);

            bounding_box = new double[][]{
                    new double[]{longitude1, latitude1}, // south-west
                    new double[]{longitude2, latitude2}  // north-east
            };

            LOGGER.info("Coordinates:         '" + bounding_box[0][0] + " " + bounding_box[0][1] +
                                            " "  + bounding_box[1][0] + " " + bounding_box[1][1] + "'");
            have_filters = true;
            have_coordinate_filter = true;
        }

        if (keywords != null) {
            if (keywords.trim().length() != 0) {
                splitKeywords = keywords.split(",");
                for (int i = 0; i < splitKeywords.length; i++) {
                    splitKeywords[i] = splitKeywords[i].trim();
                }

                LOGGER.info("keywords:            {}", Arrays.toString(splitKeywords));
                have_filters = true;
                have_keyword_filter = true;
            }
        }


        maxBatchSize = context.getInteger("maxBatchSize", maxBatchSize);
        maxBatchDurationMillis = context.getInteger("maxBatchDurationMillis",
                maxBatchDurationMillis);

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();


    }

    @Override
    public synchronized void start() {
        LOGGER.info("Starting twitter source {} ...", this);
        documentCount = 0;
        startTime = System.currentTimeMillis();
        exceptionCount = 0;
        totalTextIndexed = 0;
        skippedDocs = 0;
        batchEndTime = System.currentTimeMillis() + maxBatchDurationMillis;

        final ChannelProcessor channel = getChannelProcessor();

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                String jsonTweet = getStringJSONTweet(status);
                Event event = EventBuilder.withBody(jsonTweet, Charset.forName("UTF8"));

                eventBatch.add(event);

                if (eventBatch.size() >= maxBatchSize ||
                        System.currentTimeMillis() >= batchEndTime) {
                    batchEndTime = System.currentTimeMillis() + maxBatchDurationMillis;

                    channel.processEventBatch(eventBatch); // send batch of events (one per tweet) to the flume sink
                    eventBatch.clear();


                }

                documentCount++;
                if ((documentCount % REPORT_INTERVAL) == 0) {
                    LOGGER.info(String.format("Processed %s docs", numFormatter.format(documentCount)));
                }
                if ((documentCount % STATS_INTERVAL) == 0) {
                    logStats();
                }

            }

            // This listener will ignore everything except for new tweets
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            public void onScrubGeo(long userId, long upToStatusId) {
            }

            public void onStallWarning(StallWarning warning) {
            }

            public void onException(Exception e) {
                LOGGER.error("Exception while streaming tweets", e);
            }
        };

        twitterStream.addListener(listener);


        if (have_filters) {
            FilterQuery filterQuery = new FilterQuery();

            if (have_coordinate_filter) {
                filterQuery.locations(bounding_box);
                LOGGER.info("Coordinates added to filter query: {}",
                        bounding_box[0][0] + " " + bounding_box[0][1] + " " + bounding_box[1][0] + " " + bounding_box[1][1]);
            }
            if (have_keyword_filter) {
                filterQuery.track(splitKeywords);
                LOGGER.info("Keywords added to filter query: {}", Arrays.toString(splitKeywords));
            }
            twitterStream.filter(filterQuery);
            LOGGER.info("Filter Query created: {}", filterQuery.toString());
        }

        LOGGER.info("Twitter source {} started.", getName());

        super.start();
    }

    @Override
    public synchronized void stop() {
        LOGGER.info("Twitter source {} stopping...", getName());
        twitterStream.shutdown();
        super.stop();
        LOGGER.info("Twitter source {} stopped.", getName());
    }


    private String getStringJSONTweet(Status status) {
        String username = status.getUser().getScreenName();
        LOGGER.info("username: '" + username + "'");
        GeoLocation statusGeoLocation = status.getGeoLocation();
        if (statusGeoLocation != null)
        {
            LOGGER.info("geolocation: '" +
                    statusGeoLocation.getLatitude() + ", " + statusGeoLocation.getLongitude() + "'");
        }
        else {
            LOGGER.info("geolocation: null");
        }

        String jsonTweet = DataObjectFactory.getRawJSON(status);
        totalTextIndexed += jsonTweet.length();

        return jsonTweet;
    }


    private void logStats() {
        double mbIndexed = totalTextIndexed / (1024 * 1024.0);
        long seconds = (System.currentTimeMillis() - startTime) / 1000;
        seconds = Math.max(seconds, 1);
        LOGGER.info(String.format("Total docs indexed: %s, total skipped docs: %s",
                numFormatter.format(documentCount), numFormatter.format(skippedDocs)));
        LOGGER.info(String.format("    %s docs/second",
                numFormatter.format(documentCount / seconds)));
        LOGGER.info(String.format("Run took %s seconds and processed:",
                numFormatter.format(seconds)));
        LOGGER.info(String.format("    %s MB/sec sent to index",
                numFormatter.format(((float) totalTextIndexed / (1024 * 1024)) / seconds)));
        LOGGER.info(String.format("    %s MB text sent to index",
                numFormatter.format(mbIndexed)));
        LOGGER.info(String.format("There were %s exceptions ignored: ",
                numFormatter.format(exceptionCount)));
    }


}