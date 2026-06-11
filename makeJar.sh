#!/bin/bash

# compile my framework with servlet_api.jar in the classpath in package bin
javac -cp ../lib/servlet_api.jar -d ../src/ ../src/*.java

# create in the package bin
cd ../src/
jar cvf ../lib/Framework.jar *.class