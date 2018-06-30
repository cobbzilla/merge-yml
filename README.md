
  YAML Merge
==============


Merges multiple YAML files into a single file, and substitutes environment variables found as `{{VARIABLE_NAME}}`.

This is a fork by Ondrej Zizka, with upgraded dependencies.

Original code from 2013 by Jonathan Cobb.
This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html

## Build

    mvn -P uberjar package
    
Requires [Maven build automation tool](http://maven.apache.org/).


## Usage

    ./bin/yaml-merge.sh file1.yaml file2.yaml ... > merged-result.yaml

Files are merged in order, such that files listed later will override files listed earlier.

The merged result is written to `stdout`. Logs (for info & errors) are written to `stderr`.

Within the YAML files to be merged, you may include references to environment variables using
mustache-style syntax, for example:

    callbackUrl: http://www.{{DEPLOY_HOST_NAME}}.example.com:8102/my_callback

Then make sure you've exported the `DEPLOY_HOST_NAME` environment variable to whatever place you invoke `yaml-merge` from.
