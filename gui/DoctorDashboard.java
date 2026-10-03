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

public class DoctorDashboard {
    private final MainWindow main;

    public DoctorDashboard(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        VBox welcome = new VBox(10);
        welcome.setPadding(new Insets(30));
        welcome.setStyle(Theme.glassStyle());
        welcome.getChildren().addAll(
                Theme.titleLabel("Clinical Operations Portal"),
                Theme.bodyLabel("Welcome back, Dr. " + main.getDisplayName() + ". Your specialized queue is ready."));
        root.getChildren().add(welcome);

        GridPane metrics = new GridPane();
        metrics.setHgap(20);
        metrics.setVgap(20);
        try {
            metrics.add(statMini("PENDING REPORTS", String.valueOf(DBManager.countPendingTests()), Theme.COL_WARNING),
                    0, 0);
            metrics.add(statMini("PATIENTS TODAY", String.valueOf(DBManager.getTodaysPatientsCount()), Theme.COL_CYAN),
                    1, 0);
            metrics.add(statMini("VERIFIED DIAGNOSTICS", String.valueOf(DBManager.getTotalTests()), Theme.COL_SUCCESS),
                    2, 0);
        } catch (Exception e) {
            metrics.add(Theme.bodyLabel("Stat Error"), 0, 0);
        }

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(33.3);
        metrics.getColumnConstraints().addAll(cc, cc, cc);
        root.getChildren().add(metrics);

        HBox split = new HBox(20);
        VBox queue = new VBox(15);
        queue.setPadding(new Insets(20));
        queue.setStyle(Theme.glassStyle());
        queue.getChildren().addAll(Theme.sectionLabel("Your Scheduled Queue"), Theme.styledSeparator());

        // Live Scheduled Queue
        VBox qList = new VBox(10);
        try {
            int dId = DBManager.getDoctorIdByUsername(main.getUsername());
            ResultSet rs = DBManager.getScheduledQueue(dId);
            boolean any = false;
            while (rs != null && rs.next()) {
                any = true;
                HBox row = new HBox(15);
                row.setPadding(new Insets(10));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 8;");

                Label time = new Label(rs.getString("app_date").substring(11, 16));
                time.setStyle("-fx-text-fill: " + Theme.COL_CYAN + "; -fx-font-weight: bold;");

                Label name = new Label(rs.getString("first_name") + " " + rs.getString("last_name"));
                name.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                name.setMinWidth(120);

                Label reason = new Label(rs.getString("reason"));
                reason.setStyle("-fx-text-fill: gray;");

                row.getChildren().addAll(time, name, reason);
                qList.getChildren().add(row);
            }
            if (!any)
                qList.getChildren().add(Theme.bodyLabel("No active sessions in your queue."));
        } catch (Exception e) {
            qList.getChildren().add(Theme.bodyLabel("Queue Error: " + e.getMessage()));
        }
        queue.getChildren().add(qList);

        VBox stats = new VBox(15);
        stats.setPrefWidth(300);
        stats.setPadding(new Insets(20));
        stats.setStyle(Theme.glassStyle());
        stats.getChildren().addAll(Theme.sectionLabel("Clinical Accuracy"), Theme.styledSeparator());
        stats.getChildren().add(new Label("Verifications: 98%") {
            {
                setStyle("-fx-text-fill: white; -fx-font-size: 16px; ");
            }
        });
        stats.getChildren().add(new Label("Patient Satisfaction: 4.9/5") {
            {
                setStyle("-fx-text-fill: " + Theme.COL_SUCCESS + "; ");
            }
        });

        HBox.setHgrow(queue, Priority.ALWAYS);
        split.getChildren().addAll(queue, stats);
        root.getChildren().add(split);

        return new ScrollPane(root) {
            {
                setFitToWidth(true);
                setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            }
        };
    }

    private VBox statMini(String t, String v, String c) {
        VBox b = new VBox(5);
        b.setPadding(new Insets(20));
        b.setStyle("-fx-background-color: " + Theme.cBgMid()
                + "; -fx-background-radius: 12; -fx-border-color: rgba(255,255,255,0.05);");
        Label tl = Theme.sectionLabel(t);
        tl.setStyle("-fx-font-size: 9px;");
        Label vl = new Label(v);
        vl.setFont(Font.font("Inter", FontWeight.BLACK, 24));
        vl.setTextFill(Color.web(c));
        b.getChildren().addAll(tl, vl);
        return b;
    }
}
