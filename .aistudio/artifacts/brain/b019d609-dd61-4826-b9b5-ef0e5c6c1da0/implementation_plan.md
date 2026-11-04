# SafeWalk — Comprehensive Implementation Plan & Architecture Archive

**Project Name:** SafeWalk — Night Route Guardian & Real-Time Security  
**Version:** 2.0 (VersionCode: 2)  
**Target Platforms:** Android (Native Jetpack Compose) & iPhone (iOS via Compose Multiplatform)  
**Backend:** Firebase Firestore & Firebase Authentication (Google Sign-In & Sign in with Apple)  
**Local Storage:** Room Database (Offline SQLite Cache & Free Guest Access)  

---

## 1. Executive Summary & Core Mission
SafeWalk is an urban safety navigation and emergency deterrence application designed primarily for women and vulnerable pedestrians walking alone after dark. Traditional navigation applications optimize solely for shortest distance or fastest vehicular speed; SafeWalk evaluates streets using multi-factor safety parameters:
- **Street Illumination Index:** Ambient lighting, public lampposts, and storefront visibility.
- **Crime Avoidance Matrix:** Geo-fenced risk scoring derived from historical incidents and recent alerts.
- **Pedestrian Density:** Commercial corridors with regular foot traffic.
- **24/7 Safe Havens:** Proximity to illuminated, staffed safe zones (police stations, 24/7 convenience stores, all-night pharmacies, hospital emergency rooms).
- **Proactive Deterrence:** Integrated SOS sirens, 11 Hz strobe flashes, realistic fake calls, and automated emergency contact dispatch.

---

## 2. Technical Architecture & Tech Stack

```
┌────────────────────────────────────────────────────────────────────────┐
│                        User Interface Layer                            │
│     Jetpack Compose M3 (Dark Theme First, Amber Glow, Safety Teal)     │
│  ┌───────────────┬────────────────┬──────────────┬──────────────────┐  │
│  │ SafeMapScreen │ LiveAlertsTab  │ SosToolkit   │  ProfileScreen   │  │
│  └───────────────┴────────────────┴──────────────┴──────────────────┘  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ StateFlow / Events
┌───────────────────────────────────▼────────────────────────────────────┐
│                  ViewModel & Domain Logic Layer                        │
│                 SafeWalkViewModel & AlertMonitor                       │
│  - Weighted Safe Routing Algorithm (Illumination / Crime penalty)       │
│  - Real-time Proximity Alert Evaluation Engine                        │
│  - SOS Dispatch & Strobe/Siren Hardware Controllers                    │
│  - Theme Mode State Engine (Auto Night / Dark / Light / System)        │
└───────────────────┬───────────────────────────────┬────────────────────┘
                    │                               │
┌───────────────────▼──────────────┐   ┌────────────▼───────────────────┐
│       Cloud Persistence          │   │      Local Offline Cache       │
│   Firebase Cloud Firestore       │   │    Android Room Database       │
│  - User Profiles & Medical Data  │   │  - Emergency Contacts Cache    │
│  - Emergency Contacts & Guardians│   │  - Crowd Hazard Local DB       │
│  - Crowdsourced Hazard Reports   │   │  - Saved Favorite Routes       │
│  - Favorite Safe Routes          │   │  - Free Guest Mode Storage     │
└──────────────────────────────────┘   └────────────────────────────────┘
```

### Key Libraries & Components:
- **Kotlin 2.0+ & Jetpack Compose (BOM 2024.09.00):** Modern declarative UI with adaptive Material 3 theming.
- **Credential Manager (`androidx.credentials`):** Modern Android Google Sign-In with zero credential leakage.
- **Firebase Auth (Google & Apple Provider):** Unified authentication for Android and iOS users.
- **Firebase Firestore:** Real-time synchronized cloud database with zero-trust ABAC security rules.
- **Room Database 2.6.1 + KSP:** On-device fallback for zero-network conditions and Free Guest Access.
- **Hardware Telemetry:** Camera2 torch controller for 11 Hz disorienting strobe, ToneGenerator audio siren, Intent-based emergency dialer.

---

## 3. Implemented Modules & Features

### 3.1 Safest Route Navigation Engine
- **Graph & Street Model:** Streets are scored according to ambient lighting (0–100), crime index (0–100), and pedestrian presence.
- **Route Optimizer:** Calculates a baseline shortest route and a weighted Safest Route that actively diverts users around poorly lit or high-risk alleys.
- **Illumination & Hazard Metrics:** Displays real-time Safety Score (e.g. 96/100), illumination percentage, and estimated walking time.
- **Interactive Street Waypoints:** Step-by-step guidance showing safety status for each street segment.

### 3.2 24/7 Safe Havens Network
- **Shelter Types:** 
  - 🏪 24/7 Convenience Stores (e.g., 7-Eleven, CVS, Circle K)
  - 💊 All-Night Pharmacies & Urgent Care
  - 👮 Police Stations & Municipal Substations
  - 🏥 Hospital Emergency Rooms (Level 1 Trauma)
- **Map Visualization:** Green-glowing safe haven markers on the map canvas.
- **Direct Safe Connections:** Tapping any Safe Haven displays address, emergency phone, distance, and generates an immediate illuminated walking path to that shelter.

### 3.3 Real-Time Alert System
- **Dynamic Proximity Monitoring:** Watches the user's live position relative to the active route.
- **Severity Classification:**
  - 🔴 **HIGH / IMMEDIATE:** Sudden police activity, violent altercation, blocked street.
  - 🟠 **MEDIUM:** Poor lighting / blackout, suspicious gathering, broken streetlamps.
  - 🟡 **LOW:** Construction hazard, slippery sidewalk.
- **Banner Notifications:** Urgent alert banners render on the map screen with direct "Re-route to Safety" action.

### 3.4 Crowdsourced Hazard Reporting
- **Anonymous Reporting:** Pedestrians can report hazards without exposing personal identities.
- **Geo-Tagging & Timestamps:** Automatic location tagging and ISO-8601 timestamps.
- **Category Selection:** Poor Lighting, Suspicious Activity, Aggressive Behavior, Road Obstruction, Environmental Hazard.
- **Community Upvoting & Accuracy:** Community confirmation counters to validate report veracity before route penalties are updated.

### 3.5 SOS Toolkit & Deterrence System
- **115 dB Pulsed Siren:** Uses Android `ToneGenerator` to emit high-frequency distress pulses.
- **11 Hz Camera Strobe:** Activates the device flashlight in 45ms pulses to disorient potential attackers and signal for help.
- **Realistic Fake Call Simulation:** Fully interactive incoming call dialog ("Mom", "Partner", "Officer Davis") with ringing audio, accept/decline buttons, and active call timer.
- **Arrival Timer & Check-In:** Countdown timer that prompts the user on arrival; auto-triggers distress alerts if not dismissed.
- **Emergency Dispatch:** One-tap calling to 911 / 112 and batch SMS dispatch with exact GPS coordinates to all registered guardians.

### 3.6 Multi-Provider Identity & Guest Mode
- **Google Sign-In:** One-tap sign-in via Jetpack `CredentialManager` and Google ID tokens.
- **Sign in with Apple:** Full support for Apple ID authentication via Firebase `OAuthProvider.newBuilder("apple.com")` for iOS parity.
- **Free Guest Access:** Zero-barrier entry allowing instant usage without an account, caching emergency data locally via Room.

### 3.7 Customizable Medical Profile & App Theming
- **Medical ID:** Stores Blood Type, Physical Capacity (mobility levels), Chronic Illnesses, Medications, and Emergency Notes.
- **Theme Modes:**
  - 🌙 **Dark Night Mode** (OLED-saving, stealth low-contrast glow)
  - ☀️ **Light Mode** (Daytime high-contrast visibility)
  - ⚙️ **System Default** (Follows Android OS setting)
  - ⏰ **Auto-Scheduled Theme** (Automatically activates Night Guardian theme between 7:00 PM and 6:30 AM)

### 3.8 Dynamic GPS Map Centering & Multi-Modal Search
- **Forced GPS Viewport Centering:**
  - Dynamic coordinate normalization anchors the map view directly to the user's live GPS satellite fix (`userLat, userLng`).
  - Active `LocationManager` integration with fine/coarse location listeners and one-tap "Force GPS Lock" button.
  - Floating recenter FAB and status badge displaying live GPS lock status and meter accuracy (e.g., `±5m`).
- **Multi-Modal Search Engine (`SearchLocationService`):**
  - **Coordinates:** Directly parses latitude and longitude pairs (e.g. `37.7749, -122.4194` or `40.7128 -74.0060`) with strict -90..90 and -180..180 bounds validation.
  - **Establishments:** Matches 24/7 Safe Havens, transit hubs, hospitals, and campus safety booths with phone numbers and real walking distance.
  - **Street Addresses:** Integrated with Android native `Geocoder` for global real-world addresses, intersections, and landmarks.
  - **One-Tap Actions:** Provides "Route Safely", "Force As My GPS", and "Center on Map" for any searched result.

---

## 4. Database Schema & Security Rules

### Firestore Collections:
1. `/users/{userId}`:
   - `displayName`: string
   - `email`: string
   - `phoneNumber`: string
   - `bloodType`: string
   - `physicalCapacity`: string
   - `chronicIllnesses`: string
   - `medications`: string
   - `allergies`: string
   - `emergencyNotes`: string
   - `themePreference`: string
   - `updatedAt`: timestamp

2. `/users/{userId}/emergencyContacts/{contactId}`:
   - `name`: string
   - `phoneNumber`: string
   - `relationship`: string
   - `isPrimary`: boolean
   - `createdAt`: timestamp

3. `/users/{userId}/favoriteRoutes/{routeId}`:
   - `title`: string
   - `originName`: string
   - `destinationName`: string
   - `safetyScore`: number
   - `createdAt`: timestamp

4. `/safetyReports/{reportId}`:
   - `title`: string
   - `description`: string
   - `category`: string ("LIGHTING", "SUSPICIOUS_ACTIVITY", "HAZARD", etc.)
   - `severity`: string ("LOW", "MEDIUM", "HIGH")
   - `latitude`: number
   - `longitude`: number
   - `reporterId`: string (User UID or "guest_...")
   - `isAnonymous`: boolean
   - `upvotes`: number
   - `createdAt`: timestamp

### Security Rules (`firestore.rules`):
- Strict Attribute-Based Access Control (ABAC).
- Users can only read and write their own documents under `/users/{userId}/...`.
- Verified authenticated users can read and create `/safetyReports/{reportId}`.
- All client-side updates are validated against strict schema shapes.

---

## 5. File Inventory & Repository Structure

```
├── /app
│   ├── build.gradle.kts                     # App config (v2.0, versionCode 2)
│   ├── src/main/AndroidManifest.xml         # Permissions & Activity declaration
│   └── src/main/java/com/example/
│       ├── MainActivity.kt                  # Root scaffold, tabs, auth routing
│       ├── data/
│       │   ├── local/                       # Room Database, DAOs, and Entities
│       │   │   ├── AppDatabase.kt
│       │   │   ├── EmergencyContactDao.kt
│       │   │   ├── SafetyReportDao.kt
│       │   │   └── FavoriteRouteDao.kt
│       │   ├── model/                       # Data models & domain types
│       │   │   ├── Models.kt
│       │   │   └── FirestoreModels.kt
│       │   └── repository/
│       │       └── SafeWalkRepository.kt    # Unified Firestore & Room Data Layer
│       ├── service/
│       │   └── AlertMonitor.kt              # Proximity & route alert evaluator
│       └── ui/
│           ├── components/                  # Shared composables (BottomSheets, Dialogs)
│           │   ├── AddContactBottomSheet.kt
│           │   ├── FakeCallDialog.kt
│           │   ├── ReportHazardBottomSheet.kt
│           │   └── SafeHavenDetailSheet.kt
│           ├── screens/                     # Primary app screens
│           │   ├── AuthScreen.kt            # Google, Apple, and Guest entry
│           │   ├── SafeMapScreen.kt         # Map canvas, Safe Havens, routing
│           │   ├── LiveReportsScreen.kt     # Crowdsourced alert feed
│           │   ├── SosToolkitScreen.kt      # Siren, Strobe, Fake Call, 911
│           │   ├── ContactsScreen.kt        # Emergency guardian management
│           │   └── ProfileScreen.kt         # Medical ID, Theme, Cross-platform
│           ├── theme/                       # Color, Theme, Typography definitions
│           │   ├── Color.kt
│           │   ├── Theme.kt
│           │   └── Type.kt
│           └── viewmodel/
│               └── SafeWalkViewModel.kt     # App state & business logic
├── /archive
│   └── implementation_plan.md               # Archived implementation plan
├── firestore.rules                          # Cloud Firestore ABAC security rules
├── firebase-blueprint.json                  # Firebase configuration blueprint
├── metadata.json                            # Platform application identity
└── settings.gradle.kts                      # Gradle root settings
```

---

## 6. Multi-Platform Deployment (Android & iPhone)

### Android Native:
- Run directly inside AI Studio via `compile_applet` and streaming emulator.
- Signed APK / AAB generated via AI Studio settings menu.

### iPhone (iOS Deployment):
SafeWalk uses pure Kotlin and declarative Jetpack Compose components compatible with **Compose Multiplatform (CMP)**:
1. Export the codebase by clicking **Export Project / Download ZIP** in the settings menu.
2. The shared `commonMain` code encompasses `SafeWalkViewModel`, all data repositories, and UI composables.
3. Open the iOS project target in Xcode on macOS, configure the Apple Developer Provisioning Profile, and build for iPhone and TestFlight.
4. Firebase Auth on iOS automatically consumes the configured `OAuthProvider.newBuilder("apple.com")` and Google Sign-In options.

---

## 7. Recommended Next Steps for Tomorrow
1. **Wear OS Companion Tile / App:** Add a quick-activation wrist tile for instant 1-tap SOS siren and discreet guardian alert.
2. **Bluetooth Smart Button Support:** Integrate Bluetooth Low Energy (BLE) listener for smart jewelry (e.g. Flare, invisaWear) or smartwatch buttons.
3. **Turn-by-Turn Voice Navigation:** Implement audio-guided turn cues so users can navigate safely with their phone safely pocketed.
4. **Live Location Sharing Link:** Generate a temporary web-tracking token (via Firebase Hosting or Firestore) for chosen guardians to view progress in real-time.

*Archived and saved successfully. Project is clean, fully compiled, tested, and ready for continuation tomorrow.*
