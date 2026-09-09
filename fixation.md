# Android modernization and remediation plan

## Objective and scope

Bring both Android modules to the current, supported AGP 9.3 toolchain; remove unused build inputs; replace deprecated build technology; make release builds, Android Lint, static analysis, unit tests, and instrumentation tests required verification gates. Do not merge any item unless every command in its verification section passes.

## Research-verified compatibility baseline

| Component | Adopted version / approach | Verification source |
| --- | --- | --- |
| Android Gradle Plugin | `9.3.2` | [AGP 9.3 release notes](https://developer.android.com/build/releases/agp-9-3-0-release-notes) identify 9.3.2 as the current 9.3 patch and require Gradle 9.5/JDK 17. |
| Gradle | `9.5` | [AGP compatibility table](https://developer.android.com/build/releases/about-agp) requires Gradle 9.5 for AGP 9.3. |
| Kotlin build integration | AGP built-in Kotlin; remove `org.jetbrains.kotlin.android` and `kotlin-kapt` | [Built-in Kotlin migration](https://developer.android.com/build/migrate-to-built-in-kotlin) documents the required AGP 9 migration. |
| Annotation processing | KSP `2.3.7`; migrate Hilt compiler from kapt to KSP | [KSP releases](https://github.com/google/ksp/releases) and [Hilt setup](https://dagger.dev/hilt/gradle-setup.html). |
| Hilt | `2.60.1` | [Hilt Gradle setup](https://dagger.dev/hilt/gradle-setup.html). |
| CameraX | `1.6.2` stable | [CameraX release notes](https://developer.android.com/jetpack/androidx/releases/camera). |
| Core / AppCompat / Activity / Lifecycle / ConstraintLayout | `1.19.0` / `1.8.0` / `1.13.0` / `2.11.0` / `2.2.2` | [AndroidX versions](https://developer.android.com/jetpack/androidx/versions), [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle), and [ConstraintLayout](https://developer.android.com/jetpack/androidx/releases/constraintlayout). |

## Completed build-system remediation

1. Upgrade the wrapper to Gradle 9.5 and AGP to 9.3.2.
2. Enable AGP 9 built-in Kotlin by removing the obsolete Kotlin Android plugin and `kotlinOptions` blocks. Java 17 compile options remain the single JVM-target authority.
3. Replace kapt with KSP and move Hilt to 2.60.1 using the plugins DSL.
4. Remove conflicting, obsolete root `buildscript` classpaths and obsolete direct dependencies. Keep only dependencies demonstrably used by Kotlin source or XML resources.
5. Remove unused `ConnectionModel` and `GlideModule`; Hilt no longer provides an unused Glide binding.

## Required source remediation

### Deprecations and platform correctness

1. Replace all storage-file path access and `Environment`/legacy external-storage flows with the Storage Access Framework (`ActivityResultContracts.OpenDocument`, `OpenMultipleDocuments`, and `DocumentFile`) or MediaStore collection APIs, selected per data type. Remove `MANAGE_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_EXTERNAL_STORAGE`, and `requestLegacyExternalStorage` when the replacement is complete.
2. Replace `WifiP2pConfig.deviceAddress` connection construction with the current Wi-Fi Direct APIs where the target SDK/Lint flags it; enforce `NEARBY_WIFI_DEVICES` runtime permission on Android 13+ and location permission only on versions where it is actually required.
3. Remove `QUERY_ALL_PACKAGES` unless a Play policy-reviewed core requirement proves it necessary. For APK sharing, use a narrowly scoped package query and user-selected packages instead.
4. Replace `onBackPressed`-style behavior with `OnBackPressedDispatcher` callbacks, register callbacks with the view lifecycle in fragments, and make every receiver/observer unregistration lifecycle-safe.
5. Remove the custom Motorola permission and any manifest entry that cannot be tied to executing source code, a documented device integration, or a product requirement.

### Reliability, architecture, and security

1. Move file transfer, Wi-Fi Direct, and media scanning work from ad-hoc `CoroutineScope(Dispatchers.IO)` instances into lifecycle-aware `viewModelScope`/structured coroutine scopes. Surface state as immutable `StateFlow`/`SharedFlow`, cancel work on lifecycle end, and avoid leaking activities through listeners.
2. Make `FileTransferSDK` and `DataReceiverManger` report typed results (`sealed interface TransferResult`) instead of logs and nullable global socket state. Close every stream/socket with `use`, validate lengths before allocation, reject traversal filenames, and write received data atomically through a temporary file followed by rename.
3. Replace mutable global `selectedPath` with a ViewModel-owned immutable selection state. Adapters receive render state and emit selection events; they do not modify process-global data.
4. Use `ListAdapter` plus `DiffUtil` for every RecyclerView adapter; remove `notifyDataSetChanged`, detach checkbox listeners before binding state, and use stable content URIs rather than `Uri.path!!`.
5. Use a single `FileProvider` authority derived from `BuildConfig.APPLICATION_ID`; never hard-code `com.msn.dataselectionviewpager.fileprovider`.
6. Remove debug-only device, file-path, contact, and transfer logs or gate them with `BuildConfig.DEBUG`. Never log personally identifiable data or filesystem paths in release builds.
7. Replace duplicate XML/layout names and commented-out legacy implementations after confirming no resource/reference remains with `aapt2`/Lint.

## Static analysis and quality gates

1. Add Detekt with a checked-in `config/detekt/detekt.yml`, `detektBaseline.xml` only for pre-existing findings, and a zero-new-findings policy. Enable type resolution for both modules.
2. Add `lint.xml` with severity `error` for `NewApi`, `ObsoleteSdkInt`, `InlinedApi`, `MissingPermission`, `UnsafeOptInUsageError`, `ExportedActivity`, `FileProviderPath`, `TrustAllX509TrustManager`, and `HardcodedText`; do not suppress findings without a linked, documented justification.
3. Enable `warningsAsErrors` for Kotlin and Java compilation after fixing all current warnings. Turn on resource shrinking and code shrinking in release, then use AGP 9.3's `:app:analyzeReleaseR8Config` report to add only minimal, evidence-backed keep rules.
4. Add CI jobs for `help`, `build --dry-run`, `lint`, `detekt`, unit tests, connected tests on an API 28 device, and `analyzeReleaseR8Config`. Cache Gradle only after the first successful run.

## Dry-run and acceptance commands

Run these in order with JDK 17, Android SDK Build Tools 36.0.0, and a fresh Gradle user home. Stop immediately on the first failure and fix the cause before continuing:

```text
./gradlew help
./gradlew build --dry-run
./gradlew :app:lint :smartswitch:lint
./gradlew detekt
./gradlew test
./gradlew :app:assembleDebug :app:assembleRelease
./gradlew :app:analyzeReleaseR8Config
./gradlew connectedCheck
```

## Mandatory post-upgrade cross-check

For every dependency retained in `gradle/libs.versions.toml` or declared directly, run `dependencyInsight` for its group, read its official release notes, and record: latest stable version, minimum SDK, AGP/Kotlin compatibility, migration notes, and whether the existing API call still exists. Replace APIs only when the official documentation marks them obsolete or removed. This is intentionally a gate, not an assumption: it prevents a version bump from silently preserving removed or deprecated calls.

## Exit criteria

The work is complete only when all acceptance commands pass, no release Lint errors or unsuppressed Detekt findings remain, the release R8 report has no unresolved missing/unsafe rules, and the post-upgrade cross-check has a recorded outcome for every retained external dependency.
