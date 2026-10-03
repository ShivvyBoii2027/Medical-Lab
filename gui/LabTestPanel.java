/*
 * MediLab Pro — LabTestPanel
 * Rewritten: Role-aware. Admins use dropdowns + auto-approve.
 * Doctors use name search + PENDING_APPROVAL status.
 */
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

public class LabTestPanel {

    private final MainWindow main;
    private ComboBox<String> patientCombo, doctorCombo, testTypeCombo;
    private int urgencyValue = 3;
    private Label[] stars = new Label[5];
    private TextArea notesArea;

    public LabTestPanel(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox root = new VBox(25);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: transparent;");

        VBox card = new VBox(30);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        // Header
        HBox hdr = new HBox(20);
        hdr.setAlignment(Pos.CENTER_LEFT);
        Label icon = new Label("🧪");
        icon.setFont(Font.font(40));
        icon.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN)));
        boolean isAdmin = "ADMIN".equals(main.getRole());
        VBox texts = new VBox(5,
                Theme.titleLabel("Diagnostic Requisition"),
                Theme.bodyLabel(isAdmin
                        ? "Admin: requests are auto-approved and immediately queued"
                        : "Submit laboratory analysis requests for administrator approval"));
        hdr.getChildren().addAll(icon, texts);
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        // ── Patient Selection ──
        card.getChildren().add(Theme.sectionLabel("Target Patient"));
        patientCombo = Theme.styledCombo();
        patientCombo.setPromptText("Select patient...");
        patientCombo.setMaxWidth(Double.MAX_VALUE);
        loadPatients();
        card.getChildren().add(vField("PATIENT ", patientCombo));

        card.getChildren().add(Theme.styledSeparator());

        // ── Requesting Doctor ──
        card.getChildren().add(Theme.sectionLabel("Requesting Specialist"));
        doctorCombo = Theme.styledCombo();
        doctorCombo.setPromptText("Select doctor...");
        doctorCombo.setMaxWidth(Double.MAX_VALUE);
        loadDoctors();

        // If doctor is logged in, pre-select them and lock the combo
        if ("DOCTOR".equals(main.getRole())) {
            int myId = DBManager.getDoctorIdByUsername(main.getUsername());
            if (myId != -1) {
                try {
                    ResultSet rs = DBManager.getDoctorById(myId);
                    if (rs.next()) {
                        String myEntry = "[" + myId + "] Dr. " + rs.getString("last_name") + " ("
                                + rs.getString("specialization") + ")";
                        doctorCombo.setValue(myEntry);
                        doctorCombo.setDisable(true);
                        doctorCombo.setOpacity(0.7);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        card.getChildren().add(vField("SPECIALIST ", doctorCombo));

        card.getChildren().add(Theme.styledSeparator());

        // ── Analysis Protocol ──
        card.getChildren().add(Theme.sectionLabel("Diagnostic Protocol"));
        testTypeCombo = Theme.styledCombo();
        testTypeCombo.setMaxWidth(Double.MAX_VALUE);
        loadTests();
        card.getChildren().add(vField("SELECT ANALYSIS TYPE *", testTypeCombo));

        // Urgency Stars
        VBox urgencyBox = new VBox(10);
        Label uLbl = Theme.formLabel("URGENCY CLASS (1-5)");
        HBox starRow = new HBox(10);
        starRow.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            stars[i] = new Label("★");
            stars[i].setFont(Font.font(32));
            stars[i].setCursor(javafx.scene.Cursor.HAND);
            stars[i].setOnMouseClicked(e -> {
                urgencyValue = idx + 1;
                refreshStars();
            });
            starRow.getChildren().add(stars[i]);
        }
        refreshStars();
        urgencyBox.getChildren().addAll(uLbl, starRow);
        card.getChildren().add(urgencyBox);

        notesArea = new TextArea();
        notesArea.setPromptText("Clinical observations or special handling protocols...");
        notesArea.setPrefHeight(100);
        notesArea.setStyle("-fx-control-inner-background: " + Theme.COL_BG_DEEP
                + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: " + Theme.COL_BORDER + ";");
        card.getChildren().add(vField("CLINICAL NOTES", notesArea));

        // ── Buttons ──
        card.getChildren().add(Theme.styledSeparator());
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancel = Theme.ghostButton("ABORT REQUEST");
        cancel.setOnAction(e -> clearAll());

        String submitLabel = isAdmin ? "⚡  INITIATE & AUTO-APPROVE" : "🧪  SUBMIT FOR APPROVAL";
        Button submit = Theme.primaryButton(submitLabel);
        submit.setOnAction(e -> doSubmit());

        actions.getChildren().addAll(cancel, submit);
        card.getChildren().add(actions);

        root.getChildren().add(card);
        sp.setContent(root);
        return sp;
    }

    private VBox vField(String l, Control f) {
        VBox b = new VBox(8);
        b.getChildren().addAll(Theme.formLabel(l), f);
        return b;
    }

    private void loadPatients() {
        patientCombo.getItems().clear();
        try {
            ResultSet rs = DBManager.getAllPatients();
            while (rs != null && rs.next()) {
                patientCombo.getItems().add("[" + rs.getInt("patient_id") + "] " +
                        rs.getString("first_name") + " " + rs.getString("last_name"));
            }
        } catch (Exception ignored) {
        }
    }

    private void loadDoctors() {
        doctorCombo.getItems().clear();
        try {
            ResultSet rs = DBManager.getAllDoctors();
            while (rs != null && rs.next()) {
                doctorCombo.getItems().add("[" + rs.getInt("doctor_id") + "] Dr. " +
                        rs.getString("last_name") + " (" + rs.getString("specialization") + ")");
            }
        } catch (Exception ignored) {
        }
    }

    private void loadTests() {
        testTypeCombo.getItems().clear();
        try {
            ResultSet rs = DBManager.getAllTestTypes();
            while (rs != null && rs.next()) {
                testTypeCombo.getItems().add("[" + rs.getString("test_code") + "] " + rs.getString("test_name"));
            }
        } catch (Exception ignored) {
        }
    }

    private void refreshStars() {
        for (int i = 0; i < 5; i++) {
            stars[i].setTextFill(Color.web(i < urgencyValue ? Theme.COL_CYAN : Theme.COL_BG_DEEP));
            stars[i].setEffect(i < urgencyValue ? Theme.glowShadow(Color.web(Theme.COL_CYAN, 0.5)) : null);
        }
    }

    private void clearAll() {
        patientCombo.setValue(null);
        if (!"DOCTOR".equals(main.getRole()))
            doctorCombo.setValue(null);
        testTypeCombo.setValue(null);
        notesArea.clear();
        urgencyValue = 3;
        refreshStars();
    }

    private int parseId(String comboValue) {
        if (comboValue == null)
            return -1;
        try {
            return Integer.parseInt(comboValue.substring(1, comboValue.indexOf("]")));
        } catch (Exception e) {
            return -1;
        }
    }

    private void doSubmit() {
        try {
            int pId = parseId(patientCombo.getValue());
            int dId = parseId(doctorCombo.getValue());
            String sel = testTypeCombo.getValue();

            if (pId == -1) {
                Theme.showToast(main.getStage(), "Please select a patient", Theme.COL_DANGER);
                return;
            }
            if (dId == -1) {
                Theme.showToast(main.getStage(), "Please select a doctor", Theme.COL_DANGER);
                return;
            }
            if (sel == null) {
                Theme.showToast(main.getStage(), "Please select a test type", Theme.COL_DANGER);
                return;
            }

            String code = sel.substring(1, sel.indexOf("]"));
            int ttId = -1;
            ResultSet rs = DBManager.getAllTestTypes();
            while (rs != null && rs.next()) {
                if (rs.getString("test_code").equals(code)) {
                    ttId = rs.getInt("test_type_id");
                    break;
                }
            }

            if (ttId == -1) {
                Theme.showToast(main.getStage(), "Test type not found", Theme.COL_DANGER);
                return;
            }

            DBManager.addTest(pId, dId, ttId, code, urgencyValue, notesArea.getText());

            boolean isAdmin = "ADMIN".equals(main.getRole());
            util.SpecialEffects.playClinicalSuccess(main.getStage().getScene().getRoot());
            Theme.showToast(main.getStage(),
                    isAdmin ? "⚡  Lab Request Auto-Approved & Queued" : "✔  Submitted — Awaiting Admin Approval",
                    isAdmin ? Theme.COL_SUCCESS : Theme.COL_WARNING);

            clearAll();
            main.navigate("TestRecords");
        } catch (Exception e) {
            Theme.showError("Requisition Fault", e.getMessage());
        }
    }
}