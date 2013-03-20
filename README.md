merge-yml
=========

Merge multiple YML files into a single file.

(c) Copyright 2013 Jonathan Cobb.
This code is available under the Apache License, version 2: http://www.apache.org/licenses/LICENSE-2.0.html

## Usage:

./bin/merge-yml.sh file1.yml file2.yml ...

Files are merged in order, such that files listed later will override files listed earlier.

The merged result is written to stdout. Logs (for info & errors) are written to stderr.

