/*
 * MediLab Pro — TestTypesPanel
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

public class TestTypesPanel {

    private final MainWindow main;
    private VBox listBox;

    public TestTypesPanel(MainWindow m) {
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
                Theme.titleLabel("Clinical Analysis Protocols"),
                Theme.bodyLabel("Master directory of laboratory test types and diagnostic categories"));
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        // Add Section
        VBox addSec = new VBox(20);
        Label addTitle = Theme.sectionLabel("➕ Define Diagnostic Protocol");

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(15);

        TextField codeF = Theme.styledField("Protocol Code (e.g., HEM01)");
        TextField nameF = Theme.styledField("Analysis Name (e.g., Full Blood Count)");
        TextField catF = Theme.styledField("Category (e.g., Hematology)");

        form.add(vField("PROTOCOL CODE *", codeF), 0, 0);
        form.add(vField("ANALYSIS DESIGNATION *", nameF), 1, 0);
        form.add(vField("CLINICAL CATEGORY", catF), 2, 0);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(cc, cc, cc);

        Button addBtn = Theme.primaryButton("REGISTER PROTOCOL");
        addBtn.setOnAction(e -> {
            try {
                if (codeF.getText().isEmpty() || nameF.getText().isEmpty()) {
                    Theme.showToast(main.getStage(), "Validation: Code & Name required", Theme.COL_DANGER);
                    return;
                }
                DBManager.addTestType(codeF.getText().trim(), nameF.getText().trim(), catF.getText().trim(), "", "", "",
                        "", "", "", 0.0);
                codeF.clear();
                nameF.clear();
                catF.clear();
                Theme.showToast(main.getStage(), "Protocol Registered Successfully", Theme.COL_SUCCESS);
                loadList();
            } catch (Exception ex) {
                Theme.showError("Registry Fault", ex.getMessage());
            }
        });

        addSec.getChildren().addAll(addTitle, form, addBtn);
        card.getChildren().add(addSec);
        card.getChildren().add(Theme.styledSeparator());

        // List Section
        VBox listSec = new VBox(20);
        Label listTitle = Theme.sectionLabel("📋 Active Protocols Registry");
        listBox = new VBox(10);

        listSec.getChildren().addAll(listTitle, listBox);
        card.getChildren().add(listSec);

        root.getChildren().add(card);
        sp.setContent(root);
        loadList();
        return sp;
    }

    private VBox vField(String l, TextField f) {
        VBox b = new VBox(8);
        b.getChildren().addAll(Theme.formLabel(l), f);
        return b;
    }

    private void loadList() {
        listBox.getChildren().clear();
        if (!DBManager.isConnected()) {
            listBox.getChildren().add(Theme.bodyLabel("System Offline: Database unreachable."));
            return;
        }
        try {
            ResultSet rs = DBManager.getAllTestTypes();
            String lastCat = "";
            int count = 0;
            while (rs != null && rs.next()) {
                count++;
                String cat = rs.getString("category");
                if (cat == null)
                    cat = "UNCLASSIFIED";

                if (!cat.equalsIgnoreCase(lastCat)) {
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
                row.setPadding(new Insets(10, 20, 10, 20));
                row.setAlignment(Pos.CENTER_LEFT);
                row.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 8; -fx-border-color: rgba(34,211,238,0.1); -fx-border-radius: 8;");

                Label code = new Label("[" + rs.getString("test_code") + "]");
                code.setFont(javafx.scene.text.Font.font("Monospaced", javafx.scene.text.FontWeight.BOLD, 12));
                code.setTextFill(Color.web(Theme.COL_TEAL));
                code.setMinWidth(100);

                Label name = new Label(rs.getString("test_name"));
                name.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.SEMI_BOLD, 13));
                name.setTextFill(Color.WHITE);

                HBox.setHgrow(name, Priority.ALWAYS);
                row.getChildren().addAll(code, name);
                listBox.getChildren().add(row);
            }
            if (count == 0)
                listBox.getChildren().add(Theme.bodyLabel("No protocols matching search criteria."));
        } catch (Exception e) {
            Theme.showError("Data Stream Fault", e.getMessage());
        }
    }
}