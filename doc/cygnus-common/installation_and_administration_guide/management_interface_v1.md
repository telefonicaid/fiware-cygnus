##<a name="section14"></a> PUT `/v1/admin/log/appenders`

Puts an appender in a running Cygnus. If parameterised with `transient=true` (or omitting this parameter) the appender is put on Cygnus, if `transient=false` is put on the file.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/log/appenders?transient=<transient_value>" -d
'{
	"appender": {
		"name":".....",
		"class":"....."
	  },
	  "pattern": {
		"layout":".....",
		"ConversionPattern":"....."  
	  }
  }'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '....' put."}}
```

When the appender exist and is updated:
```
{"success":"true","result":{"Appender '....' updated succesfully"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section15"></a> POST `/v1/admin/log/appenders`

Posts an appender in a running Cygnus. If parameterised with `transient=true` (or omitting this parameter) the appender is posted on Cygnus, if `transient=false` is posted on the file.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>" -d
'{
	"appender": {
		"name":".....",
		"class":"....."
	  },
	  "pattern": {
		"layout":".....",
		"ConversionPattern":"....."  
	 }
  }'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '.....' posted."}}
```

When the appender exist and is updated:
```
{"success":"false","result":{"Appender '.....' already exist"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section16"></a> PUT `/v1/admin/log/loggers`

Puts a logger in a running Cygnus. If parameterised with `transient=true` (or omitting this parameter) the logger is put on Cygnus, if `transient=false` is put on the file.

```
PUT "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>" -d
'{
	"logger": {
		"name":".....",
		"level":"....."
	}
}'
```

Responses:

When a new appender is put:
```
{"success":"true","result":{"Appender '....' put."}}
```

When the appender exist and is updated:
```
{"success":"true","result":{"Appender '....' updated succesfully"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)

##<a name="section17"></a> POST `/v1/admin/log/loggers`

Posts an logger on a running Cygnus. If parameterised with `transient=true` (or omitting this parameter) the appender is posted on Cygnus, if `transient=false` is posted on the file.

```
POST "http://<cygnus_host>:<management_port>/v1/admin/log/loggers?transient=<transient_value>" -d
'{
	"logger": {
		"name":".....",
		"level":"....."
	}
}'
```

Responses:

When a new logger is put:
```
{"success":"true","result":{"Logger '.....' posted."}}
```

When the logger exist and is updated:
```
{"success":"false","result":{"Logger '.....' already exist"}
```

When an invalid `transient` parameter is given:
```
{"success":"false","result":{"Invalid 'transient' parameter"}}
```

[Top](#top)
