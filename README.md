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
puis trois onglets navigables par **swipe gauche/droite** ou via la barre
inférieure :

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
├── MainActivity.kt            # NavHost + HorizontalPager (Accueil, Habitudes, Stats)
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

## Installation de l'environnement de développement

Le projet nécessite trois composants : un JDK 17, le SDK Android (platform 37,
build-tools 37) et Gradle (fourni via le wrapper). Les instructions ci-dessous
couvrent ArchLinux. Pour les autres distributions, adaptez les noms de paquets.

### 1. JDK 17

```bash
sudo pacman -S jdk17-openjdk
```

Vérifier :

```bash
java -version
# openjdk version "17.x.x"
```

### 2. SDK Android (cmdline-tools + platform-tools)

Les paquets officiels ne fournissent que les cmdline-tools ; les composants du
SDK (platform, build-tools) s'installent ensuite via `sdkmanager`.

```bash
# cmdline-tools (AUR)
yay -S android-sdk-cmdline-tools-latest

# Donner les droits d'écriture sur le dossier SDK
sudo chown -R $USER:$USER /opt/android-sdk

# platform-tools fournit adb (dépot officiel)
sudo pacman -S android-tools
```

### 3. Variables d'environnement

Ajouter à `~/.bashrc` (ou `~/.zshrc`) :

```bash
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
```

Recharger le shell :

```bash
source ~/.bashrc
```

### 4. Composants du SDK

```bash
# Accepter les licences
sdkmanager --licenses

# Installer platform 37, build-tools 37 et platform-tools
sdkmanager "platforms;android-37.0" "build-tools;37.0.0" "platform-tools"
```

> **Note** : le nom du package platform est `platforms;android-37.0` (avec le
> `.0`), pas `platforms;android-37`.

### 5. `local.properties`

Créer le fichier `local.properties` à la racine du projet pointant vers le SDK :

```bash
echo "sdk.dir=/opt/android-sdk" > local.properties
```

### 6. Wrapper Gradle

Si le dossier `gradle/wrapper/` est absent (il est dans `.gitignore`), générer
le wrapper à partir d'une distribution Gradle temporaire :

```bash
curl -L https://services.gradle.org/distributions/gradle-9.7.1-bin.zip -o /tmp/gradle.zip
unzip -q /tmp/gradle.zip -d /tmp/
/tmp/gradle-9.7.1/bin/gradle wrapper --gradle-version 9.7.1
rm -rf /tmp/gradle.zip /tmp/gradle-9.7.1
```

### 7. Débogage USB (optionnel)

Pour tester sur un téléphone physique :

```bash
sudo pacman -S android-tools
```

Activez le « Débogage USB » dans les options développeur du téléphone, branchez-le
et vérifiez la connexion :

```bash
adb devices
```

## Pré-requis (récapitulatif)

| Composant        | Version     | Source                              |
|------------------|-------------|-------------------------------------|
| JDK              | 17          | `jdk17-openjdk` (pacman)            |
| Android SDK      | platform 37 | `android-sdk-cmdline-tools-latest` (AUR) + `sdkmanager` |
| Build-tools      | 37.0.0      | `sdkmanager`                        |
| Platform-tools   | 37.x        | `android-tools` (pacman)            |
| Gradle           | 9.7.1       | via le wrapper `./gradlew`          |
| minSdk / target  | 24 / 37     | —                                   |

## Build et exécution

Vérifier que tout est en place :

```bash
./gradlew --version
```

Compiler l'APK debug sans l'installer :

```bash
./gradlew :app:assembleDebug
# APK généré : app/build/outputs/apk/debug/app-debug.apk
```

Installer sur un appareil/émulateur connecté :

```bash
./gradlew installDebug
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
