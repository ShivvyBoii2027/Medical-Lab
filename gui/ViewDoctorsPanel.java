/*
 * MediLab Pro — ViewDoctorsPanel
 * Goated Revision: Fixed SQL Mapping & Cyber-Grid UI
 */
package gui;

import db.DBManager;
import util.Theme;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import java.sql.ResultSet;

public class ViewDoctorsPanel {

    private final MainWindow main;
    private FlowPane grid;
    private TextField searchField;

    public ViewDoctorsPanel(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox root = new VBox(25);
        root.setPadding(new Insets(10, 10, 30, 10));
        root.setStyle("-fx-background-color: transparent;");

        // Header Control Bar
        HBox header = new HBox(15);
        header.setPadding(new Insets(25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle(Theme.glassStyle());
        header.setEffect(Theme.cardShadow());

        Label title = Theme.titleLabel("Medical Staff Specialist Directory");

        searchField = Theme.styledField("Filter by specialist name or expertise area...");
        searchField.setPrefWidth(400);
        searchField.setOnAction(e -> loadGrid(searchField.getText().trim()));

        Button searchBtn = Theme.primaryButton("SEARCH");
        searchBtn.setOnAction(e -> loadGrid(searchField.getText().trim()));

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        header.getChildren().addAll(title, s1, searchField, searchBtn);
        root.getChildren().add(header);

        // Result Grid
        grid = new FlowPane(20, 20);
        grid.setPadding(new Insets(5));
        root.getChildren().add(grid);

        loadGrid("");
        sp.setContent(root);
        return sp;
    }

    private void loadGrid(String q) {
        grid.getChildren().clear();
        if (!DBManager.isConnected()) {
            grid.getChildren().add(Theme.bodyLabel("System Offline: Database unreachable."));
            return;
        }
        try {
            ResultSet rs = q.isEmpty() ? DBManager.getAllDoctors() : DBManager.searchDoctors(q);
            int count = 0;
            while (rs != null && rs.next()) {
                count++;
                grid.getChildren().add(buildDoctorCard(rs));
            }
            if (count == 0)
                grid.getChildren().add(Theme.bodyLabel("No matching specialist records found."));
        } catch (Exception e) {
            Theme.showError("Directory Fault", e.getMessage());
        }
    }

    private VBox buildDoctorCard(ResultSet rs) throws Exception {
        int id = rs.getInt("doctor_id");
        String name = "Dr. " + rs.getString("first_name") + " " + rs.getString("last_name");
        String spec = rs.getString("specialization");
        String lic = rs.getString("license_number");

        VBox card = new VBox(15);
        card.setPrefWidth(260);
        card.setPadding(new Insets(25));
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle("-fx-background-color: " + Theme.COL_BG_MID + "; -fx-background-radius: 18; -fx-border-color: "
                + Theme.COL_BORDER + "; -fx-border-width: 1;");
        card.setEffect(Theme.cardShadow());

        // Hover Effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + Theme.COL_BG_DEEP
                + "; -fx-background-radius: 18; -fx-border-color: " + Theme.COL_CYAN + "; -fx-border-width: 1.5;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + Theme.COL_BG_MID
                + "; -fx-background-radius: 18; -fx-border-color: " + Theme.COL_BORDER + "; -fx-border-width: 1;"));

        // Avatar
        StackPane avatar = new StackPane();
        Circle circ = new Circle(40, Color.web(Theme.COL_TEAL, 0.1));
        circ.setStroke(Color.web(Theme.COL_TEAL, 0.3));
        circ.setStrokeWidth(1.5);
        Label icon = new Label("👨\u200D⚕️");
        icon.setFont(Font.font(32));
        avatar.getChildren().addAll(circ, icon);

        Label nameLbl = new Label(name.toUpperCase());
        nameLbl.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(TextAlignment.CENTER);

        Label specLbl = new Label(spec != null ? spec.toUpperCase() : "GENERAL MEDICINE");
        specLbl.setFont(Font.font("Inter", FontWeight.BLACK, 9));
        specLbl.setTextFill(Color.web(Theme.COL_CYAN));
        specLbl.setPadding(new Insets(4, 10, 4, 10));
        specLbl.setStyle(
                "-fx-background-color: rgba(34,211,238,0.1); -fx-background-radius: 12; -fx-border-color: rgba(34,211,238,0.3); -fx-border-radius: 12;");

        Label licLbl = new Label("LIC: " + (lic != null ? lic : "PENDING"));
        licLbl.setFont(Font.font("Inter", 11));
        licLbl.setTextFill(Color.web(Theme.COL_TEXT_MUTED));

        Button patientsBtn = Theme.ghostButton("TELEMETRY LOGS");
        patientsBtn.setStyle(patientsBtn.getStyle() + "-fx-font-size: 10px; -fx-padding: 5 10;");
        patientsBtn.setOnAction(e -> showDoctorPatients(id, name));

        card.getChildren().addAll(avatar, nameLbl, specLbl, licLbl, Theme.styledSeparator(), patientsBtn);
        return card;
    }

    private void showDoctorPatients(int dId, String dName) {
        Stage dia = new Stage();
        dia.setTitle("Specialist Audit — " + dName);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setBackground(Theme.mainBackground());

        Label title = Theme.titleLabel("Authorized Assignments: " + dName);
        root.getChildren().addAll(title, Theme.styledSeparator());

        try {
            ResultSet rs = DBManager.getTestsForDoctor(dId);
            VBox list = new VBox(10);
            boolean any = false;
            while (rs != null && rs.next()) {
                any = true;
                HBox row = new HBox(15);
                row.setPadding(new Insets(12));
                row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10;");
                row.setAlignment(Pos.CENTER_LEFT);

                Label code = new Label(rs.getString("test_code"));
                code.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                code.setTextFill(Color.web(Theme.COL_CYAN));
                code.setMinWidth(110);

                Label pat = new Label("👤 " + rs.getString("p_first") + " " + rs.getString("p_last"));
                pat.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                pat.setTextFill(Color.WHITE);
                HBox.setHgrow(pat, Priority.ALWAYS);

                Label type = new Label(rs.getString("type_name"));
                type.setFont(Font.font("Inter", FontWeight.MEDIUM, 11));
                type.setTextFill(Color.web(Theme.COL_TEXT_SOFT));

                row.getChildren().addAll(code, pat, type);
                list.getChildren().add(row);
            }
            if (!any)
                list.getChildren().add(Theme.bodyLabel("No historical assignments found in local cache."));

            ScrollPane sp = new ScrollPane(list);
            sp.setFitToWidth(true);
            sp.setPrefHeight(350);
            sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            root.getChildren().add(sp);
        } catch (Exception e) {
            root.getChildren().add(Theme.bodyLabel("System Error: " + e.getMessage()));
        }

        Button close = Theme.primaryButton("CLOSE LOGS");
        close.setOnAction(e -> dia.close());
        root.getChildren().add(close);

        Scene s = new Scene(root, 750, 550);
        dia.setScene(s);
        dia.show();
    }
}