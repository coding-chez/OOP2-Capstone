package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.particle.ParticleComponent;
import com.almasb.fxgl.particle.ParticleEmitter;
import com.oop2.typewiz.util.ThreadManager;
import javafx.animation.*;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.effect.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.util.Duration;
import com.almasb.fxgl.audio.Music;
import com.oop2.typewiz.util.SoundManager;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainMenuScreen extends FXGLMenu {

    private ExecutorService executorService = Executors.newCachedThreadPool();
    private Music bgmMusic;
    private Timeline musicFadeTimeline;


    public MainMenuScreen() {
        super(MenuType.MAIN_MENU);

        // Start menu music
        SoundManager.getInstance().playBGM("menu");

        // Main container with magical gradient background
        StackPane root = new StackPane();
        FXGL.getAssetLoader().loadSound("sound-library/click.wav");
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #2a0845, #4a148c);");
        root.setStyle(
                "-fx-background-image: url('assets/textures/background-and-platforms/mainmenubg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );
        // Glass panel effect (matches difficulty screen)
        Rectangle panel = new Rectangle(600, 700); // was 500 x 550
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new DropShadow(20, Color.web("#ffeb3b", 0.3)));

        // Game title with magical effects (Bouncing Animation)
        Text title = new Text("TYPEWIZ");
        title.setFont(Font.font("Papyrus", FontWeight.EXTRA_BOLD, 72));
        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#b388ff")),
                new Stop(0.5, Color.web("#ffeb3b")),
                new Stop(1, Color.web("#b388ff"))));
        title.setEffect(new Glow(0.8));

        // Bouncing animation for title
        ThreadManager.runAsyncThenUI(
                () -> {
                    TranslateTransition bounce = new TranslateTransition(Duration.seconds(1), title);
                    bounce.setByY(-20); // Move up
                    bounce.setCycleCount(Animation.INDEFINITE);
                    bounce.setAutoReverse(true);
                    bounce.setInterpolator(Interpolator.EASE_BOTH);
                    bounce.play();
                },
                () -> {} // UI thread callback (empty since animation starts asynchronously)
        );

        // Subtitle with typewriter effect (Fade-In Animation)
        Text subtitle = new Text("Master the Magic of Typing");
        subtitle.setFont(Font.font("Consolas", 24));
        subtitle.setFill(Color.web("#d1c4e9"));
        subtitle.setEffect(new DropShadow(5, Color.web("#7e57c2")));

        // Fade-in animation for subtitle
        ThreadManager.runAsyncThenUI(
                () -> {
                    FadeTransition fadeIn = new FadeTransition(Duration.seconds(2), subtitle);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    fadeIn.play();
                },
                () -> {} // UI thread callback
        );


        Button startButton = createWizardButton("START QUEST", () -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            FXGL.getSceneService().pushSubScene(new DifficultyMenuScreen(() -> FXGL.getSceneService().popSubScene(),
                    () -> runStartGameThread(Difficulty.APPRENTICE),
                    () -> runStartGameThread(Difficulty.WIZARD),
                    () -> runStartGameThread(Difficulty.ARCHMAGE)));
        });


        Button helpButton = createWizardButton("WIZARD'S GUIDE", () -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            FXGL.getSceneService().pushSubScene(new HowToPlayScreen(() -> FXGL.getSceneService().popSubScene()));
        });

        Button activityButton = createWizardButton("CREATORS", () -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            FXGL.getSceneService().pushSubScene(new CreditsScreen(() -> FXGL.getSceneService().popSubScene()));
        });

        Button exitButton = createWizardButton("LEAVE TOWER", () -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            FXGL.getGameController().exit();
        });

        // Layout
        VBox menuBox = new VBox(20, title, subtitle, startButton, helpButton, activityButton, exitButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);

        // Menu Box Fade and Scale Animation
        menuBox.setOpacity(0);
        ThreadManager.runAsyncThenUI(
                () -> {
                    ScaleTransition scaleIn = new ScaleTransition(Duration.seconds(1), menuBox);
                    scaleIn.setToX(1);
                    scaleIn.setToY(1);

                    FadeTransition fadeInMenu = new FadeTransition(Duration.seconds(1), menuBox);
                    fadeInMenu.setFromValue(0);
                    fadeInMenu.setToValue(1);

                    ParallelTransition parallelTransition = new ParallelTransition(fadeInMenu, scaleIn);
                    parallelTransition.play();
                },
                () -> {} // UI thread callback
        );

        // Glass panel with menu box inside
        StackPane glassPane = new StackPane(panel, menuBox);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);

        // Load and loop BGM
        bgmMusic = FXGL.getAssetLoader().loadMusic("bgm.mp3");
        FXGL.getAudioPlayer().loopMusic(bgmMusic);


        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
//
//// Start volume at 0
//        FXGL.getSettings().setGlobalMusicVolume(0.0);
//
//// Fade in using timeline (manually updating)
//        musicFadeTimeline = new Timeline(
//                new KeyFrame(Duration.ZERO, event -> FXGL.getSettings().setGlobalMusicVolume(0.0)),
//                new KeyFrame(Duration.seconds(3), event -> FXGL.getSettings().setGlobalMusicVolume(0.4)) // Fade to target volume
//        );
//        musicFadeTimeline.setCycleCount(1);
//        musicFadeTimeline.play();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();

        // Fade out manually
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, event -> FXGL.getSettings().setGlobalMusicVolume(0.4)),
                new KeyFrame(Duration.seconds(2), event -> FXGL.getSettings().setGlobalMusicVolume(0.0))
        );
        fadeOut.setOnFinished(e -> FXGL.getAudioPlayer().stopMusic(bgmMusic));
        fadeOut.play();

        // Fade out menu music when leaving menu
        SoundManager.getInstance().fadeOutBGM(Duration.seconds(2.0));
    }



    private void runStartGameThread(Difficulty difficulty) {
        ThreadManager.runAsyncThenUI(
                () -> {
                    FXGL.getWorldProperties().setValue("difficulty", difficulty);
                    saveDifficultyToFile(difficulty);
                    System.out.println("[DEBUG] Set difficulty in menu: " + FXGL.getWorldProperties().getObject("difficulty"));
                    // Simulate preloading sounds/resources
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {}
                },
                () -> FXGL.getGameController().startNewGame()
        );
    }

    private static void saveDifficultyToFile(Difficulty difficulty) {
        try {
            java.nio.file.Files.writeString(java.nio.file.Path.of("selected_difficulty.txt"), difficulty.name());
        } catch (Exception e) {
            System.out.println("[DEBUG] Failed to save difficulty: " + e.getMessage());
        }
    }

    private void preloadAssetsInBackground() {
        // Simulate loading assets
        try {
            Thread.sleep(300); // Replace with actual preload code like FXGL.getAssetLoader().load...
        } catch (InterruptedException ignored) {}
    }

    private Button createWizardButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setFont(Font.font("Papyrus", FontWeight.BOLD, 28));
        button.setTextFill(Color.web("#e2b0ff"));
        button.setPrefWidth(350);
        button.setPrefHeight(70);

        // Set default transparent background
        button.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(10),
                javafx.geometry.Insets.EMPTY
        )));

        // Gradient border
        button.setBorder(new Border(new BorderStroke(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#b388ff")),
                        new Stop(0.5, Color.web("#ffeb3b")),
                        new Stop(1, Color.web("#b388ff"))),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(3))
        ));

        // Set default cursor
        button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);

        // Hover effects (Scale Animation)
        button.setOnMouseEntered(e -> {
            // Play hover sound
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
            SoundManager.getInstance().playButtonHover();
            button.setTextFill(Color.web("#f8bbd0"));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.1);
            st.setToY(1.1);
            st.play();
        });

        button.setOnMouseExited(e -> {
            button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            button.setTextFill(Color.web("#ce93d8"));
            ScaleTransition st = new ScaleTransition(Duration.millis(200), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });

        // Click effect (Scale Animation)
        button.setOnAction(e -> {
            // Play click sound
            ScaleTransition st = new ScaleTransition(Duration.millis(100), button);
            st.setToX(0.9);
            st.setToY(0.9);
            st.setOnFinished(event -> action.run());
            st.play();
        });

        return button;
    }

    private void startGame(Difficulty difficulty) {
        FXGL.getWorldProperties().setValue("difficulty", difficulty);
        FXGL.getGameController().startNewGame();
    }
}
