#!/bin/bash
mkdir -p ~/.bintray
eval "echo \"$(< ./travis/bintray.template)\"" > ~/.bintray/.credentials