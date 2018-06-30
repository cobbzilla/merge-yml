#!/bin/bash
#
# Usage:
#
#   yaml-merge.sh file1.yaml [file2.yaml] [file3.yaml] ...
#
# Merge multiple YAML files into a single YAML file.
# Files are merged in order, such that files listed later will override files listed earlier.
#

function die {
  echo "${1}" >&2 && exit 1
}

if [ -z "${1}" ] ; then
  die "No files to merge"
fi

THISDIR=$(cd $(dirname $0) && pwd)
TARGET=$(cd ${THISDIR}/../target && pwd)
JAR=$(ls -1 ${TARGET}/merge-yml-*.jar | head -1)
if [ -z "${JAR}" ] ; then
  die "No jar matching yaml-merge-*.jar found in ${TARGET}"
fi

java -jar "${JAR}" "${@}"
