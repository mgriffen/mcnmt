# mcnmt

MCN Mileage Tracker — Android app that tracks driving mileage from 9am to 6pm Monday through Friday. Automatically activates when moving faster than 10mph for 30 seconds. Allows manual editing and produces a monthly mileage report.

## Setup

(Filled in as project evolves — install steps, env vars, etc.)

## Project structure

(Filled in as files are created)

## Stack

- **Language:** Kotlin
- **Build:** Android Studio / Gradle
- **UI:** Jetpack Compose + Material 3
- **Storage:** Room (trips), DataStore (settings) — local-first, no cloud backend for MVP
- **Async:** Kotlin Coroutines + Flow
- **Architecture:** MVVM + Repository pattern
- **DI:** Hilt (if useful)
- **Location:** FusedLocationProviderClient (Google Play Services)
- **Activity detection:** Activity Recognition API
- **Tracking:** Foreground Service with persistent notification
- **Background jobs:** WorkManager (monthly reports, exports, cleanup)
- **Export:** CSV first, PDF later

## MVP scope

1. Manual start/stop trip tracking
2. Local trip storage (Room)
3. Trip history screen
4. Trip detail / edit screen
5. Monthly mileage report
6. CSV export / share
7. Settings screen
8. Automatic trip detection (after the basic app works)

Auto-detection target: start trip when moving >10 mph for 30s; stop after several minutes stopped or moving very slowly. Active tracking runs as a foreground service with persistent notification.

**Priority:** reliable local-first MVP first. First version must work even if auto-detection isn't finished yet.

## Key details

- Private working files go in `.working/` — never commit these
- Obsidian vault note: `/mnt/c/Users/mgrif/obsidianvaults/Sync Vault/Projects/mcnmt/mcnmt.md`
- Keystores, `google-services.json`, and signing configs are gitignored — never commit them

## Publishing rules

This project is PUBLIC.

- **Do not push to origin** unless Matt explicitly says to push
- **Do not publish** (Play Store, GitHub Release, etc.) unless Matt explicitly says to publish
- **Work on feature branches** — never commit WIP directly to main
- **Before any push to main:** verify build passes, verify no private files staged (`.working/`, `.claude/`, `.env`, keystores, `local.properties`, `google-services.json`), verify `.gitignore` is current
- **Ask before any build** (APK/AAB, release bundle, long Gradle task) — builds take minutes and block other work
- When in doubt, ask before pushing

## Machine handoff

Before ending a session where work is in progress:
1. Push the current branch to origin (with Matt's approval)
2. Update the Obsidian vault note with: current branch name, what was completed, what's next
3. The vault note is the handoff document — the next session on any machine reads it first

## Memory

Update memory files as decisions happen — don't batch to session end.
Memory lives at `~/.claude/projects/-home-griffen-projects-mcnmt/memory/`.
