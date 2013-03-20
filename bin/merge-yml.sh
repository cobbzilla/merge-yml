#!/bin/bash

ORIG_DIR=`pwd`

THISDIR=`dirname $0`
cd ${THISDIR}/..
THISDIR=`pwd`

cd ${ORIG_DIR}

java -jar ${THISDIR}/target/mergeyml-1.0.0-SNAPSHOT.jar "$@"
