#!/bin/bash

# http://stackoverflow.com/questions/59895/getting-the-source-directory-of-a-bash-script-from-within?noredirect=1&lq=1
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"

set -ex

cd $DIR

lein uberjar

cd -

java -jar $DIR/target/uberjar/testapp-0.1.0-SNAPSHOT-standalone.jar $@
