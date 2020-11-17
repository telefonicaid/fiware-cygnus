curl -X POST 'http://localhost:5050/notify' -H 'Content-Type: application/ld+json' -H 'Fiware-service: openiot2' -d @- <<EOF
{
  "id": "urn:ngsi-ld:Notification:352334523452",
  "type": "Notification",
  "subscriptionId": "urn:ngsi-ld:Subscription:5e6257789f300bcd70b7b6e",
  "data": [
    {
        "id": "urn:ngsi:ld:OffStreetParking:Downtown1",
        "type": "OffStreetParking",
        "@context": [
        "http://example.org/ngsi-ld/parking.jsonld",
        "https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context.jsonld"],
        "name": {
        "type": "Property",
           "value": "Downtown One"
        },
        "availableSpotNumber": {
            "type": "Property",
            "value": 122,
            "observedAt": "2017-07-29T12:05:02Z",
            "reliability": {
                "type": "Property",
                "value": 0.7
            },
            "providedBy": {
                "type": "Relationship",
                "object": "urn:ngsi-ld:Camera:C1"
            }
        },
        "totalSpotNumber": {
            "type": "Property",
            "value": 200
        },
            "location": {
            "type": "GeoProperty",
            "value": {
                "type": "MultiPoint",
                "coordinates": [[ 1.01, 4.003 ], [ 2.01, 4.003 ]]
            }
        }
    }
  ]
}
EOF