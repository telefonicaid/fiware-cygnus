Summary:          MGW Api Thor
Name:             tdaf-api-thor
Version:          %{_product_version}
Release:          1%{?dist}
License:          AGPLv3
BuildRoot:        %{_topdir}/BUILDROOT/
BuildArch:        noarch
Requires(post):   /sbin/chkconfig, /usr/sbin/useradd
Requires(preun):  /sbin/chkconfig, /sbin/service
Requires(postun): /sbin/service
Group:            Applications/MicroGateway
Vendor:           Telefonica I+D

%description
This connector is a (conceptual) derivative work of ngsi2cosmos, and implements 
a Flume-based connector for context data coming from Orion Context Broker.

# Project information
%define _prefix_company tid
%define _project_name tdaf-api-thor
%define _project_user thor
%define _service_name thor

# System folders
%define _srcdir %{_sourcedir}/../../
%define _install_dir /opt
%define _project_install_dir %{_install_dir}/%{_prefix_company}/%{_project_name}

# _localstatedir is a system var that goes to /var
%define _log_dir %{_localstatedir}/log/%{_project_name}

# RPM Building folder
%define _build_root_project %{buildroot}%{_project_install_dir}


# -------------------------------------------------------------------------------------------- #
# prep section, setup macro:
# -------------------------------------------------------------------------------------------- #
%prep
echo "[INFO] Preparing installation"
# Create rpm/BUILDROOT folder
rm -Rf $RPM_BUILD_ROOT && mkdir -p $RPM_BUILD_ROOT
[ -d %{_build_root_project} ] || mkdir -p %{_build_root_project}

# Copy all from src to rpm/BUILDROOT/opt/tid/tdaf-api-thor
#cp -R %{_srcdir}/bin \
#      %{_srcdir}/config \
#      %{_build_root_project}

# Copy "extra files" from rpm/SOURCES to rpm/BUILDROOT
cp -R %{_sourcedir}/* %{buildroot}

# Create folder to store the PID (used by the Service)
mkdir -p %{buildroot}/var/run/%{_project_name}
# Create log folder
mkdir -p %{buildroot}/var/log/%{_project_name}

# -------------------------------------------------------------------------------------------- #
# Build section:
# -------------------------------------------------------------------------------------------- #
%build

# -------------------------------------------------------------------------------------------- #
# pre-install section:
# -------------------------------------------------------------------------------------------- #
%pre
echo "[INFO] Creating %{_project_user} user"
grep ^%{_project_user} /etc/passwd
RET_VAL=$?
if [ "$RET_VAL" != "0" ]; then
      /usr/sbin/useradd -c '%{_project_user}' -s /bin/false \
      -r -d %{_project_install_dir} %{_project_user}
      RET_VAL=$?
      if [ "$RET_VAL" != "0" ]; then
         echo "[ERROR] Unable create user" \
         exit $RET_VAL
      fi
fi

# -------------------------------------------------------------------------------------------- #
# post-install section:
# -------------------------------------------------------------------------------------------- #
%post
echo "Configuring application... "


#Logs
echo "Done"

# -------------------------------------------------------------------------------------------- #
# pre-uninstall section:
# -------------------------------------------------------------------------------------------- #
%preun
if [ $1 == 0 ]; then


	echo "[INFO] Removing application log files"
	# Log
	[ -d %{_log_dir} ] && rm -rfv %{_log_dir}

	echo "[INFO] Removing application files"
	# Installed files
	[ -d %{_project_install_dir} ] && rm -rfv %{_project_install_dir}

   echo "Done"
fi

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
%config /etc/logrotate.d/%{_project_name}-daily
%config %attr(0644, root, root) /etc/cron.d/logrotate-api-thor-size
%config /etc/init.d/%{_service_name}
%{_project_install_dir}
/var/
