#<a name="top"></a>Grouping Rules configuration
The file `grouping_rules.conf` can be instantiated from a template given in the Cygnus repository, `conf/grouping_rules.conf.template`.

The rules are writing in Json format. The following Json code is just an example:

```
{
    "grouping_rules": [
        {
            "id": 1,
            "fields": [
                ...
            ],
            "regex": "...",
            "destination": "...",
            "fiware_service_path": "..."
        },
        ...
    ]
}
```

Being:

* <b>id</b>: A unique unsigned integer-based identifier.
* <b>fields</b>: These are the fields that will be concatenated for regular expression matching. The available dictionary of fields for concatenation is "entityId", "entityType" and "servicePath". The order of these fields is important since the concatenation is made from left to right.
* <b>regex</b>: Java-like regular expression to be applied on the concatenated fields. Special characters like '\' must be escaped ('\' is escaped as "\\\\").
* <b>destination</b>: Name of the HDFS file or CKAN resource where the data will be effectively persisted. In the case of MySQL, Mongo and STH Comet this sufixes the table/collection name.
* <b>fiware\_service\_path</b>: New `fiware-servicePath` replacing the notified one. The sinks will translate this into the name of the HDFS folder or CKAN package where the above destination entity will be placed. In the case of MySQL, Mongo and STH Comet this prefixes the table/collection name. It must start with `/` or the whole rule will be discarded.
