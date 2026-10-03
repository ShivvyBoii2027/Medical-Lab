/*
 * MediLab Pro — TestRecordsPanel
 * Goated Revision: Fixed SQL Mapping & Cyber-Tech Navigator
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
import java.util.ArrayList;
import java.util.List;

public class TestRecordsPanel {

    private final MainWindow main;
    private List<String[]> allTests = new ArrayList<>();
    private int currentIdx = -1;

    private Label testCodeLbl, testTypeLbl, urgencyLbl, statusLbl, patientLbl, doctorLbl, dateLbl, notesLbl, counterLbl;
    private Button prevBtn, nextBtn, editBtn, deleteBtn;

    // Edit Mode fields
    private ComboBox<String> editTypeCombo;
    private Label[] editStars = new Label[5];
    private int editUrgency = 3;
    private boolean isEditing = false;
    private HBox editActions;

    public TestRecordsPanel(MainWindow m) {
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

        // Header
        VBox hdr = new VBox(10);
        hdr.setPadding(new Insets(30));
        hdr.setStyle(Theme.glassStyle());
        hdr.setEffect(Theme.cardShadow());
        hdr.getChildren().addAll(
                Theme.titleLabel("Clinical Records Navigator"),
                Theme.bodyLabel("Review and manage historical diagnostic data across the network"));
        root.getChildren().add(hdr);

        // Record Card
        VBox card = buildRecordCard();
        root.getChildren().add(card);

        // Nav Controls
        HBox controls = buildNavControls();
        root.getChildren().add(controls);

        counterLbl = new Label("No data synchronized");
        counterLbl.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        counterLbl.setTextFill(Color.web(Theme.COL_CYAN));
        root.getChildren().add(counterLbl);

        loadData();
        sp.setContent(root);
        return sp;
    }

    private VBox buildRecordCard() {
        VBox card = new VBox(25);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(18);

        testCodeLbl = valLabel("");
        testTypeLbl = valLabel("");
        urgencyLbl = valLabel("");
        statusLbl = valLabel("");
        patientLbl = valLabel("");
        doctorLbl = valLabel("");
        dateLbl = valLabel("");
        notesLbl = valLabel("");
        notesLbl.setWrapText(true);

        // Edit Components
        editTypeCombo = Theme.styledCombo();
        refreshTestTypes();
        editTypeCombo.setVisible(false);
        editTypeCombo.setManaged(false);

        HBox starBox = new HBox(10);
        starBox.setAlignment(Pos.CENTER_LEFT);
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            editStars[i] = new Label("★");
            editStars[i].setFont(Font.font(28));
            editStars[i].setCursor(javafx.scene.Cursor.HAND);
            editStars[i].setVisible(false);
            editStars[i].setManaged(false);
            editStars[i].setOnMouseClicked(e -> {
                editUrgency = idx + 1;
                syncEditStars();
            });
            starBox.getChildren().add(editStars[i]);
        }

        addGridRow(grid, 0, "TEST TRACKING UID", testCodeLbl);

        Label typeHead = Theme.formLabel("DIAGNOSTIC TYPE");
        grid.add(typeHead, 0, 1);
        grid.add(testTypeLbl, 1, 1);
        grid.add(editTypeCombo, 1, 1);

        Label urgHead = Theme.formLabel("URGENCY CLASSIFICATION");
        grid.add(urgHead, 0, 2);
        grid.add(urgencyLbl, 1, 2);
        grid.add(starBox, 1, 2);

        addGridRow(grid, 3, "CURRENT STATUS", statusLbl);
        addGridRow(grid, 4, "IDENTIFIED PATIENT", patientLbl);
        addGridRow(grid, 5, "AUTHORIZING SPECIALIST", doctorLbl);
        addGridRow(grid, 6, "TIMESTAMP", dateLbl);
        addGridRow(grid, 7, "CLINICAL OBSERVATIONS", notesLbl);

        ColumnConstraints c1 = new ColumnConstraints(200);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c1, c2);

        card.getChildren().add(grid);

        // Inline Actions for Edit Mode
        editActions = new HBox(15);
        editActions.setAlignment(Pos.CENTER_RIGHT);
        editActions.setVisible(false);
        editActions.setManaged(false);

        Button save = Theme.primaryButton("COMMIT CHANGES");
        save.setOnAction(e -> handleSave());

        Button cancel = Theme.ghostButton("ABORT");
        cancel.setOnAction(e -> exitEditMode());

        editActions.getChildren().addAll(cancel, save);
        card.getChildren().add(editActions);

        // Approval Actions (Admin only, for PENDING_APPROVAL records)
        HBox approvalActions = new HBox(15);
        approvalActions.setAlignment(Pos.CENTER_RIGHT);

        Button approve = Theme.successButton("✔  APPROVE REQUISITION");
        approve.setOnAction(e -> handleApprove());

        Button reject = Theme.dangerButton("✖  DENY ACCESS");
        reject.setOnAction(e -> handleReject());

        approvalActions.getChildren().addAll(reject, approve);
        // Bind visibility: only show when PENDING_APPROVAL and current user is ADMIN
        statusLbl.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean show = "PENDING_APPROVAL".equals(newVal) && (DBManager.isAdmin() || "DOCTOR".equals(main.getRole()));
            approvalActions.setVisible(show);
            approvalActions.setManaged(show);
        });
        approvalActions.setVisible(false);
        approvalActions.setManaged(false);
        card.getChildren().add(approvalActions);

        return card;
    }

    private void addGridRow(GridPane g, int r, String h, Label v) {
        g.add(Theme.formLabel(h), 0, r);
        g.add(v, 1, r);
    }

    private Label valLabel(String t) {
        Label l = new Label(t);
        l.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 15));
        l.setTextFill(Color.WHITE);
        return l;
    }

    private HBox buildNavControls() {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        prevBtn = Theme.ghostButton("◀  PREVIOUS");
        nextBtn = Theme.ghostButton("NEXT  ▶");
        editBtn = Theme.primaryButton("✎  MODIFY");
        deleteBtn = Theme.dangerButton("🗑  ERASE");

        prevBtn.setOnAction(e -> {
            currentIdx--;
            updateUI();
        });
        nextBtn.setOnAction(e -> {
            currentIdx++;
            updateUI();
        });
        editBtn.setOnAction(e -> enterEditMode());
        deleteBtn.setOnAction(e -> handleDelete());

        row.getChildren().addAll(prevBtn, nextBtn, new Separator(Orientation.VERTICAL) {
            {
                setStyle("-fx-opacity: 0.2;");
            }
        }, editBtn, deleteBtn);
        return row;
    }

    private void loadData() {
        allTests.clear();
        if (!DBManager.isConnected())
            return;
        try {
            ResultSet rs;
            String role = main.getRole();
            if ("DOCTOR".equals(role)) {
                int dId = DBManager.getDoctorIdByUsername(main.getUsername());
                rs = DBManager.getTestsForDoctorFull(dId);
            } else if ("PATIENT".equals(role)) {
                int pId = DBManager.getPatientIdByUsername(main.getUsername());
                rs = DBManager.getTestsForPatientFull(pId);
            } else {
                rs = DBManager.getAllTests();
            }

            while (rs != null && rs.next()) {
                allTests.add(new String[] {
                        rs.getString("test_id"),
                        rs.getString("test_code"),
                        rs.getString("type_name"),
                        rs.getString("type_code"),
                        String.valueOf(rs.getInt("urgency")),
                        rs.getString("status"),
                        rs.getString("p_first") + " " + rs.getString("p_last"),
                        rs.getString("d_first") + " " + rs.getString("d_last"),
                        rs.getString("request_date") != null ? rs.getString("request_date") : "N/A",
                        rs.getString("notes") != null ? rs.getString("notes") : ""
                });
            }
            currentIdx = allTests.isEmpty() ? -1 : 0;
            updateUI();
        } catch (Exception e) {
            Theme.showToast(main.getStage(), "Fault: Data Fetch Interrupted", Theme.COL_DANGER);
        }
    }

    private void updateUI() {
        if (allTests.isEmpty() || currentIdx < 0) {
            clearDisplay();
            counterLbl.setText("No records found in local database.");
            return;
        }

        String[] t = allTests.get(currentIdx);
        testCodeLbl.setText(t[1]);
        testTypeLbl.setText("[" + t[3] + "] " + t[2]);

        int urg = Integer.parseInt(t[4]);
        urgencyLbl.setText("★".repeat(urg) + "☆".repeat(5 - urg));
        urgencyLbl.setTextFill(Color.web(Theme.COL_CYAN));

        statusLbl.setText(t[5]);
        statusLbl.setTextFill(Color.web("COMPLETED".equals(t[5]) ? Theme.COL_SUCCESS : Theme.COL_WARNING));

        patientLbl.setText(t[6]);
        doctorLbl.setText(t[7]);
        dateLbl.setText(t[8]);
        notesLbl.setText(t[9]);

        counterLbl.setText("DISPLAYING RECORD " + (currentIdx + 1) + " OF " + allTests.size());

        prevBtn.setDisable(currentIdx == 0);
        nextBtn.setDisable(currentIdx == allTests.size() - 1);

        // Animation Triggers
        if ("COMPLETED".equals(t[5])) {
            util.SpecialEffects.playClinicalSuccess(statusLbl);
        } else if ("CANCELLED".equals(t[5]) || "PENDING_APPROVAL".equals(t[5])) {
            util.SpecialEffects.playClinicalFault(statusLbl);
        }
    }

    private void clearDisplay() {
        testCodeLbl.setText("");
        testTypeLbl.setText("");
        urgencyLbl.setText("");
        statusLbl.setText("");
        patientLbl.setText("");
        doctorLbl.setText("");
        dateLbl.setText("");
        notesLbl.setText("");
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
    }

    private void refreshTestTypes() {
        editTypeCombo.getItems().clear();
        try {
            ResultSet rs = DBManager.getAllTestTypes();
            while (rs.next()) {
                editTypeCombo.getItems().add("[" + rs.getString("test_code") + "] " + rs.getString("test_name"));
            }
        } catch (Exception ignored) {
        }
    }

    private void enterEditMode() {
        if (currentIdx < 0)
            return;
        isEditing = true;
        String[] t = allTests.get(currentIdx);

        testTypeLbl.setVisible(false);
        testTypeLbl.setManaged(false);
        editTypeCombo.setVisible(true);
        editTypeCombo.setManaged(true);
        editTypeCombo.setValue("[" + t[3] + "] " + t[2]);

        urgencyLbl.setVisible(false);
        urgencyLbl.setManaged(false);
        editUrgency = Integer.parseInt(t[4]);
        for (Label s : editStars) {
            s.setVisible(true);
            s.setManaged(true);
        }
        syncEditStars();

        editActions.setVisible(true);
        editActions.setManaged(true);
        prevBtn.setDisable(true);
        nextBtn.setDisable(true);
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
    }

    private void exitEditMode() {
        isEditing = false;
        testTypeLbl.setVisible(true);
        testTypeLbl.setManaged(true);
        editTypeCombo.setVisible(false);
        editTypeCombo.setManaged(false);
        urgencyLbl.setVisible(true);
        urgencyLbl.setManaged(true);
        for (Label s : editStars) {
            s.setVisible(false);
            s.setManaged(false);
        }
        editActions.setVisible(false);
        editActions.setManaged(false);
        updateUI();
        editBtn.setDisable(false);
        deleteBtn.setDisable(false);
    }

    private void syncEditStars() {
        for (int i = 0; i < 5; i++) {
            editStars[i].setTextFill(Color.web(i < editUrgency ? Theme.COL_CYAN : Theme.COL_BG_DEEP));
        }
    }

    private void handleSave() {
        try {
            String sel = editTypeCombo.getValue();
            String code = sel.substring(1, sel.indexOf("]"));

            int ttId = -1;
            ResultSet rs = DBManager.getAllTestTypes();
            while (rs.next()) {
                if (rs.getString("test_code").equals(code)) {
                    ttId = rs.getInt("test_type_id");
                    break;
                }
            }

            DBManager.updateTest(allTests.get(currentIdx)[0], ttId, editUrgency, allTests.get(currentIdx)[9]);
            Theme.showToast(main.getStage(), "Record Updated Successfully", Theme.COL_SUCCESS);
            loadData();
            exitEditMode();
        } catch (Exception e) {
            Theme.showError("Save Fault", e.getMessage());
        }
    }

    private void handleApprove() {
        if (currentIdx < 0)
            return;
        try {
            DBManager.approveLab(allTests.get(currentIdx)[0]);
            util.SpecialEffects.playClinicalSuccess(statusLbl);
            Theme.showToast(main.getStage(), "✔  Diagnostic Protocol Approved", Theme.COL_SUCCESS);
            loadData();
        } catch (Exception e) {
            Theme.showError("Approval Fault", e.getMessage());
        }
    }

    private void handleReject() {
        if (currentIdx < 0)
            return;
        try {
            DBManager.rejectLab(allTests.get(currentIdx)[0]);
            util.SpecialEffects.playClinicalFault(statusLbl);
            Theme.showToast(main.getStage(), "✖  Requisition Denied", Theme.COL_DANGER);
            loadData();
        } catch (Exception e) {
            Theme.showError("Rejection Fault", e.getMessage());
        }
    }

    private void handleDelete() {
        if (currentIdx < 0)
            return;
        if (Theme.showConfirm("Wipe Record", "Permanently erase record " + allTests.get(currentIdx)[1] + "?")) {
            try {
                DBManager.deleteTest(allTests.get(currentIdx)[0]);
                Theme.showToast(main.getStage(), "Data Purged", Theme.COL_SUCCESS);
                loadData();
            } catch (Exception e) {
                Theme.showError("Delete Fault", e.getMessage());
            }
        }
    }
}