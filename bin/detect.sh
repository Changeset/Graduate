#!/bin/bash

# This program is used to intrusion detecting.
# Xuelong Liao <cserlxl@gmail.com>

print_help()
{
    echo "IDS start | detect | stop | kill"
    echo ""
    echo "'IDS start' : starts IDS"
    echo "'IDS detect' : run detect program"
    echo "'IDS stop' : stop IDS"
    echo "'IDS kill' : kill IDS"
}

IDS_ROOT = "$( cd "$( dirname "${BASH_SOURCE[0]}" ) "/../ && pwd )"
CLASSPATH="$IDS_ROOT/build:$IDS_ROOT/lib/*"
JAVALIBPATH="$IDS_ROOT/lib/"
JVMARGS="-server -Xms8G -Xmx16G"
JVMARGSDEBUG="-server -Xms8G -Xmx16G -XX:+UseConcMarkSweepGC"

pushd "${IDS_ROOT}" > /dev/null

if [ $# -eq 0 ] ; then
    print_help
else
    if [ $1 = "detect" ] ; then
        java -Djava.library.path="$JAVALIBPATH" -cp "$CLASSPATH" $JVMARGSDEBUG ids.core.Detect $2
    fi
fi

popd > /dev/null