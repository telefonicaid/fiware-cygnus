# Copyright 2014 Telefonica Investigación y Desarrollo, S.A.U
# 
# This file is part of fiware-connectors (FI-WARE project).
# 
# cosmos-injector is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
# Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
# later version.
# cosmos-injector is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
# warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
# details.
# 
# You should have received a copy of the GNU Affero General Public License along with fiware-connectors. If not, see
# http://www.gnu.org/licenses/.
# 
# For those usages not covered by the GNU Affero General Public License please contact with Francisco Romero
# frb@tid.es

Summary:          Package for cygnus component
Name:             cygnus
Version:          %{_product_version}
Release:          %{_product_release}
License:          AGPLv3
BuildRoot:        %{_topdir}/BUILDROOT/
BuildArch:        x86_64
Requires(post):   /sbin/chkconfig, /usr/sbin/useradd
Requires(preun):  /sbin/chkconfig, /sbin/service
Requires(postun): /sbin/service
Group:            Applications/cygnus
Vendor:           Telefonica I+D

%description
This connector is a (conceptual) derivative work of ngsi2cosmos, and implements
a Flume-based connector for context data coming from Orion Context Broker.

# Project information 
%define _project_name cygnus
%define _project_user cygnus
%define _service_name cygnus

# System folders
# _sourcedir =${topdir}/SOURCES
%define _srcdir %{_sourcedir}/../../../
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
# Build section:
# -------------------------------------------------------------------------------------------- #
%build
# Read from BUILD, write into BUILD

echo "[INFO] Building..."

# -------------------------------------------------------------------------------------------- #
# pre-install section:
# -------------------------------------------------------------------------------------------- #
%pre
# Read from BUILD, write into BUILDROOT

echo "[INFO] Creating %{_project_user} user"
getent group  %{_project_user} >/dev/null || groupadd -r %{_project_user}
getent passwd %{_project_user} >/dev/null || useradd -r -g %{_project_user} -m -s /bin/bash -c 'Cygnus user account' %{_project_user}

# -------------------------------------------------------------------------------------------- #
# Install section:
# -------------------------------------------------------------------------------------------- #
%install
# Read from BUILD and write into BUILDROOT
# RPM_BUILD_ROOT = BUILDROOT

echo "[INFO] Installing the %{name}"

echo "[INFO] Creating the directories"
mkdir -p %{_build_root_project}/init.d
# Create folder to store the PID (used by the Service)
mkdir -p %{buildroot}/var/run/%{_project_name}
# Create log folder
mkdir -p %{buildroot}/${_log_dir}
# Create /etc/cron.d directory
mkdir -p %{buildroot}/etc/cron.d
# Create /etc/logrotate.d directory
mkdir -p %{buildroot}/etc/logrotate.d

cp -R %{_builddir}/usr/cygnus/*                       %{_build_root_project}
cp %{_builddir}/init.d/%{_service_name}               %{_build_root_project}/init.d/%{_service_name}
cp %{_builddir}/config/*                              %{_build_root_project}/conf/
cp %{_builddir}/cron.d/cleanup_old_cygnus_logfiles    %{buildroot}/etc/cron.d
cp %{_builddir}/logrotate.d/logrotate-cygnus-daily    %{buildroot}/etc/logrotate.d

# -------------------------------------------------------------------------------------------- #
# post-install section:
# -------------------------------------------------------------------------------------------- #
%post

echo "[INFO] Configuring application"
mkdir -p /etc/%{_project_name}
echo "[INFO] Creating links"
ln -s %{_project_install_dir}/init.d/%{_service_name} /etc/init.d/%{_service_name}
ln -s %{_project_install_dir}/conf/flume.conf /etc/%{_project_name}/flume.conf
ln -s %{_project_install_dir}/bin/flume-ng /usr/bin/flume-ng

#Logs
echo "[INFO] Creating log directory"
mkdir -p %{_log_dir}
chown %{_project_user}:%{_project_user} %{_log_dir}
chmod g+s %{_log_dir}
setfacl -d -m g::rwx %{_log_dir}
setfacl -d -m o::rx %{_log_dir}

echo "[INFO] Configuring application service"
# FIXME! Not supported
# chkconfig --add %{_service_name}
echo "Done"

# -------------------------------------------------------------------------------------------- #
# pre-uninstall section:
# -------------------------------------------------------------------------------------------- #
%preun

echo "[INFO] Uninstall the %{_project_name}"
/etc/init.d/%{_service_name} stop
/sbin/chkconfig --del %{_service_name}

echo "[INFO] Deleting links"
rm /etc/init.d/%{_service_name} \
/etc/%{_project_name}/flume.conf \
/usr/bin/flume-ng

echo "[INFO] Removing application log files"
[ -d %{_log_dir} ] && rm -rfv %{_log_dir} &> /dev/null

echo "[INFO] Deleting the %{_project_name} folder"
[ -d %{_project_install_dir} ] && rm -rfv %{_project_install_dir} &> /dev/null

echo "Done"

# -------------------------------------------------------------------------------------------- #
# post-uninstall section:
# clean section:
# -------------------------------------------------------------------------------------------- #
%postun
%clean
rm -rf $RPM_BUILD_ROOT

# -------------------------------------------------------------------------------------------- #
# Files to add to the RPM
# -------------------------------------------------------------------------------------------- #
%files
%defattr(755,%{_project_user},%{_project_user},755)
%attr(0644, root, root) /etc/cron.d/cleanup_old_cygnus_logfiles

%{_project_install_dir}
/var/
