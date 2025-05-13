package com.oop2.typewiz;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import com.oop2.typewiz.database.UserDatabaseService;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.text.Text;
import javafx.scene.layout.Region;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import org.jetbrains.annotations.NotNull;

public class RegisterScreen extends FXGLMenu {

    private TextField emailField;
    private TextField usernameField;
    private PasswordField passwordField;
    private PasswordField confirmPasswordField;


    private HBox root;

    public RegisterScreen() {
        super(MenuType.MAIN_MENU);
        addRegisterUI();
    }

    private void addRegisterUI() {
        root = new HBox(10);
        root.setPrefSize(1280, 720);
        root.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
        root.setAlignment(Pos.CENTER);
        root.setStyle(
                "-fx-background-image: url('assets/textures/background-and-platforms/darkerpurplebg.png');" +
                        "-fx-background-repeat: no-repeat;" +
                        "-fx-background-size: cover;" +
                        "-fx-background-position: center center;"
        );

        // Left VBox with Logo
        VBox leftPane = new VBox();
        leftPane.setPrefSize(481, 800);
        leftPane.setAlignment(Pos.CENTER);

        ImageView logo = new ImageView(new Image(
                getClass().getResource("assets/typewiz_logo.png").toExternalForm()));
        logo.setFitHeight(530);
        logo.setFitWidth(546);
        logo.setPreserveRatio(true);
        logo.setEffect(new DropShadow(20, Color.color(0.16, 0.14, 0.14)));

        leftPane.getChildren().add(logo);

        // Right VBox with Form
        VBox formPane = new VBox(10);
        formPane.setPrefSize(619, 725);
        formPane.setAlignment(Pos.CENTER);

        Label title = new Label("Sign up");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Viner Hand ITC", 52));
        title.setEffect(new DropShadow(10, Color.web("#c85bff")));
        VBox.setMargin(title, new Insets(0, 60, 20, 0));

        HBox emailBox = createInputField("assets/profile_icon_login.png", "Email", false);
        HBox usernameBox = createInputField("assets/profile_icon_login.png", "Username", false);
        HBox passwordBox = createInputField("assets/password_icon.png", "Password", true);
        HBox confirmPasswordBox = createInputField("assets/password_icon.png", "Confirm Password", true);
        VBox.setMargin(confirmPasswordBox, new Insets(0, 0, 20, 0)); //

        Button createAccountBtn = new Button("Create Account");
        styleButton(createAccountBtn, "#c85bff", Color.WHITE);
        createAccountBtn.setOnAction(e -> {
            FXGL.play("sound-library/click.wav");

            String email = emailField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                FXGL.getDialogService().showMessageBox("All fields are required!");
                return;
            }

            if (!password.equals(confirmPassword)) {
                FXGL.getDialogService().showMessageBox("Passwords do not match!");
                return;
            }

            boolean success = UserDatabaseService.registerUser(username, email, password);

            if (success) {
                FXGL.getDialogService().showMessageBox("Registration successful!");
                SceneManager.showScreen(TypeWizApp.ScreenType.LOGIN);
            } else {
                FXGL.getDialogService().showMessageBox("Registration failed. Try again.");
            }
        });


        HBox separatorBox = new HBox();
        separatorBox.setAlignment(Pos.CENTER);
        separatorBox.setSpacing(10);
        Separator sepLeft = new Separator();
        sepLeft.setPrefWidth(172);
        Separator sepRight = new Separator();
        sepRight.setPrefWidth(178);
        Text orText = FXGL.getUIFactoryService().newText("Or", Color.WHITE, 16);
        separatorBox.getChildren().addAll(sepLeft, orText, sepRight);

        Button loginBtn = new Button("Login");
        styleButton(loginBtn, "#ffffff", Color.BLACK);
        loginBtn.setOnAction(e -> {
            FXGL.play("sound-library/click.wav"); // plays the sound
            SceneManager.showScreen(TypeWizApp.ScreenType.LOGIN);
        });


        formPane.getChildren().addAll(title, emailBox, usernameBox, passwordBox, confirmPasswordBox, createAccountBtn, separatorBox, loginBtn);

        // Left logo animation
        animateNode(logo, 0);

        animateNode(title, 0.2);
        animateNode(emailBox, 0.4);
        animateNode(usernameBox, 0.6);
        animateNode(passwordBox, 0.8);
        animateNode(confirmPasswordBox, 1.0);
        animateNode(createAccountBtn, 1.2);
        animateNode(separatorBox, 1.4);
        animateNode(loginBtn, 1.6);

        root.getChildren().addAll(leftPane, formPane);
        getContentRoot().getChildren().add(root);

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
            pf.setFont(Font.font("Book Antiqua Italic", 20));
            pf.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            pf.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(pf, Priority.ALWAYS);

            if (promptText.equalsIgnoreCase("Password")) {
                passwordField = pf;
            } else if (promptText.equalsIgnoreCase("Confirm Password")) {
                confirmPasswordField = pf;
            }

            TextField visibleTF = new TextField();
            visibleTF.setFont(Font.font("Book Antiqua Italic", 20));
            visibleTF.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            visibleTF.setManaged(false);
            visibleTF.setVisible(false);
            visibleTF.textProperty().bindBidirectional(pf.textProperty());
            visibleTF.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(visibleTF, Priority.ALWAYS);

            ImageView eyeIcon = new ImageView(new Image(getClass().getResource("assets/password_hash_icon.png").toExternalForm()));
            eyeIcon.setFitHeight(24);
            eyeIcon.setFitWidth(24);
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
            tf.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
            tf.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(tf, Priority.ALWAYS);

            if (promptText.equalsIgnoreCase("Email")) {
                emailField = tf;
            } else if (promptText.equalsIgnoreCase("Username")) {
                usernameField = tf;
            }

            box.getChildren().addAll(icon, tf);
        }

        return box;
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
            button.setCursor(TypeWizApp.OPEN_BOOK_CURSOR);
            FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("sound-library/hover.wav"));
            button.setStyle(hoverStyle);
        });
        button.setOnMouseExited(e -> {
            button.setCursor(TypeWizApp.CLOSED_BOOK_CURSOR);
            button.setStyle(baseStyle);
        });
    }

    private void animateNode(javafx.scene.Node node, double delaySeconds) {
        node.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(0.6), node);
        slide.setFromY(50); // Start slightly below
        slide.setToY(0);     // Move to original Y position
        slide.setDelay(Duration.seconds(delaySeconds));

        FadeTransition fade = new FadeTransition(Duration.seconds(0.6), node);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(Duration.seconds(delaySeconds));

        slide.play();
        fade.play();
    }

//    public static void main(String[] args) {
//        launch(args);
//    }
}
