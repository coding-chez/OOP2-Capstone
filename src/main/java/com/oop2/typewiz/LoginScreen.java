package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class LoginScreen extends FXGLMenu {
    private HBox root; // class-level root

    public LoginScreen() {
        super(MenuType.MAIN_MENU);
        addLoginUI();
        getContentRoot().getChildren().add(root);
        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
    }

    public void addLoginUI() {
        root = new HBox(10);
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(1280, 720);
        // root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
//        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1c0033, #4b0082);");
        root.setStyle(
                "-fx-background-image: url('assets/textures/background-and-platforms/darkerpurplebg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // Apply cursor style to all elements
        root.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        root.setStyle(root.getStyle() + " * { -fx-cursor: none; }");


        // Left logo
        VBox leftBox = new VBox();
        leftBox.setAlignment(Pos.CENTER);
        leftBox.setPrefWidth(481);
        Image logoImage = new Image(getClass().getResource("assets/typewiz_logo.png").toExternalForm());
        ImageView logo = new ImageView(logoImage);
        logo.setFitWidth(546);
        logo.setFitHeight(530);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(6, Color.BLACK));
        leftBox.getChildren().add(logo);
        playBounceAnimation(logo);

        // Right: Login form
        VBox rightBox = new VBox(10);
        rightBox.setAlignment(Pos.CENTER);
        rightBox.setPrefWidth(619);
        rightBox.setPrefHeight(790);

        Text loginText = FXGL.getUIFactoryService().newText("Login", Color.WHITE, 52);
        loginText.fontProperty().unbind();
        loginText.setFont(Font.font("Viner Hand ITC", 52));
        loginText.setEffect(new DropShadow(10, Color.web("#c85bff")));
        VBox.setMargin(loginText, new Insets(0, 60, 20, 0));

        HBox usernameBox = createInputField("assets/profile_icon_login.png", "Username", false);
        HBox passwordBox = createInputField("assets/password_icon.png", "Password", true);

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        optionsBox.setPadding(new Insets(0, 0, 0, 10));
        CheckBox rememberMe = new CheckBox("Remember Me");
        rememberMe.setTextFill(Color.web("#a1a1a1"));
        rememberMe.fontProperty().unbind();
        rememberMe.setFont(Font.font("System Italic", 12));
        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.setStyle("-fx-text-fill: #bb86fc; -fx-underline: true;");
        forgotPassword.setOnMouseEntered(e -> forgotPassword.setStyle("-fx-text-fill: #e0aaff; -fx-underline: true;"));
        forgotPassword.setOnMouseExited(e -> forgotPassword.setStyle("-fx-text-fill: #bb86fc; -fx-underline: true;"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        optionsBox.getChildren().addAll(rememberMe, spacer, forgotPassword);

        // Buttons
        Button loginButton = FXGL.getUIFactoryService().newButton("Login");
        styleButton(loginButton, "#c85bff", Color.WHITE);
        loginButton.setOnAction(e -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            SceneManager.showScreen(TypeWizApp.ScreenType.LOADING);
        });


        Button signUpButton = FXGL.getUIFactoryService().newButton("Sign up");
        styleButton(signUpButton, "#ffffff", Color.BLACK);

        signUpButton.setOnAction(e -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            SceneManager.showScreen(TypeWizApp.ScreenType.REGISTER);
        });


        // Separator
        HBox separatorBox = new HBox();
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setSpacing(10);
        Separator sepLeft = new Separator(); sepLeft.setPrefWidth(172);
        Separator sepRight = new Separator(); sepRight.setPrefWidth(178);
        Text orText = FXGL.getUIFactoryService().newText("Or", Color.WHITE, 16);
        separatorBox.getChildren().addAll(sepLeft, orText, sepRight);

        VBox buttonBox = new VBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(loginButton, new Insets(20, 0, 0, 0));
        buttonBox.getChildren().addAll(loginButton, separatorBox, signUpButton);
        playSlideInAnimation(buttonBox, 1.2);

        rightBox.getChildren().addAll(
                animateNode(loginText, 0.2),
                animateNode(usernameBox, 0.4),
                animateNode(passwordBox, 0.6),
                animateNode(optionsBox, 0.8),
                buttonBox
        );
        root.getChildren().addAll(leftBox, rightBox);
////        FXGL.getGameScene().getRoot().setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
//        javafx.application.Platform.runLater(() ->
        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
//        );
//        applyFadeInAnimation(root);
    }

    private void styleButton(Button button, String bgColor, Color textColor) {
        button.setPrefSize(378, 58);
        button.fontProperty().unbind();
        button.setFont(Font.font("Book Antiqua", 20));
        button.setTextFill(textColor);
        button.setEffect(new InnerShadow());

        String textFillHex = textColor.toString().replace("0x", "#");
        String baseStyle = "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textFillHex + "; -fx-background-radius: 30; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: derive(" + bgColor + ", 10%); -fx-text-fill: " + textFillHex + "; -fx-background-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(200,91,255,0.6), 10, 0.3, 0, 3);";

        button.setStyle(baseStyle);

        button.setOnMouseEntered(e -> {
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("sound-library/hover.wav"));
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
            button.setStyle(hoverStyle);
        });

        button.setOnMouseExited(e ->{
            button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            button.setStyle(baseStyle);
        });
    }

    private void applyFadeInAnimation(Pane root) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1), root);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void playBounceAnimation(ImageView logo) {
        logo.setTranslateY(-100);
        logo.setScaleX(0.7);
        logo.setScaleY(0.7);
        TranslateTransition translate = new TranslateTransition(Duration.seconds(1), logo);
        translate.setToY(0);
        ScaleTransition scale = new ScaleTransition(Duration.seconds(1), logo);
        scale.setToX(1);
        scale.setToY(1);
        new ParallelTransition(translate, scale).play();
    }

    private void playSlideInAnimation(VBox box, double delaySeconds) {
        box.setTranslateY(100);
        box.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.8), box);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.seconds(delaySeconds));

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.8), box);
        slide.setFromY(100);
        slide.setToY(0);
        slide.setDelay(Duration.seconds(delaySeconds));

        fade.play();
        slide.play();
    }

    private Node animateNode(Node node, double delay) {
        node.setTranslateY(30);
        node.setOpacity(0);

        FadeTransition fade = new FadeTransition(Duration.seconds(0.6), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.seconds(delay));

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.6), node);
        slide.setFromY(30);
        slide.setToY(0);
        slide.setDelay(Duration.seconds(delay));

        fade.play();
        slide.play();

        return node;
    }

    private HBox createInputField(String iconPath, String promptText, boolean isPassword) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPrefSize(600, 54);
        box.setStyle("-fx-background-color: rgba(255,255,255,0.07); -fx-background-radius: 30;");
        box.setPadding(new Insets(0, 20, 0, 20));
        box.setEffect(new Glow(0.3));

        ImageView icon = new ImageView(new Image(getClass().getResource(iconPath).toExternalForm()));
        icon.setFitHeight(26);
        icon.setFitWidth(26);

        if (isPassword) {
            PasswordField pf = new PasswordField();
            pf.setPromptText(promptText);
            pf.fontProperty().unbind();
            pf.setFont(Font.font("Book Antiqua Italic", 20));
            pf.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #bbbbbb;");
            pf.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(pf, Priority.ALWAYS);

            // Add cursor behavior to password field
            pf.setOnMouseEntered(e -> pf.setCursor(TypeWizApp.OPEN_BOOK_CURSOR));
            pf.setOnMouseExited(e -> pf.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR));

            TextField visibleTF = new TextField();
            visibleTF.setFont(Font.font("Book Antiqua Italic", 20));
            visibleTF.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #bbbbbb;");
            visibleTF.setManaged(false);
            visibleTF.setVisible(false);
            visibleTF.textProperty().bindBidirectional(pf.textProperty());
            visibleTF.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(visibleTF, Priority.ALWAYS);

            // Add cursor behavior to visible text field
            visibleTF.setOnMouseEntered(e -> visibleTF.setCursor(TypeWizApp.OPEN_BOOK_CURSOR));
            visibleTF.setOnMouseExited(e -> visibleTF.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR));

            ImageView eyeIcon = new ImageView(new Image(getClass().getResource("assets/password_hash_icon.png").toExternalForm()));
            eyeIcon.setFitHeight(24);
            eyeIcon.setFitWidth(24);

            // Add cursor behavior to eye icon
            eyeIcon.setOnMouseEntered(e -> eyeIcon.setCursor(TypeWizApp.OPEN_BOOK_CURSOR));
            eyeIcon.setOnMouseExited(e -> eyeIcon.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR));

            eyeIcon.setOnMouseClicked(e -> {
                boolean showing = visibleTF.isVisible();
                visibleTF.setVisible(!showing);
                visibleTF.setManaged(!showing);
                pf.setVisible(showing);
                pf.setManaged(showing);
                String eyePath = showing ? "assets/password_hash_icon.png" : "assets/password_unhash_icon.png";
                eyeIcon.setImage(new Image(getClass().getResource(eyePath).toExternalForm()));
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.NEVER);
            box.getChildren().addAll(icon, pf, visibleTF, spacer, eyeIcon);
        } else {
            TextField tf = new TextField();
            tf.setPromptText(promptText);
            tf.setFont(Font.font("Book Antiqua Italic", 20));
            tf.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-prompt-text-fill: #bbbbbb;");
            tf.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tf, Priority.ALWAYS);

            // Add cursor behavior to text field
            tf.setOnMouseEntered(e -> tf.setCursor(TypeWizApp.OPEN_BOOK_CURSOR));
            tf.setOnMouseExited(e -> tf.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR));

            box.getChildren().addAll(icon, tf);
        }

        return box;
    }

}
