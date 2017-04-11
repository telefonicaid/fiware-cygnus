# <a name="top"></a>Deprecated and removed functionality
Content:

* [Functionality deprecation and remove policy](#section1)
* [Deprecated functionalities](#section2)
    * [Grouping Rules](#section2.1)
    * [`flip_coordinates` parameter](#section2.2)
* [Removed functionalities](#section3)
    * [`events_ttl` parameter](#section3.1)
    * [XML notifications support](#section3.2)
    * [HDFS parameters](#section3.3)
    * [Data model by attribute in `NGSICartoDBSink`](#section3.4)
    * [`matching_table` parameter](#section3.5)
    * [Hash-based collection names in MongoDB/STH](#section3.6)
    * [`enable_raw` Carto parameter](#section3.7)
    * [`enable_distance` Carto parameter](#section3.8)

## <a name="section1"></a>Functionality deprecation and remove policy
At Cygnus NGSI agent (cygnus-ngsi), functionality lifecycle is:

1. New feature is designed.
2. New feature is implemented and released.
3. Existent feature is fixed, when required.
4. Existent feature is deprecated if a new feature encloses it or becomes unnecessary.
5. Existent feature is definitely removed after certain period of time.

While a feature is deprecated, it is still available at cygnus-ngsi, i.e. it can be used and it is documented. Nevertheless, it is no longer supported nor fixed nor improved. A disclaimer is added in the documentation, and optionally, in the logs.

Deprecated features are removed not before 3 development sprints (usually, a development sprint comprises one month).

**This policy will be effective from release 1.6.0 (included)**. Before that, this policy has been implemented <i>de facto</i> with more or less success.

[Top](#top)

## <a name="section2"></a>Deprecated functionalities
### <a nane="section2.1"></a>Grouping Rules
Added at version [0.5](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.5) (issue [107](https://github.com/telefonicaid/fiware-cygnus/issues/107)).

Deprecated after releasing version [1.6.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.6.0) (issue [1182](https://github.com/telefonicaid/fiware-cygnus/issues/1182)).

[Top](#top)

### <a name="section2.2"></a>`flip_coordinates` parameter
Added at version [1.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.0.0) (issue [927](https://github.com/telefonicaid/fiware-cygnus/issues/927)).

Deprecated after releasing version [1.6.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.6.0) (issue [1313](https://github.com/telefonicaid/fiware-cygnus/issues/1313)).

[Top](#top)

## <a name="section3"></a>Removed functionalities
### <a name="section3.1"></a>`events_ttl` parameter
Added at version [0.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.1).

Never deprecated.

Removed in favor of `batch_ttl` parameter after releasing version [0.13.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/0.13.0) (issue [714](https://github.com/telefonicaid/fiware-cygnus/issues/714)).

[Top](#top)

### <a name="section3.2"></a>XML notifications support
Added at version [0.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.1).

Deprecated in favor of Json notifications from the very beginning of the development.

Removed after releasing version [0.13.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.0.0) (issue [448](https://github.com/telefonicaid/fiware-cygnus/issues/448)).

[Top](#top)

### <a name="section3.3"></a>`cosmos_`-like HDFS parameters
Added at version [0.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.1).

Deprecated in favor of `hdfs_`-like parameters after releasing version [0.8.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.8.1) (issue [374](https://github.com/telefonicaid/fiware-cygnus/issues/374)).

Removed after releasing version [1.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.0.0) (issue [868](https://github.com/telefonicaid/fiware-cygnus/issues/868)).

[Top](#top)

### <a name="section3.4"></a>Data model by attribute in `NGSICartoDBSink`
Added at version [1.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.0.0) (issue [927](https://github.com/telefonicaid/fiware-cygnus/issues/927)).

Never deprecated.

Removed after releasing version [1.1.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.1.0) (issue [1030](https://github.com/telefonicaid/fiware-cygnus/issues/1030)).

[Top](#top)

### <a name="section3.5"></a>`matching_table` parameter
Added at version [0.5](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.5) (issue [https://github.com/telefonicaid/fiware-cygnus/issues/107](107)).

Deprecated in favor of `grouping_rules_conf_file` after releasing version [0.8.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/release-0.8.1) (issue [https://github.com/telefonicaid/fiware-cygnus/issues/387](387)).

Removed after releasing version [1.1.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.1.0) (issue [1048](https://github.com/telefonicaid/fiware-cygnus/issues/1048)).

[Top](#top)

### <a name="section3.6"></a>Hash-based collection names for MongoDB/STH
Added at version [0.8.1](https://github.com/telefonicaid/fiware-cygnus/releases/tag/0.8.1) (issue [420](https://github.com/telefonicaid/fiware-cygnus/issues/420)).

Never deprecated.

Removed after releasing version [1.4.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.4.0) (issue [1113](https://github.com/telefonicaid/fiware-cygnus/issues/1113)).

[Top](#top)

### <a name="section3.7"></a>`enable_raw` Carto parameter
Added at version [1.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.0.0).

Deprecated in favor of `enable_raw_historic` parameters after releasing version [1.8.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.8.0) (issue [1314](https://github.com/telefonicaid/fiware-cygnus/issues/1314)).

[Top](#top)

### <a name="section3.8"></a>`enable_distance` Carto parameter
Added at version [1.1.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.1.0).

Deprecated in favor of `enable_distance_historic` parameters after releasing version [1.8.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.8.0) (issue [1314](https://github.com/telefonicaid/fiware-cygnus/issues/1314)).

[Top](#top)
