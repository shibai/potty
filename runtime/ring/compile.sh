#!/bin/bash

export POKE_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../.." && pwd )"

find ${POKE_HOME} -name *.java > sources_list.txt
javac -d ${POKE_HOME}/classes -classpath .:${POKE_HOME}/lib/'*' @sources_list.txt