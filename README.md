# mcnmt

MCN Mileage Tracker — Android app for tracking work-hours driving mileage.

## What it is

An Android app that logs driving trips during work hours (9am–6pm, Monday–Friday) and produces a monthly mileage report. Trips can be started manually or detected automatically when moving faster than 10 mph for 30 seconds. Storage is local-first; trips can be edited and exported to CSV.

## Stack

Kotlin, Jetpack Compose, Room, Hilt, Coroutines/Flow, FusedLocationProvider + Activity Recognition, Foreground Service, WorkManager.

## Status

Early development. Scaffolding, brand theme, Room data layer, trip list, and trip detail are in place. Auto-detection and monthly reporting are not yet complete.

## Build

Standard Android Gradle project. Open in Android Studio, or from the command line:

```
./gradlew assembleDebug
```
