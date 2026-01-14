# 🎨 Drawio - Multiplayer Drawing Game

```html
<div align="center"> <img src="images/drawio.png" alt="Logo" width="200" height="200">

<h1 align="center">Drawio</h1>

<p align="center"> <strong>Une réimplémentation moderne de Skribbl.io développée en Java 21 et JavaFX</strong> <br /> <br /> <a href="#-installation-et-lancement"><strong>Installation »</strong></a> · <a href="#-galerie--screenshots"><strong>Screenshots »</strong></a> · <a href="#-troubleshooting"><strong>Support »</strong></a> </p> </div>
```

**Drawio** est une réimplémentation moderne du célèbre jeu de dessin et de devinettes "Skribbl.io", développée en **Java 21** avec **JavaFX**.

Le jeu repose sur une architecture **Autorité serveur** robuste utilisant des sockets TCP, permettant des parties multijoueurs en temps réel avec synchronisation du dessin, tchat en direct et système de lobby.

---

## ✨ Fonctionnalités

* **🎨 Dessin en temps réel :** Outils pinceau, gomme, sélecteur de couleurs, et taille de trait ajustable. La synchronisation est fluide entre tous les clients.
* **🕹️ Gameplay complet :**
    * Système de **Lobby** (Waiting Room) avec statut "Prêt" et comptage des joueurs.
    * Sélection d'avatar personnalisé depuis le disque dur via l'écran de connexion.
    * Choix de mots parmi 3 propositions pour le dessinateur.
    * Détection automatique des réponses dans le tchat.
    * Timer de manche et calcul des scores dynamique.
* **🏆 Fin de partie immersive :** Tableau des scores (Podium Or/Argent/Bronze) et animation de confettis pour le vainqueur.
* **💬 Tchat intégré :** Discussion entre joueurs et annonces système (connexions, tours, victoires).
* **🔊 Ambiance sonore :** Effets sonores pour les événements (victoire, défaite, temps écoulé, message).

---

## 📸 Galerie & Screenshots

|            Connexion & Avatar             |             Lobby d'attente              |
|:-----------------------------------------:|:----------------------------------------:|
|   ![Connexion](images/join_screen.png)    |  ![Lobby](images/wait_for_players.png)   |
| *Écran de connexion avec upload d'avatar* |      *Salle d'attente des joueurs*       |

|           En Jeu (Dessin)            |           Fin de Partie           |
|:------------------------------------:|:---------------------------------:|
| ![In Game](images/guessing_word.png) | ![Game Over](images/end_game.png) |
|               *En jeu*               |          *Fin de partie*          |

---

## 🛠️ Stack Technique

* **Langage :** Java 21
* **Interface Graphique :** JavaFX 21 (FXML + CSS modulaire)
* **Build System :** Gradle (Kotlin DSL)
* **Réseau :** Sockets Java (TCP)
* **Distribution :** Plugin `jlink` pour générer des exécutables autonomes légers.

---

## 🚀 Installation et Lancement

### Prérequis
* Un système d'exploitation Windows, Linux ou macOS.
* **(Optionnel)** JDK 21 installé si vous voulez développer, mais le build `jlink` inclut son propre runtime.

### 1. Cloner le projet
```bash
git clone [https://github.com/votre-compte/projects7-skribbl.git](https://github.com/votre-compte/projects7-skribbl.git)
cd projects7-skribbl
```

### 2. Compiler le projet (Génération des exécutables)

Le projet utilise **Gradle** avec le plugin **JLink** pour créer une version portable du client et du serveur.

**Sur Windows (PowerShell) :**
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"; .\gradlew jlinkZip
```

**Sur Linux / macOS :**
```bash
chmod +x gradlew
JAVA_HOME=/usr/lib/jvm/java-21-openjdk ./gradlew jlinkZip
```

### 3. Lancer le Serveur et le Client

Une fois la compilation terminée, rendez-vous dans le dossier de distribution :
`build/distributions/`

Décompressez le fichier `.zip`. Dans le dossier `bin` extrait, vous trouverez deux lanceurs :

1.  **Lancer le Serveur :**
    * Windows: Double-cliquez sur `SkribblServer.bat`
    * Linux/Mac: `./SkribblServer`
2.  **Lancer le Client :**
    * Windows: Double-cliquez sur `SkribblClient.bat`
    * Linux/Mac: `./SkribblClient`

---

## 📂 Structure du Projet

```
src/main/java/fr/polytech/wid/s7projectskribbl
├── client               # Partie Client JavaFX
│   ├── actions          # Logique de réception des paquets (Pattern Command)
│   ├── controller       # Contrôleurs FXML (GameController, JoinRoom...)
│   ├── network          # Gestion des Sockets Client
│   ├── service          # Gestionnaires (SoundManager, PopupService...)
│   └── Launcher.java    # Point d'entrée Client
├── server               # Partie Serveur
│   ├── client           # Gestion des clients connectés
│   ├── room             # Logique de gestion de la partie (GameLoop)
│   └── Main.java        # Point d'entrée Serveur
└── common               # Code partagé (Payloads, Constantes)
```

---

## 🐛 Troubleshooting

**Le jeu ne se lance pas sous Windows (SmartScreen) ?**
Windows peut bloquer les fichiers `.bat` non signés.
1. Clic droit sur le fichier ZIP généré -> **Propriétés**.
2. Cocher **"Débloquer"** (Unblock) en bas.
3. Appliquer, puis dézipper à nouveau.

**Problèmes de son sous Linux ?**
Assurez-vous d'avoir les bibliothèques nécessaires :
`sudo pacman -S ffmpeg gst-libav` (Arch) ou `sudo apt install libasound2 ffmpeg` (Ubuntu).

---

## 👥 Auteurs

Ce projet a été réalisé dans le cadre du cursus Polytech.

* **Dalil NAAMNA**
* **Wassim HAMRIT**
* **Ibrahim OZEL**