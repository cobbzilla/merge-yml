merge-yml
=========

Merge multiple YML files into a single file, and substitute for any environment variables found

(c) Copyright 2013 Jonathan Cobb.
This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html

## Build

    mvn -P uberjar package
    
Requires 'maven' build automation tool.

Install maven on OS X using [homebrew](http://brew.sh/): `brew install maven`

## Usage

    ./bin/merge-yml.sh file1.yml file2.yml ... > merged-result.yml

Files are merged in order, such that files listed later will override files listed earlier.

The merged result is written to stdout. Logs (for info & errors) are written to stderr.

Within the YML files to be merged, you may include references to system environment variables using
mustache-style syntax, for example:

    callbackUrl: http://www.{{DEPLOY_HOST_NAME}}.example.com:8102/my_callback

Then make sure you've exported the DEPLOY_HOST_NAME environment variable to whatever place you invoke
merge-yml from.
