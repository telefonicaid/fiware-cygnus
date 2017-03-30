# <a name="top"></a>Twitter Source
Content:

* [Functionality](#section1)
    * [Mapping Twitter events to flume events](#section1.1)
* [Administration guide](#section2)
    * [Configuration](#section2.1)
* [Programmers guide](#section3)
    * [`TwitterSource` class](#section3.1)

## <a name="section1"></a>Functionality
`com.telefonica.iot.cygnus.sources.TwitterSource`, or simply `TwitterSource` is a source designed to collect data from [Twitter] (https://twitter.com).

Tweets are always transformed into internal Flume events at `TwitterSource`. In the end, the information within these Flume events must be mapped into specific data structures at the corresponding sinks.

Next sections will explain this in detail.

[Top](#top)

### <a name="section1.1"></a>Mapping Twitter events to flume events
Received Twitter events are transformed into Flume events (specifically `TwitterEvent`), independently of the final backend where it is persisted.

The body of a flume TwitterEvent is the representation of a tweet in JSON format. Once translated, the data (now, as a Flume event) is put into the internal channels for future consumption (see next section).

[Top](#top)

## <a name="section2"></a>Administration guide
### <a name="section2.1"></a>Configuration
`TwitterSource` is configured through the following parameters that are defined in the configuration file `agent_<id>.conf`.

The name of the source:
`cygnus-twitter.sources = twitter-source`

In order to perform the Twitter query, the most relevant parameters are: the source, the keywords, the coordinates, and the credentials to connect with Twitter.

The source:
`cygnus-twitter.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource`

The keyworks (hashtags) that are used in the twitter query to filter tweets with a specific keyword(s):

`cygnus-twitter.sources.twitter-source.keywords = keyword1, keyword2, keyword3`

The coordinates to specify the spatial area where the source will collect geo-located tweets. The coordinates will be used in the twitter query:
```
cygnus-twitter.sources.twitter-source.south_west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south_west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north_east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north_east_longitude = -0.3124204
```

These coordinates are used to define a rectangle filter where tweets have been geo-located. Only tweets inside this rectangle are stored.
```
             -------------- north-east
            |                  |
            |                  |
            |                  |
       south-west ------------   
```

The credentials used to connect with Twitter API. Credentials can be obtained [here](https://dev.twitter.com/oauth/overview/application-owner-access-tokens):
```
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxx
```

A configuration example could be:
```
#=============================================
# source configuration
# source class, must not be changed
cygnus-twitter.sources.http-source.type = org.telefonica.iot.cygnus.sources.TwitterSource
# keywords
# cygnus-twitter.sources.twitter-source.keywords = keyword1, keyword2, keyword3
# Coordinates for filter query
cygnus-twitter.sources.twitter-source.south_west_latitude = 39.4247692
cygnus-twitter.sources.twitter-source.south_west_longitude = -0.4315448
cygnus-twitter.sources.twitter-source.north_east_latitude = 39.5038788
cygnus-twitter.sources.twitter-source.north_east_longitude = -0.3124204
cygnus-twitter.sources.twitter-source.consumerKey = xxxxxxxx
cygnus-twitter.sources.twitter-source.consumerSecret = xxxxxxxx
cygnus-twitter.sources.twitter-source.accessToken = xxxxxxxx
cygnus-twitter.sources.twitter-source.accessTokenSecret = xxxxxxxx
```

## <a name="section3"></a>Programmers guide
### <a name="section3.1"></a>`TwitterSource` class
`TwitterSource` has two main methods that are described in the following paragraphs.

`public void configure(Context context)`

This method reads the configuration file parameters related to the source (i.e., consumerKey, consumerSecret, accessToken, accessTokenSecret, south_west_latitude, south_west_longitude, north_east_latitude, north_east_longitude, and keywords) and creates an object `TwitterStream` from the [Twitter4j](http://twitter4j.org/en/index.html) library to be ready to collect data from Twitter, and a `ChannelProcessor` object to be ready to send data. 

`public synchronized void start()`

The start method creates a `StatusListener` object that collects status objects that contains the information of a tweet in an asynchronous way. Each tweet is processed to generate a string with a JSON format. This string is used to create a Flume event. Events are added to a list of events that will be sent through a `ChannelProcessor`.

[Top](#top)

