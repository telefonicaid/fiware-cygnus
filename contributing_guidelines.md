#<a name="section0"></a>Contributing guidelines
Content:

* [Introduction](#section1)
* [Adopted conventions](#section2)
* [Language of the main repository](#section3)
* [Repository organization](#section4)
* [Backlog](#section5)
* [Contributing to the repository](#section6)
    * [Main repository versus forked repositories](#section6.1)
    * [Pull requests](#section6.2)
    * [Contribution contents](#section6.3)
    * [Commits and squashing](#section6.4)
* [Releasing](#section7)
* [Deployers and installers (RPMs and dockers)](#section8)
* [Documentation](#section9)
* [References](#section10)

##<a name="section1"></a>Introduction
This document is intended to developers aiming at contributing a complete Cygnus agent to the Cygnus suite. In order to accept those contributions a [contribution policy](./ContributionPolicy.txt) document has to be signed beforehand.

Within this document developers will find detailed guidelines regarding how to contribute to the main Cygnus repository.

Any doubt you may have, please refer [here](./reporting_issues_and_contact.md).

[Top](#top)

##<a name="section2"></a>Adopted conventions
1. This document uses the following guidelines with regard to the usage of MUST, SHOULD and MAY (and NOT) keywords:
    * MUST Guidelines. They are mandatory and your GEi project must conform to that.
    * SHOULD Guidelines. They are not mandatory but highly recommended if you want to have a mature development process.
    * MAY Guidelines. They are nice to have.

2. It MUST be differentiated between the main repository under the `telefonicaid` namespace ([https://github.com/telefonicaid/fiware-cygnus](https://github.com/telefonicaid/fiware-cygnus)) and any private forked repository (e.g. [https://github.com/frbattid/fiware-cygnus](https://github.com/frbattid/fiware-cygnus)).

3. Cygnus Core Team members are those listed in the [reporting issues and contact information](./reporting_issues_and_contact.md) document.

[Top](#top)

##<a name="section3"></a>Language of the main repository
The main repository language MUST be English.

[Top](#top)

##<a name="section4"></a>Repository organization
Each agent MUST have a dedicated folder. Each folder MUST be prefixed with `cygnus-`. For instance:

* `cygnus-ngsi`
* `cygnus-twitter`

Each folder MUST have, at least, the following subdirectories and files:

* `src/` → functional code and unit tests
* `doc/` → documentation in Markdown language
* `docker/` → everything about deploying Cygnus by means of Docker
* `test/` → acceptance tests, e2e tests, performance tests, others
* `neore/` or `re/` → everything about installing Cygnus by means of a RPM
* `conf/` → templates for configuration files required to run the agent
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

##<a name="section5"></a>Backlog
The <i>issues</i> section of the main repository MUST be used for tracking all the features, hardening, bugs and task to be implemented by every agent.

The name of each issue MUST follow the following format:

    [<agent name>] [feature|hardening|bug|task] <short description>

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    [cygnus-ngsi] [hardening] [grouping rules] Precompile patterns from regexes

Alternatively, labels for each agent and task type SHOULD be created.

Every issue MUST have a description as detailed as the creator considers, but it MUST be enough to understand the purpose of the issue and to allow the community to start a discussion.

Every issue SHOULD have an associated sprint/milestone among the ones in the <i>milestones</i> section on the main repository.

There MUST NOT be assignee because each issue is considered to be assigned to a development team related to the agent; so, the assignation MUST be done internally to the team. Anyway, the real Github user ID assignee to the issue MAY be added to the description of the issue; in that case, the following format MUST be used:

    * Assignee: @<Github’s user ID>

[Top](#top)

##<a name="section6"></a>Contributing to the repository
###<a name="section6.1"></a>Main repository versus forked repositories
Every team in charge of an agent MUST create one or more forks of the main repository. Every team SHOULD synchronize their forked repositories with the main one after opening a pull request (see next section).

Only those contributions merged into the main repository MUST be considered as part of the official Cygnus development.

[Top](#top)

###<a name="section6.2"></a>Pull requests
Any contribution MUST be done through a new opened pull request (PR). These PRs MUST compare certain branch at any forked repository against the `develop` base branch in the main repository.

The review process made by the Cygnus Core Team MUST check that the content of the PR is aligned with guidelines. In addition, as any other contribution, a code-wise review MAY be performed by the Cygnus Core Team or any other member of the Community.

However, `cygnus-common` contributions MUST be fully reviewed and approved by a member of the Cygnus Core Team.

Internally to every team, private code reviews SHOULD be done before pull requesting to the main repository.

[Top](#top)

###<a name="section6.3"></a>Contribution contents
Every contribution/PR MUST include:

* The code implementing the feature/hardening/bug/task.
* Unit tests.
* Documentation.

Other tests MAY be included (acceptance, e2e, performance).

Every contribution/PR MUST also add a new line in a special file within the root of the main repository, `CHANGES_NEXT_RELEASE`. The format of each line MUST follow this format:

    - [<agent name>] [feature|hardening|bug|task] <short description> (#<issue number>)

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    - [cygnus-ngsi] [hardening] [grouping rules] Precompile regexes (#209)

[Top](#top)

###<a name="section6.4"></a>Commits nad squashing
Commits within PRs MUST include a comment following this format:

    [<agent name>] [issue number] <short description>

Where <i>short description</i> MAY enclose other “[...]” sublevels. For instance:

    [cygnus-ngsi] [873] Update CHANGES_NEXT_RELEASE
    [cygnus-ngsi] [873] Add support for pattern storage
    [cygnus-ngsi] [873] Add regex compilation

With regards to the squashing policy, the main repository MUST be configured with the <i>Allow Squash Merging</i> option.

[Top](#top)

##<a name="section7"></a>Releasing
When generating a new version of Cygnus from the main repository, all the agents MUST be released at the same time as a whole.

A minor version (0.X.0, at the moment of writing 0.13.0) of Cygnus MUST be released at the end of each sprint/milestone. A sprint SHOULD comprise a natural month, however sometimes the sprints MAY comprise a different period, for instance a month and a half or half a month (usually, in order to adapt to holydays time). Every sprint MUST be scheduled in advance by Cygnus Core Team in the form of deadline in the related milestone. Agent teams SHOULD use this information in order to, internally, schedule the sprint in terms of issues to be implemented.

New releases MUST be obtained from the `develop` branch of the main repository, because such a branch contains all the contributions made during the last sprint.

While a sprint is active, the Cygnus version in the different `pom.xml` files MUST be:

    cygnus-0.<last release minor version>.0_SNAPSHOT

For instance, if the current latest release is 0.13.0 then the Cygnus version in the `develop` branch is:

    cygnus-0.13.0_SNAPSHOT

Obtaining a new release MUST imply creating a new branch  `release/0.X.0` directly from the  `develop` branch in the main repository and creating a new tag `release-0.X.0` in the main repository.

Releases MUST be published in the <i>releases</i> section of the main repository.

##<a name="section8"></a>Deployers and installers (RPMs and dockers)
Work in progress.

[Top](#top)

##<a name="section9"></a>Documentation
Work in progress.

[Top](#top)

##<a name="section10"></a>References
https://help.github.com/articles/configuring-pull-request-merge-squashing/
https://github.com/blog/2141-squash-your-commits

[Top](#top)
