package com.oop2.typewiz;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.util.ThreadManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class DifficultyMenuScreen extends FXGLMenu {

    public static Node create(Runnable backAction, Runnable easyAction, Runnable mediumAction, Runnable hardAction) {
        return new DifficultyMenuScreen(backAction, easyAction, mediumAction, hardAction).getContentRoot();
    }

    DifficultyMenuScreen(Runnable backAction, Runnable easyAction, Runnable mediumAction, Runnable hardAction) {
        super(MenuType.MAIN_MENU);

        FXGL.getAssetLoader().loadSound("sound-library/click.wav");

        // Main container with magical gradient
        StackPane root = new StackPane();
        root.setPrefSize(FXGL.getAppWidth(), FXGL.getAppHeight());
        root.setStyle(
                "-fx-background-image: url('assets/textures/background-and-platforms/menubg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // Glass panel effect
        Rectangle panel = new Rectangle(500, 500);
        panel.setArcHeight(30);
        panel.setArcWidth(30);
        panel.setFill(Color.web("rgba(60, 0, 90, 0.5)"));
        panel.setStroke(Color.web("#b388ff"));
        panel.setStrokeWidth(2);
        panel.setEffect(new javafx.scene.effect.DropShadow(20, Color.web("#ffeb3b", 0.3)));

        // Title
        Text title = new Text("Choose Your Magic Level");
        title.setFont(Font.font("Papyrus", 36));
        title.setFill(Color.web("#ffeb3b"));
        title.setEffect(new javafx.scene.effect.Glow(0.5));

        // Difficulty buttons
        Button easyButton = createDifficultyButton("Apprentice", "Slow incantations", easyAction);
        Button mediumButton = createDifficultyButton("Wizard", "Standard spells", mediumAction);
        Button hardButton = createDifficultyButton("Archmage", "Lightning-fast charms", hardAction);

//        easyButton.setOnAction(e -> {
//            FXGL.play("sound-library/click.wav"); // plays the sound
//           SceneManager.showScreen(TypeWizApp.ScreenType.LOADING);
//        });
//
//        mediumButton.setOnAction(e -> {
//            FXGL.play("sound-library/click.wav"); // plays the sound
////            SceneManager.showScreen(TypeWizApp.ScreenType.LOADING);
//        });
//
//        hardButton.setOnAction(e -> {
//            FXGL.play("sound-library/click.wav"); // plays the sound
//           SceneManager.showScreen(TypeWizApp.ScreenType.LOADING);
//        });


        // Back button
        Button backButton = new Button("Back to Tower");
        backButton.setFont(Font.font("Consolas", 20));
        backButton.setTextFill(Color.web("#e2b0ff"));
        backButton.setBackground(Background.EMPTY);
        backButton.setBorder(new Border(new BorderStroke(
                Color.web("#b388ff"),
                BorderStrokeStyle.SOLID,
                new CornerRadii(5),
                new BorderWidths(1))
        ));

        addMagicHover(backButton);
        backButton.setOnAction(e -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            backAction.run();
        });

        // Layout
        VBox menuBox = new VBox(20, title, easyButton, mediumButton, hardButton, backButton);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setMaxWidth(400);

        StackPane glassPane = new StackPane(panel, menuBox);
        root.getChildren().add(glassPane);
        getContentRoot().getChildren().add(root);
        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);

    }

    private void addMagicHover(Button button) {
        button.setOnMouseEntered(e -> {
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
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
    }

    private Button createDifficultyButton(String title, String description, Runnable action) {
        Button button = new Button();
        button.setPrefWidth(350);
        button.setPrefHeight(100);

        // Main title
        Text titleText = new Text(title);
        titleText.setFont(Font.font("Papyrus", 28));
        titleText.setFill(Color.web("#ffeb3b"));

        // Description
        Text descText = new Text(description);
        descText.setFont(Font.font("Consolas", 16));
        descText.setFill(Color.web("#d1c4e9"));

        VBox content = new VBox(5, titleText, descText);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new javafx.geometry.Insets(0, 20, 0, 20));

        button.setGraphic(content);

        // Styling
        button.setBackground(new Background(new BackgroundFill(
                Color.TRANSPARENT,
                new CornerRadii(10),
                javafx.geometry.Insets.EMPTY
        )));

        button.setBorder(new Border(new BorderStroke(
                new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#b388ff")),
                        new Stop(1, Color.web("#ffeb3b"))),
                BorderStrokeStyle.SOLID,
                new CornerRadii(10),
                new BorderWidths(3))
        ));


        // Hover effects (Scale Animation)
        button.setOnMouseEntered(e -> {
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("sound-library/hover.wav"));

            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleUp = new ScaleTransition(Duration.seconds(0.2), button);
                        scaleUp.setToX(1.1);
                        scaleUp.setToY(1.1);
                        scaleUp.play();
                    },
                    () -> {} // UI thread callback
            );
        });

        button.setOnMouseExited(e -> {
            button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleDown = new ScaleTransition(Duration.seconds(0.2), button);
                        scaleDown.setToX(1);
                        scaleDown.setToY(1);
                        scaleDown.play();
                    },
                    () -> {} // UI thread callback
            );
        });

        // Click effect (Scale Animation)
        button.setOnAction(e -> {
            ThreadManager.runAsyncThenUI(
                    () -> {
                        ScaleTransition scaleClick = new ScaleTransition(Duration.seconds(0.1), button);
                        scaleClick.setToX(0.9);
                        scaleClick.setToY(0.9);
                        scaleClick.setOnFinished(event -> action.run());
                        scaleClick.play();
                    },
                    () -> {} // UI thread callback
            );
        });


        return button;
    }
}

