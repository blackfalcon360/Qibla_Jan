# Qibla

A full-screen **black** screen with one **long arrow** that points to the Qibla, with the **Kaaba emoji 🕋** on top of the arrow.

Below the arrow:
- **Qibla Direction** in degrees (from true north) and how far to turn ("Turn 47° right")
- **Distance to the Kaaba** in km
- Your **GPS coordinates in DMS** (degrees ° minutes ′ seconds ″), e.g. `33°54′12.3″ N   72°24′36.1″ E`
- Heading and the GPS accuracy

The arrow turns **green** when you face the Qibla (within 3°). Top-right corner: **By: Black Falcon**.

Package: `qiblaarrow.blackfalcon.jan` — App name: **Qibla** — APK: `Qibla.apk`

## Works offline
- Position comes from the phone's **GPS chip**, which needs no internet. The app has **no INTERNET permission** at all.
- The last position is saved, so the arrow still works if GPS has no fix yet.
- Magnetic declination is calculated on the phone.
- First GPS fix without internet can take a minute or two outdoors.

## Notes
- "Up" on the screen is the way you are facing: phone flat → top of the phone; phone upright → back of the phone.
- Keep the phone away from magnets and metal; move it in a figure-8 to calibrate if the arrow seems off.
- Android 8.0+.

## Build
Push to GitHub — the **Build APK** workflow runs automatically.
Open **Actions → latest run → Artifacts → Qibla** to download `Qibla.apk`.
