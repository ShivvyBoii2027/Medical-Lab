package gui;

import db.DBManager;
import util.Theme;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

public class DatabasePanel {
    private final MainWindow main;
    private Label statusLabel, statsLabel;
    private Button mainToggle;

    public DatabasePanel(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        VBox root = new VBox(25);
        root.setStyle("-fx-background-color: transparent;");
        root.setPadding(new Insets(10));

        VBox card = new VBox(25);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        // Header Section
        VBox header = new VBox(8);
        header.getChildren().addAll(
                Theme.titleLabel("System Infrastructure"),
                Theme.bodyLabel("Manage Core SQL Environment and Service Connectivity"));

        // Status Panel
        VBox statusBox = new VBox(15);
        statusBox.setPadding(new Insets(25));
        statusBox.setStyle(Theme.glassStyleLight());

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Inter", FontWeight.BOLD, 18));

        statsLabel = new Label();
        statsLabel.setFont(Font.font("Inter", 13));
        statsLabel.setTextFill(Color.web(Theme.COL_TEXT_SOFT));

        updateUI();

        // Control Panel
        mainToggle = new Button();
        mainToggle.setPrefHeight(45);
        mainToggle.setPrefWidth(220);
        syncToggleBtn();

        mainToggle.setOnAction(e -> {
            if (DBManager.isConnected()) {
                DBManager.disconnect();
                Theme.showToast(main.getStage(), "Connection Closed", Theme.COL_DANGER);
            } else {
                if (DBManager.connect()) {
                    Theme.showToast(main.getStage(), "Handshake Successful", Theme.COL_SUCCESS);
                } else {
                    Theme.showToast(main.getStage(), "Network Error", Theme.COL_DANGER);
                }
            }
            updateUI();
            main.syncDBStatus(); // Sync header button
        });

        Button refreshBtn = Theme.ghostButton("RESYNC ANALYTICS");
        refreshBtn.setOnAction(e -> {
            updateUI();
            Theme.showToast(main.getStage(), "Telemetry Data Refreshed", Theme.COL_CYAN);
        });

        HBox controls = new HBox(15, mainToggle, refreshBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        statusBox.getChildren().addAll(statusLabel, statsLabel, new Separator() {
            {
                setStyle("-fx-opacity: 0.1;");
            }
        }, controls);

        // Details Panel
        VBox details = new VBox(15);
        details.getChildren().addAll(
                Theme.sectionLabel("Environment Specifications"),
                infoRow("Database Provider", "MySQL Relational Engine"),
                infoRow("Connection URL", "jdbc:mysql://localhost:3306/medilab_pro"),
                infoRow("Service Account", "root"),
                infoRow("Driver Class", "com.mysql.cj.jdbc.Driver"));

        card.getChildren().addAll(header, Theme.styledSeparator(), statusBox, details);
        root.getChildren().add(card);

        return root;
    }

    private void updateUI() {
        boolean ok = DBManager.isConnected();
        statusLabel.setText(ok ? "● SYSTEM OPERATIONAL" : "○ SYSTEM OFFLINE");
        statusLabel.setTextFill(Color.web(ok ? Theme.COL_SUCCESS : Theme.COL_DANGER));

        if (ok) {
            statsLabel.setText("Active Records: " + DBManager.countPatients() + " Patients | "
                    + DBManager.countDoctors() + " Specialists | "
                    + DBManager.getTestCount() + " Diagnostic Reports");
        } else {
            statsLabel.setText("Database services are currently unreachable. Verification required.");
        }
        if (mainToggle != null)
            syncToggleBtn();
    }

    private void syncToggleBtn() {
        boolean ok = DBManager.isConnected();
        mainToggle.setText(ok ? "TERMINATE SERVICE" : "INITIALIZE HANDSHAKE");
        mainToggle.setStyle("-fx-background-color: " + (ok ? "rgba(244,63,94,0.1)" : Theme.COL_CYAN) + "; "
                + "-fx-text-fill: " + (ok ? Theme.COL_DANGER : "white") + "; "
                + "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; "
                + "-fx-border-color: " + (ok ? Theme.COL_DANGER : "transparent") + "; "
                + "-fx-border-width: 2; -fx-border-radius: 8;");
    }

    private HBox infoRow(String k, String v) {
        Label key = Theme.formLabel(k);
        key.setMinWidth(160);
        Label val = Theme.bodyLabel(v);
        val.setTextFill(Color.WHITE);
        HBox r = new HBox(20, key, val);
        r.setAlignment(Pos.CENTER_LEFT);
        return r;
    }
}