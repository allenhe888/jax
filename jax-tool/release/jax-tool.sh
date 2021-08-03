#!/usr/bin/env bash

target="$0"
# For the case, the executable has been directly symlinked, figure out
# the correct bin path by following its symlink up to an upper bound.
# Note: we can't use the readlink utility here if we want to be POSIX
# compatible.
iteration=0
while [ -L "$target" ]; do
    if [ "$iteration" -gt 100 ]; then
        echo "Cannot resolve path: You have a cyclic symlink in $target."
        break
    fi
    ls=`ls -ld -- "$target"`
    target=`expr "$ls" : '.* -> \(.*\)$'`
    iteration=$((iteration + 1))
done

# Convert relative path to absolute path
DIR=`dirname "$target"`


if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    echo "JAVA_HOME is not set and java could not be found."
    exit 1
fi

JAX_TOOL_OPTS="-Xmx1024m -Xms256m"
JAX_TOOL_MAIN="com.eoi.jax.tool.JaxTool"
JAX_TOOL_LIB="${DIR}/lib/*"
JAX_TOOL_CP="${JAX_TOOL_CLASS_PATH}:${JAX_TOOL_LIB}"

echo ">>> ${JAX_TOOL_OPTS} -classpath ${JAX_TOOL_CP} ${JAX_TOOL_MAIN} $@"

exec "$JAVA" ${JAX_TOOL_OPTS} -classpath ${JAX_TOOL_CP} ${JAX_TOOL_MAIN} "$@"
