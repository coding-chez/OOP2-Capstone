package com.oop2.typewiz.util;

import com.almasb.fxgl.audio.Music;
import com.almasb.fxgl.dsl.FXGL;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;

/**
 * Manages all game sounds and music.
 * Implements the Singleton pattern for global sound management.
 */
public class SoundManager {
    private static SoundManager instance;
    private Music currentBGM;
    private double bgmVolume = 0.4;
    private double sfxVolume = 0.6;

    // BGM files
    private static final String MENU_BGM = "bgm.mp3";
    private static final String GAME_BGM = "bgm2.mp3";
//    private static final String BOSS_BGM = "bgm/boss_theme.wav";

    // SFX files
    private static final String[] TYPING_SOUNDS = {
            "sound-library/type.mp3",
            "sound-library/type.mp3",
            "sound-library/type.mp3"
    };
    private static final String ENEMY_DEFEAT = "sound-library/death.mp3";
    private static final String GAME_OVER = "sound-library/gameover.wav";
    private static final String VICTORY = "sound-library/victory.mp3";
    private static final String WAVE_ANNOUNCE = "sound-library/wave.mp3";
    private static final String WING_FLAP = "sound-library/flappingsfx.wav";
    private static final String ERROR_TYPE = "sound-library/error.mp3";
    private static final String BUTTON_HOVER = "sound-library/hover.wav";
    private static final String BUTTON_CLICK = "sound-library/click.wav";
    private static final String SPACEBAR_COMPLETE = "sound-library/spacebar.mp3";
    private static final String SHIFT_CYCLE = "sound-library/shift.mp3";
    private static final String DAMAGE = "sound-library/damage.mp3";


    private SoundManager() {
        // Private constructor for singleton
        preloadSounds();
    }

    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    private void preloadSounds() {
        // Preload all sound effects to avoid lag
        try {
            // Load BGM
            FXGL.getAssetLoader().loadMusic(MENU_BGM);
            FXGL.getAssetLoader().loadMusic(GAME_BGM);
//            FXGL.getAssetLoader().loadMusic(BOSS_BGM);

            // Load SFX
            for (String typingSound : TYPING_SOUNDS) {
                FXGL.getAssetLoader().loadSound(typingSound);
            }
            FXGL.getAssetLoader().loadSound(ENEMY_DEFEAT);
            FXGL.getAssetLoader().loadSound(GAME_OVER);
            FXGL.getAssetLoader().loadSound(VICTORY);
            FXGL.getAssetLoader().loadSound(WAVE_ANNOUNCE);
            FXGL.getAssetLoader().loadSound(WING_FLAP);
            FXGL.getAssetLoader().loadSound(ERROR_TYPE);
            FXGL.getAssetLoader().loadSound(BUTTON_HOVER);
            FXGL.getAssetLoader().loadSound(BUTTON_CLICK);
            FXGL.getAssetLoader().loadSound(SPACEBAR_COMPLETE);
            FXGL.getAssetLoader().loadSound(SHIFT_CYCLE);
            FXGL.getAssetLoader().loadSound(DAMAGE);
        } catch (Exception e) {
            System.err.println("Error preloading sounds: " + e.getMessage());
        }
    }

    private void ensureCorrectVolume() {
        // Get current volume settings
        double currentMusicVolume = FXGL.getSettings().getGlobalMusicVolume();
        double currentSoundVolume = FXGL.getSettings().getGlobalSoundVolume();

        System.out.println("[DEBUG] Current volume settings - Music: " + currentMusicVolume + ", Sound: " + currentSoundVolume);

        // Reset to default values if too low
        if (currentMusicVolume < 0.1) {
            System.out.println("[DEBUG] Music volume too low, resetting to default");
            FXGL.getSettings().setGlobalMusicVolume(bgmVolume);
        }

        if (currentSoundVolume < 0.1) {
            System.out.println("[DEBUG] Sound volume too low, resetting to default");
            FXGL.getSettings().setGlobalSoundVolume(sfxVolume);
        }
    }

    /**
     * Plays background music
     * @param type The type of BGM to play ("menu" or "game")
     */
    public void playBGM(String type) {
        System.out.println("[DEBUG] Playing BGM: " + type);
        try {
            // Ensure volume settings are correct
            ensureCorrectVolume();

            if (currentBGM != null) {
                System.out.println("[DEBUG] Stopping previous BGM");
                FXGL.getAudioPlayer().stopMusic(currentBGM);
            }

            String musicFile;
            if ("game".equals(type)) {
                musicFile = GAME_BGM;
            } else {
                musicFile = MENU_BGM;
            }

            System.out.println("[DEBUG] Loading music file: " + musicFile);
            currentBGM = FXGL.getAssetLoader().loadMusic(musicFile);

            if (currentBGM != null) {
                // Set volume before playing
                System.out.println("[DEBUG] Setting music volume to: " + bgmVolume);
                FXGL.getSettings().setGlobalMusicVolume(bgmVolume);

                // Play the music
                System.out.println("[DEBUG] Starting music playback");
                FXGL.getAudioPlayer().loopMusic(currentBGM);

                // Verify volume after playing
                double currentVolume = FXGL.getSettings().getGlobalMusicVolume();
                System.out.println("[DEBUG] Current global music volume: " + currentVolume);

                System.out.println("[DEBUG] Music started playing");
            } else {
                System.out.println("[DEBUG] Failed to load music file");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Error playing BGM: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stopBGM() {
        if (currentBGM != null) {
            FXGL.getAudioPlayer().stopMusic(currentBGM);
            currentBGM = null;
        }
    }

    public void playTypingSound(boolean correct) {
        try {
            if (correct) {
                // Play random typing sound
                int index = (int)(Math.random() * TYPING_SOUNDS.length);
                FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(TYPING_SOUNDS[index]));
            } else {
                // Play error sound
                FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(ERROR_TYPE));
            }
        } catch (Exception e) {
            System.err.println("Error playing typing sound: " + e.getMessage());
        }
    }

    public void playEnemyDefeat() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(ENEMY_DEFEAT));
        } catch (Exception e) {
            System.err.println("Error playing enemy defeat sound: " + e.getMessage());
        }
    }

    public void playGameOver() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(GAME_OVER));
        } catch (Exception e) {
            System.err.println("Error playing game over sound: " + e.getMessage());
        }
    }

    public void playVictory() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(VICTORY));
        } catch (Exception e) {
            System.err.println("Error playing victory sound: " + e.getMessage());
        }
    }

    private void ensureBackgroundMusic(String type) {
        if (currentBGM == null) {
            System.out.println("[DEBUG] Background music not playing, restarting it");
            playBGM(type);
        }
    }

    public void playWaveAnnounce() {
        try {
            // Ensure game music is playing
            ensureBackgroundMusic("game");

            // Temporarily lower BGM volume
            double originalVolume = FXGL.getSettings().getGlobalMusicVolume();
            FXGL.getSettings().setGlobalMusicVolume(originalVolume * 0.5);

            // Play the wave announcement
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(WAVE_ANNOUNCE));

            // Restore BGM volume after a delay
            FXGL.runOnce(() -> {
                FXGL.getSettings().setGlobalMusicVolume(originalVolume);
                // Double check that music is still playing
                ensureBackgroundMusic("game");
            }, javafx.util.Duration.seconds(2));
        } catch (Exception e) {
            System.err.println("Error playing wave announce sound: " + e.getMessage());
        }
    }

    public void playWingFlap() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(WING_FLAP));
        } catch (Exception e) {
            System.err.println("Error playing wing flap sound: " + e.getMessage());
        }
    }

    public void playButtonHover() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(BUTTON_HOVER));
        } catch (Exception e) {
            System.err.println("Error playing button hover sound: " + e.getMessage());
        }
    }

    public void playButtonClick() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(BUTTON_CLICK));
        } catch (Exception e) {
            System.err.println("Error playing button click sound: " + e.getMessage());
        }
    }

    public void playSpacebarComplete() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(SPACEBAR_COMPLETE));
        } catch (Exception e) {
            System.err.println("Error playing spacebar complete sound: " + e.getMessage());
        }
    }

    public void playShiftCycle() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(SHIFT_CYCLE));
        } catch (Exception e) {
            System.err.println("Error playing shift cycle sound: " + e.getMessage());
        }
    }

    public void setMusicVolume(double volume) {
        bgmVolume = Math.max(0.0, Math.min(1.0, volume));
        FXGL.getSettings().setGlobalMusicVolume(bgmVolume);
    }

    public void setSFXVolume(double volume) {
        sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        FXGL.getSettings().setGlobalSoundVolume(sfxVolume);
    }

    public void fadeOutBGM(Duration duration) {
        if (currentBGM != null) {
            // Simply lower the volume - the music will be stopped separately
            FXGL.getSettings().setGlobalMusicVolume(0.0);
        }
    }

    public void fadeInBGM(Duration duration) {
        if (currentBGM != null) {
            // Simply restore the volume
            FXGL.getSettings().setGlobalMusicVolume(bgmVolume);
        }
    }

    public void playDamage() {
        try {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound(DAMAGE));
        } catch (Exception e) {
            System.err.println("Error playing button hover sound: " + e.getMessage());
        }
    }
}