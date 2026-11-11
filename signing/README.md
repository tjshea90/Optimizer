# Signing

Every APK this repo releases is signed by GitHub Actions with the **same key**, so each update installs over the last one on the tablet
(Android refuses an update signed by a different key and forces an uninstall, which erases the app's data).

- `dev/optimizer-dev.jks` is a **PUBLIC dev key, committed on purpose** (alias and password `optimizer-dev`), the same arrangement the owner chose for
  Launcher ("it can be signed by github if possible, security isn't important"). The repo is public, so anyone can sign an APK with it; that only
  matters if someone tricks the owner into installing it. `tools/secretscan.sh` allows this one path and nothing else.
- `CERT_SHA256` pins that key's certificate. `tools/verify-apk.sh` and the `android.yml` gate fail a build whose certificate is different,
  and `tools/test_handoff.sh` fails if the pin and the committed keystore ever disagree.
- **Never regenerate the key once anything signed with it is installed.** Never change the `applicationId` either.
- To move to a private key later (the workflow already supports it): add the repo secrets `SIGNING_KEYSTORE_BASE64` (`base64 -w0` of the keystore),
  `SIGNING_STORE_PASSWORD`, `SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, then re-pin `CERT_SHA256` on purpose. v3 signing is enabled so a later
  rotation does not need an uninstall. The owner runs that step; a key is never pasted into chat.
