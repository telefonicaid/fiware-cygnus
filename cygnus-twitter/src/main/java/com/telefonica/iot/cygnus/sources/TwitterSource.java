/**
 * Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
 * <p>
 * This file is part of fiware-cygnus (FIWARE project).
 * <p>
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 * <p>
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
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

import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class TwitterSource extends AbstractSource
        implements EventDrivenSource, Configurable {

    private TwitterStream twitterStream;
    private List<Event> eventBatch = new ArrayList<Event>();

    private boolean haveFilters = false;
    private boolean haveCoordinateFilter = false;
    private boolean haveKeywordFilter = false;

    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    private double[][] boundingBox;
    private String[] splitKeywords;

    private long documentCount = 0;
    private long startTime = 0;
    private long exceptionCount = 0;
    private long totalTextIndexed = 0;
    private long skippedDocs = 0;
    private long batchEndTime = 0;

    private int maxBatchSize = 1000;
    private int maxBatchDurationMillis = 1000;

    private DecimalFormat numFormatter = new DecimalFormat("###,###.###");

    private static int REPORT_INTERVAL = 100;
    private static int STATS_INTERVAL = REPORT_INTERVAL * 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSource.class);


    String getConsumerKey() {
        return consumerKey;
    }

    String getConsumerSecret() {
        return consumerSecret;
    }

    String getAccessToken() {
        return accessToken;
    }

    String getAccessTokenSecret() {
        return accessTokenSecret;
    }

    double[][] getBoundingBox() {
        return boundingBox;
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

        String southWestLatitude;
        String southWestLongitude;
        String northEastLatitude;
        String northEastLongitude;
        String keywords;

        //Top-left coordinate
        southWestLatitude = context.getString("south_west_latitude");
        southWestLongitude = context.getString("south_west_longitude");
        LOGGER.info("South-West coordinate: '" + southWestLatitude + " " + southWestLongitude + "'");

        //Bottom-right coordinate
        northEastLatitude = context.getString("north_east_latitude");
        northEastLongitude = context.getString("north_east_longitude");
        LOGGER.info("North-East coordinate: '" + northEastLatitude + " " + northEastLongitude + "'");

        keywords = context.getString("keywords");
        LOGGER.info("Keywords:            '" + keywords + "'");

        if (southWestLatitude != null && southWestLongitude != null
                && northEastLatitude != null && northEastLongitude != null) {
            double latitude1 = Double.parseDouble(southWestLatitude);
            double longitude1 = Double.parseDouble(southWestLongitude);

            double latitude2 = Double.parseDouble(northEastLatitude);
            double longitude2 = Double.parseDouble(northEastLongitude);

            boundingBox = new double[][]{
                new double[]{longitude1, latitude1}, // south-west
                new double[]{longitude2, latitude2}  // north-east
            };

            LOGGER.info("Coordinates:         '" + boundingBox[0][0] + " " + boundingBox[0][1]
                    + " " + boundingBox[1][0] + " " + boundingBox[1][1] + "'");
            haveFilters = true;
            haveCoordinateFilter = true;
        }

        if (keywords != null) {
            if (keywords.trim().length() != 0) {
                splitKeywords = keywords.split(",");
                for (int i = 0; i < splitKeywords.length; i++) {
                    splitKeywords[i] = splitKeywords[i].trim();
                }

                LOGGER.info("keywords:            {}", Arrays.toString(splitKeywords));
                haveFilters = true;
                haveKeywordFilter = true;
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

                if (eventBatch.size() >= maxBatchSize || System.currentTimeMillis() >= batchEndTime) {
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


        if (haveFilters) {
            FilterQuery filterQuery = new FilterQuery();

            if (haveCoordinateFilter) {
                filterQuery.locations(boundingBox);
                LOGGER.info("Coordinates added to filter query: {}",
                       boundingBox[0][0] + " " + boundingBox[0][1] + " " + boundingBox[1][0] + " " + boundingBox[1][1]);
            }
            if (haveKeywordFilter) {
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
        String jsonTweet = TwitterObjectFactory.getRawJSON(status);
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
