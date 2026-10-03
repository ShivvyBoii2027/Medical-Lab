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

public class GlobalSearchPanel {
    private final MainWindow main;
    private final String query;
    private VBox resultsContainer;

    public GlobalSearchPanel(MainWindow m, String q) {
        this.main = m;
        this.query = q;
    }

    public Node build() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox root = new VBox(30);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = Theme.titleLabel("Search Results: \"" + query + "\"");
        header.getChildren().add(title);
        root.getChildren().add(header);
        root.getChildren().add(Theme.styledSeparator());

        resultsContainer = new VBox(25);
        root.getChildren().add(resultsContainer);

        performSearch();

        sp.setContent(root);
        return sp;
    }

    private void performSearch() {
        resultsContainer.getChildren().clear();

        // 1. Search Patients
        VBox patientSection = new VBox(15);
        Label pHeader = Theme.sectionLabel("PATIENT MATCHES");
        patientSection.getChildren().add(pHeader);
        
        try {
            ResultSet rs = DBManager.searchPatients(query);
            boolean found = false;
            while (rs != null && rs.next()) {
                found = true;
                patientSection.getChildren().add(searchResultRow("👤", 
                    rs.getString("first_name") + " " + rs.getString("last_name"),
                    "Patient ID: " + rs.getInt("patient_id") + " | " + rs.getString("email"),
                    () -> main.navigate("ViewPatients"))); // In a real app, we'd navigate to a specific profile
            }
            if (!found) patientSection.getChildren().add(Theme.bodyLabel("No patients matched."));
        } catch (Exception e) {
            patientSection.getChildren().add(Theme.bodyLabel("Error searching patients."));
        }
        resultsContainer.getChildren().add(patientSection);

        // 2. Search Doctors
        VBox doctorSection = new VBox(15);
        Label dHeader = Theme.sectionLabel("MEDICAL STAFF MATCHES");
        doctorSection.getChildren().add(dHeader);
        
        try {
            ResultSet rs = DBManager.searchDoctors(query);
            boolean found = false;
            while (rs != null && rs.next()) {
                found = true;
                doctorSection.getChildren().add(searchResultRow("👨‍⚕️", 
                    "Dr. " + rs.getString("first_name") + " " + rs.getString("last_name"),
                    rs.getString("specialization") + " | " + rs.getString("email"),
                    () -> main.navigate("ViewDoctors")));
            }
            if (!found) doctorSection.getChildren().add(Theme.bodyLabel("No specialists matched."));
        } catch (Exception e) {
            doctorSection.getChildren().add(Theme.bodyLabel("Error searching doctors."));
        }
        resultsContainer.getChildren().add(doctorSection);
    }

    private HBox searchResultRow(String icon, String title, String sub, Runnable onAction) {
        HBox row = new HBox(15);
        row.setPadding(new Insets(15, 20, 15, 20));
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12; -fx-cursor: hand;");
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.06); -fx-background-radius: 12; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 12; -fx-cursor: hand;"));
        row.setOnMouseClicked(e -> onAction.run());

        Label ic = new Label(icon);
        ic.setFont(Font.font(24));
        
        VBox texts = new VBox(4);
        Label t = new Label(title);
        t.setFont(Font.font("Inter", FontWeight.BOLD, 15));
        t.setTextFill(Color.WHITE);
        
        Label s = new Label(sub);
        s.setFont(Font.font("Inter", FontWeight.MEDIUM, 11));
        s.setTextFill(Color.web(Theme.COL_TEXT_SOFT));
        
        texts.getChildren().addAll(t, s);
        row.getChildren().addAll(ic, texts);
        return row;
    }
}
