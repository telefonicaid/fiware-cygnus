# Cygnus Roadmap

This product is a FIWARE Generic Enabler. If you would like to learn about 
the overall Roadmap of FIWARE, please check section "Roadmap" on 
the [FIWARE Catalogue](https://www.fiware.org/developers/catalogue/).

## Introduction

This section elaborates on proposed new features or tasks which are expected to
be added to the product in the foreseeable future. There should be no assumption
of a commitment to deliver these features on specific dates or in the order
given. The development team will be doing their best to follow the proposed
dates and priorities, but please bear in mind that plans to work on a given
feature or task may be revised. All information is provided as a general
guidelines only, and this section may be revised to provide newer information at
any time.

Disclaimer:

* This section has been last updated in March 2022. Please take into account its 
  content could be obsolete.
* Note we develop this software in Agile way, so development plan is continuously 
  under review. Thus, this roadmap has to be understood as rough plan of features 
  to be done along time which is fully valid only at the time of writing it. This
  roadmap has not be understood as a commitment on features and/or dates.
* Some of the roadmap items may be implemented by external community developers, 
  out of the scope of GE owners. Thus, the moment in which these features will be
  finalized cannot be assured.

## Short term

The following list of features are planned to be addressed in the short term,
and incorporated in a next release of the product in the short term:

## Medium term

The following list of features are planned to be addressed in the medium term,
typically within the subsequent release(s) generated in the next **9 months**
after next planned release:

## Long term

The following list of features are proposals regarding the longer-term evolution
of the product even though development of these features has not yet been
scheduled for a release in the near future. Please feel free to contact us if
you wish to get involved in the implementation or influence the roadmap

-   Binary CB-Cygnus communication (based in MQTT). Need to be validated before.

## Features already completed

The following list contains all features that were in the roadmap and have already been implemented.

- JSON native types persistence (MySQL, PostGIS and PostgreSQL sinks) [#1782](https://github.com/telefonicaid/fiware-cygnus/issues/1782), [#1780](https://github.com/telefonicaid/fiware-cygnus/issues/1780) ([1.18.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.18.0))
- Refactor and improvements in batch support (MySQL, PostGIS, PostgreSQL, CKAN sinks) [#1787](https://github.com/telefonicaid/fiware-cygnus/issues/1787) ([1.18.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.18.0))
- Ability to disable metadata (all sinks) [#1706](https://github.com/telefonicaid/fiware-cygnus/issues/1706) ([1.18.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/1.18.0))
- dm-by-entitytype in additional sinks (PostGIS and PostgreSQL sinks) [#1684](https://github.com/telefonicaid/fiware-cygnus/issues/1684) ([2.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/2.0.0))
- Improve the error feedback provided to users (all sinks) [#1791](https://github.com/telefonicaid/fiware-cygnus/issues/1791) ([2.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/2.0.0))
- Operational requirements (logs, correlation id, etc.) (all sinks) [#1770](https://github.com/telefonicaid/fiware-cygnus/issues/1770) ([2.0.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/2.0.0))
- New mode dm-by-entity-shifted (CKAN sink) [#1792](https://github.com/telefonicaid/fiware-cygnus/issues/1792) ([2.2.0](https://github.com/telefonicaid/fiware-cygnus/releases/tag/2.2.0))
