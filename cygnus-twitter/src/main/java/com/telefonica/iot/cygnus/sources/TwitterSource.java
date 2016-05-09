
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
    private double[][] coordinates;
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

    double[][] getCoordinates() {
        return coordinates;
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


        String top_left_latitude;
        String top_left_longitude;

        String bottom_right_latitude;
        String bottom_right_longitude;

        String keywords;


        //First corrdinate
        top_left_latitude = context.getString("top_left_latitude");
        top_left_longitude = context.getString("top_left_longitude");
        LOGGER.info("Top-left coordinate: '" + top_left_latitude + " " + top_left_longitude + "'");

        //Second coordinate
        bottom_right_latitude = context.getString("bottom_right_latitude");
        bottom_right_longitude = context.getString("bottom_right_longitude");
        LOGGER.info("Bottom-right coordinate: '" + bottom_right_latitude + " " + bottom_right_longitude + "'");

        keywords = context.getString("keywords");
        LOGGER.info("Keywords:            '" + keywords + "'");


        if (top_left_latitude != null && top_left_longitude != null && bottom_right_latitude != null && bottom_right_longitude != null) {
            double latitude1 = Double.parseDouble(top_left_latitude);
            double longitude1 = Double.parseDouble(top_left_longitude);

            double latitude2 = Double.parseDouble(bottom_right_latitude);
            double longitude2 = Double.parseDouble(bottom_right_longitude);

            coordinates = new double[][]{{longitude1, latitude1}, {longitude2, latitude2}};

            LOGGER.info("Coordinates:         '" + coordinates[0][0] + " " + coordinates[0][1] + " " + coordinates[1][0] + " " + coordinates[1][1] + "'");
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
                filterQuery.locations(coordinates);
                LOGGER.info("\nCoordinates added to filter query: {}\n", coordinates);
            }
            if (have_keyword_filter) {
                filterQuery.track(splitKeywords);
                LOGGER.info("\nKeywords added to filter query: {}\n", splitKeywords);
            }
            twitterStream.filter(filterQuery);
        }


        twitterStream.sample();

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
        User user = status.getUser();
        String username = status.getUser().getScreenName();
        LOGGER.info("username: '" + username + "'");
        String profileLocation = user.getLocation();
        LOGGER.info("proflocation: '" + profileLocation + "'");

        String content = status.getText();
        LOGGER.info(" \n" + content + " \n");

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