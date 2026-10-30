# CardSaviour

Android app that reads a business card with the camera and opens the phone’s Add Contact screen. The photo and the fields stay in memory only until you leave the card. Nothing is stored in the app, and nothing is uploaded.

## Flow

1. Allow the camera, or choose a photo if the camera is unavailable.
2. Fit the card in the frame. Tap **Scan card**, or leave **Capture when the card is steady** on.
3. Correct any highlighted field.
4. **Save to phone book** opens the system Add Contact screen, already filled in.
5. **Download vCard** writes a `.vcf` file you choose in the system picker.
6. **Generate QR of this contact** shows a QR code of that same vCard.

Text is read on the device with ML Kit’s bundled Latin model. The app does not request a contacts permission; the contacts app saves the person after you confirm.

This app reads text from the photo and creates a contact file the phone can save.

## Not in this app

- No database of cards. After you leave the review screen, the photo is dropped.
- No Google Drive backup and no background sync. The phone book already syncs with the account on the device.
- No accessibility service.

## Build

Open this folder in Android Studio (AGP 9.4, JDK 17 or 21).

- `./gradlew test`
- `./gradlew lintDebug`
- `./gradlew assembleDebug`
- `./gradlew bundleRelease` for a Play App Bundle. Release builds use R8.

Signing is off until these are set in the environment or in `~/.gradle/gradle.properties`:

- `CARD_KEYSTORE_FILE`
- `CARD_KEYSTORE_PASSWORD`
- `CARD_KEY_ALIAS`
- `CARD_KEY_PASSWORD`

SDK: min 33, target 36. compileSdk is 37 because the current AndroidX releases (Compose, Navigation, core) refuse to compile against 36. The app does not opt into API 37 behavior.

## Themes

Aurora has four themes — Ink, Harbor, Copper, Grove — each in day, night, or system. The choice is kept in DataStore on the device.
