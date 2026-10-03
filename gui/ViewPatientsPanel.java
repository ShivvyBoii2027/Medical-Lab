/*
 * MediLab Pro — ViewPatientsPanel
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.sql.ResultSet;

public class ViewPatientsPanel {

    private final MainWindow main;
    private FlowPane grid;
    private TextField searchField;

    public ViewPatientsPanel(MainWindow m) {
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

        Label title = Theme.titleLabel("Patient Directory");

        searchField = Theme.styledField("Filter by name, UID, or bio-data...");
        searchField.setPrefWidth(350);
        searchField.setOnAction(e -> loadGrid(searchField.getText().trim()));

        Button searchBtn = Theme.primaryButton("SEARCH");
        searchBtn.setOnAction(e -> loadGrid(searchField.getText().trim()));

        Button exportBtn = Theme.ghostButton("EXPORT CSV");
        exportBtn.setOnAction(e -> handleExport());

        Region s1 = new Region();
        HBox.setHgrow(s1, Priority.ALWAYS);
        header.getChildren().addAll(title, s1, searchField, searchBtn, exportBtn);
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
            ResultSet rs = q.isEmpty() ? DBManager.getAllPatients() : DBManager.searchPatients(q);
            int count = 0;
            while (rs != null && rs.next()) {
                count++;
                grid.getChildren().add(buildPatientCard(rs));
            }
            if (count == 0)
                grid.getChildren().add(Theme.bodyLabel("No matching biometric records found."));
        } catch (Exception e) {
            Theme.showError("Directory Fault", e.getMessage());
        }
    }

    private VBox buildPatientCard(ResultSet rs) throws Exception {
        int id = rs.getInt("patient_id");
        String name = rs.getString("first_name") + " " + rs.getString("last_name");
        String gender = rs.getString("gender");
        int age = rs.getInt("age");

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
        Circle circ = new Circle(40, Color.web(Theme.COL_CYAN, 0.1));
        circ.setStroke(Color.web(Theme.COL_CYAN, 0.3));
        circ.setStrokeWidth(1.5);
        Label icon = new Label(genderIcon(gender));
        icon.setFont(Font.font(32));
        avatar.getChildren().addAll(circ, icon);

        Label nameLbl = new Label(name.toUpperCase());
        nameLbl.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        nameLbl.setTextFill(Color.WHITE);
        nameLbl.setWrapText(true);
        nameLbl.setTextAlignment(TextAlignment.CENTER);

        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER);
        chips.getChildren().addAll(makeChip("UID:" + id, Theme.COL_CYAN), makeChip(gender, Theme.COL_TEAL));

        Label bioLbl = new Label("Precision Age: " + age + " | PID-HASH: " + id);
        bioLbl.setFont(Font.font("Inter", 11));
        bioLbl.setTextFill(Color.web(Theme.COL_TEXT_SOFT));

        Button reportsBtn = Theme.ghostButton("VIEW CLINICAL REPORTS");
        reportsBtn.setStyle(reportsBtn.getStyle() + "-fx-font-size: 10px; -fx-padding: 5 10;");
        reportsBtn.setOnAction(e -> showReports(id, name));

        card.getChildren().addAll(avatar, nameLbl, chips, bioLbl, Theme.styledSeparator(), reportsBtn);
        return card;
    }

    private void showReports(int pId, String pName) {
        Stage dia = new Stage();
        dia.setTitle("Telemetry Analysis — " + pName);

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setBackground(Theme.mainBackground());

        Label title = Theme.titleLabel("Analysis History: " + pName);
        root.getChildren().addAll(title, Theme.styledSeparator());

        try {
            ResultSet rs = DBManager.getTestsForPatient(pId);
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

                Label type = new Label(rs.getString("type_name"));
                type.setFont(Font.font("Inter", FontWeight.BOLD, 13));
                type.setTextFill(Color.WHITE);
                HBox.setHgrow(type, Priority.ALWAYS);

                Label status = new Label(rs.getString("status"));
                status.setFont(Font.font("Inter", FontWeight.BLACK, 9));
                String st = rs.getString("status");
                String col = "COMPLETED".equals(st) ? Theme.COL_SUCCESS : Theme.COL_WARNING;
                status.setTextFill(Color.web(col));
                status.setStyle("-fx-background-color: " + col
                        + "15; -fx-background-radius: 8; -fx-padding: 3 10; -fx-border-color: " + col
                        + "33; -fx-border-radius: 8;");

                row.getChildren().addAll(code, type, status);
                list.getChildren().add(row);
            }
            if (!any)
                list.getChildren().add(Theme.bodyLabel("No diagnostic history found for this entity."));

            ScrollPane sp = new ScrollPane(list);
            sp.setFitToWidth(true);
            sp.setPrefHeight(350);
            sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            root.getChildren().add(sp);
        } catch (Exception e) {
            root.getChildren().add(Theme.bodyLabel("System Error: " + e.getMessage()));
        }

        Button close = Theme.primaryButton("CLOSE TELEMETRY");
        close.setOnAction(e -> dia.close());
        root.getChildren().add(close);

        Scene s = new Scene(root, 750, 550);
        dia.setScene(s);
        dia.show();
    }

    private Label makeChip(String t, String c) {
        Label l = new Label(t);
        l.setFont(Font.font("Inter", FontWeight.BOLD, 9));
        l.setTextFill(Color.web(c));
        l.setPadding(new Insets(3, 8, 3, 8));
        l.setStyle("-fx-background-color: " + c + "15; -fx-background-radius: 10; -fx-border-color: " + c
                + "33; -fx-border-radius: 10;");
        return l;
    }

    private String genderIcon(String g) {
        if (g == null)
            return "👤";
        return switch (g.toLowerCase()) {
            case "female" -> "👩";
            case "male" -> "👨";
            default -> "👤";
        };
    }

    private void handleExport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Export Bio-Registry");
        fc.setInitialFileName("patients_registry.csv");
        File f = fc.showSaveDialog(main.getStage());
        if (f == null)
            return;
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            pw.println("PID,First_Name,Last_Name,Age,Gender,Phone,Registration_Date");
            ResultSet rs = DBManager.getAllPatients();
            while (rs.next()) {
                pw.printf("%d,%s,%s,%d,%s,%s,%s%n",
                        rs.getInt("patient_id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getInt("age"), rs.getString("gender"), rs.getString("phone"),
                        rs.getString("created_date"));
            }
            Theme.showToast(main.getStage(), "Registry Exported to Disk", Theme.COL_SUCCESS);
        } catch (Exception e) {
            Theme.showError("Export Fault", e.getMessage());
        }
    }
}