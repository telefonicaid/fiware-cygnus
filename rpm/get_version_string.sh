#!/usr/bin/env bash
# Copyright 2013 Telefonica Investigacion y Desarrollo, S.A.U
#
# This file is part of fiware-cygnus (FI-WARE project).
#
# fiware-cygnus is free software: you can redistribute it and/or
# modify it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
#
# fiware-cygnus is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero
# General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with fiware-cygnus. If not, see http://www.gnu.org/licenses/.
#
# For those usages not covered by this license please contact with
# iot_support at tid dot es

# Bash lib to know the RPM version and revision from a GitHub repository
# Call method get_rpm_version_string to obtain them for rpmbuild
#

if [[ $(ps -hp  $$ | grep bash) ]]; then
  shopt -s extglob
elif [[ $(ps -hp  $$ | grep zsh) ]]; then
  setopt kshglob
fi

get_branch()
{
    git rev-parse --abbrev-ref HEAD
}

## Specific functions according the TID workflow
get_branch_type()
{
    local branch="$(get_branch)"
    case $branch in
        release/*) echo "release";;
        develop) echo "develop";;
        master) echo "stable";;
        *) echo "other";;
    esac
}

get_version_string()
{
    local branch branch_name describe_tags version ancestor release
    case $(get_branch_type) in
        stable)
           # If we are on stable branch get last tag as the version, but transform to x.x.x-x-SHA1
           describe_tags="$(git describe --tags --long  --match "[[:digit:]]*.[[:digit:]]*.[[:digit:]]*" 2>/dev/null)"
           version="${describe_tags%-*-*}"
           echo "${version%.*}-${version#*.*.*.}-$(git log --pretty=format:'%h' -1)"
        ;;
        develop)
          ## If we are in develop use the total count of commits of the repo
          total_commit_number=$(git rev-list --all --count)
          short_hash=$(git rev-parse --short HEAD)
          version="$(git describe --tags --long  --match "[[:digit:]]*.[[:digit:]]*.[[:digit:]]*" 2>/dev/null)"
          version="${version%-*-*}"
          version="${version%KO}"
          echo "${version}-${total_commit_number}-${short_hash}"
        ;;
        release)
          ## in release branches the version is a tag named
          branch_name="$(get_branch)"
          branch_name="${branch_name#*/}"
          describe_tags="$(git describe --tags --long  --match ${branch_name} 2>/dev/null)"
          version="${describe_tags%-*-*}"
          version="${version%KO}"
          release=${describe_tags#*.*.*-}
          echo "${version}-${release}"
        ;;
        other)
            ## We are in detached mode, use the last x-y-z tag
            version="$(git describe --tags --long  --match "[[:digit:]]*.[[:digit:]]*.[[:digit:]]*" 2>/dev/null)"
            version="${version%-*-*}"
            version="${version%KO}"
            echo "${version}"
        ;;
        *)
           # RMs don't stablish any standard here, we use branch name as version
           version=$(get_branch)
           # Using always develop as parent branch does not describe correctly the number of revision
           # for branches not starting there, but works as an incremental rev
           ancestor="$(git merge-base $version develop)"
           version=${version#*/}
           local res="$(git log --oneline ${ancestor}.. --pretty='format:%h')"
           ## wc alone does not get the last line when there's no new line
           [[ -z $res ]] && rel=0 || rel=$(echo "$res" | wc -l | tr -d ' ')
           echo "${version}-${rel}-g$(git log --pretty=format:'%h' -1)"
    esac
}

get_rpm_version_string() {
    local version_string ver rel
    version_string="$(get_version_string)"
    ver="${version_string%-*-*}"
    rel="${version_string:$((${#ver}+1))}"
    echo "${ver//[[:space:]-\/#]}" "${rel//[-]/.}"
}

#cd $1
#get_version_string| cut -d "-" -f $2
