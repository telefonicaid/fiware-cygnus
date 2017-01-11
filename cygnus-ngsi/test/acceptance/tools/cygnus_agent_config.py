# -*- coding: utf-8 -*-
#
# Copyright 2015-2017 Telefonica Investigación y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FIWARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
#
# You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
# http://www.gnu.org/licenses/.
#
# For those usages not covered by the GNU Affero General Public License please contact:
# iot_support at tid.es
#
__author__ = 'Iván Arias León (ivan.ariasleon at telefonica dot com)'

# constants
FILENAME_SOURCE        = u'agent.conf.template'
FILENAME_TARGET        = u'agent_%s.conf'

EMPTY                  = u''
COMMAND                = u'command'
CURRENT_PATH           = u'path'
SUDO                   = u'sudo'
SOURCE_PATH            = u'source_path'
TARGET_PATH            = u'target_path'
PATH_DEFAULT           = u'/'
ID                     = u'id'
SINK                   = u'sink'
CHANNEL                = u'channel'
PORT                   = u'port'
DEFAULT_SERVICE        = u'default_service'
DEFAULT_SERVICE_PATH   = u'default_service_path'
TTL                    = u'ttl'
GROUPING_RULES_FILE    = u'grouping_rules_file'
HOST                   = u'host'
HOST_PORT              = u'host_port'
LOCALHOST              = u'localhost'
DATA_MODEL             = u'data_model'
DB_PREFIX              = u'db_prefix'
COLLECTION_PREFIX      = u'collection prefix'
USER                   = u'user'
PASSWORD               = u'password'
API                    = u'api'
API_HTTPFS             = u'httpfs'
API_WEBHDFS            = u'webhdfs'
PERSISTENCE            = u'persistence'
PERSISTENCE_COLUMN     = u'column'
PERSISTENCE_ROW        = u'row'
HIVE_HOST              = u'hive_host'
HIVE_PORT              = u'hive_port'
KRB5_AUTH              = u'krb5_auth'
KRB5_USER              = u'krb5_user'
KRB5_PASSWORD          = u'krb5_password'
KRB5_LOGIN_FILE        = u'krb5_login_file'
KRB5_CONF_FILE         = u'krb5_conf_file'
API_KEY                = u'api_key'
ORION_URL              = u'orion_url'
SSL                    = u'ssl'
CAPACITY               = u'capacity'
TRANSACTION_CAPACITY   = u'transaction_capacity'

# commands list
OPS_LIST = []


class Agent:
    """
    modify agent_<id>.conf file with several parameters:
       - parameters associated to source and each sink
    """
    def __init__(self, **kwargs):
        """
        constructor
        :param source_path: source path where read the cygnus_instance.conf
        :param target_path: target path where store the cygnus_instance.conf
        :param id: id postfix used by instances
        :param sudo:  with superuser privileges (True | False)
        """
        self.source_path = kwargs.get(SOURCE_PATH, PATH_DEFAULT)
        self.target_path = kwargs.get(TARGET_PATH, PATH_DEFAULT)
        self.id          = kwargs.get(ID, EMPTY)
        self.sudo        = kwargs.get(SUDO, False)

    def __append_command(self, command, path, sudo):
        """
        append command lines into a list with its path and if it is necessary the superuser privilege (sudo)
        :param command: command to execute
        :param path: current path where is executed the command
        :param sudo: if it is necessary the superuser privilege (sudo) (True | False)
        """
        OPS_LIST.append({COMMAND:command, CURRENT_PATH: path, SUDO: sudo})

    def append_id (self, **kwargs):
        """
        append a new agent of cygnus
        :param source_path: source path where read the cygnus_instance.conf
        :param target_path: target path where store the cygnus_instance.conf
        :param id: id postfix used by instances
        :return commands list
        """
        global OPS_LIST
        OPS_LIST = []
        self.source_path = kwargs.get(SOURCE_PATH, self.source_path)
        self.target_path = kwargs.get(TARGET_PATH, self.target_path)
        self.id   = kwargs.get(ID, self.id)


        self.name = FILENAME_TARGET % (self.id)
        # copy agent.conf.template in target path with id postfix appended
        self.__append_command('cp -R %s/%s %s/%s' % (self.source_path, FILENAME_SOURCE, self.target_path, self.name), EMPTY, self.sudo)
        # replace all cygnusagent by AGENT_NAME defined in cygnus_instances_<id>.conf
        self.__append_command('sed -i "s/cygnusagent./%s./" %s/%s ' % (self.id, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def source(self, **kwargs):
        """
        parameters values in source configuration
        :param sink: sinks used (hdfs-sink mysql-sink ckan-sink)
        :param channel: channels used (hdfs-channel mysql-channel ckan-channel)
        :param cygnus_port: port used in each instance. It must be different in each instance
        :param default_service: tenant used by default
        :param default_service_path: service path used by default
        :param ttl: Number of channel re-injection retries before a Flume event is definitely discarded (-1 means infinite retries)
        :param grouping_rules_file: Matching table for the destination extractor interceptor, put the right absolute path to the file if necessary
        :return commands list
        """
        self.sink                 = kwargs.get(SINK, EMPTY)
        self.channel              = kwargs.get(CHANNEL, EMPTY)
        self.cygnus_port          = kwargs.get(PORT, "5050")
        self.default_service      = kwargs.get(DEFAULT_SERVICE, "def_serv")
        self.default_service_path = kwargs.get(DEFAULT_SERVICE_PATH, "def_servpath")
        self.ttl                  = kwargs.get(TTL, "10")
        self.grouping_rules_file  = kwargs.get(GROUPING_RULES_FILE, "/usr/cygnus/conf/grouping_rules.conf")
        sed_grouping_rules_file = self.grouping_rules_file.replace("/", "\/")    # replace / to \/ in path that is used in sed command
        self.__append_command('sed -i "s/%s.sinks = .*/%s.sinks = %s/" %s/%s ' % (self.id, self.id, self.sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channels = .*/%s.channels = %s/" %s/%s ' % (self.id, self.id, self.channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.sources\.http-source\.channels = .*/%s.sources\.http-source.channels = %s/" %s/%s ' % (self.id, self.id, self.channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.sources.http-source.port = .*/%s.sources.http-source.port = %s/" %s/%s ' % (self.id, self.id, self.cygnus_port, self.target_path, self.name), self.target_path, self.sudo)
        if self.default_service != EMPTY:
            self.__append_command('sed -i "s/%s.sources.http-source.handler.default_service = .*/%s.sources.http-source.handler.default_service = %s/" %s/%s ' % (self.id, self.id, self.default_service, self.target_path, self.name), self.target_path, self.sudo)
        if self.default_service_path != EMPTY:
            sed_default_service_path = self.default_service_path.replace("/", "\/")    # replace / to \/ in path that is used in sed command
            self.__append_command('sed -i "s/%s.sources.http-source.handler.default_service_path = .*/%s.sources.http-source.handler.default_service_path = %s/" %s/%s ' % (self.id, self.id, sed_default_service_path, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.sources.http-source.handler.events_ttl = .*/%s.sources.http-source.handler.events_ttl = %s/" %s/%s ' % (self.id, self.id, self.ttl, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.sources.http-source.interceptors.de.grouping_rules = .*/%s.sources.http-source.interceptors.de.grouping_rules = %s/" %s/%s ' % (self.id, self.id, sed_grouping_rules_file, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_hdfs_sink(self, **kwargs):
        """
        parameters values in hdfs sink
        :param sink: sinks used (hdfs-sink1, hdfs-sink2,...,hdfs-sinkN)
        :param channel: specific channel(hdfs-channel)
        :param host: Comma-separated list of FQDN/IP address Namenode endpoints, If you are using Kerberos authentication, then the usage of FQDNs instead of IP addresses is mandatory
        :param port: port of the service listening for persistence operations; 14000 for httpfs, 50070 for webhdfs and free choice for inifinty
        :param user: default username allowed to write in HDFS
        :param password: default password for the default username
        :param api: HDFS backend type (webhdfs, httpfs or infinity)
        :param persistence:  how the attributes are stored, either per row either per column (row, column)
        :param hive_host: Hive FQDN/IP address of the Hive server
        :param hive_port:  Hive port for Hive external table provisioning
        :param kbr5_auth: Kerberos-based authentication enabling
        :param krb5_user: Kerberos username
        :param krb5_password: Kerberos password
        :param krb5_login_file: Kerberos login file
        :param krb5_conf_file: Kerberos configuration file
        :return commands list
        """
        self.hdfs_sink                 = kwargs.get(SINK, "hdfs-sink")
        self.hdfs_channel              = kwargs.get(CHANNEL, "hdfs-channel")
        self.hdfs_host                 = kwargs.get(HOST, LOCALHOST)
        self.hdfs_port                 = kwargs.get(PORT, "14000")
        self.hdfs_user                 = kwargs.get(USER, EMPTY)
        self.hdfs_password             = kwargs.get(PASSWORD, EMPTY)
        self.hdfs_api                  = kwargs.get(API, API_HTTPFS)
        self.hdfs_persistence          = kwargs.get(PERSISTENCE, PERSISTENCE_ROW)
        self.hdfs_hive_host            = kwargs.get(HIVE_HOST, LOCALHOST)
        self.hdfs_hive_port            = kwargs.get(HIVE_PORT, "10000")
        self.hdfs_krb5_auth            = kwargs.get(KRB5_AUTH, False)
        self.hdfs_krb5_user            = kwargs.get(KRB5_USER, EMPTY)
        self.hdfs_krb5_password        = kwargs.get(KRB5_PASSWORD, EMPTY)
        self.hdfs_krb5_login_file      = kwargs.get(KRB5_LOGIN_FILE, "/usr/cygnus/conf/krb5_login.conf")
        self.hdfs_krb5_conf_file       = kwargs.get(KRB5_CONF_FILE, "/usr/cygnus/conf/krb5.conf")

        # replace all hdfs sink name by a new one
        self.__append_command('sed -i "s/%s.sinks.hdfs-sink./%s.sinks.%s./" %s/%s ' % (self.id, self.id, self.hdfs_sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channel = hdfs-channel/%s.channel = %s/" %s/%s ' % (self.hdfs_sink, self.hdfs_sink,self.hdfs_channel, self.target_path, self.name), self.target_path, self.sudo)
        # replace all hdfs channel in configuration by a new one
        self.__append_command('sed -i "s/.channels.hdfs-channel./.channels.%s./" %s/%s ' % (self.hdfs_channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hdfs_host = .*/.hdfs_host = %s/" %s/%s ' % (self.hdfs_host, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hdfs_port = .*/.hdfs_port = %s/" %s/%s ' % (self.hdfs_port, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hdfs_username = .*/.hdfs_username = %s/" %s/%s ' % (self.hdfs_user, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hdfs_password = .*/.hdfs_password = %s/" %s/%s ' % (self.hdfs_password, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hdfs_api = httpfs/.hdfs_api = %s/" %s/%s ' % (self.hdfs_api, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.attr_persistence = .*/%s.attr_persistence = %s/" %s/%s ' % (self.hdfs_sink, self.hdfs_sink, self.hdfs_persistence, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hive_host = x.y.z.w/.hive_host = %s/" %s/%s ' % (self.hdfs_hive_host, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.hive_post = 10000/.hive_port = %s/" %s/%s ' % (self.hdfs_hive_port, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.krb5_auth = false/.krb5_auth = %s/" %s/%s ' % (self.hdfs_krb5_auth, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.krb5_auth.krb5_user = krb5_username/.krb5_auth.krb5_user = %s/" %s/%s ' % (self.hdfs_krb5_user, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.krb5_auth.krb5_password = .*/.krb5_auth.krb5_password = %s/" %s/%s ' % (self.hdfs_krb5_password, self.target_path, self.name), self.target_path, self.sudo)
        sed_hdfs_krb5_login_file = self.hdfs_krb5_login_file.replace("/", "\/")    # replace / to \/ in path that is used in sed command
        self.__append_command('sed -i "s/.krb5_auth.krb5_login_conf_file = .*/.krb5_auth.krb5_login_conf_file = %s/" %s/%s ' % (sed_hdfs_krb5_login_file, self.target_path, self.name), self.target_path, self.sudo)
        sed_hdfs_krb5_conf_file = self.hdfs_krb5_conf_file.replace("/", "\/")    # replace / to \/ in path that is used in sed command
        self.__append_command('sed -i "s/.krb5_auth.krb5_conf_file = .*/.krb5_auth.krb5_conf_file = %s/" %s/%s ' % (sed_hdfs_krb5_conf_file, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_ckan_sink(self, **kwargs):
        """
        parameters values in ckan sink
        :param sink: sinks used (ckan-sink1, ckan-sink2,...,ckan-sinkN)
        :param channel: specific channel(ckan-channel)
        :param api_key:  the CKAN API key to use
        :param host: the FQDN/IP address for the CKAN API endpoint
        :param port:  the port for the CKAN API endpoint
        :param orion_url: Orion URL used to compose the resource URL with the convenience operation URL to query it
        :param persistence: how the attributes are stored, either per row either per column (row, column)
        :param ssl: enable SSL for secure Http transportation; 'true' or 'false'
        :return commands list
        """
        self.ckan_sink                 = kwargs.get(SINK, "ckan-sink")
        self.ckan_channel              = kwargs.get(CHANNEL, "ckan-channel")
        self.ckan_api_key              = kwargs.get(API_KEY, EMPTY)
        self.ckan_host                 = kwargs.get(HOST, LOCALHOST)
        self.ckan_port                 = kwargs.get(PORT, "80")
        self.ckan_orion_url            = kwargs.get(ORION_URL, "http://localhost:1026")
        self.ckan_persistence          = kwargs.get(PERSISTENCE, PERSISTENCE_COLUMN)
        self.ckan_ssl                  = kwargs.get(SSL, "false")

        self.__append_command('sed -i "s/%s.sinks.ckan-sink./%s.sinks.%s./" %s/%s ' % (self.id, self.id, self.ckan_sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channel = ckan-channel/%s.channel = %s/" %s/%s ' % (self.ckan_sink, self.ckan_sink,self.ckan_channel, self.target_path, self.name), self.target_path, self.sudo)
        # replace all hdfs channel in configuration by a new one
        self.__append_command('sed -i "s/.channels.ckan-channel./.channels.%s./" %s/%s ' % (self.ckan_channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.api_key = .*/.api_key = %s/" %s/%s ' % (self.ckan_api_key, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.ckan_host = .*/.ckan_host = %s/" %s/%s ' % (self.ckan_host, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.ckan_port = .*/.ckan_port = %s/" %s/%s ' % (self.ckan_port, self.target_path, self.name), self.target_path, self.sudo)
        sed_ckan_orion_url = self.ckan_orion_url.replace("/", "\/")    # replace / to \/ in path that is used in sed command
        self.__append_command('sed -i "s/.orion_url = .*/.orion_url = %s/" %s/%s ' % (sed_ckan_orion_url, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.attr_persistence = .*/%s.attr_persistence = %s/" %s/%s ' % (self.ckan_sink, self.ckan_sink, self.ckan_persistence, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.ssl = .*/.ssl = %s/" %s/%s ' % (self.ckan_ssl, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_mysql_sink(self, **kwargs):
        """
        parameters values in mysql sink
        :param sink: sinks used mysql-sink1, mysql-sink2,...,mysql-sinkN)
        :param channel: specific channel(ckan-channel)
        :param host: the FQDN/IP address where the MySQL server runs
        :param port: the port where the MySQL server listes for incomming connections
        :param user: a valid user in the MySQL server
        :param password:  password for the user above
        :param persistence:  how the attributes are stored, either per row either per column (row, column)
        """
        self.mysql_sink                 = kwargs.get(SINK, "mysql-sink")
        self.mysql_channel              = kwargs.get(CHANNEL, "mysql-channel")
        self.mysql_host                 = kwargs.get(HOST, LOCALHOST)
        self.mysql_port                 = kwargs.get(PORT, "3306")
        self.mysql_user                 = kwargs.get(USER, EMPTY)
        self.mysql_password             = kwargs.get(PASSWORD, EMPTY)
        self.mysql_persistence          = kwargs.get(PERSISTENCE, PERSISTENCE_COLUMN)

        self.__append_command('sed -i "s/%s.sinks.mysql-sink./%s.sinks.%s./" %s/%s ' % (self.id, self.id, self.mysql_sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channel = mysql-channel/%s.channel = %s/" %s/%s ' % (self.mysql_sink, self.mysql_sink,self.mysql_channel, self.target_path, self.name), self.target_path, self.sudo)
        # replace all hdfs channel in configuration by a new one
        self.__append_command('sed -i "s/.channels.mysql-channel./.channels.%s./" %s/%s ' % (self.mysql_channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.mysql_host = .*/.mysql_host = %s/" %s/%s ' % (self.mysql_host, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.mysql_port = .*/.mysql_port = %s/" %s/%s ' % (self.mysql_port, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.mysql_username = .*/.mysql_username = %s/" %s/%s ' % (self.mysql_user, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.mysql_password = .*/.mysql_password = %s/" %s/%s ' % (self.mysql_password, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.attr_persistence = .*/%s.attr_persistence = %s/" %s/%s ' % (self.mysql_sink, self.mysql_sink, self.mysql_persistence, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_mongo_sink(self, **kwargs):
        """
        parameters values in mongo sink
        :param sink: sinks used mongo-sink1, mongo-sink2,...,mongo-sinkN)
        :param channel: specific channel(ckan-channel)
        :param host: the FQDN/IP address where the MySQL server runs
        :param port: the port where the MySQL server listes for incomming connections
        :param user: a valid user in the MySQL server
        :param password:  password for the user above
        :param persistence:  how the attributes are stored, either per row either per column (row, column)
        """
        mongo_sink                 = kwargs.get(SINK, "mongo-sink")
        mongo_channel              = kwargs.get(CHANNEL, "mongo-channel")
        mongo_host_port            = kwargs.get(HOST_PORT, "localhost:227017")
        mongo_user                 = kwargs.get(USER, EMPTY)
        mongo_password             = kwargs.get(PASSWORD, EMPTY)
        mongo_data_model           = kwargs.get(DATA_MODEL, "collection-per-entity")
        db_prefix                  = kwargs.get(DB_PREFIX, "sth_")
        collection_prefix          = kwargs.get(COLLECTION_PREFIX, "sth_")

        self.__append_command('sed -i "s/%s.sinks.mongo-sink./%s.sinks.%s./" %s/%s ' % (self.id, self.id, mongo_sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channel = mongo-channel/%s.channel = %s/" %s/%s ' % (mongo_sink, mongo_sink,mongo_channel, self.target_path, self.name), self.target_path, self.sudo)
        # replace all hdfs channel in configuration by a new one
        self.__append_command('sed -i "s/%s.channels.mongo-channel./%s.channels.%s./" %s/%s ' % (mongo_sink,mongo_sink, mongo_channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_hosts = .*/%s.mongo_hosts = %s/" %s/%s ' % (mongo_sink,mongo_sink, mongo_host_port, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_username = .*/%s.mongo_username = %s/" %s/%s ' % (mongo_sink,mongo_sink, mongo_user, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_password = .*/%s.mongo_password = %s/" %s/%s ' % (mongo_sink,mongo_sink, mongo_password, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.data_model = .*/%s.data_model = %s/" %s/%s ' % (mongo_sink, mongo_sink, mongo_data_model, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.db_prefix = .*/%s.db_prefix = %s/" %s/%s ' % (mongo_sink, mongo_sink, db_prefix, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.collection_prefix = .*/%s.collection_prefix = %s/" %s/%s ' % (mongo_sink, mongo_sink, collection_prefix, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_sth_sink(self, **kwargs):
        """
        parameters values in sth sink
        :param sink: sinks used sth-sink1, sth-sink2,...,sth-sinkN)
        :param channel: specific channel(ckan-channel)
        :param host: the FQDN/IP address where the MySQL server runs
        :param port: the port where the MySQL server listes for incomming connections
        :param user: a valid user in the MySQL server
        :param password:  password for the user above
        :param persistence:  how the attributes are stored, either per row either per column (row, column)
        """
        sth_sink                 = kwargs.get(SINK, "sth-sink")
        sth_channel              = kwargs.get(CHANNEL, "sth-channel")
        sth_host_port            = kwargs.get(HOST_PORT, "localhost:27017")
        sth_user                 = kwargs.get(USER, EMPTY)
        sth_password             = kwargs.get(PASSWORD, EMPTY)
        sth_data_model           = kwargs.get(DATA_MODEL, "collection-per-entity")
        db_prefix                  = kwargs.get(DB_PREFIX, "sth_")
        collection_prefix          = kwargs.get(COLLECTION_PREFIX, "sth_")

        self.__append_command('sed -i "s/%s.sinks.sth-sink./%s.sinks.%s./" %s/%s ' % (self.id, self.id, sth_sink, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.channel = sth-channel/%s.channel = %s/" %s/%s ' % (sth_sink, sth_sink,sth_channel, self.target_path, self.name), self.target_path, self.sudo)
        # replace all hdfs channel in configuration by a new one
        self.__append_command('sed -i "s/%s.channels.sth-channel./%s.channels.%s./" %s/%s ' % (sth_sink,sth_sink, sth_channel, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_hosts = .*/%s.mongo_hosts = %s/" %s/%s ' % (sth_sink,sth_sink, sth_host_port, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_username = .*/%s.mongo_username = %s/" %s/%s ' % (sth_sink,sth_sink, sth_user, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.mongo_password = .*/%s.mongo_password = %s/" %s/%s ' % (sth_sink,sth_sink, sth_password, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.data_model = .*/%s.data_model = %s/" %s/%s ' % (sth_sink, sth_sink, sth_data_model, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.db_prefix = .*/%s.db_prefix = %s/" %s/%s ' % (sth_sink, sth_sink, db_prefix, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/%s.collection_prefix = .*/%s.collection_prefix = %s/" %s/%s ' % (sth_sink, sth_sink, collection_prefix, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def config_channel(self, channel, **kwargs):
        """
        parameters values in channel configuration
        :param channel: specific channel(hdfs-channel mysql-channel ckan-channel, mongo-channel, sth-channel)
        :param capacity: capacity of the channel
        :param transaction_capacity: amount of bytes that can be sent per transaction
        """
        self.capacity             = kwargs.get(CAPACITY, "1000")
        self.transaction_capacity = kwargs.get(TRANSACTION_CAPACITY, "100")

        self.__append_command('sed -i "s/.channels.%s.capacity = .*/.channels.%s.capacity = %s/" %s/%s ' % (channel, channel, self.capacity, self.target_path, self.name), self.target_path, self.sudo)
        self.__append_command('sed -i "s/.channels.%s.transactionCapacity = .*/.channels.%s.transactionCapacity = %s/" %s/%s ' % (channel, channel, self.transaction_capacity, self.target_path, self.name), self.target_path, self.sudo)
        return OPS_LIST

    def get_file_name (self, id):
        """
        return file name with id included. ex: agent_<id>.conf
        :param id: id to instance
        :return: string
        """
        return FILENAME_TARGET % (id)
