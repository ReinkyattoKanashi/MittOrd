# MittOrd

A personal vocabulary app for Android. Save a word, give it translations in any
number of languages, add a note and a photo — and find it again later.

Built as a learning project while picking up Norwegian, which is where the name comes
from (*mitt ord* — "my word"). The dictionary itself is language-agnostic: a word can
carry translations into as many languages as you like, and the app works out which
language each field is in on its own.

<!--
Screenshots go here. See "Adding screenshots" at the bottom of this file.

<p align="center">
  <img src="docs/screenshots/home.png" width="24%" alt="Word list" />
  <img src="docs/screenshots/add-word.png" width="24%" alt="Adding a word" />
  <img src="docs/screenshots/word-detail.png" width="24%" alt="Word editor" />
  <img src="docs/screenshots/settings.png" width="24%" alt="Settings" />
</p>
-->

## What it does

- **Word list** with instant search across words and their translations.
- **Add a word without leaving the list** — the bottom bar expands into a form
  instead of pushing a new screen.
- **Automatic language detection.** Start typing and the field marks itself with the
  right flag; you can always override it by hand.
- **One-tap translation** — pick a target language and the translation field fills
  itself in.
- **Word editor** with any number of translations, a free-form note and a photo.
- **Share text into the app.** Select a word in any other app, choose MittOrd from
  the text selection menu, and the add-word form opens with it already filled in.
- **Profile** with an avatar (JPEG or animated GIF), learning and native language,
  and a dark theme switch.

## Tech

| | |
|---|---|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2026.06.01), Material 3 |
| Architecture | MVVM, unidirectional state, Hilt for DI |
| Persistence | Room 2.8.4 |
| Networking | Retrofit 2.11 + OkHttp 4.12 + Gson |
| Images | Coil 2.7 |
| Build | AGP 8.13, KSP, minSdk 26, targetSdk 36 |

```
data/       network access (Retrofit service, translate repository)
domain/     use cases, language model and helpers, no Android imports
database/   Room entities, DAO, dictionary repository
ui/         screens, each with its own ViewModel and components
util/       preferences, file storage, custom Modifier extensions
```

### A few things worth a look

- **Animation without recomposition.** The bottom navigation bar animates its size,
  position and colours through lambdas that are read in the layout and draw phases,
  so a state change costs one recomposition and the following 400 ms cost none.
  `util/Modifier.kt` holds the lambda-based `height {}`, `size {}` and
  `paddingLayout {}` this relies on.
- **State grouped by how often it changes,** not by topic: the search query lives
  apart from the navigation state because one changes on every keystroke and the
  other on a tap, and merging them would put typing on the whole screen's
  recomposition path.
- **A single language detector** shared by both screens, keyed per field, with
  debouncing and cancellation in one place instead of nine copies.

## Building

Requires **JDK 17** and Android SDK 36.

```bash
./gradlew assembleDebug          # debug APK
./gradlew testDebugUnitTest      # unit tests
./gradlew assembleRelease        # minified release build
```

Debug builds install alongside a release one — they use the `.debug` application id
suffix, so both can sit on the device at the same time.

Release builds are shrunk and obfuscated with R8 (14 MB → 2 MB). Keep
`app/build/outputs/mapping/release/mapping.txt` for every build you distribute, or
crash reports from it will be unreadable.

## Tests

47 unit tests, run with `./gradlew testDebugUnitTest`:

- `LanguageUtilsTest` — BCP-47 normalisation and flag lookup.
- `WordListMapperTest` — the rules behind a list row: show the translation in the
  user's native language, fall back to the first stored one, and treat "first" as
  lowest id because the database promises no order.
- `LanguageDetectorTest` — debouncing, cancellation and per-field isolation, on a
  virtual clock.
- `SeedDatabaseUseCaseTest` — sample data generation.

`DictionaryRepositoryTest` under `androidTest` runs the repository against a real
in-memory Room database, covering the CASCADE delete and the way saving a word
replaces its whole translation set. It needs a device or emulator:
`./gradlew connectedDebugAndroidTest`.

## Known limitations

**Translation goes through an unofficial Google endpoint.** The app calls
`translate.googleapis.com/translate_a/single?client=gtx`, which is the internal
endpoint behind the Google Translate web widget rather than a documented API. It
needs no key and no billing account, which is why it is here — but it has no schema
guarantee (responses are parsed positionally out of a nested array), it is rate
limited per IP, and it can change without notice. This is a deliberate, temporary
choice for a personal project, not something to ship at scale. The intended
replacement is ML Kit Language Identification for detection, which is free, offline
and covers 100+ languages, and Google Cloud Translation for the translation itself.

**The word detail screen is waiting on a redesign.** Its photo block uses a fixed
200 dp height with a centre crop, so tall images are cut off and there is no way to
adjust it. The layout is deliberately left untouched until the new design lands; only
the state handling behind it has been reworked.

**The interface is English only.** Given the app is about languages, that is a little
ironic, and localisation is on the list.

## Adding screenshots

1. Take them on a device or emulator:
   `adb exec-out screencap -p > docs/screenshots/home.png`
2. Drop the files into `docs/screenshots/`.
3. Uncomment the block near the top of this file and adjust the names.

Four are enough: the word list, the add-word form expanded, the word editor and the
profile screen. Keep them in the same theme so the row reads as one set.
