#!/bin/sh
set -e

# Fix volume mount permissions — runs as root before dropping to khansoft.
# Needed because Docker named volumes are created root-owned on first mount.
chown -R khansoft:khansoft /app/images /app/logs 2>/dev/null || true

exec su-exec khansoft sh -c "exec java $JAVA_OPTS -jar /app/app.jar"
