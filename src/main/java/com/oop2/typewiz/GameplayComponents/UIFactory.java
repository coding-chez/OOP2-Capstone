package com.oop2.typewiz.GameplayComponents;

import com.almasb.fxgl.app.GameApplication;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.scene.Node;

/**
 * Factory class for creating UI components.
 * This class follows the Factory Method pattern to centralize UI creation.
 */
public class UIFactory {
    // UI theme colors
    private static final Color UI_PRIMARY_COLOR = Color.rgb(128, 0, 128);     // Deep purple
    private static final Color UI_SECONDARY_COLOR = Color.rgb(138, 43, 226);  // Blue violet
    private static final Color UI_ACCENT_COLOR = Color.rgb(255, 215, 0);      // Mystical gold
    private static final Color UI_BG_COLOR = Color.rgb(25, 25, 35, 0.9);      // Dark mystical background
    private static final Color UI_TEXT_PRIMARY = Color.rgb(230, 230, 250);    // Lavender text
    private static final Color UI_TEXT_SECONDARY = Color.rgb(255, 223, 0);    // Gold text

    // UI style constants
    private static final String FONT_FAMILY = "Papyrus";
    private static final double UI_CORNER_RADIUS = 20;
    private static final double UI_PANEL_OPACITY = 0.85;
    private static final double UI_BORDER_WIDTH = 2.5;

    // Performance colors
    private static final Color GOOD_PERFORMANCE = Color.rgb(147, 112, 219);   // Purple for excellent
    private static final Color MEDIUM_PERFORMANCE = Color.rgb(65, 105, 225);  // Blue for good
    private static final Color POOR_PERFORMANCE = Color.rgb(178, 34, 34);     // Dark red for poor

    /**
     * Creates a health bar panel
     * @param initialHealth Initial health value
     * @param maxHealth Maximum health value
     * @return A VBox containing the health display
     */
    public static VBox createHealthDisplay(int initialHealth, int maxHealth) {
        VBox healthDisplay = new VBox(6);  // Reduced spacing
        healthDisplay.setTranslateX(25);
        healthDisplay.setTranslateY(20);   // Moved up
        healthDisplay.setPadding(new Insets(10));  // Reduced padding
        healthDisplay.setMaxWidth(180);    // Smaller width

        healthDisplay.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(healthDisplay, UI_ACCENT_COLOR, UI_CORNER_RADIUS);

        Text healthLabel = new Text("VITALITY");
        healthLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));  // Smaller font
        healthLabel.setFill(UI_ACCENT_COLOR);
        addTextGlow(healthLabel, UI_SECONDARY_COLOR, 0.6);

        Rectangle healthBar = new Rectangle(150, 20, getHealthColor(initialHealth, maxHealth));  // Smaller width
        healthBar.setArcWidth(10);
        healthBar.setArcHeight(10);

        DropShadow healthGlow = new DropShadow();
        healthGlow.setColor(UI_SECONDARY_COLOR);
        healthGlow.setRadius(8);  // Reduced glow
        healthGlow.setSpread(0.3);
        healthBar.setEffect(healthGlow);

        Rectangle healthBarBg = new Rectangle(150, 20, Color.rgb(40, 0, 60, 0.6));  // Match width
        healthBarBg.setArcWidth(10);
        healthBarBg.setArcHeight(10);

        Pane healthBarPane = new Pane();
        healthBarPane.setPrefSize(150, 20);  // Match width
        healthBarPane.setMaxSize(150, 20);   // Match width
        healthBarPane.getChildren().addAll(healthBarBg, healthBar);

        Text healthText = new Text(initialHealth + "/" + maxHealth);
        healthText.setFont(Font.font(FONT_FAMILY, 16));  // Smaller font
        healthText.setFill(UI_TEXT_PRIMARY);
        addTextGlow(healthText, UI_SECONDARY_COLOR, 0.4);

        healthDisplay.getChildren().addAll(healthLabel, healthBarPane, healthText);
        healthDisplay.setUserData(new HealthDisplayData(healthBar, healthText, maxHealth));

        return healthDisplay;
    }

    /**
     * Updates the health bar with a new health value
     * @param healthDisplay The health display container
     * @param currentHealth Current health value
     */
    public static void updateHealthBar(VBox healthDisplay, int currentHealth) {
        if (healthDisplay == null || healthDisplay.getUserData() == null) return;

        HealthDisplayData data = (HealthDisplayData) healthDisplay.getUserData();
        double healthPercentage = (double) currentHealth / data.maxHealth;
        ((Rectangle)data.getComponent()).setWidth(150 * healthPercentage);
        data.healthText.setText(currentHealth + "/" + data.maxHealth);

        // Update color based on health with smoother gradient
        ((Rectangle)data.getComponent()).setFill(getHealthColor(currentHealth, data.maxHealth));
    }

    private static Color getHealthColor(int health, int maxHealth) {
        double healthPercentage = (double) health / maxHealth;
        if (healthPercentage > 0.6) {
            return Color.rgb(147, 112, 219); // Magical purple for high health
        } else if (healthPercentage > 0.3) {
            return Color.rgb(255, 165, 0);   // Mystical orange for medium health
        } else {
            return Color.rgb(178, 34, 34);   // Dark red for low health
        }
    }

    /**
     * Creates a top bar with score and wave display
     * @param initialScore Initial score value
     * @param initialWave Initial wave value
     * @param maxWaves Maximum number of waves
     * @param difficultyLabel Difficulty label
     * @return An HBox containing the top bar
     */
    public static HBox createTopBar(int initialScore, int initialWave, int maxWaves, String difficultyLabel) {
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(5));
        topBar.setMaxHeight(60);
        double centerX = FXGL.getAppWidth() / 2 - 200;
        topBar.setTranslateX(centerX);
        topBar.setTranslateY(20);
        Rectangle bg = new Rectangle(400, 80);
        bg.setArcWidth(30);
        bg.setArcHeight(30);
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 0, true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(45, 0, 75, 0.9)),
                new Stop(0.5, Color.rgb(75, 0, 130, 0.9)),
                new Stop(1, Color.rgb(45, 0, 75, 0.9))
        );
        bg.setFill(gradient);
        bg.setStroke(UI_ACCENT_COLOR);
        bg.setStrokeWidth(2);
        VBox scoreDisplay = new VBox(2);
        scoreDisplay.setAlignment(Pos.CENTER);
        scoreDisplay.setPadding(new Insets(2, 8, 2, 8));
        Text scoreLabel = new Text("ARCANE POINTS");
        scoreLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        scoreLabel.setFill(UI_ACCENT_COLOR);
        addTextGlow(scoreLabel, UI_SECONDARY_COLOR, 0.4);
        Text scoreText = new Text(Integer.toString(initialScore));
        scoreText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 22));
        scoreText.setFill(UI_TEXT_SECONDARY);
        addTextGlow(scoreText, UI_SECONDARY_COLOR, 0.4);
        scoreDisplay.getChildren().addAll(scoreLabel, scoreText);
        scoreDisplay.setUserData(scoreText);
        VBox waveDisplay = new VBox(2);
        waveDisplay.setAlignment(Pos.CENTER);
        waveDisplay.setPadding(new Insets(2, 8, 2, 8));
        Text waveLabel = new Text("Difficulty: " + difficultyLabel);
        waveLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        waveLabel.setFill(UI_ACCENT_COLOR);
        addTextGlow(waveLabel, UI_SECONDARY_COLOR, 0.4);
        Text waveText = new Text(initialWave + "/" + maxWaves);
        waveText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 22));
        waveText.setFill(UI_TEXT_SECONDARY);
        addTextGlow(waveText, UI_SECONDARY_COLOR, 0.4);
        waveDisplay.getChildren().addAll(waveLabel, waveText);
        waveDisplay.setUserData(waveText);
        Rectangle separator = new Rectangle(2, 40);
        separator.setFill(UI_ACCENT_COLOR);
        addShapeGlow(separator, UI_SECONDARY_COLOR, 0.3);
        HBox content = new HBox(15, scoreDisplay, separator, waveDisplay);
        content.setAlignment(Pos.CENTER);
        StackPane finalLayout = new StackPane(bg, content);
        finalLayout.setAlignment(Pos.CENTER);
        topBar.getChildren().add(finalLayout);
        topBar.setUserData(new TopBarData(scoreText, waveText, maxWaves, waveLabel, difficultyLabel));
        return topBar;
    }

    /**
     * Updates the score display
     * @param topBar The top bar container
     * @param score New score value
     */
    public static void updateScore(HBox topBar, int score) {
        if (topBar == null || topBar.getUserData() == null) return;

        TopBarData data = (TopBarData) topBar.getUserData();
        data.scoreText.setText(Integer.toString(score));
    }

    /**
     * Updates the wave display
     * @param topBar The top bar container
     * @param wave New wave value
     */
    public static void updateWave(HBox topBar, int wave) {
        if (topBar == null || topBar.getUserData() == null) return;
        TopBarData data = (TopBarData) topBar.getUserData();
        if (wave == 1) {
            data.waveLabel.setText("Difficulty: " + data.difficultyLabel);
            data.waveText.setText("1/" + data.maxWaves);
        } else {
            data.waveLabel.setText("Wave " + wave + " - " + data.difficultyLabel);
            data.waveText.setText(wave + "/" + data.maxWaves);
        }
    }

    /**
     * Creates an instruction text display
     * @return A Text node for instructions
     */
    public static Text createInstructionText() {
        Text instructionText = new Text("");
        instructionText.setTranslateX(FXGL.getAppWidth() / 2 - 200);
        instructionText.setTranslateY(FXGL.getAppHeight() / 2 - 100);
        instructionText.setFill(UI_TEXT_SECONDARY);
        instructionText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 32));
        instructionText.setVisible(false);
        addTextGlow(instructionText, UI_SECONDARY_COLOR, 0.6);

        return instructionText;
    }

    /**
     * Creates a controls help text display
     * @return A Text node for controls help
     */
    public static Node createControlsText() {
        // Create the text
        Text controlsText = new Text("Magical Controls: Type to cast | SHIFT to switch targets | SPACE to banish word");
        controlsText.setFill(UI_ACCENT_COLOR);                      // Use gold color for better visibility
        controlsText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 20));  // Larger, bold font

        // Add stronger glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(UI_SECONDARY_COLOR);
        glow.setRadius(15);
        glow.setSpread(0.5);
        controlsText.setEffect(glow);

        // Create semi-transparent background with less transparency
        Rectangle bg = new Rectangle();
        bg.setFill(Color.rgb(0, 0, 0, 0.80));  // Reduced transparency
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setStroke(UI_ACCENT_COLOR);
        bg.setStrokeWidth(2);

        // Create container for text and background
        StackPane container = new StackPane(bg, controlsText);
        container.setPadding(new Insets(10, 20, 10, 20));

        // Center the container horizontally
        container.setTranslateX((FXGL.getAppWidth() - controlsText.getLayoutBounds().getWidth()) / 2 - 150);
        container.setTranslateY(FXGL.getAppHeight() - 100);  // Position from bottom

        // Ensure it's always on top
        container.setViewOrder(-1000);

        // Bind background size to text
        bg.widthProperty().bind(controlsText.layoutBoundsProperty().map(bounds -> bounds.getWidth() + 40));
        bg.heightProperty().bind(controlsText.layoutBoundsProperty().map(bounds -> bounds.getHeight() + 20));

        // Set ID for the container
        container.setId("controls-guide");

        return container;
    }

    /**
     * Creates a stylish button
     * @param text Button text
     * @param width Button width
     * @param height Button height
     * @param color Button color
     * @return A StackPane containing the button
     */
    public static StackPane createStylishButton(String text, double width, double height, Color color) {
        Rectangle buttonBg = new Rectangle(width, height);
        buttonBg.setArcWidth(UI_CORNER_RADIUS);
        buttonBg.setArcHeight(UI_CORNER_RADIUS);
        buttonBg.setFill(UI_BG_COLOR);
        buttonBg.setStroke(UI_ACCENT_COLOR);
        buttonBg.setStrokeWidth(3);

        DropShadow buttonGlow = new DropShadow();
        buttonGlow.setColor(UI_SECONDARY_COLOR);
        buttonGlow.setRadius(20);
        buttonGlow.setSpread(0.3);
        buttonBg.setEffect(buttonGlow);

        Text buttonText = new Text(text);
        buttonText.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 28));
        buttonText.setFill(UI_TEXT_SECONDARY);

        Glow textGlow = new Glow(0.8);
        DropShadow textShadow = new DropShadow();
        textShadow.setColor(UI_SECONDARY_COLOR);
        textShadow.setRadius(10);
        textShadow.setInput(textGlow);
        buttonText.setEffect(textShadow);

        StackPane button = new StackPane(buttonBg, buttonText);
        button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);

        // Store original values
        Color originalFill = (Color) buttonBg.getFill();
        Color originalStroke = (Color) buttonBg.getStroke();
        double originalRadius = buttonGlow.getRadius();
        double originalSpread = buttonGlow.getSpread();

        // Hover effect
        button.setOnMouseEntered(e -> {
            buttonBg.setFill(Color.rgb(45, 0, 75, 0.9));
            buttonGlow.setRadius(30);
            buttonGlow.setSpread(0.5);
            button.setScaleX(1.05);
            button.setScaleY(1.05);

            // Play hover sound
            SoundManager.getInstance().playButtonHover();
        });

        // Reset on exit
        button.setOnMouseExited(e -> {
            buttonBg.setFill(originalFill);
            buttonGlow.setRadius(originalRadius);
            buttonGlow.setSpread(originalSpread);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        // Click effect
        button.setOnMousePressed(e -> {
            buttonBg.setFill(Color.rgb(35, 0, 55, 0.9));
            buttonBg.setStroke(UI_SECONDARY_COLOR);
            button.setScaleX(0.95);
            button.setScaleY(0.95);

            // Play click sound
            SoundManager.getInstance().playButtonClick();

            javafx.animation.TranslateTransition tt = new javafx.animation.TranslateTransition(
                    Duration.millis(50), button);
            tt.setByX(3);
            tt.setByY(0);
            tt.setCycleCount(2);
            tt.setAutoReverse(true);
            tt.play();
        });

        // Reset on release
        button.setOnMouseReleased(e -> {
            buttonBg.setFill(originalFill);
            buttonBg.setStroke(originalStroke);
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });

        return button;
    }

    /**
     * Creates a performance monitoring display
     * @return A VBox containing the performance display
     */
    public static VBox createPerformanceDisplay() {
        VBox performanceDisplay = new VBox(4);  // Reduced spacing
        performanceDisplay.setTranslateX(FXGL.getAppWidth() - 180);  // Moved left
        performanceDisplay.setTranslateY(20);   // Moved up
        performanceDisplay.setPadding(new Insets(8));  // Reduced padding
        performanceDisplay.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
        addPanelBorder(performanceDisplay, UI_SECONDARY_COLOR, UI_CORNER_RADIUS);

        Text performanceLabel = new Text("Arcane Flow:");
        performanceLabel.setFont(Font.font(FONT_FAMILY, 16));  // Smaller font
        performanceLabel.setFill(UI_TEXT_SECONDARY);
        addTextGlow(performanceLabel, UI_SECONDARY_COLOR, 0.4);

        Rectangle performanceBar = new Rectangle(120, 12, GOOD_PERFORMANCE);  // Smaller size
        performanceBar.setArcWidth(8);
        performanceBar.setArcHeight(8);

        DropShadow barGlow = new DropShadow();
        barGlow.setColor(UI_SECONDARY_COLOR);
        barGlow.setRadius(6);  // Reduced glow
        barGlow.setSpread(0.3);
        performanceBar.setEffect(barGlow);

        Text performanceText = new Text("FPS: 144");
        performanceText.setFont(Font.font(FONT_FAMILY, 14));  // Smaller font
        performanceText.setFill(UI_TEXT_PRIMARY);
        addTextGlow(performanceText, UI_SECONDARY_COLOR, 0.3);

        performanceDisplay.getChildren().addAll(performanceLabel, performanceBar, performanceText);
        performanceDisplay.setUserData(new PerformanceData(performanceBar, performanceText));

        return performanceDisplay;
    }

    /**
     * Updates the performance display
     * @param performanceDisplay The performance display container
     * @param fps Current FPS value
     */
    public static void updatePerformanceDisplay(VBox performanceDisplay, double fps) {
        if (performanceDisplay == null || performanceDisplay.getUserData() == null) return;

        PerformanceData data = (PerformanceData) performanceDisplay.getUserData();
        final double TARGET_FPS = 60.0;

        // Update FPS text
        data.performanceText.setText(String.format("FPS: %.1f", fps));

        // Calculate performance percentage
        double performancePercentage = Math.min(fps / TARGET_FPS, 1.0);

        // Update performance bar
        ((Rectangle)data.getComponent()).setWidth(120 * performancePercentage);

        // Update performance bar color
        if (performancePercentage >= 0.9) {
            ((Rectangle)data.getComponent()).setFill(GOOD_PERFORMANCE);
        } else if (performancePercentage >= 0.7) {
            ((Rectangle)data.getComponent()).setFill(MEDIUM_PERFORMANCE);
        } else {
            ((Rectangle)data.getComponent()).setFill(POOR_PERFORMANCE);
        }
    }

    // Helper methods for UI styling
    public static Background createPanelBackground(Color color, double cornerRadius) {
        return new Background(
                new BackgroundFill(
                        color,
                        new CornerRadii(cornerRadius),
                        Insets.EMPTY
                )
        );
    }

    public static void addPanelBorder(Region panel, Color color, double cornerRadius) {
        panel.setBorder(new Border(
                new BorderStroke(
                        color,
                        BorderStrokeStyle.SOLID,
                        new CornerRadii(cornerRadius),
                        new BorderWidths(UI_BORDER_WIDTH)
                )
        ));

        // Add a subtle glow around the panel
        DropShadow glow = new DropShadow();
        glow.setColor(color);
        glow.setRadius(15);
        glow.setSpread(0.4);
        panel.setEffect(glow);
    }

    public static void addTextGlow(Text text, Color color, double intensity) {
        Glow glow = new Glow(intensity);
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        text.setEffect(shadow);
    }

    public static void addShapeGlow(Shape shape, Color color, double intensity) {
        Glow glow = new Glow(intensity);
        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(5);
        shadow.setInput(glow);
        shape.setEffect(shadow);
    }

    // Inner classes to store UI component references
    /**
     * Generic base class for UI component data
     * @param <T> The type of UI component
     */
    private static class DisplayData<T> {
        protected final T component;

        DisplayData(T component) {
            this.component = component;
        }

        public T getComponent() {
            return component;
        }
    }

    private static class HealthDisplayData extends DisplayData<Rectangle> {
        final Text healthText;
        final int maxHealth;

        HealthDisplayData(Rectangle healthBar, Text healthText, int maxHealth) {
            super(healthBar);
            this.healthText = healthText;
            this.maxHealth = maxHealth;
        }
    }

    private static class TopBarData extends DisplayData<HBox> {
        final Text scoreText;
        final Text waveText;
        final int maxWaves;
        final Text waveLabel;
        final String difficultyLabel;
        TopBarData(Text scoreText, Text waveText, int maxWaves, Text waveLabel, String difficultyLabel) {
            super(null);
            this.scoreText = scoreText;
            this.waveText = waveText;
            this.maxWaves = maxWaves;
            this.waveLabel = waveLabel;
            this.difficultyLabel = difficultyLabel;
        }
    }

    private static class PerformanceData extends DisplayData<Rectangle> {
        final Text performanceText;

        PerformanceData(Rectangle performanceBar, Text performanceText) {
            super(performanceBar);
            this.performanceText = performanceText;
        }
    }

    /**
     * Generic Theme class for managing game assets by theme
     * @param <T> The type of asset paths the theme manages
     */
    public static class Theme<T> {
        private final String name;
        private final T backgroundPath;
        private final T platformPath;

        public Theme(String name, T backgroundPath, T platformPath) {
            this.name = name;
            this.backgroundPath = backgroundPath;
            this.platformPath = platformPath;
        }

        public String getName() {
            return name;
        }

        public T getBackgroundPath() {
            return backgroundPath;
        }

        public T getPlatformPath() {
            return platformPath;
        }
    }

    /**
     * Randomly selects a theme for the game (background and platform)
     * @return The selected theme name ("winter", "purple", or "fall")
     */
    private static boolean isFirstRun = true;

    private static Theme<String> selectRandomTheme() {
        Theme<String>[] themes = new Theme[] {
                new Theme<>("winter",
                        "background-and-platforms/bg-winter_1280_720.png",
                        "background-and-platforms/hdlargerplatform-winter.png"),
                new Theme<>("purple",
                        "background-and-platforms/bg-midnightpurple_1280_720.png",
                        "background-and-platforms/hdlargerplatform-purple.png"),
                new Theme<>("fall",
                        "background-and-platforms/bg-fall_1280_720.png",
                        "background-and-platforms/hdlargerplatform-fall.png")
        };

        // Always select the purple theme on first run
        if (isFirstRun) {
            isFirstRun = false;
            // Return the purple theme (index 1)
            return themes[1];
        }

        int randomIndex = (int)(Math.random() * themes.length);
        return themes[randomIndex];
    }

    /**
     * Creates the background for the game
     * @param width Screen width
     * @param height Screen height
     */
    public static void createBackground(int width, int height) {
        Theme<String> theme = selectRandomTheme();

        // Store the selected theme for platform creation
        FXGL.getWorldProperties().setValue("currentTheme", theme.getName());
        FXGL.getWorldProperties().setValue("currentThemeObj", theme);

        Image backgroundImage = FXGL.image(theme.getBackgroundPath());
        Rectangle background = new Rectangle(width, height);
        background.setFill(new ImagePattern(backgroundImage));

        FXGL.entityBuilder()
                .at(0, 0)
                .view(background)
                .zIndex(-100)
                .buildAndAttach();
    }

    /**
     * Creates the platform for the game
     */
    public static void createPlatform() {
        // Try to get the theme object first for direct access
        if (FXGL.getWorldProperties().exists("currentThemeObj")) {
            @SuppressWarnings("unchecked")
            Theme<String> theme = (Theme<String>) FXGL.getWorldProperties().getObject("currentThemeObj");
            Image platformImage = FXGL.image(theme.getPlatformPath());
            createPlatformEntity(platformImage);
        } else {
            // Fallback to the old method if the theme object isn't available
            String themeName = FXGL.getWorldProperties().getString("currentTheme");

            String platformFile;
            switch(themeName) {
                case "purple":
                    platformFile = "background-and-platforms/hdlargerplatform-purple.png";
                    break;
                case "fall":
                    platformFile = "background-and-platforms/hdlargerplatform-fall.png";
                    break;
                case "winter":
                default:
                    platformFile = "background-and-platforms/hdlargerplatform-winter.png";
                    break;
            }

            Image platformImage = FXGL.image(platformFile);
            createPlatformEntity(platformImage);
        }
    }

    /**
     * Creates the platform entity with the given image
     * @param platformImage The platform image to use
     */
    private static void createPlatformEntity(Image platformImage) {
        ImageView platformView = new ImageView(platformImage);

        double platformWidth = 1450;
        double platformHeight = 820;
        platformView.setFitWidth(platformWidth);
        platformView.setFitHeight(platformHeight);
        platformView.setPreserveRatio(false);

        double platformOffsetY = 100;
        double platformOffsetX = (platformWidth - FXGL.getAppWidth()) / -2;

        FXGL.entityBuilder()
                .type(Game.EntityType.PLATFORM)
                .at(platformOffsetX, platformOffsetY)
                .view(platformView)
                .bbox(new HitBox(BoundingShape.box(platformWidth, platformHeight)))
                .with(new PhysicsComponent())
                .zIndex(10)
                .buildAndAttach();
    }

    /**
     * Creates the wizard character
     */
    public static void createWizard() {
        Image wizardImage = FXGL.image("wizard/wizard.png");

        int frameWidth = 500;
        int frameHeight = 500;

        AnimationChannel idleAnimation = new AnimationChannel(wizardImage,
                4,
                frameWidth, frameHeight,
                Duration.seconds(0.25),
                0, 3);

        AnimatedTexture wizardTexture = new AnimatedTexture(idleAnimation);
        wizardTexture.loop();

        double wizardScale = 0.30;

        FXGL.entityBuilder()
                .type(Game.EntityType.PLAYER)
                .at(-5, 50)
                .view(wizardTexture)
                .scale(wizardScale, wizardScale)
                .bbox(new HitBox(BoundingShape.box(frameWidth * wizardScale, frameHeight * wizardScale)))
                .zIndex(25)
                .buildAndAttach();
    }

    /**
     * Creates and sets up all UI elements
     * @param game Reference to the game class
     */
    public static void createUI(GameApplication game) {
        PlayerManager playerManager = (PlayerManager) FXGL.getWorldProperties().getObject("playerManager");
        VBox healthDisplay = createHealthDisplay(
                playerManager.getHealth(),
                playerManager.getMaxHealth()
        );
        playerManager.setHealthDisplay(healthDisplay);
        for (Node child : healthDisplay.getChildren()) {
            if (child instanceof Text) {
                Text text = (Text) child;
                if (text.getText().contains("/")) {
                    playerManager.setHealthText(text);
                    break;
                }
            }
        }
        // Get difficulty and maxWaves
        String difficultyLabel = "Apprentice";
        int maxWaves = 5;
        if (FXGL.getWorldProperties().exists("difficulty")) {
            Object diffObj = FXGL.getWorldProperties().getObject("difficulty");
            if (diffObj != null) {
                String diffStr = diffObj.toString();
                difficultyLabel = diffStr.charAt(0) + diffStr.substring(1).toLowerCase();
                if (diffStr.equals("WIZARD")) maxWaves = 8;
                else if (diffStr.equals("ARCHMAGE")) maxWaves = 12;
            }
        }
        HBox topBar = createTopBar(playerManager.getScore(), 1, maxWaves, difficultyLabel);
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
                                    if (label.getText().equals("ARCANE POINTS") && box.getChildren().size() > 1) {
                                        playerManager.setScoreText((Text) box.getChildren().get(1));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        VBox performanceDisplay = createPerformanceDisplay();
        FXGL.addUINode(healthDisplay);
        FXGL.addUINode(topBar);
        FXGL.addUINode(performanceDisplay);
    }

    /**
     * Updates the performance display with the current FPS
     * @param tpf Time per frame
     */
    public static void updatePerformanceDisplay(double tpf) {
        // Calculate FPS
        double fps = 1.0 / Math.max(tpf, 0.0001);

        // Find and update the performance display
        for (Node node : FXGL.getGameScene().getUINodes()) {
            if (node instanceof VBox) {
                VBox vbox = (VBox) node;
                if (!vbox.getChildren().isEmpty() && vbox.getChildren().get(0) instanceof Text) {
                    Text titleText = (Text) vbox.getChildren().get(0);
                    if ("Arcane Flow:".equals(titleText.getText())) {
                        updatePerformanceDisplay(vbox, fps);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates a styled game label using the generic component factory
     * @param text Label text
     * @param style Label style (e.g., "title", "score", "health", "info")
     * @return The styled Text node
     */
    public static Text createGameLabel(String text, String style) {
        return createComponent(new ComponentCreator<Text, LabelConfig>() {
            @Override
            public Text create(LabelConfig config) {
                Text label = new Text(config.text);

                // Apply styling based on style type
                switch(config.style) {
                    case "title":
                        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 32));
                        label.setFill(UI_ACCENT_COLOR);
                        addTextGlow(label, UI_ACCENT_COLOR, 0.6);
                        break;
                    case "score":
                        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
                        label.setFill(UI_TEXT_SECONDARY);
                        addTextGlow(label, UI_TEXT_SECONDARY, 0.4);
                        break;
                    case "health":
                        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
                        label.setFill(UI_ACCENT_COLOR);
                        addTextGlow(label, UI_ACCENT_COLOR, 0.4);
                        break;
                    case "info":
                    default:
                        label.setFont(Font.font(FONT_FAMILY, 16));
                        label.setFill(UI_TEXT_PRIMARY);
                        break;
                }

                return label;
            }
        }, new LabelConfig(text, style));
    }

    /**
     * Configuration class for label creation
     */
    private static class LabelConfig {
        final String text;
        final String style;

        LabelConfig(String text, String style) {
            this.text = text;
            this.style = style;
        }
    }

    /**
     * Generic component factory method for creating UI components
     * @param <T> Type of component to create
     * @param <P> Type of parameter used for configuration
     * @param creator Function to create the component
     * @param config Configuration parameter
     * @return The created component
     */
    public static <T extends Node, P> T createComponent(ComponentCreator<T, P> creator, P config) {
        T component = creator.create(config);

        // Add default styling if component is a Region
        if (component instanceof Region) {
            Region region = (Region) component;
            region.setPadding(new Insets(5));
        }

        return component;
    }

    /**
     * Functional interface for component creation
     * @param <T> Type of component to create
     * @param <P> Type of parameter used for configuration
     */
    @FunctionalInterface
    public interface ComponentCreator<T extends Node, P> {
        T create(P config);
    }

    /**
     * Creates a styled game container using the generic component factory
     * @param <T> Type of container to create (must extend Region)
     * @param containerType Class object for the container type
     * @param style Container style (e.g., "panel", "header", "footer")
     * @return The styled container
     */
    public static <T extends Region> T createGameContainer(Class<T> containerType, String style) {
        return createComponent(new ComponentCreator<T, String>() {
            @Override
            public T create(String styleType) {
                T container;
                try {
                    // Create new instance of the specified container type
                    container = containerType.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    // Fall back to VBox if instantiation fails
                    System.err.println("Failed to create container: " + e.getMessage());
                    return (T) new VBox();
                }

                // Apply basic styling
                container.setPadding(new Insets(10));

                // Apply style-specific styling
                switch(styleType) {
                    case "panel":
                        container.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
                        addPanelBorder(container, UI_ACCENT_COLOR, UI_CORNER_RADIUS);
                        break;
                    case "header":
                        container.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
                        addPanelBorder(container, UI_ACCENT_COLOR, UI_CORNER_RADIUS);
                        break;
                    case "footer":
                        container.setBackground(createPanelBackground(UI_BG_COLOR, UI_CORNER_RADIUS));
                        addPanelBorder(container, UI_ACCENT_COLOR, UI_CORNER_RADIUS);
                        break;
                    default:
                        // Default transparent style
                        container.setBackground(null);
                        break;
                }

                return container;
            }
        }, style);
    }

    /**
     * Creates a game interface panel using the generic component factories
     * @param title Panel title
     * @param content Panel content description
     * @return A VBox containing the styled panel
     */
    public static VBox createGamePanel(String title, String content) {
        // Create main container using the generic container factory
        VBox panel = createGameContainer(VBox.class, "panel");
        panel.setSpacing(10);

        // Create title using the generic label factory
        Text titleLabel = createGameLabel(title, "title");

        // Create content using the generic label factory
        Text contentLabel = createGameLabel(content, "info");

        // Add title and content to panel
        panel.getChildren().addAll(titleLabel, contentLabel);

        return panel;
    }

    public static void setupPlayAgainButton(Node endGameScreen, Runnable playAgainAction) {
        // Find the play again button in the screen
        if (endGameScreen instanceof VBox) {
            VBox container = (VBox) endGameScreen;
            for (Node node : container.getChildren()) {
                if (node instanceof HBox && "button-container".equals(node.getId())) {
                    HBox buttonContainer = (HBox) node;

                    // Create the back to tower button
                    StackPane backToTowerButton = createStylishButton("Back to Tower", 200, 50, UI_ACCENT_COLOR);
                    backToTowerButton.setId("back-to-tower-button");

                    // Add spacing between buttons
                    buttonContainer.setSpacing(20);

                    // Add the back to tower button
                    buttonContainer.getChildren().add(backToTowerButton);

                    // Set up the play again button action
                    for (Node buttonNode : buttonContainer.getChildren()) {
                        if (buttonNode instanceof StackPane) {
                            StackPane button = (StackPane) buttonNode;
                            if ("play-again-button".equals(button.getId())) {
                                button.setOnMouseClicked(e -> playAgainAction.run());
                            } else if ("back-to-tower-button".equals(button.getId())) {
                                button.setOnMouseClicked(e -> {
                                    // Stop game music
                                    SoundManager.getInstance().stopBGM();
                                    // Play button click sound
                                    SoundManager.getInstance().playButtonClick();
                                    // Show main menu
                                    SceneManager.showScreen(TypeWizApp.ScreenType.MAIN_MENU);
                                });
                            }
                        }
                    }
                    break;
                }
            }
        }
    }
}