/*
 * MediLab Pro — Theme Utility
 * Aesthetic: High-Tech Medical Cyber — Deep Navy / Cyan / Teal / Neon
 */
package util;
 
import gui.MainWindow;
import javafx.scene.Node;
import javafx.geometry.Pos;

import javafx.animation.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.paint.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Theme {

    /* ── PALETTE ── */
    public static final String COL_BG_DEEP = "#020817"; // ultra dark navy
    public static final String COL_BG_MID = "#0f172a"; // deep slate
    public static final String COL_BG_CARD = "#1e293b"; // slate blue card
    public static final String COL_CYAN = "#06b6d4"; // vibrant cyan
    public static final String COL_TEAL = "#14b8a6"; // teal accent
    public static final String COL_BLUE = "#3b82f6"; // bright blue
    public static final String COL_NEON_CYAN = "#22d3ee"; // neon glow
    public static final String COL_CREAM = "#f8fafc"; // slate cream text
    public static final String COL_TEXT_SOFT = "#94a3b8"; // soft slate
    public static final String COL_TEXT_MUTED = "#64748b"; // muted slate
    public static final String COL_DANGER = "#f43f5e"; // rose red
    public static final String COL_SUCCESS = "#10b981"; // emerald
    public static final String COL_WARNING = "#f59e0b"; // amber
    public static final String COL_BORDER = "rgba(34,211,238,0.15)";

    // Compatibility aliases for legacy panels
    public static final String COL_LAVENDER = "#22d3ee"; // Map to Cyan
    public static final String COL_ROSE_GOLD = "#14b8a6"; // Map to Teal

    /* ── DYNAMIC ACCESSORS ── */
    public static String cPrimary() {
        return ThemeManager.getInstance().getAccentColor();
    }

    public static String cBgDeep() {
        return ThemeManager.getInstance().getBgDeep();
    }

    public static String cBgMid() {
        return ThemeManager.getInstance().getBgMid();
    }

    public static String cBgCard() {
        return ThemeManager.getInstance().getBgCard();
    }

    public static String cText() {
        return ThemeManager.getInstance().getTextColor();
    }

    public static String cTextSoft() {
        return ThemeManager.getInstance().getTextSoft();
    }

    /* ── GRADIENTS ── */
    public static String gradientMain() {
        return "linear-gradient(135deg, " + cBgDeep() + " 0%, " + cBgMid() + " 50%, " + cBgCard() + " 100%)";
    }

    public static String gradientCard() {
        return "linear-gradient(145deg, " + cBgCard() + "ee 0%, " + cBgMid() + "f2 100%)";
    }

    public static String gradientSidebar() {
        return "linear-gradient(180deg, " + cBgDeep() + " 0%, " + cBgMid() + " 100%)";
    }

    public static String gradientHeader() {
        return "linear-gradient(90deg, " + cBgDeep() + " 0%, " + cBgMid() + " 100%)";
    }

    public static String gradientButton(String hex1, String hex2) {
        return "linear-gradient(135deg, " + hex1 + " 0%, " + hex2 + " 100%)";
    }

    public static String gradientAccent() {
        return "linear-gradient(135deg," + cPrimary() + " 0%," + COL_BLUE + " 100%)";
    }

    /* ── GLASS PANE ── */
    public static String glassStyle() {
        String border = "LIGHT".equals(ThemeManager.getInstance().getMode()) ? "rgba(0,0,0,0.1)"
                : "rgba(255,255,255,0.1)";
        return "-fx-background-color: " + cBgCard() + "cc;"
                + "-fx-background-radius: 16;"
                + "-fx-border-color: " + border + ";"
                + "-fx-border-radius: 16;"
                + "-fx-border-width: 1;";
    }

    public static String glassStyleLight() {
        return "-fx-background-color: rgba(255,255,255,0.03);"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: rgba(34,211,238,0.1);"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1;";
    }

    /* ── DROP SHADOW ── */
    public static DropShadow cardShadow() {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.rgb(0, 0, 0, 0.4));
        ds.setRadius(30);
        ds.setSpread(0.1);
        return ds;
    }

    public static DropShadow glowShadow(Color c) {
        DropShadow ds = new DropShadow();
        ds.setColor(c);
        ds.setRadius(20);
        ds.setSpread(0.2);
        return ds;
    }

    /* ── BUTTON FACTORIES ── */
    public static Button primaryButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + gradientAccent() + ";"
                        + "-fx-text-fill: white; -fx-font-family: 'Inter', 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-font-size: " + ThemeManager.getInstance().getFontSize()
                        + "px; -fx-padding: 10 26; -fx-background-radius: 8;"
                        + "-fx-cursor: hand; -fx-border-width: 0;"
                        + "-fx-effect: dropshadow(gaussian, #06b6d44d, 10, 0, 0, 2);");
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + gradientAccent() + ";"
                        + "-fx-text-fill: white; -fx-font-family: 'Inter', 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-font-size: " + ThemeManager.getInstance().getFontSize()
                        + "px; -fx-padding: 10 26; -fx-background-radius: 8;"
                        + "-fx-cursor: hand; -fx-border-width: 0;"
                        + "-fx-effect: dropshadow(gaussian, #06b6d480, 15, 0, 0, 3);"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + gradientAccent() + ";"
                        + "-fx-text-fill: white; -fx-font-family: 'Inter', 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-font-size: " + ThemeManager.getInstance().getFontSize()
                        + "px; -fx-padding: 10 26; -fx-background-radius: 8;"
                        + "-fx-cursor: hand; -fx-border-width: 0;"
                        + "-fx-effect: dropshadow(gaussian, #06b6d44d, 10, 0, 0, 2);"));
        return btn;
    }

    public static Button dangerButton(String text) {
        Button btn = new Button(text);
        applyStatusStyle(btn, COL_DANGER, false);
        btn.setOnMouseEntered(e -> applyStatusStyle(btn, COL_DANGER, true));
        btn.setOnMouseExited(e -> applyStatusStyle(btn, COL_DANGER, false));
        return btn;
    }

    public static Button successButton(String text) {
        Button btn = new Button(text);
        applyStatusStyle(btn, COL_SUCCESS, false);
        btn.setOnMouseEntered(e -> applyStatusStyle(btn, COL_SUCCESS, true));
        btn.setOnMouseExited(e -> applyStatusStyle(btn, COL_SUCCESS, false));
        return btn;
    }

    private static void applyStatusStyle(Button b, String color, boolean hover) {
        b.setStyle(
                "-fx-background-color: " + (hover ? color : "transparent") + ";"
                        + "-fx-text-fill: " + (hover ? "white" : color)
                        + "; -fx-font-family: 'Inter', 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-font-size: " + ThemeManager.getInstance().getFontSize()
                        + "px; -fx-padding: 10 26; -fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
                        + "-fx-border-color: " + color + "; -fx-border-radius: 8; -fx-border-width: 2;");
    }

    public static Button ghostButton(String text) {
        Button btn = new Button(text);
        applyGhostStyle(btn, false);
        btn.setOnMouseEntered(e -> applyGhostStyle(btn, true));
        btn.setOnMouseExited(e -> applyGhostStyle(btn, false));
        return btn;
    }

    private static void applyGhostStyle(Button b, boolean hover) {
        b.setStyle(
                "-fx-background-color: " + (hover ? "rgba(255,255,255,0.05)" : "transparent") + ";"
                        + "-fx-text-fill: " + cTextSoft()
                        + "; -fx-font-family: 'Inter', 'Segoe UI'; -fx-font-weight: bold;"
                        + "-fx-font-size: " + ThemeManager.getInstance().getFontSize()
                        + "px; -fx-padding: 10 26; -fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
                        + "-fx-border-color: " + cTextSoft() + "; -fx-border-radius: 8; -fx-border-width: 1.5;");
    }

    /* ── TEXT FIELD ── */
    public static TextField styledField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        applyFieldStyle(tf);
        return tf;
    }

    public static void applyFieldStyle(TextField tf) {
        String base = "-fx-background-color: " + cBgDeep() + "99;"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: rgba(34,211,238,0.2);"
                + "-fx-border-radius: 8; -fx-border-width: 1.5;"
                + "-fx-text-fill: " + cText() + ";"
                + "-fx-prompt-text-fill: " + COL_TEXT_MUTED + ";"
                + "-fx-font-size: " + ThemeManager.getInstance().getFontSize() + "px; -fx-padding: 12 16;";
        tf.setStyle(base);
        tf.focusedProperty().addListener((o, old, nv) -> {
            if (nv) {
                tf.setStyle(base + "-fx-border-color: " + cPrimary()
                        + "; -fx-effect: dropshadow(gaussian,rgba(6,182,212,0.2),8,0,0,0);");
            } else {
                tf.setStyle(base);
            }
        });
    }

    public static PasswordField styledPassword(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        applyFieldStyle(pf);
        return pf;
    }

    public static <T> ComboBox<T> styledCombo() {
        ComboBox<T> cb = new ComboBox<>();
        String base = "-fx-background-color: rgba(15,23,42,0.6);"
                + "-fx-background-radius: 8;"
                + "-fx-border-color: rgba(34,211,238,0.2);"
                + "-fx-border-radius: 8; -fx-border-width: 1.5;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px; -fx-padding: 8 12;";
        cb.setStyle(base);
        cb.focusedProperty().addListener((o, old, nv) -> {
            if (nv) {
                cb.setStyle(base + "-fx-border-color: " + COL_CYAN
                        + "; -fx-effect: dropshadow(gaussian,rgba(6,182,212,0.2),8,0,0,0);");
            } else {
                cb.setStyle(base);
            }
        });
        return cb;
    }

    public static Hyperlink styledLink(String text) {
        Hyperlink link = new Hyperlink(text);
        link.setFont(Font.font("Inter", FontWeight.BOLD, ThemeManager.getInstance().getFontSize() - 1));
        link.setTextFill(Color.web(cPrimary()));
        link.setStyle("-fx-border-color: transparent; -fx-padding: 0;");
        link.setOnMouseEntered(e -> link.setTextFill(Color.web(cPrimary()).brighter()));
        link.setOnMouseExited(e -> link.setTextFill(Color.web(cPrimary())));
        return link;
    }

    /* ── LABELS ── */
    public static Label titleLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", FontWeight.BOLD, ThemeManager.getInstance().getFontSize() + 11));
        l.setTextFill(Color.web(cText()));
        l.setStyle("-fx-effect: dropshadow(gaussian," + cPrimary() + "66,15,0,0,0);");
        return l;
    }

    public static Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setFont(Font.font("Inter", FontWeight.BLACK, ThemeManager.getInstance().getFontSize() - 2));
        l.setTextFill(Color.web(cPrimary()));
        return l;
    }

    public static Label bodyLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", ThemeManager.getInstance().getFontSize()));
        l.setTextFill(Color.web(cTextSoft()));
        return l;
    }

    public static Label formLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, ThemeManager.getInstance().getFontSize() - 1));
        l.setTextFill(Color.web(cTextSoft()));
        return l;
    }

    public static Separator styledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(34,211,238,0.1); -fx-padding: 0;");
        return sep;
    }

    /* ── BACKGROUND ── */
    public static Background mainBackground() {
        String customPath = ThemeManager.getInstance().getBgImage();
        if (customPath != null && !customPath.isEmpty()) {
            try {
                // Load from local file system
                java.io.File file = new java.io.File(customPath);
                if (file.exists()) {
                    Image img = new Image(file.toURI().toString());
                    BackgroundImage bgi = new BackgroundImage(img,
                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                            BackgroundPosition.CENTER,
                            new BackgroundSize(100, 100, true, true, true, true));
                    return new Background(bgi);
                }
            } catch (Exception e) {
                System.err.println("Background Image Load Fault: " + e.getMessage());
            }
        }

        // Fallback to Dynamic Gradient
        Stop[] stops = {
                new Stop(0.0, Color.web(cBgDeep())),
                new Stop(0.5, Color.web(cBgMid())),
                new Stop(1.0, Color.web(cBgDeep()))
        };
        LinearGradient lg = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE, stops);
        return new Background(new BackgroundFill(lg, CornerRadii.EMPTY, Insets.EMPTY));
    }

    /* ── COOL FEATURES: TOASTS ── */
    public static void showToast(Stage stage, String message, String color) {
        Popup popup = new Popup();
        Label label = new Label(message);
        label.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-padding: 12 24; "
                + "-fx-background-radius: 30; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        popup.getContent().add(label);
        popup.show(stage);

        // Center bottom
        popup.setX(stage.getX() + stage.getWidth() / 2 - 100);
        popup.setY(stage.getY() + stage.getHeight() - 80);

        FadeTransition ft = new FadeTransition(Duration.millis(300), label);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();

        PauseTransition pause = new PauseTransition(Duration.seconds(2.5));
        pause.setOnFinished(e -> {
            FadeTransition out = new FadeTransition(Duration.millis(300), label);
            out.setFromValue(1);
            out.setToValue(0);
            out.setOnFinished(ev -> popup.hide());
            out.play();
        });
        pause.play();
    }

    /* ── COOL FEATURES: LIVE CLOCK ── */
    public static void startClock(Label label) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss  |  MMM dd, yyyy");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            label.setText(LocalDateTime.now().format(dtf));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    /* ── ALERTS (STYLED) ── */
    public static void showInfo(String title, String msg) {
        showAlert(Alert.AlertType.INFORMATION, title, msg);
    }

    public static void showError(String title, String msg) {
        showAlert(Alert.AlertType.ERROR, title, msg);
    }

    public static boolean showConfirm(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        return a.showAndWait().filter(r -> r == ButtonType.OK).isPresent();
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        styleAlert(a);
        a.showAndWait();
    }

    private static void styleAlert(Alert a) {
        DialogPane dp = a.getDialogPane();
        dp.setStyle("-fx-background-color: " + COL_BG_MID + "; -fx-border-color: " + COL_CYAN
                + "; -fx-border-radius: 12; -fx-background-radius: 12;");
        dp.lookup(".content.label").setStyle("-fx-text-fill: white; -fx-font-size: 13px;");
        dp.lookup(".label").setStyle("-fx-text-fill: white;");
    }
    /* ── CONNECTION GUARD (for every UI) ── */
    public static Node connectionGuard(MainWindow main, Node content) {
        if (db.DBManager.isConnected()) {
            return content;
        }

        VBox overlay = new VBox(25);
        overlay.setAlignment(Pos.CENTER);
        overlay.setPadding(new Insets(50));
        overlay.setStyle("-fx-background-color: " + COL_BG_DEEP + "ee; -fx-background-radius: 20;");

        Label icon = new Label("📡");
        icon.setFont(Font.font(60));
        icon.setEffect(glowShadow(Color.web(COL_DANGER)));

        Label title = titleLabel("System Handshake Required");
        Label sub = bodyLabel("This module requires an active connection to the MediLab SQL Environment.");
        
        Button connectBtn = primaryButton("INITIALIZE DATABASE CONNECTION");
        connectBtn.setPrefWidth(350);
        connectBtn.setOnAction(e -> {
            if (db.DBManager.connect()) {
                main.refreshTheme(); // Reload UI
                showToast(main.getStage(), "Handshake Successful: Module Active", COL_SUCCESS);
            } else {
                showToast(main.getStage(), "Connection Refused: Verify SQL Status", COL_DANGER);
            }
        });

        overlay.getChildren().addAll(icon, title, sub, connectBtn);
        
        StackPane wrapper = new StackPane(overlay);
        wrapper.setPadding(new Insets(40));
        return wrapper;
    }
}