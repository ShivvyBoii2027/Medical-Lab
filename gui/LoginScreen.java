/*
 * MediLab Pro — LoginScreen
 * Professional Goated Revision with Auto-Connect Fix & Recovery Links
 */
package gui;

import db.DBManager;
import util.Theme;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScreen {

    private Stage stage;
    private String selectedRole = "ADMIN";
    private TextField usernameField;
    private PasswordField passwordField;
    private Label statusLabel;
    private VBox loginCard;

    public void show(Stage primaryStage) {
        util.ThemeManager.getInstance().reset(); // Purge user aesthetics
        this.stage = primaryStage;
        stage.setTitle("MediLab Pro — Portal Access");

        // ✅ SET APPLICATION WINDOW ICON
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/lab_icon.png")));
        } catch (Exception ignored) {
        }

        StackPane root = new StackPane();
        root.setBackground(Theme.mainBackground());

        // ── Centre Card ──
        loginCard = buildCard();
        root.getChildren().add(loginCard);

        // Entrance animation
        loginCard.setOpacity(0);
        loginCard.setScaleX(0.9);
        loginCard.setScaleY(0.9);

        FadeTransition ft = new FadeTransition(Duration.millis(1000), loginCard);
        ft.setFromValue(0);
        ft.setToValue(1);

        ScaleTransition st = new ScaleTransition(Duration.millis(1000), loginCard);
        st.setFromX(0.9);
        st.setFromY(0.9);
        st.setToX(1);
        st.setToY(1);

        new ParallelTransition(ft, st).play();

        Scene scene = new Scene(root, 1100, 820);
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(700);
        stage.show();

        // ── Auto Connect ──
        autoConnect();
    }

    private void autoConnect() {
        new Thread(() -> {
            boolean ok = DBManager.connect();
            Platform.runLater(() -> {
                if (ok) {
                    statusLabel.setText("✔  System Online — Database Connected");
                    statusLabel.setTextFill(Color.web(Theme.COL_SUCCESS));
                } else {
                    statusLabel.setText("⚠  System Offline — Check MySQL Connection");
                    statusLabel.setTextFill(Color.web(Theme.COL_WARNING));
                }
            });
        }).start();
    }

    private VBox buildCard() {
        VBox card = new VBox(0);
        card.setMaxWidth(460);
        card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: " + Theme.COL_BG_MID + "f2; "
                + "-fx-background-radius: 24; -fx-border-color: " + Theme.COL_BORDER + "; "
                + "-fx-border-radius: 24; -fx-border-width: 1.5;");
        card.setEffect(Theme.cardShadow());
        StackPane.setAlignment(card, Pos.CENTER);

        // ── Header (Official Logo) ──
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 30, 0));

        Node logoNode;
        try {
            ImageView img = new ImageView(new Image(getClass().getResourceAsStream("/lab_icon.png")));
            img.setFitHeight(80);
            img.setPreserveRatio(true);
            img.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN, 0.5)));
            logoNode = img;
        } catch (Exception e) {
            Label logoFallback = new Label("🔬");
            logoFallback.setFont(Font.font(50));
            logoFallback.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN)));
            logoNode = logoFallback;
        }

        Label appTitle = Theme.titleLabel("MediLab Pro");
        Label subtitle = Theme.bodyLabel("Advanced Laboratory Information System");
        subtitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        header.getChildren().addAll(logoNode, appTitle, subtitle);

        // ── Body ──
        VBox body = new VBox(20);

        Label roleHeader = Theme.sectionLabel("Identity Verification");

        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER);
        ToggleGroup tg = new ToggleGroup();

        // Initialize Fields FIRST to avoid NPE in listener
        usernameField = Theme.styledField("Username / ID");
        passwordField = Theme.styledPassword("Password");
        passwordField.setOnAction(e -> doLogin());

        for (String role : new String[] { "ADMIN", "DOCTOR", "PATIENT" }) {
            ToggleButton tb = new ToggleButton(role);
            tb.setToggleGroup(tg);
            tb.setUserData(role);
            tb.setPrefWidth(110);
            styleRoleBtn(tb, false);
            tb.selectedProperty().addListener((o, old, nv) -> {
                if (nv) {
                    selectedRole = (String) tb.getUserData();
                    styleRoleBtn(tb, true);
                    updatePrompt();
                } else {
                    styleRoleBtn(tb, false);
                }
            });
            roleBox.getChildren().add(tb);
        }
        ((ToggleButton) tg.getToggles().get(0)).setSelected(true);

        statusLabel = new Label("Initializing connectivity...");
        statusLabel.setFont(Font.font("Inter", 12));
        statusLabel.setTextFill(Color.web(Theme.COL_TEXT_MUTED));

        Button loginBtn = Theme.primaryButton("ACCESS PORTAL");
        loginBtn.setPrefWidth(Double.MAX_VALUE);
        loginBtn.setPrefHeight(50);
        loginBtn.setOnAction(e -> doLogin());

        // ── Recovery & Registration Links ──
        HBox links = new HBox(15);
        links.setAlignment(Pos.CENTER);
        Hyperlink forgotU = Theme.styledLink("Forgot Username");
        Hyperlink forgotP = Theme.styledLink("Forgot Password");
        Hyperlink regLink = Theme.styledLink("Create Account");

        forgotU.setOnAction(e -> handleForgotUsername());
        forgotP.setOnAction(e -> handleForgotPassword());
        regLink.setOnAction(e -> new RegisterScreen(stage).show());

        links.getChildren().addAll(forgotU, forgotP);
        if ("PATIENT".equals(selectedRole)) {
            Hyperlink forgotI = Theme.styledLink("Retrieve Patient ID");
            forgotI.setOnAction(e -> handleForgotID());
            links.getChildren().add(forgotI);
        }
        links.getChildren().add(regLink);

        body.getChildren().addAll(roleHeader, roleBox, usernameField, passwordField, statusLabel, loginBtn, links);

        card.getChildren().addAll(header, body);
        return card;
    }

    private void styleRoleBtn(ToggleButton tb, boolean selected) {
        if (selected) {
            tb.setStyle("-fx-background-color: " + Theme.COL_CYAN + "; -fx-text-fill: " + Theme.COL_BG_DEEP + "; "
                    + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            tb.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN, 0.4)));
        } else {
            tb.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: " + Theme.COL_TEXT_SOFT + "; "
                    + "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 8;");
            tb.setEffect(null);
        }
    }

    private void updatePrompt() {
        if ("PATIENT".equals(selectedRole))
            usernameField.setPromptText("Username or Patient ID#");
        else
            usernameField.setPromptText("Staff Username");
    }

    private void doLogin() {
        String u = usernameField.getText().trim();
        String p = passwordField.getText();

        if (u.isEmpty() || p.isEmpty()) {
            statusLabel.setText("⚠  Fields cannot be empty");
            statusLabel.setTextFill(Color.web(Theme.COL_DANGER));
            shake(loginCard);
            return;
        }

        if (!DBManager.isConnected()) {
            statusLabel.setText("✖  Connection Offline — Authentication Unavailable");
            statusLabel.setTextFill(Color.web(Theme.COL_DANGER));
            return;
        }

        try {
            boolean ok = DBManager.authenticate(u, p);
            if (ok) {
                String resolvedUser = DBManager.getCurrentUser();
                int linkedId = -1;
                if ("PATIENT".equals(selectedRole)) linkedId = DBManager.getPatientIdByUsername(resolvedUser);
                else if ("DOCTOR".equals(selectedRole)) linkedId = DBManager.getDoctorIdByUsername(resolvedUser);
                
                util.ThemeManager.getInstance().loadSettings(resolvedUser); // Load user preferences
                MainWindow mw = new MainWindow();
                mw.show(stage, resolvedUser, selectedRole, linkedId);
            } else {
                statusLabel.setText("✖  Invalid Credentials — Access Denied");
                statusLabel.setTextFill(Color.web(Theme.COL_DANGER));
                shake(loginCard);
            }
        } catch (Exception ex) {
            statusLabel.setText("✖  Error: " + ex.getMessage());
        }
    }

    private void handleForgotUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MediLab — Identity Recovery");
        dialog.setHeaderText("Account Username Retrieval");
        dialog.setContentText("Enter your registered email address:");

        dialog.showAndWait().ifPresent(email -> {
            try {
                String username = DBManager.getUsernameByEmail(email);
                if (username != null) {
                    Theme.showInfo("Recovery Success", "IDENTITY LOCATED\n\nAssociated Username: " + username
                            + "\n\nPlease use this identifier for future logins.");
                } else {
                    Theme.showToast(stage, "Email not found in registry", Theme.COL_DANGER);
                }
            } catch (Exception ex) {
                Theme.showError("Recovery Fault", ex.getMessage());
            }
        });
    }

    private void handleForgotID() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("MediLab — ID Retrieval");
        dialog.setHeaderText("Clinical Patient ID Recovery");
        dialog.setContentText("Enter your registered email address:");

        dialog.showAndWait().ifPresent(email -> {
            try {
                String id = DBManager.getPatientIdByEmail(email);
                if (id != null) {
                    Theme.showInfo("Recovery Success",
                            "ID LOCATED\n\nYour Patient ID is: " + id + "\n\nYou may use this ID to log in instantly.");
                } else {
                    Theme.showToast(stage, "Email not found in patient directory", Theme.COL_DANGER);
                }
            } catch (Exception ex) {
                Theme.showError("Recovery Fault", ex.getMessage());
            }
        });
    }

    private void handleForgotPassword() {
        VBox recoveryRoot = new VBox(15);
        recoveryRoot.setPadding(new Insets(20));
        TextField uField = Theme.styledField("Username");
        TextField eField = Theme.styledField("Email Address");
        PasswordField npField = Theme.styledPassword("New Password");
        recoveryRoot.getChildren().addAll(
                Theme.formLabel("USERNAME"), uField,
                Theme.formLabel("EMAIL"), eField,
                Theme.formLabel("NEW PASSWORD"), npField);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("MediLab — Security Reset");
        alert.setHeaderText("Functional Password Reset Protocol");
        alert.getDialogPane().setContent(recoveryRoot);
        alert.getDialogPane().setPrefWidth(450);

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String u = uField.getText().trim();
                String e = eField.getText().trim();
                String np = npField.getText();
                try {
                    if (!u.isEmpty() && !e.isEmpty() && !np.isEmpty()) {
                        DBManager.resetPasswordByEmail(e, np);
                        Theme.showInfo("Security Protocol",
                                "PASSWORD RESET SUCCESSFUL\n\nYour clinical credentials have been synchronized. You may now log in with your new password.");
                    } else {
                        Theme.showToast(stage, "Validation Failed: Empty Fields", Theme.COL_WARNING);
                    }
                } catch (Exception ex) {
                    Theme.showError("Reset Fault", ex.getMessage());
                }
            }
        });
    }

    private void shake(VBox node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(50), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.play();
    }
}