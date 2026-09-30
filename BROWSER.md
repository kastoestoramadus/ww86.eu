# Checking pages in a browser

`playwright-core@1.63.0` from npm in your scratchpad, browsers in `~/.cache/ms-playwright`.
On this WSL unpack `libnspr4 libnss3 libasound2t64` without sudo (`apt-get download`, `dpkg-deb -x <deb>
<scratch>/libs`) and run with `LD_LIBRARY_PATH=<scratch>/libs/usr/lib/x86_64-linux-gnu`. Sites that refuse
curl, WebFetch and the headless shell (Leroy Merlin, Carrefour, Facebook) open in the full
`chromium-1243/chrome-linux64/chrome`, headless, with a desktop user agent and
`--disable-blink-features=AutomationControlled`; `DISPLAY=:0` (WSLg) gives a window if the user must log in.
