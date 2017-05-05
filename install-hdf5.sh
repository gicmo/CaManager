#!/bin/sh

H5J_VERSION=${H5J_VERSION:-2.11.0}
H5J_HOME=${H5J_HOME:-~/Downloads}

echo "HOME:    $H5J_HOME"
echo "VERSION: $H5J_VERSION"

mvn install:install-file -Dfile=$H5J_HOME/jarhdf-$H5J_VERSION.jar -DgroupId=org.hdfgroup -DartifactId=jhdf -Dversion=$H5J_VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$H5J_HOME/jarhdfobj.jar -DgroupId=org.hdfgroup -DartifactId=jhdfobj -Dversion=$H5J_VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$H5J_HOME/jarhdf5-$H5J_VERSION.jar -DgroupId=org.hdfgroup -DartifactId=jhdf5 -Dversion=$H5J_VERSION -Dpackaging=jar
mvn install:install-file -Dfile=$H5J_HOME/jarh5obj.jar -DgroupId=org.hdfgroup -DartifactId=jhdf5obj -Dversion=$H5J_VERSION -Dpackaging=jar


