# <a name="section0"></a>Contributing guidelines
Content:

* [Introduction](#section1)
* [Adopted conventions](#section2)
* [Contributing to the repository](#section3)
    * [Language of the main repository](#section3.1)
    * [License header](#section3.1bis)
    * [Repository organization](#section3.2)
    * [Backlog](#section3.3)
    * [Main repository versus forked repositories](#section3.4)
    * [Pull requests](#section3.5)
    * [Contribution contents](#section3.6)
    * [Coding style](#section3.7)
    * [Commits and squashing](#section3.8)
    * [Releasing](#section3.9)
* [Deployers](#section4)
    * [RPMs](#section4.1)
    * [Dockers](#section4.2)
* [Documentation](#section5)
    * [Repository documentation](#section5.1)
    * [`readthedocs.org` documentation](#section5.2)
* [Logs and alarms](#section6)
    * [log4j](#section6.1)
    * [Section in the documentation](#section6.2)
* [Configuration files](#section7)

## <a name="section1"></a>Introduction
This document is intended to developers aiming at contributing a complete Cygnus agent to the Cygnus suite. In order to accept those contributions a [contribution policy](./ContributionPolicy.txt) document has to be signed beforehand.

Within this document developers will find detailed guidelines regarding how to contribute to the main Cygnus repository.

Any doubt you may have, please refer to [here](https://github.com/telefonicaid/fiware-cygnus/blob/master/reporting_issues_and_contact.md).

[Top](#top)

## <a name="section2"></a>Adopted conventions
1. This document uses the following guidelines with regard to the usage of MUST, SHOULD and MAY (and NOT) keywords:
    * MUST Guidelines. They are mandatory and your agent must conform to that.
    * SHOULD Guidelines. They are not mandatory but highly recommended if you want to have a mature development process.
    * MAY Guidelines. They are nice to have.

2. It MUST be differentiated between the main repository under the `telefonicaid` namespace ([https://github.com/telefonicaid/fiware-cygnus](https://github.com/telefonicaid/fiware-cygnus)) and any private forked repository (e.g. [https://github.com/frbattid/fiware-cygnus](https://github.com/frbattid/fiware-cygnus)).

3. Cygnus Core Team members are those listed in the [reporting issues and contact information](https://github.com/telefonicaid/fiware-cygnus/blob/master/reporting_issues_and_contact.md) document.

[Top](#top)

## <a name="section3"></a>Contributing to the repository
### <a name="section3.1"></a>Language of the main repository
The main repository language MUST be English.

[Top](#top)

### <a name="section3.1bis"></a>License header

All source code files (along with other files of similar nature, i.e. scripts, configuration files, etc.)
MUST have the following copyrigth header:

```
/**
 * Copyright XXXX Telefonica Investigación y Desarrollo, S.A.U
 *
 * This file is part of fiware-cygnus (FIWARE project).
 *
 * fiware-cygnus is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * fiware-cygnus is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with fiware-cygnus. If not, see
 * http://www.gnu.org/licenses/.
 *
 * For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
 *
 */
```

Optionally, you MAY add an authorship line within the header just after the above text:

```
* Authorship: <your name/company>
```

If the file already exists and you modify it, you MAY add a line about it at the end of the header in the following form:

```
* Modified by: <your name/company>
```

[Top](#top)

### <a name="section3.2"></a>Repository organization
Each agent MUST have a dedicated folder. Each folder MUST be prefixed with `cygnus-`. For instance:

* `cygnus-ngsi`
* `cygnus-twitter`

Each folder MUST have, at least, the following subdirectories and files:

* `src/` → functional code and unit tests
* `docker/` → everything about deploying Cygnus by means of Docker
* `test/` → acceptance tests, e2e tests, performance tests, others
* `conf/` → templates for configuration files required to run the agent
* `spec/` → spec file for generating a RPM
* `pom.xml` → Maven’s Project Object Model

A folder with common content named `cygnus-common` MUST exist. It will be a Maven project in charge of building a Cygnus common library that SHOULD be used by all the agents, enforcing the reusability of code.

Every source file at any agent folder or `cygnus-common` MUST be under a Java package, following this format:

    com.telefonica.iot.cygnus.<etc>.<etc>…

Typically, these are the Java packages that SHOULD be used, but any other can be added:

* `com.telefonica.iot.cygnus.sinks` → for new sink development
* `com.telefonica.iot.cygnus.handlers` → for new handlers development
* `com.telefonica.iot.cygnus.backends` → for new backends development
* `com.telefonica.iot.cygnus.containers` → for new Json containers development
* ...

Any Java package MUST match a couple of paths like these ones:

* `/<agent_name>/src/main/java/com/telefonica/iot/cygnus/<etc>/<etc>/…` → functional code
* `/<agent_name>/src/test/java/com/telefonica/iot/cygnus/<etc>/<etc>/…` → unit tests

As can be seen, despite the repository organization, from a Java perspective all the agents code is under `com.telefonica.iot.cygnus`.

[Top](#top)

### <a name="section3.3"></a>Backlog
The <i>issues</i> section of the main repository MUST be used for tracking all the features, hardening, bugs and task to be implemented by every agent.

The name of each issue MUST follow the following format:

    [<agent name>][feature|hardening|bug|task] <short description>

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    [cygnus-ngsi][hardening][grouping rules] Precompile patterns from regexes

Alternatively, labels for each agent and task type SHOULD be created.

Every issue MUST have a description as detailed as the creator considers, but it MUST be enough to understand the purpose of the issue and to allow the community to start a discussion.

Every issue SHOULD have an associated sprint/milestone among the ones in the <i>milestones</i> section on the main repository.

There MUST NOT be assignee because each issue is considered to be assigned to a development team related to the agent; so, the assignation MUST be done internally to the team. Anyway, the real Github user ID assignee to the issue MAY be added to the description of the issue; in that case, the following format MUST be used:

    * Assignee: @<Github’s user ID>

[Top](#top)

### <a name="section3.4"></a>Main repository versus forked repositories
Every team in charge of an agent MUST create one or more forks of the main repository. Every team SHOULD synchronize their forked repositories with the main one after opening a pull request (see next section).

Only those contributions merged into the main repository MUST be considered as part of the official Cygnus development.

[Top](#top)

### <a name="section3.5"></a>Pull requests
Any contribution MUST be done through a new opened pull request (PR). These PRs MUST compare certain branch at any forked repository against the `develop` base branch in the main repository.

The review process made by the Cygnus Core Team MUST check that the content of the PR is aligned with guidelines. In addition, as any other contribution, a code-wise review MAY be performed by the Cygnus Core Team or any other member of the Community.

However, `cygnus-common` contributions MUST be fully reviewed and approved by a member of the Cygnus Core Team.

Internally to every team, private code reviews SHOULD be done before pull requesting to the main repository.

[Top](#top)

### <a name="section3.6"></a>Contribution contents
Every contribution/PR MUST include:

* The code implementing the feature/hardening/bug/task.
* Unit tests.
* Documentation.

Other tests MAY be included (acceptance, e2e, performance).

Every contribution/PR MUST also add a new line in a special file within the root of the main repository, `CHANGES_NEXT_RELEASE`. The format of each line MUST follow this format:

    - [<agent name>][feature|hardening|bug|task] <short description> (#<issue number>)

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    - [cygnus-ngsi][hardening][grouping rules] Precompile regexes (#209)

[Top](#top)

### <a name="section3.7"></a>Coding style
The `fiware-cygnus/telefonica_checkstyle.xml` file MUST be configured in any Integrated Development Environment (IDE) used by the different development teams as a coding style checker. This XML file contains all the coding style rules accepted by Telefónica.

NOTE: it some cases we have found problems with `telefonica_checkstyle.xml` in recent versions of checkstyle with the [Eclipse plugin](http://eclipse-cs.sourceforge.net), which are solved commenting out the following line:

```
<module name="RedundantThrows"/>
```

[Top](#top)

### <a name="section3.8"></a>Commits and squashing
Commits within PRs MUST include a comment following this format:

    [<issue number>][<agent name>] <short description>

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    [873][cygnus-ngsi] Update CHANGES_NEXT_RELEASE
    [873][cygnus-ngsi] Add support for pattern storage
    [873][cygnus-ngsi] Add regex compilation

With regards to the [squashing policy](https://help.github.com/articles/about-pull-request-merges/#squash-and-merge-your-pull-request-commits), the main repository MUST be configured with the <i>Allow Squash Merging</i> option.

[Top](#top)

### <a name="section3.9"></a>Releasing
When generating a new version of Cygnus from the main repository, all the agents MUST be released at the same time as a whole.

A minor version (0.X.0, at the moment of writing 0.13.0) of Cygnus MUST be released at the end of each sprint/milestone. A sprint SHOULD comprise a natural month, however sometimes the sprints MAY comprise a different period, for instance a month and a half or half a month (usually, in order to adapt to holydays time). Every sprint MUST be scheduled in advance by Cygnus Core Team in the form of deadline in the related milestone. Agent teams SHOULD use this information in order to, internally, schedule the sprint in terms of issues to be implemented.

New releases MUST be obtained from the `develop` branch of the main repository, because such a branch contains all the contributions made during the last sprint.

While a sprint is active, the Cygnus version in the different `pom.xml` files MUST be:

    cygnus-0.<last release minor version>.0_SNAPSHOT

For instance, if the current latest release is 0.13.0 then the Cygnus version in the `develop` branch is:

    cygnus-0.13.0_SNAPSHOT

Obtaining a new release MUST imply creating a new branch  `release/0.X.0` directly from the  `develop` branch in the main repository and creating a new tag `release-0.X.0` in the main repository.

Releases MUST be published in the <i>releases</i> section of the main repository.

As a result of the release, `CHANGES_NEXT_RELEASE` file MUST be emptied in Github repo.

## <a name="section4"></a>Deployers
### <a name="section4.1"></a>RPMs
There MUST exist a `rpm/` folder at the root of the main repository. A packaging script MUST generate a RPM based on the spec file of each Cygnus agent, including `cygnus-common`. Such a spec file MUST live at the `spec` subfolder within the agent folder.

Upon releasing, these RPMs MUST be created and uploaded to some repository in order they are available. As an example, `cygnus-ngsi` agent's RPM is uploaded to `http://repositories.testbed.fiware.org`.

Agents' RPMs MUST depend on `cygnus-common` RPM, which MUST be in charge of installing not only the common classes to all the agents, but installing Apache Flume, default configuration templates and provisioning the Cygnus plugin. `cygnus-common` RPM is typically uploaded to `http://repositories.testbed.fiware.org` as well.

All RPMs spec files (spec for `cygnus-common` and any other agent) MUST contain a copy of the content of `CHANGES_NEXT_RELEASE` file.

[Top](#top)

### <a name="section4.2"></a>Dockers
There MUST exist a `docker/` folder at the root of the main repository. Every Cygnus agent MUST include a docker subfolder as per the following rules:

* `docker/cygnus-common`
* `docker/cygnus-ngsi`
* `docker/cygnus-twitter`
* ...

Each docker subfolder MUST contain at least a `Dockerfile` file. Agents' dockerfiles MUST contain `cygnus-common`, default configuration templates, Apache Flume and the Cygnus plugin provisioned.

Upon releasing, images for the agents MUST be uploaded to `https://hub.docker.com/r/fiware/` with a version and agent tag.

[Top](#top)

## <a name="section5"></a>Documentation
### <a name="section5.1"></a>Repository documentation
There MUST exist a `doc/` folder at the root of the main repository. Every Cygnus agent MUST include a documentation subfolder as per the following rules:

* `doc/cygnus-common`
* `doc/cygnus-ngsi`
* `doc/cygnus-twitter`
* ...

In addition, any agent documentation subfolder MUST have at least the following elements:

* `doc/<agent name>/installation_and_administration_guide/` → Any document regarding how to install and administrate the agent MUST be placed here. Markdown MUST be used for the documents within this subfolder.
* `doc/<agent name>/user_and_programmer_guide/` → Any document regarding how to use and programme the agent MUST be placed here. Markdown MUST be used for the documents within this subfolder.

The following elements SHOULD be present as well:

* `doc/<agent name>/flume_extensions_catalogue/` → For each class added to the native Apache Flume library, there SHOULD be a document placed here. Markdown MUST be used for the documents within this subfolder.
* `doc/<agent name>/quick_start_guide.md/` → Simple and ready-to-use commands/actions to be taken in order to quickly test the agent SHOULD be documented here. Markdown MUST be used.

[Top](#top)

### <a name="section5.2"></a>`readthedocs.org` documentation
The documentation within the `doc/` folder MUST be published to `readthedocs.org`. In order to achieve this, a `mkdocs.yml` file MUST live in the root of the main repository acting as a hook.

The format of this `mkdocs.yml` file MUST follow this example:

```
site_name: fiware-cygnus
site_url: https://fiware-cygnus.readthedocs.org
repo_url: https://github.com/telefonicaid/fiware-cygnus.git
site_description: Cygnus Documentation
docs_dir: doc
site_dir: html
markdown_extensions: [toc,fenced_code]
use_directory_urls: false
theme: readthedocs
extra_css: ["https://fiware.org/style/fiware_readthedocs.css"]
pages:
  - Home: index.md
  - 'Contributing': 'contributing/contributing_guidelines.md
  - 'cygnus-ngsi':
      - 'Quick Start Guide': 'cygnus-ngsi/quick_start_guide.md'
      - 'Installation and Administration Guide':
          - 'subsection 1 example': 'cygnus-ngsi/installation_and_administration_guide/subsection_1_example.md'
          - 'subsection 2 example': 'cygnus-ngsi/installation_and_administration_guide/subsection_2_example.md'
      - 'User and Programmer Guide':
          - ...
      - 'Flume extensions catalogue':
          - ...
  - 'cygnus-twitter':
      - 'Quick Start Guide': 'cygnus-twitter/quick_start_guide.md'
      - 'Installation and Administration Guide':
          - ...
      - 'User and Programmer Guide':
          - ...
      - 'Flume extensions catalogue':
          - ...
  - 'cygnus-common':
      - ...
```

[Top](#top)

## <a name="section6"></a>Logs and alarms
### <a name="section6.1"></a>log4j
log4j is the logging system used by Apache Flume, thus Cygnus agents MUST use log4j.

Logs traced by any Cygnus agent MUST contain the following log4 layout:

    time=%d{yyyy-MM-dd}T%d{HH:mm:ss.SSSzzz} | lvl=%p | corr=%X{correlatorId} | trans=%X{transactionId} | srv=%X{service} | subsrv=%X{subservice} | function=%M | comp=%X{agent} | msg=%C[%L] : %m%n

Field by field:

* `time`: Date and time the log was generated at.
* `lvl`: log4j logging level. Accepted levels MUST be `FATAL`, `ERROR`, `WARN`, `INFO` and `DEBUG`.
* `corr`: Correlation ID, it MUST be unique and transversal to all the IoT platform. The guidelines regarding this ID are: it MUST be re-used if sent to the Cygnus agent in some way (e.g. a Http header), otherwise it MUST be auto-generated.
* `trans`: Transaction ID, it MUST be unique and auto-generated by the Cygnus agent. It the correlation ID has to be created, then the transaction and correlation IDs MUST be equals.
* `srv`: FIWARE service sent to the Cygnus agent in some way (e.g. a Http header).
* `subsrv`: FIWARE sub-service sent to the Cygnus agent in some way (e.g. a Http header).
* `function`: Name of the Java method where the log is traced from.
* `comp`: Name of the Cygnus agent, it is the one passed in the command used for running Flume.
* `msg`: Java class containing the traced function, the specific line at the class and the application suplied message.

[Top](#top)

### <a name="section6.2"></a>Repository documentation
The installation and administration guide of any agent (`doc/<agent name>/installation_and_administration_guide/`) MUST contain a section about logs and alarms.

Such a section MUST describe the main log message types the agent uses. It is a set of easily identifiable strings or <i>tags</i> in the traces text, and each log traced by the agent MUST be of any of the types among the set. For instance, `cygnus-ngsi` considers the following ones:

* <i>Fatal error.</i>
* <i>Runtime error.</i>
* <i>Bad configuration.</i>
* <i>Bad Http notification.</i>
* <i>Bad context data.</i>
* <i>Channel error.</i>
* <i>Persistence error.</i>

In addition, a table MUST be included in charge of defining a set of alarm conditions that any third-party alarming system MUST have into account. Fields for this table are:

* Alarm ID: An interger number starting by 1.
* Severity: "CRITICAL" or "WARNING".
* Detection strategy: What has to be found in the logs in order to raise this alarm. It may be a logging level, a log message type, etc.
* Stop condition: What has to be found in the logs in order to decide the alarm is fixed. It may be a logging level, a log message type, etc.
* Description: A description of the alarm, why it was raised and including its consequences.
* Action:

[Top](#top)

## <a name="section7"></a>Configuration
When adding a new agent to Cygnus, it MUST include an agent configuration template in [Flume format](https://flume.apache.org/FlumeUserGuide.html#setup). Other configuration files MAY be added as well.

The specific agent configuration template MUST replace the one handled by `cygnus-common` in the Flume deployment donde by `cygnus-common` RPM.

[Top](#top)
