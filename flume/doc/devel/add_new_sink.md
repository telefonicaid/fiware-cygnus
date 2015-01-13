# Adding new sinks development guide

Cygnus allows for Orion context data persistence in certain storages by means of Flume sinks. As long as the current collection of sinks could be limited for your purposes, you can add your own sinks regarding a persistence technology of your choice and become an official Cygnus contributor!

This document tries to guide you on the development of such alternative sinks, by giving you guidelines about how to write the sink code, but also how the different classes must be called, the accepted coding style, etc. 

## Before starting

### Contributing to Cygnus

You can contribute to Cygnus (open sourced) as usual:

1. Fork our Github [Cygnus](https://github.com/telefonicaid/fiware-connectors) repository (you will need an account on Github).
2. Create a new branch where to code your fix/addon.
3. Submit a pull request to us!  

We will not merge new code in the Cygnus repository coming from a different path.

### Coding style 

Please, add the `fiware-connectors/flume/telefonica_checkstyle.xml` to you IDE as check style configuration. This XML file contains all the coding style rules accepted by Telefónica.

We will not merge new code in the Cygnus repository if such coding style is not met.

## New sink development

### `OrionSink` class
`OrionSink` is the base class all the Cygnus sinks extend. This class governs the consumption of the Flume events put by `OrionRestHandler` in the sink channel, taking them from the channel and calls to the persistence abstract method which in final term is the unique method that must be implemented by the extending class. All the logic about starting and stopping the sink, beginning, committing and closing Flume transactions and many other features is already there, thus you will not have to deal with it.

You find this class at the following path:

    fiware-connectors/flume/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/sinks/OrionSink.java

`OrionSink`, on its side, extends `AbstractSink` from the Flume API; this class is the one providing all the necessary methods, as previously said. As can be seen, all of them are already implemented (`start`, `stop`, etc) or overridden (`process`). Only showing relevant parts:

    public abstract class OrionSink extends AbstractSink implements Configurable {
		/**
		 * Constructor
		 */ 
		public OrionSink() {
			super();
			...
		} // OrionSink

		@Override
		public Status process() throws EventDeliveryException {
			...
			ch = getChannel();
			txn = ch.getTransaction();
			txn.begin();
			event = ch.take();
			persist(event);
			txn.commit();
			status = Status.READY;
			...
		} // process

		/**
		 * Given an event, it is preprocessed before it is persisted. Depending on the content type, it is appropriately
		 * parsed (Json or XML) in order to obtain a NotifyContextRequest instance.
		 *
		 * @param event A Flume event containing the data to be persisted and certain metadata (headers).
		 * @throws Exception
		 */
		private void persist(Event event) throws Exception {
			...
		} // persist

		/**
		 * This is the method the classes extending this class must implement when dealing with persistence.
		 * @param eventHeader Event headers
		 * @param notification Notification object (already parsed) regarding the event body
		 * @throws Exception
		 */
		abstract void persist(Map<String, String> eventHeaders, NotifyContextRequest notification) throws Exception;
    } // OrionSink   

The `process` method is responsible for getting the channel, initiating a Flume transaction, taking an event and processing it by calling the `persist` method. The abstract version of the `persist` method is the only piece of code a developer must create according to the logic of his/her sink.

Please notice that the `process` method handles all the possible errors that may occur during a Flume transaction by catching exceptions, especially those thrown by the abstract `persist` method. There exists a collection of Cygnus-related exceptions whose usage is mandatory located at:

    fiware-connectors/flume/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/errors/

### Sink configuration

In addition to extending `AbstractSink`, `OrionSink` implements the `Configure` interface which allows for parameterizing the new sink from the general Cygnus configuration file.

Configuration parameters must follow this schema:

    cygnusagent.sources = http-source
    cygnusagent.sinks = <sink_name> <other_sink_names>
    cygnusagent.channels = <sink_channel_name> <other_sink_channel_names>
    ...
    cygnusagent.sinks.<sink_name>.<parameter_1_name> = <parameter_1_value>
    cygnusagent.sinks.<sink_name>.<parameter_2_name> = <parameter_2_value>
	...
	cygnusagent.sinks.<sink_name>.<parameter_N_name> = <parameter_N_value>
	...

### Flume events structure

Orion notifications are sent by Orion to the default Flume HTTP source, which relies on `OrionRestHandler` for checking its validity (that it is a POST request, that the target is 'notify' and that the headers are OK), detecting the content type (that it is in Json format), extracting the data (the Json part) and finally creating a Flume event to be put in the channel:

    event={
		body=json_data,
		headers={
			content-type=application/json,
			fiware-service=my_company_name,
			fiware-servicepath=workingrooms_floor4,
			timestamp=1402409899391,
			transactionId=asdfasdfsdfa,
			ttl=10,
			destination=Room1-Room
		}
	}

<b>NOTE: The above is an <i>object representation</i>, not Json data nor any other data format.</b>

Let's have a look on the Flume event headers:

* The <b>content-type</b> header is a replica of the HTTP header. It is needed for the different sinks to know how to parse the event body. In this case it is JSON.
* Note that Orion can include a `Fiware-Service` HTTP header specifying the tenant/organization associated to the notification, which is added to the event headers as well (as `fiware-service`). Since version 0.3, Cygnus is able to support this header, although the actual processing of such tenant/organization depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service specified in the `default_service` configuration property. Please observe that the notified `fiware-service` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* Orion can notify another HTTP header, `Fiware-ServicePath` specifying a subservice within a tenant/organization, which is added to the event headers as well (as `fiware-servicepath`). Since version 0.6, Cygnus is able to support this header, although the actual processing of such subservice depends on the particular sink. If the notification doesn't include this header, then Cygnus will use the default service path specified in the `default_service_path` configuration property. Please observe that the notified `fiware-servicepath` is transformed following the rules described at [`doc/design/naming_conventions.md`](doc/design/naming_conventions.md).
* The notification reception time is included in the list of headers (as <b>timestamp</b>) for timestamping purposes in the different sinks. It is added by a native interceptor. See the [doc/design/interceptors.md](doc/design/interceptors.md) document for more details.
* The <b>transactionId</b> identifies a complete Cygnus transaction, starting at the source when the context data is notified, and finishing in the sink, where such data is finally persisted.
* The time-to-live (or <b>ttl</b>) specifies the number of re-injection retries in the channel when something goes wrong while persisting the data. This re-injection mechanism is part of the reliability features of Flume.
* The <b>destination</b> headers is used to identify the persistence element within the used storage, i.e. a file in HDFS, a MySQL table or a CKAN resource. This is added by a custom interceptor called `DestinationExtractor` added to the Flume's suite. See the <i>doc/design/interceptors</i> document for more details.

### Naming and placing the new sink

New sink classes must be called `Orion<technology>Sink`, being <i>technology</i> the name of the persistence backend. Examples are `OrionHDFSSink`, `OrionCKANSink` or `OrionMySQLSink` (by the way, these three exist already).

The path where the new sink is to be placed:

    fiware-connectors/flume/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/sinks
 
### Backend convenience classes

Sometimes all the necessary logic to persist the notified context data cannot be coded in the `persist` abstract method. In this case, you may want to create a backend class or set of classes wrapping the detailed interactions with the final backend. These classes must be placed at:

    fiware-connectors/flume/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/backends/<my_backend_classes>/

## Contact information
Francisco Romero Bueno (francisco.romerobueno@telefonica.com)
<br>
Fermín Galán Márquez (fermin.galanmarquez@telefonica.com) 
