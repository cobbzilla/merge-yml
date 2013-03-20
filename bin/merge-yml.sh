#!/bin/bash

THISDIR=`dirname $0`
cd ${THISDIR}/..
THISDIR=`pwd`

java -jar target/tout-mergeyml-1.0.0-SNAPSHOT.jar "$@"
