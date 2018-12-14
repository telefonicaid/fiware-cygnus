# <a name="top"></a>Flume extensions catalogue
This document details the catalogue of extensions developed for Cygnus on top of [Apache Flume](https://flume.apache.org/).

# Intended audience
The Flume extensions catalogue is a basic piece of documentation for all those FIWARE users using Cygnus. It describes the available extra components added to the Flume technology in order to deal with Twitter-like data.

Software developers may also be interested in this catalogue since it may guide the creation of new components (specially, sinks) for Cygnus/Flume.

[Top](#top)

# Structure of the document
This document describes the Twitter Source and Twitter HDFS sink. `TwitterSource` is a source designed to collect data from Twitter. This document contains an explanation about `TwitterSource` configuration and functionality. `TwitterHDFSSink` sink is currently the only one supported by the cygnus-twitter agent. This document contains an explanation about `TwitterHDFSSink` functionality (including how the information within a Flume event is mapped into the storage data structures), configuration, uses cases and implementation details are given.

[Top](#top)
