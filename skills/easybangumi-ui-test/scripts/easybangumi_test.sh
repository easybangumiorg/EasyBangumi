#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../" && pwd)"
STATE_DIR="/private/tmp/easybangumi-ui-test"
mkdir -p "$STATE_DIR"

usage() {
  cat <<'EOF'
Usage:
  easybangumi_test.sh serve <dir> [port]
  easybangumi_test.sh stop-serve [port]
  easybangumi_test.sh reverse <port>
  easybangumi_test.sh unreverse <port>
  easybangumi_test.sh clear <package>
  easybangumi_test.sh launch <package>
  easybangumi_test.sh dump-ui [output-file]
  easybangumi_test.sh tap <x> <y>
  easybangumi_test.sh text <value>
  easybangumi_test.sh swipe <x1> <y1> <x2> <y2> [duration-ms]
  easybangumi_test.sh screenshot [output-file]
  easybangumi_test.sh current-focus
EOF
}

require_adb() {
  command -v adb >/dev/null 2>&1 || {
    echo "adb not found" >&2
    exit 1
  }
}

require_python() {
  command -v python3 >/dev/null 2>&1 || {
    echo "python3 not found" >&2
    exit 1
  }
}

serve() {
  local dir="$1"
  local port="${2:-18080}"
  local pid_file="$STATE_DIR/http-${port}.pid"
  local log_file="$STATE_DIR/http-${port}.log"

  require_python

  if [[ ! -d "$dir" ]]; then
    echo "mock dir not found: $dir" >&2
    exit 1
  fi

  if [[ -f "$pid_file" ]] && kill -0 "$(cat "$pid_file")" 2>/dev/null; then
    echo "server already running on port $port (pid $(cat "$pid_file"))"
    return 0
  fi

  nohup python3 -m http.server "$port" --bind 127.0.0.1 --directory "$dir" \
    >"$log_file" 2>&1 < /dev/null &
  echo $! >"$pid_file"
  echo "started mock server on http://127.0.0.1:$port"
  echo "pid file: $pid_file"
  echo "log file: $log_file"
}

stop_serve() {
  local port="${1:-18080}"
  local pid_file="$STATE_DIR/http-${port}.pid"

  if [[ ! -f "$pid_file" ]]; then
    echo "no pid file for port $port"
    return 0
  fi

  local pid
  pid="$(cat "$pid_file")"
  if kill -0 "$pid" 2>/dev/null; then
    kill "$pid"
    echo "stopped mock server pid $pid on port $port"
  else
    echo "stale pid file for port $port"
  fi
  rm -f "$pid_file"
}

reverse_port() {
  local port="$1"
  require_adb
  adb reverse "tcp:${port}" "tcp:${port}"
}

unreverse_port() {
  local port="$1"
  require_adb
  adb reverse --remove "tcp:${port}" || true
}

clear_app() {
  local pkg="$1"
  require_adb
  adb shell pm clear "$pkg"
}

launch_app() {
  local pkg="$1"
  require_adb
  adb shell monkey -p "$pkg" -c android.intent.category.LAUNCHER 1
}

dump_ui() {
  local output="${1:-}"
  require_adb
  if [[ -n "$output" ]]; then
    adb exec-out uiautomator dump /dev/tty >"$output"
    echo "ui dumped to $output"
  else
    adb exec-out uiautomator dump /dev/tty
  fi
}

tap() {
  require_adb
  adb shell input tap "$1" "$2"
}

send_text() {
  require_adb
  adb shell input text "$1"
}

swipe() {
  require_adb
  local duration="${5:-300}"
  adb shell input swipe "$1" "$2" "$3" "$4" "$duration"
}

screenshot() {
  local output="${1:-$ROOT_DIR/easybangumi-screenshot-$(date +%Y%m%d-%H%M%S).png}"
  require_adb
  adb exec-out screencap -p >"$output"
  echo "screenshot saved to $output"
}

current_focus() {
  require_adb
  adb shell dumpsys window | rg 'mCurrentFocus|mFocusedApp'
}

cmd="${1:-}"
shift || true

case "$cmd" in
  serve) serve "$@" ;;
  stop-serve) stop_serve "$@" ;;
  reverse) reverse_port "$@" ;;
  unreverse) unreverse_port "$@" ;;
  clear) clear_app "$@" ;;
  launch) launch_app "$@" ;;
  dump-ui) dump_ui "$@" ;;
  tap) tap "$@" ;;
  text) send_text "$@" ;;
  swipe) swipe "$@" ;;
  screenshot) screenshot "$@" ;;
  current-focus) current_focus "$@" ;;
  *)
    usage
    exit 1
    ;;
esac
