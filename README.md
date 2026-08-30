# MotivationApp

Application Android de suivi d'habitudes inspirée du livre *Atomic Habits* de
James Clear. L'idée centrale : transformer de petites actions répétées en
systèmes durables, en s'appuyant sur des **déclencheurs** (triggers), un rappel
quotidien et un historique de complétion qui fait grossir les séries (streaks).

## Objectif

Aider l'utilisateur à :

- Créer des habitudes liées à un moment déclencheur (« Après mon café du matin »).
- Cocher chaque habitude accomplie dans la journée et suivre une série de jours
  consécutifs.
- Visualiser la progression du jour, les statistiques et l'historique de
  complétion.
- Recevoir des rappels notification à la fréquence choisie (quotidienne,
  hebdomadaire, etc.) et un récapitulatif de fin de journée.
- Sauvegarder et restaurer ses données via un export/import JSON.

## Fonctionnement

L'app s'ouvre sur **Accueil** (résumé du jour + citation du jour tirée du livre),
puis trois onglets en bas d'écran :

| Onglet       | Contenu                                                                 |
|--------------|-------------------------------------------------------------------------|
| Accueil      | Progression du jour, message d'encouragement, citation *Atomic Habits* |
| Habitudes    | Liste ou vue chronologique (matin/après-midi/soir), recherche, FAB « + » |
| Stats        | Statistiques hebdo/mensuelles/annuelles et historique de complétion    |

Depuis l'écran Habitudes :

- Le bouton **+** ouvre le formulaire d'ajout/édition d'une habitude (titre,
  description, déclencheur, heure de rappel, fréquence de notification).
- Un appui sur une habitude ouvre son **écran de détail** avec le calendrier de
  complétion.
- L'écran **Réglages** (depuis Accueil) permet le choix du thème (clair/sombre,
  couleur dynamique) et l'**export/import JSON** des données.
- Les **déclencheurs** sont gérables séparément ; des déclencheurs par défaut
  sont créés au premier lancement.

### Données

- **Room** (`motivation_app_db`, version 3) avec trois entités :
  `Habit`, `Trigger`, `HabitCompletion`.
- Migrations `1 → 2` (triggers + colonnes de notification) et `2 → 3` (table
  d'historique de complétion, avec backfill depuis `lastCompletedDate`).
- Les séries (`streak`) sont calculées à partir de l'historique de complétion
  (`StreakUtils`).
- Les préférences (thème, couleur dynamique) sont stockées via **DataStore**.

### Notifications

- `HabitAlarmScheduler` + `HabitReminderReceiver` : rappels planifiés par
  habitude selon sa fréquence.
- `DailyReminderWorker` (WorkManager) : récapitulatif de fin de journée des
  habitudes non complétées.
- `BootCompleteReceiver` : replanifie les alarmes après un redémarrage.

## Architecture

Pile technique : Kotlin, Jetpack Compose, Material 3, Hilt (DI), Room,
Navigation Compose, WorkManager, DataStore.

```
app/src/main/java/com/fracorbas/motivationapp/
├── MainActivity.kt            # NavHost + bottom bar (Accueil, Habitudes, Stats)
├── MotivationApp.kt           # Application Hilt
├── data/
│   ├── local/                 # Room : DAOs, HabitDatabase, migrations
│   ├── model/                 # Entités, convertisseurs, utils (StreakUtils, quotes)
│   └── repository/            # HabitRepository, TriggerRepository, BackupRepository…
├── di/AppModule.kt            # Graphe Hilt
├── notification/              # AlarmScheduler, receivers, WorkManager
├── ui/                        # Écrans Compose (Home, Main, Statistics, AddHabit…)
│   ├── components/            # Composants réutilisables (AppBottomBar, HabitRow…)
│   └── theme/                 # Couleur, typographie, thème Material 3
└── viewmodel/                 # ViewModels (HabitViewModel, StatisticsViewModel…)
```

## Pré-requis

- Android SDK (déjà configuré via `local.properties` → `sdk.dir`).
- JDK 17 (toolchain Kotlin configurée sur 17).
- minSdk 24 / targetSdk 37 / compileSdk 37.
- Gradle fourni via le wrapper (`./gradlew`).

## Build et exécution

Installer sur un appareil/émulateur connecté :

```bash
./gradlew installDebug
```

Compiler l'APK debug sans l'installer :

```bash
./gradlew :app:assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk
```

Build release (nécessite un keystore de signature) :

```bash
./gradlew :app:assembleRelease
```

Nettoyer les artefacts de build :

```bash
./gradlew clean
```

## Tests

Tests unitaires JVM (logique de série, citations, sérialisation JSON de
sauvegarde) :

```bash
./gradlew test
```

Tests instrumentés (Room : DAOs et migration 2 → 3) — nécessitent un appareil
ou un émulateur :

```bash
./gradlew connectedAndroidTest
```
