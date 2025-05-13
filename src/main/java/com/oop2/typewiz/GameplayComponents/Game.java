package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.oop2.typewiz.SceneManager;
import com.oop2.typewiz.TypeWizApp;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.*;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.Parent;
import com.oop2.typewiz.Difficulty;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Main Game class implementing MVC pattern:
 * - Model: GameStateManager, EntityManager, PlayerManager, WaveManager
 * - View: UIFactory, GamePromptFactory, GargoyleFactory
 * - Controller: InputManager, Game (as coordinator)
 *
 * @implNote This class should not be launched directly.
 *           Use {@link com.oop2.typewiz.GameLauncher} instead.
 */
public class Game extends GameApplication {
    // Define EntityType enum inside the Game class
    public enum EntityType {
        PLATFORM,
        PLAYER,
        MOVING_BLOCK,
        GARGOYLE,
        GRIMOUGE,
        VYLEYE
    }

    // Core game constants
    private static final int MAX_WAVES = 10;
    private static final double GARGOYLE_SPEED = 50.0;
    private static final double GARGOYLE_FRAME_WIDTH = 288;
    private static final double GARGOYLE_FRAME_HEIGHT = 312;
    private static final double GARGOYLE_SCALE = 0.6;

    // Manager instances (MVC components)
    private GameStateManager stateManager;     // Model
    private EntityManager entityManager;       // Model
    private WaveManager waveManager;           // Model
    private PlayerManager playerManager;       // Model
    private InputManager inputManager;         // Controller

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("TypeWiz - Winter Theme");
        settings.setManualResizeEnabled(true);
        settings.setPreserveResizeRatio(true);
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
        settings.setGameMenuEnabled(false);
        settings.setIntroEnabled(false);
        settings.setProfilingEnabled(false);
        settings.setCloseConfirmation(false);
    }

    @Override
    protected void initGame() {
        // Initialize managers first (Model and Controller components)
        initializeManagers();

        // Start game music with fade in
        SoundManager.getInstance().playBGM("game");
        SoundManager.getInstance().fadeInBGM(Duration.seconds(2.0));

        // Add global event filter to properly handle shift key and ESC key
        setupGlobalKeyHandling();

        // Initialize the game environment (View setup)
        setupGameEnvironment();

        // Set up state handlers
        setupStateHandlers();

        // Start the first wave
        stateManager.announceWave(1);
    }

    /**
     * Sets up the game environment (View component)
     */
    private void setupGameEnvironment() {
        // Set up background and platform
        UIFactory.createBackground(FXGL.getAppWidth(), FXGL.getAppHeight());
        UIFactory.createPlatform();

        // Add the wizard character
        UIFactory.createWizard();

        // Initialize animations for entities
        GargoyleFactory.initializeAnimations();
        GrimougeFactory.initializeAnimations();
        VyleyeFactory.initializeAnimations();

        // Set up UI elements
        UIFactory.createUI(this);

//        // Add controls text guide
//        Node controlsGuide = UIFactory.createControlsText();
//        controlsGuide.setId("controls-guide");
//        FXGL.addUINode(controlsGuide);
    }

    /**
     * Initializes all manager classes (Model and Controller components)
     */
    private void initializeManagers() {
        // Get selected difficulty
        Difficulty difficulty = FXGL.getWorldProperties().getObject("difficulty");
        System.out.println("[DEBUG] Selected difficulty: " + difficulty + " (" + (difficulty == null ? "null" : difficulty.getClass().getName()) + ")");
        if (difficulty == null) difficulty = Difficulty.APPRENTICE;

        // Set parameters based on difficulty
        int maxWaves;
        int maxActiveEntities;
        int[] waveSpawnsPerWave;
        double[] waveSpeedMultipliers;
        int[] minSpawnsPerGroupByWave;
        int[] maxSpawnsPerGroupByWave;
        double[] spawnDelayMultipliers;

        switch (difficulty) {
            case APPRENTICE :
                maxWaves = 5;
                maxActiveEntities = 10;
                waveSpawnsPerWave = new int[]{6, 7, 8, 9, 10};
                waveSpeedMultipliers = new double[]{0.5, 0.6, 0.7, 0.8, 0.9}; // super slow
                minSpawnsPerGroupByWave = new int[]{1, 1, 1, 1, 1};
                maxSpawnsPerGroupByWave = new int[]{1, 1, 2, 2, 3}; // max follow up entity is 1, increase per 3 waves
                spawnDelayMultipliers = new double[]{1.2, 1.1, 1.0, 0.95, 0.9};
                
            
            break;
            case WIZARD :
                maxWaves = 8;
                maxActiveEntities = 10  ;
                waveSpawnsPerWave = new int[]{6, 7, 8, 9, 10, 11, 12, 13};
                waveSpeedMultipliers = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8};
                minSpawnsPerGroupByWave = new int[]{1, 1, 1, 1, 1, 1, 1, 1};
                maxSpawnsPerGroupByWave = new int[]{0, 0, 0, 0, 1, 1, 2, 2};
                spawnDelayMultipliers = new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};
                break;
            
            case ARCHMAGE :
                maxWaves = 12;
                maxActiveEntities = 18;
                waveSpawnsPerWave = new int[]{10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32};
                waveSpeedMultipliers = new double[]{1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0, 2.2};
                minSpawnsPerGroupByWave = new int[]{1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3};
                maxSpawnsPerGroupByWave = new int[]{2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8};
                spawnDelayMultipliers = new double[]{0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5, 0.45, 0.4, 0.35};
                break;
            
            default: throw new IllegalStateException("Unknown difficulty: " + difficulty);
        }

        // Create model components
        stateManager = new GameStateManager();
        entityManager = new EntityManager(FXGL.getAppWidth(), FXGL.getAppHeight(), maxActiveEntities);
        playerManager = new PlayerManager();
        inputManager = new InputManager(entityManager, playerManager, stateManager);
        waveManager = new WaveManager(
            entityManager,
            stateManager,
            this::getRandomWordForWave,
            FXGL.getAppHeight(),
            maxWaves,
            waveSpawnsPerWave,
            waveSpeedMultipliers,
            minSpawnsPerGroupByWave,
            maxSpawnsPerGroupByWave,
            spawnDelayMultipliers
        );
        FXGL.getWorldProperties().setValue("playerManager", playerManager);
        FXGL.getWorldProperties().setValue("inputManager", inputManager);
        inputManager.setupInput();
        inputManager.setRestartGameCallback(v -> restartGame());
    }

    /**
     * Word generator based on wave difficulty
     */
    private String getRandomWordForWave() {
        // Use the WordFactory to generate an appropriate word
        return WordFactory.getInstance().getWordForWave(waveManager.getCurrentWave());
    }

    /**
     * Sets up handlers for different game states (Controller component)
     */
    private void setupStateHandlers() {
        // Set actions for state transitions
        stateManager.setStateEntryAction(GameStateManager.GameState.WAVE_ANNOUNCEMENT, wave -> {
            // Play wave announcement sound
            SoundManager.getInstance().playWaveAnnounce();
            System.out.println("Entering WAVE_ANNOUNCEMENT state");

            // Start the wave to initialize parameters
            waveManager.startWave();

            // Reset any current input state to prepare for the new wave
            inputManager.reset();

            // Update the wave counter in the UI
            updateWaveUI(waveManager.getCurrentWave());

            // Show wave announcement and start wave
            Node announcementNode = GamePromptFactory.createWaveAnnouncement(
                    waveManager.getCurrentWave(), waveManager.getMaxWaves());

            Entity announcement = FXGL.entityBuilder()
                    .view(announcementNode)
                    .zIndex(50)
                    .buildAndAttach();

            // Delay the transition to PLAYING state
            FXGL.runOnce(() -> {
                announcement.removeFromWorld();
                System.out.println("Transitioning to PLAYING state after wave announcement");
                stateManager.startPlaying(null);
                waveManager.startSpawning();
            }, Duration.seconds(2.0));
        });

        stateManager.setStateEntryAction(GameStateManager.GameState.GAME_OVER, message -> {
            // Stop game music with fade out
            SoundManager.getInstance().fadeOutBGM(Duration.seconds(2.0));
            showEndGameScreen("Game Over!", false);
        });

        stateManager.setStateEntryAction(GameStateManager.GameState.VICTORY, message -> {
            // Stop game music with fade out
            SoundManager.getInstance().fadeOutBGM(Duration.seconds(2.0));
            showEndGameScreen("Victory! Game Complete!", true);
        });

        // Add state exit actions
        stateManager.setStateExitAction(GameStateManager.GameState.GAME_OVER, message -> {
            // No need to start menu music since we'll start game music on restart
        });

        stateManager.setStateExitAction(GameStateManager.GameState.VICTORY, message -> {
            // No need to start menu music since we'll start game music on restart
        });
    }

    /**
     * Shows game over or victory screen
     */
    private void showEndGameScreen(String title, boolean isVictory) {
        // Stop game music first
        SoundManager.getInstance().stopBGM();

        // Play appropriate sound effect
        if (isVictory) {
            SoundManager.getInstance().playVictory();
        } else {
            SoundManager.getInstance().playGameOver();
        }

        // Remove controls guide
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node.getId() != null && node.getId().equals("controls-guide")) {
                nodesToRemove.add(node);
            }
        }
        // Remove nodes outside the loop to avoid concurrent modification
        for (Node node : nodesToRemove) {
            FXGL.removeUINode(node);
        }
        playerManager.updateHighScoreInDatabaseIfNeeded();

        // Pass character count to statistics factory
        StatsUIFactory.setTotalCharactersTyped(playerManager.getTotalCharactersTyped());

        // Create game over screen with all statistics
        Node endGameScreen = GamePromptFactory.createGameOverScreen(
                title,
                playerManager.getScore(),
                playerManager.calculateWPM(),
                playerManager.calculateRawWPM(),
                playerManager.calculateAccuracy(),
                playerManager.calculateConsistency(),
                playerManager.getWpmOverTime(),
                playerManager.getAccuracyOverTime(),
                PlayerManager.getHighScore()
        );

        // Add a unique ID to the screen for easy identification
        endGameScreen.setId(isVictory ? "victory-screen" : "game-over-screen");

        // Set up the play again button
        GamePromptFactory.setupPlayAgainButton(endGameScreen, this::restartGame);

        FXGL.entityBuilder()
                .view(endGameScreen)
                .zIndex(100)
                .buildAndAttach();
    }

    @Override
    protected void onUpdate(double tpf) {
        // Skip update if game is not active
        if (!stateManager.isInState(GameStateManager.GameState.PLAYING)) {
            return;
        }

        // Check player health
        if (playerManager.getHealth() <= 0) {
            stateManager.gameOver(null);
            return;
        }

        // Update wave spawning
        boolean waveCompleted = waveManager.update();
        if (waveCompleted) {
            if (waveManager.areAllWavesCompleted()) {
                stateManager.victory(null);
            } else {
                stateManager.completeWave(null);
                stateManager.announceWave(waveManager.getCurrentWave());
            }
        }

        // Update entity positions
        entityManager.updateEntities(tpf, waveManager.getCurrentWaveSpeedMultiplier());

        // Process entity removals
        entityManager.processRemovals();

        // Update performance display
        UIFactory.updatePerformanceDisplay(tpf);
    }

    /**
     * Restarts the game by resetting all components
     */
    private void restartGame() {
        System.out.println("=== Starting game restart ===");
        // Play button click sound
        SoundManager.getInstance().playButtonClick();

        // Start game music again with fade in
        SoundManager.getInstance().playBGM("game");
        SoundManager.getInstance().fadeInBGM(Duration.seconds(1.0));

        // Remove game over or victory screens
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getZIndex() >= 100) {  // Game over screens have high Z-index
                entitiesToRemove.add(entity);
            }
        }
        System.out.println("Found " + entitiesToRemove.size() + " entities to remove");

        // Remove them outside the iteration to avoid concurrent modification
        for (Entity entity : entitiesToRemove) {
            entity.removeFromWorld();
        }

        // Also check UI nodes
        List<Node> nodesToRemove = new ArrayList<>();
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node.getId() != null &&
                    (node.getId().equals("game-over-screen") ||
                            node.getId().equals("victory-screen"))) {
                nodesToRemove.add(node);
            }
        }
        System.out.println("Found " + nodesToRemove.size() + " UI nodes to remove");

        // Remove UI nodes
        for (Node node : nodesToRemove) {
            FXGL.removeUINode(node);
        }

        System.out.println("Resetting all managers...");
        // Reset all managers
        entityManager.clear();
        playerManager.reset();
        playerManager.resetHealth();
        inputManager.reset();
        waveManager.reset();

        // Reset UI to wave 1
        updateWaveUI(1);

        // Force game state to PLAYING before starting the wave
        stateManager.startPlaying(null);

        // Re-setup input handlers
        System.out.println("Re-setting up input handlers");
        inputManager.setupInput();

//        // Add back the controls guide
//        Node controlsGuide = UIFactory.createControlsText();
//        controlsGuide.setId("controls-guide");
//        FXGL.addUINode(controlsGuide);

        // Small delay before starting a new game to ensure clean state
        FXGL.getGameTimer().runOnceAfter(() -> {
            System.out.println("Starting wave 1");
            // Start a new wave
            stateManager.announceWave(1);
        }, javafx.util.Duration.millis(200));

        System.out.println("=== Game restart completed ===");
    }

    /**
     * Gets the player manager instance
     * @return The player manager
     */
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    /**
     * Gets the input manager instance
     * @return The input manager
     */
    public InputManager getInputManager() {
        return inputManager;
    }

    /**
     * Updates the wave counter in the UI
     * @param wave Current wave number
     */
    private void updateWaveUI(int wave) {
        // Find the top bar HBox in the UI
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof HBox) {
                HBox topBar = (HBox) node;

                // Check if this is the top bar by looking for wave display
                for (Node child : topBar.getChildren()) {
                    if (child instanceof VBox) {
                        VBox box = (VBox) child;
                        if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Text) {
                            Text label = (Text) box.getChildren().get(0);
                            if (label.getText().equals("WAVE")) {
                                // Found the wave display, update it
                                UIFactory.updateWave(topBar, wave);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Sets up global key event handling for special keys
     */
    private void setupGlobalKeyHandling() {
        // Track the last time the shift key was processed to prevent rapid repeats
        final long[] lastShiftKeyTime = {0};
        final long SHIFT_DEBOUNCE_MS = 200; // Debounce time in milliseconds

        // Handle SHIFT key presses for cycling through targets and ESC for pause
        FXGL.getInput().addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.SHIFT) {
                // If in playing state, cycle through targets
                if (stateManager.isInState(GameStateManager.GameState.PLAYING)) {
                    // Debounce logic to prevent too rapid cycling
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastShiftKeyTime[0] > SHIFT_DEBOUNCE_MS) {
                        inputManager.cycleToNextWordBlock();
                        lastShiftKeyTime[0] = currentTime;
                    }
                    event.consume(); // Prevent event from being processed further
                }
            } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                // Handle ESC key for pause menu
                System.out.println("ESC key pressed - Current state: " + stateManager.getCurrentState());
                if (stateManager.isInState(GameStateManager.GameState.PLAYING)) {
                    System.out.println("Showing pause menu");
                    showPauseMenu();
                    event.consume();
                } else if (stateManager.isInState(GameStateManager.GameState.PAUSED)) {
                    System.out.println("Resuming game");
                    resumeGame();
                    event.consume();
                }
            }
        });
    }

    private void showPauseMenu() {
        System.out.println("Creating pause menu");

        // Hide controls guide
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node.getId() != null && node.getId().equals("controls-guide")) {
                node.setVisible(false);
            }
        }

        // Create pause menu
        Node pauseMenu = PauseMenuFactory.createPauseMenu(
                this::resumeGame,      // Resume action
                this::restartGame,     // Restart action
                this::backToTower      // Back to tower action
        );

        // Add unique ID for the pause menu
        pauseMenu.setId("pause-menu");

        // Add to scene with high Z-index
        FXGL.entityBuilder()
                .view(pauseMenu)
                .zIndex(200)  // Higher than game over screen
                .buildAndAttach();

        // Pause the game state
        stateManager.pauseGame(null);
        System.out.println("Pause menu created and game state set to PAUSED");

        // Lower BGM volume
        SoundManager.getInstance().setMusicVolume(0.2);
    }

    private void resumeGame() {
        System.out.println("Removing pause menu");
        // Remove pause menu
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getViewComponent().getChildren().stream()
                    .anyMatch(node -> "pause-menu".equals(node.getId()))) {
                entity.removeFromWorld();
            }
        }

        // Show controls guide again
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node.getId() != null && node.getId().equals("controls-guide")) {
                node.setVisible(true);
            }
        }

        // Resume game state
        stateManager.resumeGame(null);
        System.out.println("Pause menu removed and game state set to PLAYING");

        // Restore BGM volume
        SoundManager.getInstance().setMusicVolume(0.4);
    }

    private void backToTower() {
        // Stop game music
        SoundManager.getInstance().stopBGM();

        // Play button click sound
        SoundManager.getInstance().playButtonClick();

        // TODO: Implement back to tower functionality
        // For now, just restart the game
        SceneManager.showScreen(TypeWizApp.ScreenType.MAIN_MENU);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
