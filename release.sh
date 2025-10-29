#!/bin/bash

mvn release:clean release:prepare -DgenerateBackupPoms=false &&
mvn release:perform -Darguments="-Dmaven.deploy.skip=true"