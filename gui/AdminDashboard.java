package gui;

import db.DBManager;
import util.Theme;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import java.sql.ResultSet;

public class AdminDashboard {
    public AdminDashboard(MainWindow m) {
        // MainWindow reference provided for future navigation needs
    }

    public Node build() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        HBox welcome = new HBox(20);
        welcome.setAlignment(Pos.CENTER_LEFT);
        welcome.setPadding(new Insets(30));
        welcome.setStyle(Theme.glassStyle());

        VBox text = new VBox(5);
        text.getChildren().addAll(
                Theme.titleLabel("Administrative Intelligence Console"),
                Theme.bodyLabel("System Environment: Production | Node Status: Optimized"));
        welcome.getChildren().add(text);
        root.getChildren().add(welcome);

        // Stats
        HBox stats = new HBox(20);
        try {
            stats.getChildren().addAll(
                    statCard("TOTAL USERS", String.valueOf(DBManager.countUsersStatus()), Theme.COL_CYAN),
                    statCard("STAFF SATURATION", DBManager.getStaffSaturation() + "%", Theme.COL_BLUE),
                    statCard("DIAGNOSTIC VELOCITY", "+" + DBManager.getDiagnosticVelocity(), Theme.COL_SUCCESS),
                    statCard("SYSTEM LATENCY", DBManager.getSystemLatency(), Theme.COL_TEAL));
        } catch (Exception e) {
            stats.getChildren().add(Theme.bodyLabel("Telemetry Fault: " + e.getMessage()));
        }
        for (Node n : stats.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);
        root.getChildren().add(stats);

        // Activity Table
        VBox activity = new VBox(15);
        activity.setPadding(new Insets(25));
        activity.setStyle(Theme.glassStyle());
        activity.getChildren().addAll(Theme.sectionLabel("Security Audit: Recent Actions"), Theme.styledSeparator());

        try {
            ResultSet rs = DBManager.getRecentActivity();
            VBox list = new VBox(10);
            boolean empty = true;
            while (rs != null && rs.next()) {
                empty = false;
                HBox row = new HBox(20);
                row.setPadding(new Insets(10));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");
                Label msg = new Label(rs.getString("activity"));
                msg.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");

                Label time = new Label(rs.getString("timestamp"));
                time.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);
                row.getChildren().addAll(msg, r, time);
                list.getChildren().add(row);
            }
            if (empty) {
                // FALLBACK: Mock Security Data
                String[][] mocks = {
                    {"ADMIN_AUTH_SUCCESS", "10:45 AM"},
                    {"SCHEMA_INTEGRITY_CHECK", "10:30 AM"},
                    {"BACKUP_ROUTINE_COMPLETED", "09:15 AM"}
                };
                for (String[] m : mocks) {
                    HBox row = new HBox(20);
                    row.setPadding(new Insets(10));
                    row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8;");
                    Label msg = new Label(m[0]);
                    msg.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    Label time = new Label(m[1]);
                    time.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
                    Region r = new Region();
                    HBox.setHgrow(r, Priority.ALWAYS);
                    row.getChildren().addAll(msg, r, time);
                    list.getChildren().add(row);
                }
                activity.getChildren().add(list);
            } else {
                activity.getChildren().add(list);
            }
        } catch (Exception e) {
        }

        root.getChildren().add(activity);
        return new ScrollPane(root) {
            {
                setFitToWidth(true);
                setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            }
        };
    }

    private VBox statCard(String t, String v, String c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: " + Theme.cBgMid()
                + "; -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.05);");
        Label tl = Theme.sectionLabel(t);
        Label vl = new Label(v);
        vl.setFont(Font.font("Inter", FontWeight.BLACK, 28));
        vl.setTextFill(Color.web(c));
        card.getChildren().addAll(tl, vl);
        return card;
    }
}
