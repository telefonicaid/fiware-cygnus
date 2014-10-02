# Maven installation attributes
default[:maven][:path] = "/opt"
default[:maven][:version] = "3.2.1"

# Flume installation attributes
default[:flume][:path] = "/opt"
default[:flume][:version] = "1.4.0"

# Cygnus installation attributes
default[:cygnus][:version] = "0.1"

# Cygnus configuration attributes
# all versions
default["cygnus"]["http_source"]["port"] = "5050"
# only in 0.1, 0.2 and 0.2.1 {
default[:cygnus][:http_source][:orion_version] = "0\.10\.*"
# from 0.3
default[:cygnus][:http_source][:default_organization] = "defaultOrg"

# all versions {
default[:cygnus][:hdfs_sink][:host] = "x.y.z.w"
default[:cygnus][:hdfs_sink][:port] = "14000"
default[:cygnus][:hdfs_sink][:username] = "opendata"
default[:cygnus][:hdfs_sink][:hdfs_api] = "httpfs"
# } only in 0.1, 0.2 and 0.2.1 {
default[:cygnus][:hdfs_sink][:dataset] = "path/to/data"
# } from 0.3 {
default[:cygnus][:hdfs_sink][:password] = "xxxxxxxxxx"
default[:cygnus][:hdfs_sink][:persistence_mode] = "row"
default[:cygnus][:hdfs_sink][:naming_prefix] = ""
default[:cygnus][:hdfs_sink][:hive_port] = "10000"
# }

# from 0.2 {
default[:cygnus][:ckan_sink][:api_key] = "xxxxxxxxxx"
default[:cygnus][:ckan_sink][:host] = "x.y.z.w"
default[:cygnus][:ckan_sink][:port] = "80"
default[:cygnus][:ckan_sink][:dataset] = "defaultDataset"
# } from 0.3 {
default[:cygnus][:ckan_sink][:persistence_mode] = "row"
default[:cygnus][:ckan_sink][:orion_url] = "http://orion/defaultDataset"
# }

# from 0.2 {
default[:cygnus][:mysql_sink][:host] = "x.y.z.w"
default[:cygnus][:mysql_sink][:port] = "3306"
default[:cygnus][:mysql_sink][:username] = "root"
default[:cygnus][:mysql_sink][:password] = "xxxxxxxxxx"
default[:cygnus][:mysql_sink][:persistence_mode] = "row"
# } from 0.3 {
default[:cygnus][:mysql_sink][:naming_prefix] = ""
# }

# all versions {
default[:cygnus][:hdfs_channel][:capacity] = 1000
default[:cygnus][:hdfs_channel][:transaction_capacity] = 100
# }

# from 0.2 {
default[:cygnus][:ckan_channel][:capacity] = 1000
default[:cygnus][:ckan_channel][:transaction_capacity] = 100
# }

# from 0.2 {
default[:cygnus][:mysql_channel][:capacity] = 1000
default[:cygnus][:mysql_channel][:transaction_capacity] = 100
# }
