#
# Copyright 2016-2017 Telefonica Investigación y Desarrollo, S.A.U
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
# For those usages not covered by the GNU Affero General Public License please contact with iot_support at tid dot es
#

Summary:          Package for Cygnus Twitter component
Name:             cygnus-twitter
Version:          %{_product_version}
Release:          %{_product_release}
License:          AGPLv3
BuildRoot:        %{_topdir}/BUILDROOT/
BuildArch:        x86_64
Requires:         cygnus-common = %{_product_version}-%{_product_release}
Group:            Applications/cygnus
Vendor:           Telefonica I+D
Provides:         cygnus-twitter = %{_product_version}-%{_product_release}


%description
This connector is a Flume-based connector for retrieving tweets
coming from Twitter using a query that allows hashtags and coordinates.

# Project information
%define _project_name cygnus
%define _project_user cygnus

# improve package speed avoiding jar repack
%define __jar_repack %{nil}

# System folders
# _sourcedir =${topdir}/SOURCES
%define _srcdir %{_sourcedir}/../../
%define _install_dir /usr
%define _project_install_dir %{_install_dir}/%{_project_name}

# _localstatedir is a system var that goes to /var
 %define _log_dir %{_localstatedir}/log/%{_project_name}

# RPM Building folder
%define _build_root_project %{buildroot}%{_project_install_dir}

# -------------------------------------------------------------------------------------------- #
# prep section, setup macro:
# -------------------------------------------------------------------------------------------- #
%prep
# read from SOURCES, write into BUILD

echo "[INFO] Preparing installation"
# Create rpm/BUILDROOT folder
rm -Rf $RPM_BUILD_ROOT && mkdir -p $RPM_BUILD_ROOT
[ -d %{_build_root_project} ] || mkdir -p %{_build_root_project}

# Copy all from src to rpm/BUILD
cp -R  %{_srcdir}/src \
       %{_srcdir}/pom.xml \
       %{_srcdir}/README.md \
       %{_builddir}

# Copy "extra files" from rpm/SOURCES to rpm/BUILDROOT
cp -R %{_sourcedir}/* %{_builddir}

# -------------------------------------------------------------------------------------------- #
# Install section:
# -------------------------------------------------------------------------------------------- #
%install
# Read from BUILD and write into BUILDROOT
# RPM_BUILD_ROOT = BUILDROOT

mkdir -p %{_build_root_project}/conf

echo "[INFO] Installing the %{name}"

cp -R %{_builddir}/usr/cygnus/*                       %{_build_root_project}
cp %{_builddir}/config/*                              %{_build_root_project}/conf/

%clean
rm -rf $RPM_BUILD_ROOT


# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)

%{_project_install_dir}

%changelog
* Tue Oct 29 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.17.0

* Wed Sep 17 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.16.0
- [cygnus-twitter][doc] Add Install section in README.md (#1576)
- [cygnus-twitter] Enable JAVA_OPTS for Flume (#1704)
- [cygnus-twitter] Adjust build cygnus to use Java 1.8 (#1718)

* Fri Jun 28 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.15.0

* Tue Jun 04 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.14.0

* Wed May 22 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.13.0

* Wed Apr 10 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.12.0

* Wed Apr 03 2019 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.11.0
- [cygnus-twitter] Upgrade flume-ng-node version from 1.4.0 to 1.9.0

* Thu Dec 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.10.0

* Wed Jun 13 2018 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.9.0
- [cygnus-twitter][Docker] Upgrade to CentOS 7, Maven 3.5.2 and Java 1.8.0 in Dockerfile

* Wed Sep 13 2017 Fermin Galan <fermin.galanmarquez@telefonica.com> 1.8.0
- [cygnus-twitter][bug] Fix wrong data in spec file (#1407)

* Tue Jan 31 2017 Francisco Romero <francisco.romerobueno@telefonica.com> 1.7.0

* Fri Dec 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.6.0

* Wed Nov 02 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.5.0

* Fri Oct 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.4.0

* Fri Sep 09 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.3.0
- [cygnus][hardening] Add architecture documentation (#1127)
- [cygnus][feature] Standardize logs layout (#1118)
- [cygnus][doc] Add Orion-Cygnus-Kafka integration example (#1132)

* Wed Sep 07 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.1
- [cygnus][hardening] Add architecture documentation (#1127)

* Fri Jul 01 2016 Francisco Romero <francisco.romerobueno@telefonica.com> 1.2.0
- [cygnus-twitter][feature] Add cygnus-twitter (#1023)
- [cygnus-twitter] [feature] Log time in UTC format (#1071)
- [cygnus-twitter] Add documentation about logs and alarms (#1092)

