#<a name="top"></a>Adding new sinks development guide

Content:

* [Introduction](#section1)
* [Before starting](#section2)
    * [Contributing to Cygnus](#section2.1)
    * [Coding style](#section2.2)
* [New sink development](#section3)
    * [`OrionSink` class](#section3.1)
    * [Sink configuration](#section3.2)
    * [Naming and placing the new sink](#section3.3)
    * [Backend convenience classes](#section3.4)
* [Reporting issues and contact information](#section4)

##<a name="section1"></a>Introduction
Cygnus allows for Orion context data persistence in certain storages by means of Flume sinks. As long as the current collection of sinks could be limited for your purposes, you can add your own sinks regarding a persistence technology of your choice and become an official Cygnus contributor!

This document tries to guide you on the development of such alternative sinks, by giving you guidelines about how to write the sink code, but also how the different classes must be called, the accepted coding style, etc.

[Top](#top)

##<a name="section2"></a>Before starting

###<a name="section2.1"></a>Contributing to Cygnus
You can contribute to Cygnus (open sourced) as usual:

1. Fork our Github [Cygnus](https://github.com/telefonicaid/fiware-cygnus) repository (you will need an account on Github).
2. Create a new branch where to code your fix/addon.
3. Submit a pull request to us!  

We will not merge new code in the Cygnus repository coming from a different path.

[Top](#top)

###<a name="section2.2"></a>Coding style 

Please, add the `fiware-cygnus/telefonica_checkstyle.xml` to you IDE as check style configuration. This XML file contains all the coding style rules accepted by Telefónica.

We will not merge new code in the Cygnus repository if such coding style is not met.

[Top](#top)

##<a name="section3"></a>New sink development

###<a name="section3.1"></a>`OrionSink` class
`OrionSink` is the base class all the Cygnus sinks extend. This class governs the consumption of the Flume events put by `OrionRestHandler` in the sink channel, taking them from the channel and calls to the persistence abstract method which in final term is the unique method that must be implemented by the extending class. All the logic about starting and stopping the sink, beginning, committing and closing Flume transactions and many other features is already there, thus you will not have to deal with it.

You find this class at the following path:

    fiware-cygnus/src/main/java/com/telefonica/iot/cygnus/sinks/OrionSink.java

`OrionSink`, on its side, extends `AbstractSink` from the Flume API; this class is the one providing all the necessary methods, as previously said. As can be seen, all of them are already implemented (`start`, `stop`, etc) or overridden (`process`). Only showing relevant parts in pseudo-code:

    public abstract class OrionSink extends AbstractSink implements Configurable {
    
    	Batch batch;
    	
		/**
		 * Constructor
		 */ 
		public OrionSink() {
			super();
			...
		} // OrionSink

		@Override
		public Status process() throws EventDeliveryException {
			Channel ch = getChannel();
			Transaction txn = ch.getTransaction();
			txn.begin();
			
			for (int i = 0; i < batchSize; i++) {
				Event event = ch.take();
				NotifyContextRequest notification = parseEventBody(event);
				accumulateInBatch(event.getHeaders(), notification);
			} // for
			
			persistBatch(batch);
			txn.commit();
			return Status.READY;
		} // process

    	/**
     	 * This is the method the classes extending this class must implement when dealing with a batch of events to be
     	 * persisted.
     	 * @param batch
     	 * @throws Exception
        */
    	abstract void persistBatch(Batch batch) throws Exception;
    
    } // OrionSink   

The `process` method is responsible for getting the channel, initiating a Flume transaction, taking as many events from the channel as necessary to build a `Batch` object and processing it by calling the `persistBatch` method. Such a `persistBatch` method is the only piece of code a developer must create according to the logic of his/her sink.

Please notice that the `process` method handles all the possible errors that may occur during a Flume transaction by catching exceptions, especially those thrown by the abstract `persistBatch` method. There exists a collection of Cygnus-related exceptions whose usage is mandatory located at:

    fiware-cygnus/src/main/java/com/telefonica/iot/cygnus/errors/

[Top](#top)

###<a name="section3.2"></a>Sink configuration
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

[Top](#top)

###<a name="section3.3"></a>Naming and placing the new sink
New sink classes must be called `Orion<technology>Sink`, being <i>technology</i> the name of the persistence backend. Examples are `OrionHDFSSink`, `OrionCKANSink` or `OrionMySQLSink` (by the way, these three exist already).

The path where the new sink is to be placed:

    fiware-cygnus/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/sinks
    
[Top](#top)
 
###<a name="section3.4"></a>Backend convenience classes

Sometimes all the necessary logic to persist the notified context data cannot be coded in the `persist` abstract method. In this case, you may want to create a backend class or set of classes wrapping the detailed interactions with the final backend. These classes must be placed at:

    fiware-cygnus/src/main/java/es/tid/fiware/fiwareconnectors/cygnus/backends/<my_backend_classes>/

[Top](#top)

##<a name="section4"></a>Reporting issues and contact information
There are several channels suited for reporting issues and asking for doubts in general. Each one depends on the nature of the question:

* Use [stackoverflow.com](http://stackoverflow.com) for specific questions about this software. Typically, these will be related to installation problems, errors and bugs. Development questions when forking the code are welcome as well. Use the `fiware-cygnus` tag.
* Use [ask.fiware.org](https://ask.fiware.org/questions/) for general questions about FIWARE, e.g. how many cities are using FIWARE, how can I join the accelarator program, etc. Even for general questions about this software, for instance, use cases or architectures you want to discuss.
* Personal email:
    * [francisco.romerobueno@telefonica.com](mailto:francisco.romerobueno@telefonica.com) **[Main contributor]**
    * [fermin.galanmarquez@telefonica.com](mailto:fermin.galanmarquez@telefonica.com) **[Contributor]**
    * [german.torodelvalle@telefonica.com](german.torodelvalle@telefonica.com) **[Contributor]**
    * [ivan.ariasleon@telefonica.com](mailto:ivan.ariasleon@telefonica.com) **[Quality Assurance]**

**NOTE**: Please try to avoid personaly emailing the contributors unless they ask for it. In fact, if you send a private email you will probably receive an automatic response enforcing you to use [stackoverflow.com](stackoverflow.com) or [ask.fiware.org](https://ask.fiware.org/questions/). This is because using the mentioned methods will create a public database of knowledge that can be useful for future users; private email is just private and cannot be shared.

[Top](#top)
