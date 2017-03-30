# Connecting Orion Context Broker and Cygnus
Cygnus takes advantage of the subscription-notification mechanism of [Orion Context Broker](https://github.com/telefonicaid/fiware-orion). Specifically, Cygnus needs to be notified each time certain entity's attributes change, and in order to do that, Cygnus must subscribe to those entity's attribute changes.

You can make a subscription to Orion on behalf of Cygnus by using a `curl` command or any other REST client. In the following example, we assume both Orion and Cygnus run in localhost, and Cygnus is listening in the TCP/5050 port:

```
(curl localhost:1026/v1/subscribeContext -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'Fiware-Service: vehicles' --header 'Fiware-ServicePath: /4wheels' -d @- | python -mjson.tool) <<EOF
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
EOF
```

Which means: <i>Each time the the 'car1' entity, of type 'car', which is registered under the service/tenant 'vehicles', subservice '/4wheels', changes its value of 'speed' then send a notification to http://localhost:5050/notify (where Cygnus will be listening) with the 'speed' and 'oil_level' values. This subscription will have a duration of one month, and please, do not send me notifications more than once per second.</i>
