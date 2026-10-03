/*
 * MediLab Pro — SpecializationsPanel
 * Goated Revision: Fixed Logic & Modern Tech UI
 */
package gui;

import db.DBManager;
import util.Theme;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import java.sql.ResultSet;

public class SpecializationsPanel {

    private final MainWindow main;
    private VBox listBox;

    public SpecializationsPanel(MainWindow m) {
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

        VBox card = new VBox(30);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        // Header
        VBox hdr = new VBox(5);
        hdr.getChildren().addAll(
                Theme.titleLabel("Medical Staff Expertise"),
                Theme.bodyLabel("Manage the clinical specialization registry for authorized practitioners"));
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        // Add Section
        VBox addSec = new VBox(15);
        Label addTitle = Theme.sectionLabel("➕ Register New Expertise");

        HBox inputRow = new HBox(15);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        TextField nameF = Theme.styledField("Expertise Title (e.g., Cardiology)");
        TextField catF = Theme.styledField("Category (e.g., Diagnostics)");
        nameF.setPrefWidth(300);
        catF.setPrefWidth(200);

        Button addBtn = Theme.primaryButton("REGISTER AREA");
        addBtn.setOnAction(e -> {
            try {
                if (nameF.getText().trim().isEmpty()) {
                    Theme.showToast(main.getStage(), "Validation: Title required", Theme.COL_DANGER);
                    return;
                }
                DBManager.addSpecialization(nameF.getText().trim(), catF.getText().trim().toUpperCase());
                nameF.clear();
                catF.clear();
                Theme.showToast(main.getStage(), "Registry Updated Successfully", Theme.COL_SUCCESS);
                loadList();
            } catch (Exception ex) {
                Theme.showError("Registry Fault", ex.getMessage());
            }
        });

        inputRow.getChildren().addAll(nameF, catF, addBtn);
        addSec.getChildren().addAll(addTitle, inputRow);
        card.getChildren().add(addSec);
        card.getChildren().add(Theme.styledSeparator());

        // List Section
        VBox listSec = new VBox(20);
        Label listTitle = Theme.sectionLabel("📋 Active Registry");
        listBox = new VBox(10);

        listSec.getChildren().addAll(listTitle, listBox);
        card.getChildren().add(listSec);

        root.getChildren().add(card);
        sp.setContent(root);
        loadList();
        return sp;
    }

    private void loadList() {
        listBox.getChildren().clear();
        if (!DBManager.isConnected()) {
            listBox.getChildren().add(Theme.bodyLabel("System Offline: Database unreachable."));
            return;
        }
        try {
            ResultSet rs = DBManager.getAllSpecializations();
            String lastCat = "";
            int count = 0;
            while (rs != null && rs.next()) {
                count++;
                String cat = rs.getString("category");
                if (cat == null || cat.isEmpty())
                    cat = "GENERAL";

                if (!cat.equals(lastCat)) {
                    VBox catBox = new VBox(5);
                    catBox.setPadding(new Insets(15, 0, 5, 0));
                    Label catLbl = new Label("📂  " + cat.toUpperCase());
                    catLbl.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BLACK, 11));
                    catLbl.setTextFill(Color.web(Theme.COL_CYAN));
                    catBox.getChildren().addAll(catLbl, new Separator() {
                        {
                            setStyle("-fx-opacity: 0.1;");
                        }
                    });
                    listBox.getChildren().add(catBox);
                    lastCat = cat;
                }

                HBox row = new HBox(15);
                row.setPadding(new Insets(12, 20, 12, 20));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-border-color: "
                                + Theme.COL_BORDER + "; -fx-border-radius: 10;");

                Label num = new Label(String.format("%02d", count));
                num.setFont(javafx.scene.text.Font.font("Monospaced", 13));
                num.setTextFill(Color.web(Theme.COL_CYAN));

                Label name = new Label(rs.getString("spec_name").toUpperCase());
                name.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 13));
                name.setTextFill(Color.WHITE);

                HBox.setHgrow(name, Priority.ALWAYS);
                row.getChildren().addAll(num, name);
                listBox.getChildren().add(row);
            }
            if (count == 0)
                listBox.getChildren().add(Theme.bodyLabel("No entries found in expertise registry."));
        } catch (Exception e) {
            Theme.showError("Data Stream Fault", e.getMessage());
        }
    }
}