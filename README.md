# Lily Toggle

Minimal root bridge app for Xiaomi Mi Smart Clock X04G.

## What it does

When launched from Olauncher:
1. Checks whether `lily-toggle.sh` is already running.
2. If not, asks Magisk `su` to start it in the background.
3. Closes immediately.

The actual listener remains at:

`/data/adb/modules/lily-toggle/lily-toggle.sh`

Expected listener behavior:
- Volume Up: Clock <-> Lily
- Volume Down: Android HOME / Olauncher
- Mute: unchanged

## X04G

Target Android: 10
Minimum Android: 8.0
Application is pure Java and does not require native libraries.

## Build

GitHub Actions -> Build Lily Toggle -> artifact `LilyToggle-debug`.
