# Dealayed

> Your inconvenient and annoying notifier that updates you on deals only **after** they have ended.
> But hey, at least you saved money from not buying.

Dealayed is a satirical Android app built for a hackathon. It browses real promos that have already expired, roasts you for missing them, and sends you notifications about deals you can no longer use.

## Features

- **Animated splash screen**: the "DEALAYED" logo animation, with letters that rush in, recoil and bounce.
- **Demo sign-in**: email and password validation with inline errors, a loading state and a show/hide password toggle.
- **Home feed**: a list of recently expired deals, sorted by how recently they ended, with category filter chips (All, Food, Movies, Travel, Shopping).
- **Deal details**: tap a deal to open a bottom sheet with the business, headline discount, expiration date, description, photo and a link to the original source.
- **Search**: a category dropdown plus a "Recommendations" list of deals you just missed, with an empty state when nothing matches.
- **Notifications**: a screen styled to be as passive-aggressive as possible.
  - Filter chips: All, Missed, Expired, Roasts
  - A "Live Shame" banner with "Cry about it" and "Blame traffic" buttons
  - Unread badges, "Mark all as ignored", and Today / Earlier sections
  - The bell icon on Home and Search opens it
- **Profile**: total amount overspent, missed deals count, membership tiers and preference toggles (expiry popups, Weekly Dealays), plus log out.
- **Bottom navigation**: Home, Search and Profile with cross-fade transitions.

## Tech stack

| | |
|---|---|
| Language | Kotlin |
| UI | Android Views (XML) with Jetpack Compose and Material 3 for Sign In, Profile and Notifications |
| Min / target SDK | 24 / 37 |
| Build | Gradle (Kotlin DSL), Android Gradle Plugin 9.4.1 |
| Data | Local JSON files in `assets` (no backend yet) |
| Font | Lexend |

## Project structure

```
app/src/main/
├── java/com/example/dealayed/
│   ├── MainActivity.kt           # Launcher: splash, then sign-in
│   ├── SplashScreen.kt          # Logo animation
│   ├── SignInActivity.kt        # Compose login screen and view model
│   ├── HomeActivity.kt          # Expired deals feed and deal sheet
│   ├── SearchActivity.kt        # Category search and recommendations
│   ├── NotificationsActivity.kt # Compose notifications screen
│   ├── ProfileActivity.kt       # Compose profile screen
│   ├── NotifBell.kt             # Opens Notifications from the bell icon
│   ├── BottomNav.kt             # Shared bottom navigation
│   ├── Transitions.kt           # Fade transition helper
│   ├── DealAdapter.kt           # RecyclerView adapter for Home
│   ├── DealRepository.kt        # Parses expired_deals.json into Deal objects
│   └── DealCache.kt             # In-memory cache for loaded deals
├── json_files/                   # Bundled as app assets
│   ├── expired_deals/            # expired_deals.json and img/
│   ├── notif_messages/           # Roast copy for notifications
│   └── ai_messages/              # Roast copy for expired-deal messages
└── res/                          # Layouts, drawables, fonts, colors, themes
```

## Data

The expired deals catalog (`expired_deals.json`) holds 20 real promos across four categories: Food, Shopping, Movies and Travel. Each entry includes:

- business name, title and description
- discount percent or price, start and expiration dates
- redemption instructions
- an image and a source URL

`DealRepository` loads the file from assets, normalizes image paths and dates, and sorts deals by `daysExpired`.

## Getting started

### Requirements

- Android Studio (a recent version that supports AGP 9.x)
- JDK 11 or newer
- An emulator or device running Android 7.0 (API 24) or higher

### Run it

1. Clone the repo:
   ```bash
   git clone https://github.com/k-burbos/dealayed-malfunctionhackathon.git
   ```
2. Open the folder in Android Studio and let Gradle sync.
3. Pick an emulator or device and press **Run**.

### Demo login

Sign-in is a local demo with hard-coded credentials, not real authentication:

| Email | Password |
|---|---|
| `admin@gmail.com` | `123` |

## Roadmap

- Load deals and images from a backend (the search list already has a TODO for this)
- Drive the Notifications screen from `notif_messages.json` instead of hard-coded items
- Real authentication
- Real push notifications

## Team GGEZ

- Keara Abigael A. Burbos
- Rich Andrea P. Borja
- Catherine A. Canonigo
- Johana Antonette P. Cordova
- Stephen John L. Pescadero

## [![Video Demo]([https://youtube.com)](https://www.youtube.com/watch?v=VIDEO_ID](https://drive.google.com/drive/folders/1iFCaWJEgWWZMTQFqppk8AyN4O4dBYCOY))

## Disclaimer

Dealayed is a parody project. The deals shown are historical promos that have already expired, and the roasts are meant as jokes. Please don't take them personally.
