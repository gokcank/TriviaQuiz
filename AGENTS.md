# TriviaQuiz

Offline Turkish-language trivia game for Android (`com.gokcank.triviaquiz`) with AdMob ads and Play Games achievements/leaderboards.

## Build & test
- JDK 17; compile/target SDK 36, minSdk 24. Pure CLI workflow: never refer to Android Studio menus.
- Debug APK: `./gradlew assembleDebug`
- Lint: `./gradlew lintDebug`
- Tests: `./gradlew test` (note: there are no test sources yet, so this passes trivially)
- Release AAB: `./gradlew bundleRelease` (R8 + resource shrinking, full native debug symbols)
- CI: `.github/workflows/android-ci.yml` runs lintDebug, test, assembleDebug on push/PR to `master`/`main`.
  Runs queue (`cancel-in-progress: false`).
- Default branch is `master`.
- Without `local.properties` the build uses Google's test AdMob IDs, debug signing, and Play Games switches itself off.

## Layout
- `app/src/main/java/com/gokcank/triviaquiz/`
  - `Navigation.kt`, `NavigationKeys.kt` — Navigation 3 host and NavKey classes.
  - `ads/` — banner, interstitial, rewarded (rewarded ads refill lifelines).
  - `data/` — repositories (questions, daily challenge, favorites, stats, settings, XP); `data/model/`.
  - `games/PlayGamesManager.kt` — Play Games Services v2.
  - `ui/`, `theme/`, `util/` (FeedbackManager: SoundPool + haptics).
- `app/src/main/assets/trivia_tr.json` — the bundled question bank (433 questions, 13 categories).

## Project rules
- The app is 100% Turkish (UI, questions, feedback); keep the architecture ready for more languages.
- Keep `android:configChanges` on the main Activity: rotation is handled in place. Removing it brings back a
  rotation crash (the portrait lock was removed for large-screen support).
- Keep `statusBarsPadding()` on top bars (quiz X button, timer, lifelines): edge-to-edge is forced on Android 15.
- Edge-to-edge uses `WindowInsetsController`; do not reintroduce manual system-bar coloring.
- R8 keep rules in `app/proguard-rules.pro` must cover every NavKey class, `@Serializable` models, the ads
  singletons, `data.**`, and Room/WorkManager (used internally by AdMob). Add a keep rule for each new NavKey.
- The Play Games App ID goes through a string resource (`resValue`), never directly into manifest meta-data
  (aapt parses it as an int and the SDK crashes).
- Non-store builds must use Google's test ad IDs; live AdMob IDs are only for the official store release build.
- `androidx.fragment` is pinned explicitly (1.9.0) to override an outdated transitive version.
- The Play Console "missing native debug symbols" warning is harmless (no native code of our own); not a blocker.
- Agent split (if two agents are active): app source (`app/src`) vs repo config/docs/build/store prep; do not edit the
  other agent's active area.

## Status (as of 2026-11-07)
- v1.2.1 (`versionCode = 5`, 2026-08-26): Play Console maintenance release. Nothing in progress.

## Open items
- Shelved ideas, only on request: remove-ads purchase, coin economy, more questions, English question bank.
- No automated tests yet.

## More context
Background, decisions and history are in the second brain: `/home/gokcank/ikinci-beyin/projects/trivia-quiz-app.md` (read on demand).
