#!/bin/bash

DIR=$( cd "$( /usr/bin/dirname "${BASH_SOURCE[0]}" )" && /bin/pwd )
export PATH=$PATH:"$DIR/../"
#>&2 echo $PATH