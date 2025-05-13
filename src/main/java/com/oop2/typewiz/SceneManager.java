package com.oop2.typewiz;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.SoundManager;
import javafx.scene.Node;

public class SceneManager {
    public static void showScreen(TypeWizApp.ScreenType screenType) {
        FXGL.getGameScene().clearUINodes();  // Clear existing nodes first

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
            case MAIN_MENU ->{
                FXGL.getSceneService().pushSubScene(new MainMenuScreen());
            }case DIFFICULTY_SELECTION -> {
                new DifficultyMenuScreen(
                        () -> showScreen(TypeWizApp.ScreenType.MAIN_MENU),
                        () -> startGame(Difficulty.APPRENTICE),
                        () -> startGame(Difficulty.WIZARD),
                        () -> startGame(Difficulty.ARCHMAGE)
                ).getContentRoot();
            }
            default -> throw new IllegalStateException("Unexpected screen: " + screenType);
        };

        TypeWizApp.setupCustomCursor();
    }

    private static void startGame(Difficulty difficulty) {
        // Store the selected difficulty
        FXGL.getWorldProperties().setValue("difficulty", difficulty);

        // Stop menu music first
        SoundManager.getInstance().stopBGM();

        // Small delay before starting the game to ensure clean state
        FXGL.runOnce(() -> {
            // Start the game
            FXGL.getGameController().startNewGame();
        }, javafx.util.Duration.millis(100));
    }
}
