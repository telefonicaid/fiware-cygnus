# Connecting Orion LD Context Broker and Cygnus
Cygnus takes advantage of the subscription-notification mechanism of [Orion-LD Context Broker](https://github.com/FIWARE/context.Orion-LD). Specifically, Cygnus needs to be notified each time certain entity's attributes change, and in order to do that, Cygnus must subscribe to those entity's attribute changes.

You can make a subscription to Orion on behalf of Cygnus by using a `curl` command or any other REST client. In the following example, we assume both Orion and Cygnus run in localhost, and Cygnus is listening in the TCP/5050 port:

```
curl -L -X POST 'http://localhost:1026/ngsi-ld/v1/subscriptions/' \
-H 'Content-Type: application/ld+json' \
--data-raw '{
  "description": "Notify Cygnus of low stock in Store 001",
  "type": "Subscription",
  "entities": [{"type": "Shelf"}],
  "watchedAttributes": ["numberOfItems"],
  "q": "numberOfItems<10;locatedIn==urn:ngsi-ld:Building:store001",
  "notification": {
    "attributes": ["numberOfItems", "stocks", "locatedIn"],
    "format": "keyValues",
    "endpoint": {
      "uri": "http://cygnus-ld:5050/notify",
      "accept": "application/json"
    }
  },
   "@context": "https://fiware.github.io/tutorials.Step-by-Step/tutorials-context.jsonld"
}'
```

Which means: low-stock-store001 is fired when the Products on the shelves within Store001 are getting less than 10 
