package fr.polytech.wid.s7projectskribbl.client.service;

import javafx.scene.media.AudioClip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SoundManager {

    private static SoundManager instance;
    private final Map<String, AudioClip> soundMap = new HashMap<>();
    private boolean isSoundActivated = true;

    private SoundManager() {
        loadAllSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    /**
     * Tente de charger une liste de fichiers MP3 prédéfinis.
     * Note : Java ne permet pas de scanner "/*.mp3" nativement dans un JAR de manière fiable.
     * Il faut lister les noms des fichiers ici.
     */
    private void loadAllSounds() {
        String[] filesToLoad = new String[]{
                "Connected",
                "Disconnected",
                "EndRoundAF",
                "EndRoundNOF",
                "FoundWord",
                "NTBegin",
                "Tick"
        };

        for (String fileName : filesToLoad) {
            loadSingleSound(fileName);
        }
    }

    /**
     * Charge un fichier spécifique depuis /sounds/
     * @param name Le nom du fichier sans extension (ex: "Message_Short")
     */
    private void loadSingleSound(String name) {
        try {
            String path = "/sounds/" + name + ".mp3";
            URL resource = getClass().getResource(path);

            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toExternalForm());
                soundMap.put(name, clip);
                System.out.println("[Audio] Chargé : " + name);
            } else {
                System.err.println("[Audio] Fichier introuvable : " + path);
            }
        } catch (Exception e) {
            System.err.println("[Audio] Erreur lors du chargement de " + name + " : " + e.getMessage());
        }
    }

    /**
     * Active ou désactive le son globalement.
     */
    public void setSoundActivated(boolean isActive) {
        this.isSoundActivated = isActive;
        System.out.println("[Audio] Son " + (isActive ? "activé" : "désactivé"));
        if (!isActive) {
            for (AudioClip clip : soundMap.values()) {
                if (clip.isPlaying()) clip.stop();
            }
        }
    }

    public boolean isSoundActivated() {
        return isSoundActivated;
    }

    /**
     * Joue le son demandé si le son est activé et si le fichier existe.
     * @param key Le nom du fichier sans extension
     */
    public void playSound(String key) {
        if (!isSoundActivated) return;
        if (key.endsWith(".mp3")) {
            key = key.replace(".mp3", "");
        }
        AudioClip clip = soundMap.get(key);
        if (clip != null) {
            clip.play();
            System.out.println("[Audio] Son lancé comme demandé : " + key);
        } else {
            System.out.println("[Audio] Son inconnu demandé : " + key);
        }
    }
}