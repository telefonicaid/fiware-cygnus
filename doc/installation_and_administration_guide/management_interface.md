#Management interface
From Cygnus 0.5 there is a REST-based management interface for administration purposes. Current available operations are:

<b>Get the version of the running software, including the last Git commit</b>:

    GET http://host:management_port/version

    {"version":"0.5_SNAPSHOT.8a6c07054da894fc37ef30480cb091333e2fccfa"}
