package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.oop2.typewiz.GameplayComponents.*;
import com.oop2.typewiz.util.CustomSceneFactory;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
//import javafx.scene.image.ImageView;

import com.oop2.typewiz.Difficulty;

public class TypeWizApp extends GameApplication {

    public enum ScreenType {
        LOGIN,
        REGISTER,
        LOADING,
        MAIN_MENU,
        DIFFICULTY_SELECTION
    }

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

    public static ImageCursor CLOSED_BOOK_CURSOR;
    public static ImageCursor OPEN_BOOK_CURSOR;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("TypeWiz");
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(false);  // Disable built-in game menu
        settings.setSceneFactory(new CustomSceneFactory());
        settings.setFullScreenAllowed(true);
        settings.setFullScreenFromStart(true);
    }

    public static void setupCustomCursor() {
        Image closedBookImg = FXGL.image("magicbook.png");
        Image openBookImg = FXGL.image("magicbook_hover.png");

        Image scaledClosedBookImg = new Image(closedBookImg.getUrl(), 32, 32, true, true);
        Image scaledOpenBookImg = new Image(openBookImg.getUrl(), 32, 32, true, true);

        CLOSED_BOOK_CURSOR = new ImageCursor(scaledClosedBookImg, 16, 16); // Centered hotspot
        OPEN_BOOK_CURSOR = new ImageCursor(scaledOpenBookImg, 16, 16); // Centered hotspot

        // default cursor
        FXGL.getGameScene().getRoot().setCursor(CLOSED_BOOK_CURSOR);
    }

    @Override
    protected void initUI() {
        // Initialize custom cursor for the first screen
        setupCustomCursor();
    }
    @Override
    protected void initGame() {
        System.out.println("[DEBUG] Starting game initialization");
        // Initialize managers first (Model and Controller components)
        FXGL.getAssetLoader().loadSound("sound-library/click.wav");
        initializeManagers();

        // Add global event filter to properly handle shift key
        setupGlobalKeyHandling();

        // Initialize the game environment (View setup)
        setupGameEnvironment();

        // Set up state handlers
        setupStateHandlers();

        // Make sure any previous music is stopped
        SoundManager.getInstance().stopBGM();

        // Start game music with fade in
        System.out.println("[DEBUG] Starting game music");
        SoundManager.getInstance().playBGM("game");
        SoundManager.getInstance().fadeInBGM(Duration.seconds(2.0));

        // Start the first wave
        stateManager.announceWave(1);

        System.out.println("[DEBUG] Game initialization complete");
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
    }

    /**
     * Initializes all manager classes (Model and Controller components)
     */
    private void initializeManagers() {
        // Get selected difficulty robustly
        Object diffObj = null;
        if (FXGL.getWorldProperties().exists("difficulty")) {
            diffObj = FXGL.getWorldProperties().getObject("difficulty");
        }
        Difficulty difficulty = null;
        if (diffObj instanceof Difficulty) {
            difficulty = (Difficulty) diffObj;
        } else if (diffObj instanceof String) {
            try {
                difficulty = Difficulty.valueOf((String) diffObj);
            } catch (Exception e) {
                System.out.println("[DEBUG] Invalid difficulty string: " + diffObj);
            }
        }
        // Fallback: read from file if not set in memory
        if (difficulty == null) {
            difficulty = loadDifficultyFromFile();
        }
        System.out.println("[DEBUG] Selected difficulty: " + difficulty + " (" + (difficulty == null ? "null" : difficulty.getClass().getName()) + ")");
        if (difficulty == null) difficulty = Difficulty.APPRENTICE;

        // Store both the enum and string representation
        FXGL.getWorldProperties().setValue("difficultyString", difficulty.toString());
        FXGL.getWorldProperties().setValue("difficulty", difficulty);

        // Set parameters based on difficulty
        int maxWaves;
        int maxActiveEntities;
        int[] waveSpawnsPerWave;
        double[] waveSpeedMultipliers;
        int[] minSpawnsPerGroupByWave;
        int[] maxSpawnsPerGroupByWave;
        double[] spawnDelayMultipliers;

        switch (difficulty) {
            case APPRENTICE -> {
                maxWaves = 5;
                maxActiveEntities = 5;
                waveSpawnsPerWave = new int[]{4, 5, 6, 7, 8};
                waveSpeedMultipliers = new double[]{0.4, 0.4, 0.4, 0.5, 0.6};
                minSpawnsPerGroupByWave = new int[]{1, 1, 1, 1, 1};
                maxSpawnsPerGroupByWave = new int[]{1, 1, 2, 2, 2};
                spawnDelayMultipliers = new double[]{1.5, 1.5, 1.4, 1.3, 1.3};
            }
            case WIZARD -> {
                maxWaves = 8;
                maxActiveEntities = 14;
                waveSpawnsPerWave = new int[]{8, 10, 12, 14, 16, 18, 20, 22};
                waveSpeedMultipliers = new double[]{0.8, 0.9, 1.0, 1.1, 1.2, 1.3, 1.4, 1.5};
                minSpawnsPerGroupByWave = new int[]{1, 1, 2, 2, 2, 3, 3, 3};
                maxSpawnsPerGroupByWave = new int[]{2, 2, 3, 3, 4, 4, 5, 5};
                spawnDelayMultipliers = new double[]{1.0, 0.95, 0.9, 0.85, 0.8, 0.75, 0.7, 0.65};
            }
            case ARCHMAGE -> {
                maxWaves = 12;
                maxActiveEntities = 18;
                waveSpawnsPerWave = new int[]{10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32};
                waveSpeedMultipliers = new double[]{1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 2.0, 2.2};
                minSpawnsPerGroupByWave = new int[]{1, 2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5};
                maxSpawnsPerGroupByWave = new int[]{2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8};
                spawnDelayMultipliers = new double[]{0.9, 0.85, 0.8, 0.75, 0.7, 0.65, 0.6, 0.55, 0.5, 0.45, 0.4, 0.35};
            }
            default -> {
                System.out.println("[DEBUG] Unknown difficulty value: " + difficulty);
                // Set safe defaults
                maxWaves = 5;
                maxActiveEntities = 5;
                waveSpawnsPerWave = new int[]{4, 5, 6, 7, 8};
                waveSpeedMultipliers = new double[]{0.4, 0.4, 0.4, 0.5, 0.6};
                minSpawnsPerGroupByWave = new int[]{1, 1, 1, 1, 1};
                maxSpawnsPerGroupByWave = new int[]{1, 1, 2, 2, 2};
                spawnDelayMultipliers = new double[]{1.5, 1.5, 1.4, 1.3, 1.3};
            }
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

    // Add this method to load difficulty from file
    private static Difficulty loadDifficultyFromFile() {
        try {
            String value = java.nio.file.Files.readString(java.nio.file.Path.of("selected_difficulty.txt")).trim();
            return Difficulty.valueOf(value);
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to load difficulty, defaulting to APPRENTICE: " + e.getMessage());
            return Difficulty.APPRENTICE;
        }
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

            // Start the wave to initialize parameters
            waveManager.startWave();

            // Reset any current input state to prepare for the new wave
            inputManager.reset();

            // Update the wave counter in the UI with the current wave number
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
                stateManager.startPlaying(null);
                waveManager.startSpawning();
            }, Duration.seconds(2.0));
        });

        stateManager.setStateEntryAction(GameStateManager.GameState.GAME_OVER, message -> {
            // Play game over sound and fade out game music
            SoundManager.getInstance().playGameOver();
            SoundManager.getInstance().fadeOutBGM(Duration.seconds(2.0));

            // Safely remove all UI elements first
            List<Node> nodesToRemove = new ArrayList<>();
            for (Node node : FXGL.getGameScene().getUINodes()) {
                if (node instanceof VBox || node instanceof HBox) {
                    nodesToRemove.add(node);
                }
            }
            // Remove collected nodes
            for (Node node : nodesToRemove) {
                FXGL.removeUINode(node);
            }

            // Show game over screen
            showEndGameScreen("Game Over!", false);
        });

        stateManager.setStateEntryAction(GameStateManager.GameState.VICTORY, message -> {
            // Play victory sound and fade out game music
            SoundManager.getInstance().playVictory();
            SoundManager.getInstance().fadeOutBGM(Duration.seconds(2.0));

            // Safely remove all UI elements first
            List<Node> nodesToRemove = new ArrayList<>();
            for (Node node : FXGL.getGameScene().getUINodes()) {
                if (node instanceof VBox || node instanceof HBox) {
                    nodesToRemove.add(node);
                }
            }
            // Remove collected nodes
            for (Node node : nodesToRemove) {
                FXGL.removeUINode(node);
            }

            // Show victory screen
            showEndGameScreen("Victory! Game Complete!", true);
        });

        // Add state exit actions
        stateManager.setStateExitAction(GameStateManager.GameState.GAME_OVER, message -> {
            // Start menu music when leaving game over screen
            SoundManager.getInstance().playBGM("menu");
            SoundManager.getInstance().fadeInBGM(Duration.seconds(2.0));
        });

        stateManager.setStateExitAction(GameStateManager.GameState.VICTORY, message -> {
            // Start menu music when leaving victory screen
            SoundManager.getInstance().playBGM("menu");
            SoundManager.getInstance().fadeInBGM(Duration.seconds(2.0));
        });
    }

    /**
     * Shows game over or victory screen
     */
    private void showEndGameScreen(String title, boolean isVictory) {
        // Pass character count to statistics factory
        StatsUIFactory.setTotalCharactersTyped(playerManager.getTotalCharactersTyped());

        playerManager.updateHighScoreInDatabaseIfNeeded();

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
            int nextWave = waveManager.getCurrentWave();
            System.out.println("[DEBUG] Wave completed. Current wave: " + nextWave);
            if (waveManager.areAllWavesCompleted()) {
                stateManager.victory(null);
            } else {
                // Update wave display before transitioning states
                updateWaveUI(nextWave);
                stateManager.completeWave(null);
                stateManager.announceWave(nextWave);
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
        // Play button click sound
        SoundManager.getInstance().playButtonClick();

        // Start game music
        SoundManager.getInstance().playBGM("game");
        SoundManager.getInstance().fadeInBGM(Duration.seconds(1.0));

        // Remove game over or victory screens
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getZIndex() >= 100) {  // Game over screens have high Z-index
                entitiesToRemove.add(entity);
            }
        }

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

        // Remove UI nodes
        for (Node node : nodesToRemove) {
            FXGL.removeUINode(node);
        }

        // Reset all managers
        entityManager.clear();
        playerManager.reset();
        playerManager.resetHealth();
        inputManager.reset();
        waveManager.reset();

        // Recreate UI elements
        UIFactory.createUI(this);

        // Reset UI to wave 1
        updateWaveUI(1);

        // Force game state to PLAYING before starting the wave
        stateManager.startPlaying(null);

        // Small delay before starting a new game to ensure clean state
        FXGL.getGameTimer().runOnceAfter(() -> {
            // Start a new wave
            stateManager.announceWave(1);
        }, javafx.util.Duration.millis(200));
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
        System.out.println("[DEBUG] Updating wave UI to: " + wave);
        // Find the top bar HBox in the UI
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof HBox) {
                HBox topBar = (HBox) node;

                // Check if this is the top bar by looking for wave display
                for (Node child : topBar.getChildren()) {
                    if (child instanceof StackPane) {
                        StackPane stackPane = (StackPane) child;
                        for (Node stackChild : stackPane.getChildren()) {
                            if (stackChild instanceof HBox) {
                                HBox content = (HBox) stackChild;
                                for (Node contentChild : content.getChildren()) {
                                    if (contentChild instanceof VBox) {
                                        VBox box = (VBox) contentChild;
                                        if (!box.getChildren().isEmpty() && box.getChildren().get(0) instanceof Text) {
                                            Text label = (Text) box.getChildren().get(0);
                                            if (label.getText().contains("Difficulty") || label.getText().contains("Wave")) {
                                                // Found the wave display, update it
                                                UIFactory.updateWave(topBar, wave);
                                                System.out.println("[DEBUG] Found and updated wave display");
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("[DEBUG] Could not find wave display to update");
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
                        System.out.println("Shift key pressed - cycling targets");
                        // Play shift cycle sound
                        SoundManager.getInstance().playShiftCycle();
                        // Cycle to next target
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
        // Remove pause menu - collect entities to remove first
        List<Entity> entitiesToRemove = new ArrayList<>();
        for (Entity entity : FXGL.getGameWorld().getEntities()) {
            if (entity.getViewComponent().getChildren().stream()
                    .anyMatch(node -> "pause-menu".equals(node.getId()))) {
                entitiesToRemove.add(entity);
            }
        }

        // Now safely remove the collected entities
        for (Entity entity : entitiesToRemove) {
            entity.removeFromWorld();
        }

        // Resume game state
        stateManager.resumeGame(null);
        System.out.println("Pause menu removed and game state set to PLAYING");

        // Restore BGM volume
        SoundManager.getInstance().setMusicVolume(0.4);
    }

    private void backToTower() {
        System.out.println("[DEBUG] Going back to tower");

        // Stop game music
        SoundManager.getInstance().stopBGM();

        // Play button click sound
        SoundManager.getInstance().playButtonClick();

        // Clear all game entities
        FXGL.getGameWorld().getEntities().clear();

        // Remove all UI nodes
        List<Node> nodesToRemove = new ArrayList<>(FXGL.getGameScene().getUINodes());
        for (Node node : nodesToRemove) {
            FXGL.removeUINode(node);
        }

        // Reset game state
        if (stateManager != null) {
            stateManager.startPlaying(null);
        }

        // Show main menu screen and start menu music
        SceneManager.showScreen(TypeWizApp.ScreenType.MAIN_MENU);

        // Start menu music with a small delay
        FXGL.runOnce(() -> {
            System.out.println("[DEBUG] Starting menu music after back to tower");
            SoundManager.getInstance().playBGM("menu");
        }, javafx.util.Duration.millis(200));
    }

    public static void main(String[] args) {
        launch(args);
    }
}