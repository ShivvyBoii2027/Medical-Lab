/*
 * MediLab Pro — MainWindow
 * Professional Goated Revision with Integrated DB Control & Live Clock
 */
package gui;

import db.DBManager;
import util.Theme;
import util.ThemeManager;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainWindow {

    private Stage stage;
    private String currentUser, currentRole, displayName;
    private int linkedId;
    private StackPane contentArea;
    private Label breadcrumb;
    private VBox sidebarMenu;
    private String activeSection = "";
    private Button dbToggleBtn;

    public void show(Stage st, String user, String role, int linkedId) {
        this.stage = st;
        this.currentUser = user;
        this.currentRole = role;
        this.linkedId = linkedId;

        BorderPane root = new BorderPane();
        root.setBackground(Theme.mainBackground());

        // Add instance-wide access to root for refreshing
        this.stage.setUserData(root);

        // ── TOP HEADER ──
        HBox header = buildHeader();
        root.setTop(header);

        // ── LEFT SIDEBAR ──
        VBox sidebar = buildSidebar();
        root.setLeft(sidebar);

        // ── CENTRE CONTENT ──
        contentArea = new StackPane();
        contentArea.setStyle("-fx-background-color: transparent;");
        contentArea.setPadding(new Insets(25));
        root.setCenter(contentArea);

        // Load settings
        ThemeManager.getInstance().loadSettings(currentUser);
        this.displayName = DBManager.getUserDisplayName(currentUser, currentRole);

        // Header Entry Animation
        header.setTranslateY(-70);
        TranslateTransition ht = new TranslateTransition(Duration.millis(800), header);
        ht.setToY(0);
        ht.setInterpolator(Interpolator.EASE_OUT);
        ht.play();

        // Load dashboard by default
        navigate("Dashboard");

        Scene scene = new Scene(root, 1250, 850);
        stage.setScene(scene);
        stage.setTitle("MediLab Pro — " + role + " Management System");
        stage.setMinWidth(1100);
        stage.setMinHeight(750);
        stage.show();

        Theme.showToast(stage, "System Initialized: Welcome Back", Theme.COL_SUCCESS);
    }

    /* ═══════════════ SIDEBAR ═══════════════ */
    private VBox buildSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(240);
        sidebar.setStyle("-fx-background-color: " + Theme.gradientSidebar() + "; "
                + "-fx-border-color: " + Theme.COL_BORDER + "; -fx-border-width: 0 1 0 0;");

        // Brand
        VBox brand = new VBox(8);
        brand.setPadding(new Insets(30, 20, 25, 20));
        brand.setAlignment(Pos.CENTER_LEFT);

        Label brandIcon = new Label("🔬");
        brandIcon.setFont(Font.font(32));
        brandIcon.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN)));

        Label brandName = new Label("MediLab Pro");
        brandName.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        brandName.setTextFill(Color.WHITE);

        Label brandSub = new Label("v2.5 Enterprise Edition");
        brandSub.setFont(Font.font("Inter", FontWeight.BLACK, 9));
        brandSub.setTextFill(Color.web(Theme.COL_CYAN));

        brand.getChildren().addAll(brandIcon, brandName, brandSub);
        sidebar.getChildren().add(brand);

        // Menu
        sidebarMenu = new VBox(5);
        sidebarMenu.setPadding(new Insets(10, 15, 15, 15));
        ScrollPane sp = new ScrollPane(sidebarMenu);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        if ("ADMIN".equals(currentRole)) {
            addMenuSection(sidebarMenu, "ADMINISTRATION");
            addMenuItem(sidebarMenu, "👤", "Register Patient", "PatientReg", false);
            addMenuItem(sidebarMenu, "👨⚕️", "Medical Staff Entry", "DoctorReg", false);
            addMenuItem(sidebarMenu, "🧪", "Lab Management", "LabTest", false);
            addMenuItem(sidebarMenu, "📋", "Verified Records", "TestRecords", false);
            addMenuItem(sidebarMenu, "📅", "All Appointments", "PatientSchedule", false);
            addMenuItem(sidebarMenu, "🔬", "Diagnostic Types", "TestTypes", false);
            addMenuItem(sidebarMenu, "🎓", "Area Specialties", "Specializations", false);
            addMenuItem(sidebarMenu, "🔐", "Security Controls", "AccessRights", false);
            addMenuItem(sidebarMenu, "💾", "Core Database", "Database", false);
        } else if ("DOCTOR".equals(currentRole)) {
            addMenuSection(sidebarMenu, "CLINICAL OPS");
            addMenuItem(sidebarMenu, "🧪", "Lab Requests", "LabTest", false);
            addMenuItem(sidebarMenu, "📋", "Verified Records", "TestRecords", false);
            addMenuItem(sidebarMenu, "👥", "Patient List", "ViewPatients", false);
            addMenuItem(sidebarMenu, "📅", "Appointments", "PatientSchedule", false);
        } else if ("PATIENT".equals(currentRole)) {
            addMenuSection(sidebarMenu, "MY HEALTH");
            addMenuItem(sidebarMenu, "🧬", "My Lab Results", "PatientResults", false);
            addMenuItem(sidebarMenu, "📅", "Appointments", "PatientSchedule", false);
            addMenuItem(sidebarMenu, "📁", "My Records", "PatientRecords", false);
        }

        addMenuSection(sidebarMenu, "SYSTEM");
        addMenuItem(sidebarMenu, "⚙️", "User Settings", "Settings", false);

        sidebar.getChildren().add(sp);

        // Logout
        Button logoutBtn = Theme.dangerButton("TERMINATE SESSION");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setStyle(logoutBtn.getStyle() + "-fx-font-size: 11px;");
        logoutBtn.setOnAction(e -> doLogout());

        VBox foot = new VBox(logoutBtn);
        foot.setPadding(new Insets(15));
        sidebar.getChildren().add(foot);

        return sidebar;
    }

    private void addMenuSection(VBox menu, String title) {
        Label sec = new Label(title);
        sec.setFont(Font.font("Inter", FontWeight.BLACK, 10));
        sec.setTextFill(Color.web(Theme.COL_TEXT_MUTED));
        sec.setPadding(new Insets(15, 0, 5, 10));
        menu.getChildren().add(sec);
    }

    private void addMenuItem(VBox menu, String icon, String label, String key, boolean active) {
        HBox item = new HBox(12);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 16, 12, 16));
        item.setStyle(active ? activeItemStyle() : inactiveItemStyle());
        item.setCursor(javafx.scene.Cursor.HAND);

        Label ic = new Label(icon);
        ic.setFont(Font.font(16));
        Label lb = new Label(label);
        lb.setFont(Font.font("Inter", active ? FontWeight.BOLD : FontWeight.MEDIUM, 13));
        lb.setTextFill(active ? Color.WHITE : Color.web(Theme.COL_TEXT_SOFT));

        item.getChildren().addAll(ic, lb);
        item.setOnMouseEntered(e -> {
            if (!key.equals(activeSection)) {
                item.setStyle(hoverItemStyle());
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), item);
                tt.setToX(5);
                tt.play();
            }
        });
        item.setOnMouseExited(e -> {
            if (!key.equals(activeSection)) {
                item.setStyle(inactiveItemStyle());
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), item);
                tt.setToX(0);
                tt.play();
            }
        });
        item.setOnMouseClicked(e -> navigate(key));
        item.setUserData(key);
        menu.getChildren().add(item);
    }

    private String activeItemStyle() {
        return "-fx-background-color: rgba(6,182,212,0.15); -fx-background-radius: 10; "
                + "-fx-border-color: " + Theme.COL_CYAN + "; -fx-border-width: 0 0 0 4; -fx-border-radius: 2;";
    }

    private String inactiveItemStyle() {
        return "-fx-background-color: transparent;";
    }

    private String hoverItemStyle() {
        return "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 10;";
    }

    /* ═══════════════ HEADER ═══════════════ */
    private HBox buildHeader() {
        HBox header = new HBox();
        header.setPrefHeight(75);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 25, 0, 10));
        header.setStyle("-fx-background-color: " + Theme.COL_BG_MID + "; "
                + "-fx-border-color: " + Theme.COL_BORDER + "; -fx-border-width: 0 0 1 0;");

        Region sideGap = new Region();
        sideGap.setPrefWidth(240);

        breadcrumb = new Label("📊  Dashboard");
        breadcrumb.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        breadcrumb.setTextFill(Color.WHITE);

        // Live Clock
        Label clock = new Label();
        clock.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        clock.setTextFill(Color.web(Theme.COL_CYAN));
        Theme.startClock(clock);

        // Integrated DB Status
        dbToggleBtn = createDBToggle();

        // Global Search
        TextField search = Theme.styledField("Search");
        search.setPrefWidth(280);
        search.setStyle(search.getStyle() + "-fx-background-radius: 20; -fx-border-radius: 20;");
        search.setOnAction(e -> handleGlobalSearch(search.getText()));

        // User Avatar
        HBox chip = new HBox(12);
        chip.setAlignment(Pos.CENTER);
        chip.setPadding(new Insets(6, 15, 6, 8));
        chip.setStyle(
                "-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 30; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 30;");

        Circle av = new Circle(16, Color.web(Theme.COL_CYAN));
        Label avI = new Label(roleIcon(currentRole));
        avI.setFont(Font.font(13));
        StackPane avS = new StackPane(av, avI);

        VBox info = new VBox(-2);
        Label n = new Label(displayName);
        n.setFont(Font.font("Inter", FontWeight.BOLD, 11));
        n.setTextFill(Color.WHITE);
        Label r = new Label(currentRole);
        r.setFont(Font.font("Inter", FontWeight.BLACK, 8));
        r.setTextFill(Color.web(Theme.COL_CYAN));
        info.getChildren().addAll(n, r);
        chip.getChildren().addAll(avS, info);

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        Region s2 = new Region();
        HBox.setHgrow(s2, Priority.SOMETIMES);

        header.getChildren().addAll(sideGap, breadcrumb, s1, clock, dbToggleBtn, search, s2, chip);
        HBox.setMargin(dbToggleBtn, new Insets(0, 20, 0, 20));
        return header;
    }

    private Button createDBToggle() {
        Button b = new Button();
        syncDBBtn(b);
        b.setOnAction(e -> {
            if (DBManager.isConnected()) {
                DBManager.disconnect();
                Theme.showToast(stage, "Database Connection Interrupted", Theme.COL_DANGER);
            } else {
                if (DBManager.connect()) {
                    DBManager.setCurrentUser(currentUser); // Refresh role/permissions
                    Theme.showToast(stage, "Database Securely Connected", Theme.COL_SUCCESS);
                    refreshTheme(); // Refresh UI to remove connection guards
                } else {
                    Theme.showToast(stage, "SQL Handshake Failed", Theme.COL_DANGER);
                }
            }
            syncDBBtn(b);
        });
        return b;
    }

    private void syncDBBtn(Button b) {
        boolean ok = DBManager.isConnected();
        b.setText(ok ? "● SYSTEM ONLINE" : "○ OFFLINE");
        b.setStyle("-fx-background-color: " + (ok ? "rgba(16,185,129,0.1)" : "rgba(244,63,94,0.1)") + "; "
                + "-fx-text-fill: " + (ok ? Theme.COL_SUCCESS : Theme.COL_DANGER) + "; "
                + "-fx-font-weight: bold; -fx-font-size: 10px; -fx-padding: 6 15; "
                + "-fx-background-radius: 20; -fx-border-color: " + (ok ? Theme.COL_SUCCESS : Theme.COL_DANGER) + "; "
                + "-fx-border-radius: 20; -fx-cursor: hand;");
    }

    private String roleIcon(String r) {
        return switch (r) {
            case "DOCTOR" -> "👨\u200D⚕️";
            case "PATIENT" -> "👤";
            default -> "⚙️";
        };
    }

    /* ═══════════════ NAVIGATION ═══════════════ */
    public void navigate(String key) {
        activeSection = key;
        updateSidebarHighlight(key);
        breadcrumb.setText(getMenuLabel(key));

        Node view = switch (key) {
            case "PatientReg" -> Theme.connectionGuard(this, new PatientRegistrationPanel(this).build());
            case "DoctorReg" -> Theme.connectionGuard(this, new DoctorRegistrationPanel(this).build());
            case "LabTest" -> Theme.connectionGuard(this, new LabTestPanel(this).build());
            case "TestRecords" -> Theme.connectionGuard(this, new TestRecordsPanel(this).build());
            case "ViewPatients" -> Theme.connectionGuard(this, new ViewPatientsPanel(this).build());
            case "ViewDoctors" -> Theme.connectionGuard(this, new ViewDoctorsPanel(this).build());
            case "TestTypes" -> Theme.connectionGuard(this, new TestTypesPanel(this).build());
            case "Specializations" -> Theme.connectionGuard(this, new SpecializationsPanel(this).build());
            case "AccessRights" -> Theme.connectionGuard(this, new AccessRightsPanel(this).build());
            case "Database" -> new DatabasePanel(this).build(); // No guard for the database panel itself
            case "Settings" -> new SettingsPanel(this).build();
            case "PatientSchedule" -> Theme.connectionGuard(this, new PatientSchedulePanel(this).build());
            case "Dashboard" -> switch (currentRole) {
                case "ADMIN" -> Theme.connectionGuard(this, new AdminDashboard(this).build());
                case "DOCTOR" -> Theme.connectionGuard(this, new DoctorDashboard(this).build());
                case "PATIENT" -> Theme.connectionGuard(this, new PatientDashboard(this).build());
                default -> Theme.connectionGuard(this, new DashboardPanel(this).build());
            };
            default -> Theme.connectionGuard(this, new DashboardPanel(this).build());
        };

        // Transition Animation
        view.setOpacity(0);
        view.setTranslateY(15);
        contentArea.getChildren().setAll(view);

        FadeTransition ft = new FadeTransition(Duration.millis(400), view);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(450), view);
        tt.setFromY(15);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    public void refreshTheme() {
        BorderPane root = (BorderPane) stage.getScene().getRoot();
        root.setBackground(Theme.mainBackground());
        root.setLeft(buildSidebar());
        root.setTop(buildHeader());
        navigate(activeSection);
    }

    public String getUsername() {
        return currentUser;
    }

    public String getUser() {
        return currentUser;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : currentUser;
    }

    public String getRole() {
        return currentRole;
    }

    public int getLinkedId() {
        return linkedId;
    }

    public Stage getStage() {
        return stage;
    }

    private String getMenuLabel(String k) {
        return switch (k) {
            case "Dashboard" -> "📊  Analytics Overview";
            case "PatientReg" -> "👤  Registry: Patient Engagement";
            case "DoctorReg" -> "👨⚕️  Registry: Specialist Entry";
            case "LabTest" -> "🧪  Operational: Diagnostics Request";
            case "TestRecords" -> "📋  Operational: Clinical History";
            case "ViewPatients" -> "👥  Report: Patient Directory";
            case "ViewDoctors" -> "🏥  Report: Medical Staff";
            case "TestTypes" -> "🔬  System: Diagnostic Protocols";
            case "Specializations" -> "🎓  System: Area Specialties";
            case "AccessRights" -> "🔐  Security: Permissions Engine";
            case "Database" -> "💾  Core: SQL Environment";
            case "Settings" -> "⚙️  Personalization Center";
            case "PatientSchedule" -> "📅  Clinical Appointments";
            default -> "📊  Perspective View";
        };
    }

    private void updateSidebarHighlight(String key) {
        for (Node n : sidebarMenu.getChildren()) {
            if (n instanceof HBox item) {
                boolean active = key.equals(item.getUserData());
                item.setStyle(active ? activeItemStyle() : inactiveItemStyle());
                ((Label) item.getChildren().get(1)).setTextFill(active ? Color.WHITE : Color.web(Theme.COL_TEXT_SOFT));
                ((Label) item.getChildren().get(1))
                        .setFont(Font.font("Inter", active ? FontWeight.BOLD : FontWeight.MEDIUM, 13));
            }
        }
    }

    private void handleGlobalSearch(String q) {
        if (q == null || q.trim().isEmpty())
            return;

        activeSection = "Search";
        breadcrumb.setText("🔍  Search Results");
        updateSidebarHighlight("Search");

        Node view = new GlobalSearchPanel(this, q.trim()).build();

        // Transition Animation
        view.setOpacity(0);
        view.setTranslateY(15);
        contentArea.getChildren().setAll(view);

        FadeTransition ft = new FadeTransition(Duration.millis(400), view);
        ft.setFromValue(0);
        ft.setToValue(1);

        TranslateTransition tt = new TranslateTransition(Duration.millis(400), view);
        tt.setFromY(15);
        tt.setToY(0);

        new ParallelTransition(ft, tt).play();
    }

    private void doLogout() {
        if (Theme.showConfirm("Terminate Session", "Confirming secure logout. Continue?")) {
            util.ThemeManager.getInstance().reset();
            new LoginScreen().show(stage);
        }
    }

    public void syncDBStatus() {
        if (dbToggleBtn != null)
            syncDBBtn(dbToggleBtn);
    }
}