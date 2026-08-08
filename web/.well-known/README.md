# App Links

`assetlinks.json` goes at `https://gakseong.app/.well-known/assetlinks.json`, served as
`application/json` over HTTPS with no redirect. Android fetches it on install and again periodically.

Until it is up, a `gakseong.app/s/<code>` link opens a browser chooser instead of the app. §Referral still
works in that case through the Play Install Referrer, which is the whole reason the design specifies both: the
App Link serves the invitee who already has the app, and Install Referrer serves the one who does not.

The fingerprint below is the **debug** signing key from this machine. Replace it with the release key's before
publishing, or add it alongside — the array takes several, which is what lets debug and release builds both
verify.

    keytool -list -v -keystore <release.keystore> -alias <alias> | grep SHA256

Verify a deployed file with:

    adb shell pm verify-app-links --re-verify app.gakeseong
    adb shell pm get-app-links app.gakeseong

Locally, without hosting anything:

    adb shell pm set-app-links --package app.gakeseong 2 gakseong.app
