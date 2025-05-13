package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.TypeWizApp;
import com.oop2.typewiz.util.SoundManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

public class PauseMenuFactory {
    // UI theme colors
    private static final Color BG_COLOR = Color.rgb(0, 0, 0, 0.85);
    private static final Color BORDER_COLOR = Color.rgb(255, 215, 0, 0.8); // Golden
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final String FONT_FAMILY = "Papyrus";

    /**
     * Creates a pause menu overlay
     * @param onResume Resume game callback
     * @param onRestart Restart game callback
     * @param onBackToTower Back to tower callback
     * @return The pause menu node
     */
    public static Node createPauseMenu(Runnable onResume, Runnable onRestart, Runnable onBackToTower) {
        // Create full screen overlay with blur effect
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setFill(BG_COLOR);

        // Create menu container
        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);
        menuBox.setPadding(new Insets(30));

        // Create title
        Text titleText = new Text("GAME PAUSED");
        titleText.setFont(Font.font(FONT_FAMILY, 48));
        titleText.setFill(TEXT_COLOR);
        addTextGlow(titleText);

        // Create menu panel with gradient background
        Rectangle menuPanel = new Rectangle(400, 300);
        menuPanel.setArcWidth(30);
        menuPanel.setArcHeight(30);

        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(75, 0, 130, 0.9)),
                new Stop(0.5, Color.rgb(128, 0, 128, 0.9)),
                new Stop(1, Color.rgb(75, 0, 130, 0.9))
        );
        menuPanel.setFill(gradient);
        menuPanel.setStroke(BORDER_COLOR);
        menuPanel.setStrokeWidth(2);

        // Add drop shadow to panel
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.rgb(0, 0, 0, 0.5));
        dropShadow.setRadius(20);
        dropShadow.setSpread(0.4);
        menuPanel.setEffect(dropShadow);

        // Create buttons
        StackPane resumeButton = createMenuButton("Resume", onResume);
        StackPane restartButton = createMenuButton("Restart", onRestart);
        StackPane backButton = createMenuButton("Back to Tower", onBackToTower);

        // Add buttons to menu
        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(resumeButton, restartButton, backButton);

        // Combine panel and buttons
        StackPane menuContent = new StackPane(menuPanel, buttonBox);

        // Add title and menu content to main container
        menuBox.getChildren().addAll(titleText, menuContent);

        // Create final layout
        StackPane layout = new StackPane(overlay, menuBox);
        layout.setAlignment(Pos.CENTER);

        // Add blur effect to background
        GaussianBlur blur = new GaussianBlur(5);
        overlay.setEffect(blur);

        return layout;
    }

    /**
     * Creates a styled menu button
     * @param text Button text
     * @param action Button action
     * @return Styled button
     */
    private static StackPane createMenuButton(String text, Runnable action) {
        // Create button background
        Rectangle bg = new Rectangle(320, 60);
        bg.setArcWidth(15);
        bg.setArcHeight(15);
        bg.setFill(Color.rgb(0, 0, 0, 0.6));
        bg.setStroke(BORDER_COLOR);
        bg.setStrokeWidth(2);

        // Create button text
        Text buttonText = new Text(text);
        buttonText.setFont(Font.font(FONT_FAMILY, 24));
        buttonText.setFill(TEXT_COLOR);
        addTextGlow(buttonText);

        // Combine background and text
        StackPane button = new StackPane(bg, buttonText);
        button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);

        // Add hover effects
        button.setOnMouseEntered(e -> {
            bg.setFill(Color.rgb(75, 0, 130, 0.8));
            SoundManager.getInstance().playButtonHover();
            button.setScaleX(1.1);
            button.setScaleY(1.1);
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
            buttonText.setFill(Color.web("#ffeb3b")); // Gold color on hover
        });

        button.setOnMouseExited(e -> {
            bg.setFill(Color.rgb(0, 0, 0, 0.6));
            button.setScaleX(1.0);
            button.setScaleY(1.0);
            button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            buttonText.setFill(TEXT_COLOR);
        });

        // Add click effect
        button.setOnMousePressed(e -> {
            SoundManager.getInstance().playButtonClick();
            button.setScaleX(0.9);
            button.setScaleY(0.9);
        });

        button.setOnMouseReleased(e -> {
            button.setScaleX(1.1); // Return to hover size since mouse is still over button
            button.setScaleY(1.1);
            action.run();
        });

        // Make sure the entire button area is clickable
        bg.setOnMouseEntered(e -> button.fireEvent(e));
        bg.setOnMouseExited(e -> button.fireEvent(e));
        bg.setOnMousePressed(e -> button.fireEvent(e));
        bg.setOnMouseReleased(e -> button.fireEvent(e));
        buttonText.setOnMouseEntered(e -> button.fireEvent(e));
        buttonText.setOnMouseExited(e -> button.fireEvent(e));
        buttonText.setOnMousePressed(e -> button.fireEvent(e));
        buttonText.setOnMouseReleased(e -> button.fireEvent(e));

        return button;
    }

    /**
     * Adds a glow effect to text
     * @param text The text to add glow to
     */
    private static void addTextGlow(Text text) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.rgb(255, 215, 0, 0.5));
        glow.setRadius(10);
        glow.setSpread(0.5);
        text.setEffect(glow);
    }
}