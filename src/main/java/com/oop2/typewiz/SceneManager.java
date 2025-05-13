package com.oop2.typewiz;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.Node;

public class SceneManager {
    public static void showScreen(TypeWizApp.ScreenType screenType) {
        System.out.println("[DEBUG] Showing screen: " + screenType);
        FXGL.getGameScene().clearUINodes();  // Clear existing nodes first

        // Set default cursor for the entire scene
        FXGL.getGameScene().getRoot().setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);

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

        // Ensure cursor is set after screen transition
        FXGL.runOnce(() -> {
            FXGL.getGameScene().getRoot().setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
        }, javafx.util.Duration.millis(100));
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
        }, javafx.util.Duration.millis(100));

        // Start game music with a longer delay to ensure game is initialized
        FXGL.runOnce(() -> {
            System.out.println("[DEBUG] Starting game music in SceneManager");
            SoundManager.getInstance().playBGM("game");
        }, javafx.util.Duration.millis(300));
    }
}
