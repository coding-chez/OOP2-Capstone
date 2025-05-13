package com.oop2.typewiz;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SubScene;

public class SceneManager {
    /**
     * Applies the custom cursor to all nodes in a scene graph recursively
     * @param node The root node to start applying cursors from
     */
    private static void applyCustomCursorRecursively(Node node) {
        if (node == null) return;

        try {
            // Only set cursor if it's initialized
            if (TypeWizApp.CLOSED_BOOK_CURSOR != null) {
                node.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            }

            // If the node is a parent, process all its children
            if (node instanceof Parent) {
                ((Parent) node).getChildrenUnmodifiable().forEach(SceneManager::applyCustomCursorRecursively);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to apply cursor to node: " + e.getMessage());
        }
    }

    /**
     * Ensures custom cursor is applied to the entire scene
     */
    public static void ensureCustomCursor() {
        try {
            // First ensure cursors are initialized
            if (TypeWizApp.CLOSED_BOOK_CURSOR == null || TypeWizApp.OPEN_BOOK_CURSOR == null) {
                TypeWizApp.setupCustomCursor();
            }

            // Only proceed if cursors are properly initialized
            if (TypeWizApp.CLOSED_BOOK_CURSOR == null) {
                System.out.println("[WARN] Cannot apply custom cursor - cursors not initialized");
                return;
            }

            // Apply to primary stage scene if available
            if (FXGL.getPrimaryStage() != null && FXGL.getPrimaryStage().getScene() != null) {
                FXGL.getPrimaryStage().getScene().setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
                applyCustomCursorRecursively(FXGL.getPrimaryStage().getScene().getRoot());
            }

            // Apply to game scene root
            if (FXGL.getGameScene() != null) {
                if (FXGL.getGameScene().getRoot() != null) {
                    FXGL.getGameScene().getRoot().setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
                    applyCustomCursorRecursively(FXGL.getGameScene().getRoot());
                }
            }

            // Apply to all UI nodes
            if (FXGL.getGameScene() != null) {
                FXGL.getGameScene().getUINodes().forEach(node -> {
                    if (node != null) {
                        node.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
                        applyCustomCursorRecursively(node);
                    }
                });
            }

            // Apply to current subscene if any
            if (FXGL.getSceneService().getCurrentScene() != null) {
                Node root = FXGL.getSceneService().getCurrentScene().getRoot();
                if (root != null) {
                    root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
                    applyCustomCursorRecursively(root);
                }
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Failed to ensure custom cursor: " + e.getMessage());
        }
    }

    public static void showScreen(TypeWizApp.ScreenType screenType) {
        System.out.println("[DEBUG] Showing screen: " + screenType);
        FXGL.getGameScene().clearUINodes();  // Clear existing nodes first

        // Make sure cursors are initialized before screen transition
        if (TypeWizApp.CLOSED_BOOK_CURSOR == null || TypeWizApp.OPEN_BOOK_CURSOR == null) {
            TypeWizApp.setupCustomCursor();
        }

        // Apply custom cursor before screen transition
        ensureCustomCursor();

        switch (screenType) {
            case LOGIN -> {
                FXGL.getSceneService().pushSubScene(new LoginScreen());
            }
            case REGISTER -> {
                FXGL.getSceneService().pushSubScene(new RegisterScreen());
            }
            case LOADING -> {
                FXGL.getSceneService().pushSubScene(new LoadingScreen());
            }
            case MAIN_MENU -> {
                FXGL.getSceneService().pushSubScene(new MainMenuScreen());
            }
            case DIFFICULTY_SELECTION -> {
                new DifficultyMenuScreen(
                        () -> showScreen(TypeWizApp.ScreenType.MAIN_MENU),
                        () -> startGame(Difficulty.APPRENTICE),
                        () -> startGame(Difficulty.WIZARD),
                        () -> startGame(Difficulty.ARCHMAGE)
                ).getContentRoot();
            }
            default -> throw new IllegalStateException("Unexpected screen: " + screenType);
        }

        // Ensure cursor is set after screen transition with multiple checks
        FXGL.runOnce(() -> {
            ensureCustomCursor();
        }, javafx.util.Duration.millis(100));

        FXGL.runOnce(() -> {
            ensureCustomCursor();
        }, javafx.util.Duration.millis(500));

        FXGL.runOnce(() -> {
            ensureCustomCursor();
        }, javafx.util.Duration.seconds(1));
    }

    private static void startGame(Difficulty difficulty) {
        // Store the selected difficulty
        FXGL.getWorldProperties().setValue("difficulty", difficulty);
        FXGL.getWorldProperties().setValue("difficultyString", difficulty.toString());

        // Stop menu music first
        SoundManager.getInstance().stopBGM();

        // Start the game with a small delay to ensure clean state
        FXGL.runOnce(() -> {
            // Start the game
            FXGL.getGameController().startNewGame();
            // Ensure cursor is set after game starts
            ensureCustomCursor();
        }, javafx.util.Duration.millis(100));

        // Start game music with a longer delay to ensure game is initialized
        FXGL.runOnce(() -> {
            System.out.println("[DEBUG] Starting game music in SceneManager");
            SoundManager.getInstance().playBGM("game");
            // Double check cursor after game fully loads
            ensureCustomCursor();
        }, javafx.util.Duration.millis(300));

        // Final cursor check after everything is loaded
        FXGL.runOnce(() -> {
            ensureCustomCursor();
        }, javafx.util.Duration.seconds(1));
    }
}
