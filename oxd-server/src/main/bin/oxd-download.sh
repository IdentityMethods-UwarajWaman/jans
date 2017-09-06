#!/bin/sh

warName=oxd-local-3.1.0.Final-distribution.zip
distDir=oxd-dist

# Clean up
rm -f $warName
rm -f -r $destDir

# Download and unzip
wget http://ox.gluu.org/maven/org/xdi/oxd-local/3.1.0.Final/$warName
unzip $warName -d $distDir