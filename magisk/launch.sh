#!/system/bin/sh

SCRIPT="/data/adb/modules/lily-toggle/lily-toggle.sh"
PIDFILE="/data/adb/modules/lily-toggle/lily-toggle.pid"
LOG="/data/local/tmp/lily-toggle.log"

# Already running: nothing to do.
if [ -f "$PIDFILE" ]; then
    PID="$(cat "$PIDFILE" 2>/dev/null)"
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
        exit 0
    fi
fi

# Clean stale pid.
rm -f "$PIDFILE"

# Detach from the calling su/app process.
# setsid is provided by Android toolbox/toybox on this platform.
(
    exec setsid /system/bin/sh -c "
        echo \$\$ > '$PIDFILE'
        exec /system/bin/sh '$SCRIPT'
    "
) >/dev/null 2>&1 </dev/null &

# Give the detached process a moment to establish its pid.
sleep 0.3

if [ -f "$PIDFILE" ]; then
    PID="$(cat "$PIDFILE" 2>/dev/null)"
    if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
        exit 0
    fi
fi

exit 1
