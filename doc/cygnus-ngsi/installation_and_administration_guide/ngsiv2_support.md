#NGSIv2 support
Currently, cygnus-ngsi does not support [NGSIv2](http://telefonicaid.github.io/fiware-orion/api/v2/stable/). Only notification format within NGSIv1 is accepted by `NGSIRestHandler`, i.e. something like:

```
POST /v1/subscribeContext
...
{
  "entities": [
    {
      "type": "car",
      "isPattern": "false",
      "id": "car1"
    }
  ],
  "attributes": [
    "speed",
    "oil_level"
  ],
  "reference": "http://localhost:5050/notify",
  "duration": "P1M",
  "notifyConditions": [
    {
      "type": "ONCHANGE",
      "condValues": [
        "speed"
      ]
    }
  ],
  "throttling": "PT1S"
}
```

Nevertheless, this does not mean NGSIv2 cannot be used in an integration between Orion Context Broker and Cygnus. Specifically, you can subscribe both in NGSIv1 and NGSIv2, either directly using Orion subscriptions API, either using Cygnus subscriptions API (which forwards the requests to the former one). The trick when subscribing using NGSIv2 is to set the `attrsFormat` field's value to `legacy`, as described in [Orion's documentation](http://fiware-orion.readthedocs.io/en/master/user/v1_v2_coexistence/index.html) (<i>NGSIv1 notification with NGSIv2 subscriptions section</i>). I.e. something like:

```
POST /v2/subscriptions
...
{
  "subject": {
    "entities": [
      {
        "id": "car1",
        "type": "car"
      }
    ]
  },
  "condition": {
    "attrs": [ "speed", "oil_level" ]
  },
  "notification": {
    "http": {
      "url": "http://localhost:5050/notify"
    },
    "attrs": [ "speed", "oil_level" ],
    "attrsformat": "legacy"
  },
  "expires": "2020-12-31T00:00:00.000Z",
  "throttling": 5
} 
```