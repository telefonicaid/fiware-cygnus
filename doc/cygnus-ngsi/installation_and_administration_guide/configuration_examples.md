#<a name="top"></a>Cygnus configuration examples
Content:

* [Single source, single storage (basic configuration)](#section1)
* [Single source, multiple storages](#section2)
* [Single source, single storage, parallel sinking](#section3)
* [Single source, multiple storages, parallel sinking](#section4)
* [Multiple sources](#section5)
* [Using interceptors](#section6)

##<a name="section1"></a>Single source, single storage (basic configuration)
To be done

[Top](#top)

##<a name="section2"></a>Single source, multiple storages
To be done

[Top](#top)

##<a name="section3"></a>Single source, single storage, parallel sinking
To be done

[Top](#top)

##<a name="section4"></a>Single source, multiple storages, parallel sinking
To be done

[Top](#top)

##<a name="section5"></a>Multiple sources 
To be done

[Top](#top)

##<a name="section6"></a>Using interceptors
Interceptors are components of the Flume agent architecture. Typically, such an agent is based on a source dealing with the input, a sink dealing with the output and a channel communicating them. The source processes the input, producing Flume events (an object based on a set of headers and a byte-based body) that are put in the channel; then the sink consumes the events by getting them from the channel. This basic architecture may be enriched by the addition of Interceptors, a chained sequence of Flume events preprocessors that <i>intercept</i> the events before they are put into the channel and performing one of these operations:

* Drop the event.
* Modify an existent header of the Flume event.
* Add a new header to the Flume event.

Interceptors should never modify the body part. Once an event is preprocessed, it is put in the channel as usual.

As can be seen, this mechanism allows for very useful ways of enriching the basic Flume events a certain Flume source may generate. Let's see how Cygnus makes use of this concept in order to add certain information to the Flume events created from the Orion notifications.

[Top](#top)
