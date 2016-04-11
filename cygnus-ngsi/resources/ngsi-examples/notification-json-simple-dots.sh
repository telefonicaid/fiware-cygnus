URL=$1

if [ "$2" != "" ]
then
   SERVICE=$2
else
   SERVICE=def_serv
fi

if [ "$3" != "" ]
then
   SERVICE_PATH=$3
else
   SERVICE_PATH=def_serv_path
fi

curl $URL -v -s -S --header 'Content-Type: application/json' --header 'Accept: application/json' --header 'User-Agent: orion/0.10.0' --header "Fiware-Service: $SERVICE" --header "Fiware-ServicePath: $SERVICE_PATH" -d @- <<EOF
{
  "subscriptionId" : "51c0ac9ed714fb3b37d7d5a8",
  "originator" : "localhost",
  "contextResponses" : [
    {
      "contextElement" : {
        "attributes" : [
          {
            "name" : "ORL.SOU.DH.SSTA10.T.HVAC.HeatLoad",
            "type" : "centigrade",
            "value" : "10.673299789428711",
            "metadatas": [{"name":"dofTimestamp","type":"ms","value":"2016-02-08T23:00:00.000Z"},{"name":"tag","type":"text","value":"ORL.SOU.DH.SSTA10.T.HVAC.HeatLoad"},{"name":"description","type":"text","value":"Electrical heat load"},{"name":"quality","type":"0:GOOD, +0:ERROR","value":"10813440"},{"name":"max","type":"max","value":"null"},{"name":"min","type":"min","value":"null"},{"name":"lcl","type":"lcl","value":"null"},{"name":"ucl","type":"ucl","value":"null"}]
          }
        ],
        "type" : "ETS",
        "isPattern" : "false",
        "id" : "ORL.SOU.DH.SSTA10"
      },
      "statusCode" : {
        "code" : "200",
        "reasonPhrase" : "OK"
      }
    }
  ]
}
EOF
