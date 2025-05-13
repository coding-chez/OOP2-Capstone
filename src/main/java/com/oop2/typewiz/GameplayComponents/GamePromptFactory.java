package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.SceneManager;
import com.oop2.typewiz.TypeWizApp;
import com.oop2.typewiz.util.SoundManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Parent;

import java.util.List;

/**
 * Factory class for creating game prompts, announcements, and screens.
 * This class follows the Factory Method pattern to centralize UI creation.
 */
public class GamePromptFactory {
    // Magical theme colors
    private static final Color MAGIC_PURPLE = Color.rgb(128, 0, 128);      // Deep purple for primary elements
    private static final Color MAGIC_GOLD = Color.rgb(255, 215, 0);       // Mystical gold for accents
    private static final Color MAGIC_BLUE = Color.rgb(65, 105, 225);      // Royal blue for secondary elements
    private static final Color MAGIC_VIOLET = Color.rgb(138, 43, 226);    // Blue violet for highlights
    private static final Color DARK_MYSTICAL = Color.rgb(25, 25, 35, 0.9); // Dark background with transparency

    // Text colors
    private static final Color TEXT_LIGHT = Color.rgb(255, 255, 255);     // White text
    private static final Color TEXT_GOLD = Color.rgb(255, 223, 0);        // Gold text
    private static final Color TEXT_MYSTIC = Color.rgb(230, 230, 250);    // Lavender text

    // UI style constants
    private static final String MAGIC_FONT = "Papyrus";  // More mystical font
    private static final double CORNER_RADIUS = 20;      // Rounded corners

    // Performance colors
    private static final Color PERFECT_MAGIC = Color.rgb(147, 112, 219);   // Purple for excellent
    private static final Color GOOD_MAGIC = Color.rgb(65, 105, 225);       // Blue for good
    private static final Color FAIR_MAGIC = Color.rgb(255, 165, 0);        // Orange for fair
    private static final Color POOR_MAGIC = Color.rgb(178, 34, 34);        // Dark red for poor

    /**
     * Creates a wave announcement overlay
     * @param currentWave Current wave number
     * @param maxWaves Maximum number of waves
     * @return Node containing the announcement overlay
     */
    public static Node createWaveAnnouncement(int currentWave, int maxWaves) {
        SoundManager.getInstance().playWaveAnnounce();

        // Create mystical background overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        LinearGradient mysticalGradient = new LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(75, 0, 130, 0.7)),   // Indigo top
                new Stop(1, Color.rgb(128, 0, 128, 0.7))   // Purple bottom
        );
        overlay.setFill(mysticalGradient);

        // Create magical portal-like panel
        Rectangle portalPanel = new Rectangle(600, 300);
        portalPanel.setArcWidth(50);
        portalPanel.setArcHeight(50);
        portalPanel.setFill(DARK_MYSTICAL);
        portalPanel.setStroke(MAGIC_GOLD);
        portalPanel.setStrokeWidth(3);

        // Add mystical glow
        DropShadow portalGlow = new DropShadow();
        portalGlow.setColor(MAGIC_VIOLET);
        portalGlow.setRadius(30);
        portalGlow.setSpread(0.3);
        portalPanel.setEffect(portalGlow);

        // Wave announcement with magical styling
        Text waveText = new Text("WAVE " + currentWave);
        waveText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 60));  // Slightly reduced font size
        waveText.setFill(MAGIC_GOLD);

        // Mystical text effects
        DropShadow textGlow = new DropShadow();
        textGlow.setColor(MAGIC_VIOLET);
        textGlow.setRadius(20);
        textGlow.setSpread(0.4);
        waveText.setEffect(textGlow);

        Text ofText = new Text("OF " + maxWaves);
        ofText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 36));
        ofText.setFill(TEXT_MYSTIC);

        // Get the actual selected difficulty from FXGL world properties
        String difficultyLevel;
        Color difficultyColor;

        String diffStr = "APPRENTICE";  // Default fallback
        try {
            if (FXGL.getWorldProperties().exists("difficultyString")) {
                diffStr = FXGL.getWorldProperties().getString("difficultyString");
            }
        } catch (Exception e) {
            System.out.println("[DEBUG] Error getting difficulty from world properties: " + e.getMessage());
        }

        switch (diffStr) {
            case "WIZARD":
                difficultyLevel = "WIZARD";
                difficultyColor = MAGIC_VIOLET;
                break;
            case "ARCHMAGE":
                difficultyLevel = "ARCHMAGE";
                difficultyColor = MAGIC_GOLD;
                break;
            default:
                difficultyLevel = "APPRENTICE";
                difficultyColor = MAGIC_BLUE;
                break;
        }

        Text difficultyText = new Text(difficultyLevel);
        difficultyText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 48));
        difficultyText.setFill(difficultyColor);

        // Magical glow effect
        Glow magicGlow = new Glow(0.8);
        DropShadow magicShadow = new DropShadow();
        magicShadow.setColor(difficultyColor);
        magicShadow.setRadius(15);
        magicShadow.setInput(magicGlow);
        difficultyText.setEffect(magicShadow);

        // Mystical subtitle
        Text subtitleText = new Text("Prepare your spells!");
        subtitleText.setFont(Font.font(MAGIC_FONT, 28));
        subtitleText.setFill(TEXT_MYSTIC);

        // Layout with magical spacing
        HBox waveBox = new HBox(15, waveText, ofText);
        waveBox.setAlignment(Pos.CENTER);

        VBox contentLayout = new VBox(15);  // Reduced spacing
        contentLayout.setAlignment(Pos.CENTER);
        contentLayout.getChildren().addAll(waveBox, difficultyText, subtitleText);

        // Combine elements with magical animations
        StackPane portalPane = new StackPane(portalPanel, contentLayout);

        // Center the announcement in the screen
        StackPane centeredPane = new StackPane(overlay, portalPane);
        centeredPane.setAlignment(Pos.CENTER);  // This ensures perfect centering

        // Magical appearance animation
        ScaleTransition portalOpen = new ScaleTransition(Duration.seconds(0.5), portalPane);
        portalOpen.setFromX(0.1);
        portalOpen.setFromY(0.1);
        portalOpen.setToX(1.0);
        portalOpen.setToY(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), portalPane);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        ParallelTransition magicReveal = new ParallelTransition(portalOpen, fadeIn);
        magicReveal.play();

        return centeredPane;  // Return the centered pane instead of the basic stack
    }

    /**
     * Creates a game over screen
     * @param message Message to display
     * @param score Final score
     * @param wpm Words per minute
     * @param rawWpm Raw words per minute
     * @param accuracy Typing accuracy
     * @param consistency Typing consistency
     * @param wpmData WPM data over time
     * @param accuracyData Accuracy data over time
     * @return Node containing the game over screen
     */
    public static Node createGameOverScreen(String message, int score, double wpm,
                                            double rawWpm, double accuracy, double consistency,
                                            List<Double> wpmData, List<Double> accuracyData, int highScore) {
        // Play appropriate sound effect
        if (message.contains("Victory")) {
            SoundManager.getInstance().playVictory();
        } else {
            SoundManager.getInstance().playGameOver();
        }

        // Mystical overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        LinearGradient mysticalGradient = new LinearGradient(
                0, 0, 0, 1, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(25, 25, 35, 0.95)),
                new Stop(1, Color.rgb(45, 0, 75, 0.95))
        );
        overlay.setFill(mysticalGradient);

        // Magical title with effects
        Text titleText = new Text(message);
        titleText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 72));

        if (message.contains("Victory")) {
            titleText.setFill(MAGIC_GOLD);
            Glow victoryGlow = new Glow(1.0);
            DropShadow victoryShadow = new DropShadow();
            victoryShadow.setColor(MAGIC_VIOLET);
            victoryShadow.setRadius(30);
            victoryShadow.setSpread(0.6);
            victoryShadow.setInput(victoryGlow);
            titleText.setEffect(victoryShadow);
        } else {
            titleText.setFill(POOR_MAGIC);
            DropShadow defeatShadow = new DropShadow();
            defeatShadow.setColor(Color.BLACK);
            defeatShadow.setRadius(20);
            defeatShadow.setSpread(0.4);
            titleText.setEffect(defeatShadow);
        }

        HBox titleBox = new HBox(titleText);
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setPadding(new Insets(10));  // Reduced from 30 to 10

        Text highScoreText = new Text("Highest Score: " + highScore);
        highScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        highScoreText.setFill(Color.GOLD);

        Glow highScoreGlow = new Glow(0.4);
        DropShadow highScoreShadow = new DropShadow();
        highScoreShadow.setColor(Color.GOLD);
        highScoreShadow.setRadius(8);
        highScoreShadow.setInput(highScoreGlow);
        highScoreText.setEffect(highScoreShadow);

        // Magical score display
        Text scoreText = new Text("Magical Score: " + score);
        scoreText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 42));
        scoreText.setFill(MAGIC_GOLD);

        Glow scoreGlow = new Glow(0.6);
        DropShadow scoreShadow = new DropShadow();
        scoreShadow.setColor(MAGIC_VIOLET);
        scoreShadow.setRadius(15);
        scoreShadow.setInput(scoreGlow);
        scoreText.setEffect(scoreShadow);

        // Create mystical stats panel
        VBox statsPanel = StatsUIFactory.createStatsPanel(wpm, rawWpm, accuracy, consistency);

        // Update performance data
        StatsUIFactory.setWpmData(wpmData);
        StatsUIFactory.setAccuracyData(accuracyData);

        // Create magical performance graph
        javafx.scene.canvas.Canvas graphCanvas = StatsUIFactory.createTypingGraph();

        // Mystical restart button
        StackPane restartButton = UIFactory.createStylishButton("Cast Again", 220, 65, MAGIC_PURPLE);
        restartButton.setId("play-again-button");

        // Back to tower button
        StackPane backToTowerButton = UIFactory.createStylishButton("Back to Tower", 220, 65, MAGIC_PURPLE);
        backToTowerButton.setId("back-to-tower-button");

        // Button container with spacing
        HBox buttonContainer = new HBox(20); // 20 pixels spacing between buttons
        buttonContainer.setAlignment(Pos.CENTER);
        buttonContainer.getChildren().addAll(restartButton, backToTowerButton);
        buttonContainer.setId("button-container");

        // Two-column mystical layout
        HBox statsLayout = new HBox(30);
        statsLayout.setAlignment(Pos.CENTER);
        statsLayout.setPadding(new Insets(25));

        // Left column (Magical Stats)
        VBox leftColumn = new VBox(20, scoreText, highScoreText, statsPanel);
        leftColumn.setAlignment(Pos.CENTER);
        leftColumn.setPadding(new Insets(15));

        Rectangle leftBg = new Rectangle(450, 360);
        leftBg.setArcWidth(CORNER_RADIUS);
        leftBg.setArcHeight(CORNER_RADIUS);
        leftBg.setFill(Color.rgb(45, 0, 75, 0.8));
        leftBg.setStroke(MAGIC_GOLD);
        leftBg.setStrokeWidth(2);

        // Right column (Mystical Graph)
        VBox rightColumn = new VBox(20, graphCanvas);
        rightColumn.setAlignment(Pos.CENTER);
        rightColumn.setPadding(new Insets(15));

        Rectangle rightBg = new Rectangle(450, 360);
        rightBg.setArcWidth(CORNER_RADIUS);
        rightBg.setArcHeight(CORNER_RADIUS);
        rightBg.setFill(Color.rgb(75, 0, 130, 0.8));
        rightBg.setStroke(MAGIC_VIOLET);
        rightBg.setStrokeWidth(2);

        StackPane leftStack = new StackPane(leftBg, leftColumn);
        StackPane rightStack = new StackPane(rightBg, rightColumn);

        statsLayout.getChildren().addAll(leftStack, rightStack);

        // Final magical layout with adjusted spacing and padding
        VBox fullLayout = new VBox(10);  // Reduced spacing from 20 to 10
        fullLayout.setAlignment(Pos.CENTER);
        fullLayout.setPadding(new Insets(0, 0, 30, 0));  // Added bottom padding
        fullLayout.getChildren().addAll(titleBox, statsLayout, buttonContainer);

        // Add magical entrance animation
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), fullLayout);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        return new StackPane(overlay, fullLayout);
    }

    /**
     * Creates a fade-in text instruction
     * @param text The text to display
     * @param duration Duration to display in seconds
     * @return Node containing the instruction overlay
     */
    public static Node createInstructionOverlay(String text, double duration) {
        // Create semi-transparent overlay
        Rectangle overlay = new Rectangle(FXGL.getAppWidth(), FXGL.getAppHeight());
        overlay.setFill(Color.rgb(0, 0, 0, 0.4));

        // Create instruction text
        Text instructionText = new Text(text);
        instructionText.setFont(Font.font(MAGIC_FONT, FontWeight.BOLD, 36));
        instructionText.setFill(Color.YELLOW);

        // Add glow to text
        Glow glow = new Glow(0.6);
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.ORANGE);
        shadow.setRadius(10);
        shadow.setInput(glow);
        instructionText.setEffect(shadow);

        // Create container for text
        StackPane textContainer = new StackPane(instructionText);
        textContainer.setPadding(new Insets(20));

        // Return the full view
        return new StackPane(overlay, textContainer);
    }

    /**
     * Finds and sets up the Play Again button in an end game screen
     * @param endGameScreen The game over or victory screen node
     * @param restartAction Action to execute when the button is clicked
     */
    public static void setupPlayAgainButton(Node endGameScreen, Runnable restartAction) {
        // Find the button container
        if (endGameScreen instanceof Parent) {
            Parent root = (Parent) endGameScreen;
            root.lookupAll("#button-container").forEach(node -> {
                if (node instanceof HBox) {
                    HBox buttonContainer = (HBox) node;
                    for (Node child : buttonContainer.getChildren()) {
                        if (child instanceof StackPane) {
                            StackPane button = (StackPane) child;
                            if ("play-again-button".equals(button.getId())) {
                                button.setOnMouseClicked(event -> {
                                    event.consume();
                                    FXGL.getGameTimer().runOnceAfter(restartAction::run, Duration.millis(100));
                                });
                            } else if ("back-to-tower-button".equals(button.getId())) {
                                button.setOnMouseClicked(event -> {
                                    event.consume();
                                    FXGL.getGameTimer().runOnceAfter(() -> {
                                        // Stop game music
                                        SoundManager.getInstance().stopBGM();
                                        // Play button click sound
                                        SoundManager.getInstance().playButtonClick();
                                        // Show main menu
                                        SceneManager.showScreen(TypeWizApp.ScreenType.MAIN_MENU);
                                    }, Duration.millis(100));
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Recursively searches for the Play Again button in the node hierarchy
     * @param node The current node to search in
     * @param restartAction Action to execute when the button is clicked
     * @return true if button was found and configured
     */
    private static boolean findButtonInChildren(Node node, Runnable restartAction) {
        // This method is no longer needed as we're using a different approach
        return false;
    }
} 