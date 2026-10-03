/*
 * MediLab Pro — DoctorRegistrationPanel
 * Goated Revision: Fixed Column Logic & Professional Tech UI
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

public class DoctorRegistrationPanel {

    private final MainWindow main;
    private TextField firstNameField, middleNameField, lastNameField;
    private TextField licenseField, qualificationField, experienceField, departmentField, feeField;
    private TextField emailField, phoneField, daysField;
    private ComboBox<String> specCombo;

    public DoctorRegistrationPanel(MainWindow m) {
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
        Label icon = new Label("👨‍⚕️");
        icon.setFont(Font.font(40));
        icon.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN)));
        VBox texts = new VBox(5,
                Theme.titleLabel("Specialist Onboarding"),
                Theme.bodyLabel("Register medical expertise into the MediLab Intelligence Network"));
        hdr.getChildren().addAll(icon, texts);
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        // ── Bio Data ──
        card.getChildren().add(Theme.sectionLabel("Personnel Identity"));
        GridPane grid1 = new GridPane();
        grid1.setHgap(20);
        grid1.setVgap(20);

        firstNameField = Theme.styledField("First Name");
        middleNameField = Theme.styledField("M.I.");
        lastNameField = Theme.styledField("Surname");

        grid1.add(vField("GIVEN NAME *", firstNameField), 0, 0);
        grid1.add(vField("INITIAL", middleNameField), 1, 0);
        grid1.add(vField("SURNAME *", lastNameField), 2, 0);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        grid1.getColumnConstraints().addAll(cc, cc, cc);
        card.getChildren().add(grid1);

        // ── Clinical Specialization ──
        card.getChildren().add(Theme.styledSeparator());
        card.getChildren().add(Theme.sectionLabel("Professional Expertise"));
        GridPane grid2 = new GridPane();
        grid2.setHgap(20);
        grid2.setVgap(20);

        specCombo = Theme.styledCombo();
        loadSpecs();
        specCombo.setMaxWidth(Double.MAX_VALUE);

        licenseField = Theme.styledField("License ID #");
        departmentField = Theme.styledField("e.g. Cardiology");

        grid2.add(vField("SPECIALIZATION AREA *", specCombo), 0, 0);
        grid2.add(vField("MEDICAL LICENSE *", licenseField), 1, 0);
        grid2.add(vField("DEPARTMENT UNIT", departmentField), 2, 0);

        grid2.getColumnConstraints().addAll(cc, cc, cc);
        card.getChildren().add(grid2);

        // ── Experience & Compensation ──
        GridPane grid3 = new GridPane();
        grid3.setHgap(20);
        grid3.setVgap(20);

        qualificationField = Theme.styledField("e.g. MD, PhD, MBBS");
        experienceField = Theme.styledField("Years");
        feeField = Theme.styledField("USD / Fixed");

        grid3.add(vField("ACADEMIC QUALIFICATIONS", qualificationField), 0, 0);
        grid3.add(vField("YRS OF SERVICE", experienceField), 1, 0);
        grid3.add(vField("CONSULTATION SCALE", feeField), 2, 0);

        grid3.getColumnConstraints().addAll(cc, cc, cc);
        card.getChildren().add(grid3);

        // ── Contact & Availability ──
        card.getChildren().add(Theme.styledSeparator());
        card.getChildren().add(Theme.sectionLabel("Communication Hub"));
        GridPane grid4 = new GridPane();
        grid4.setHgap(20);
        grid4.setVgap(20);

        emailField = Theme.styledField("staff@medilab.net");
        phoneField = Theme.styledField("Secure Line");
        daysField = Theme.styledField("e.g. Mon, Wed, Fri");

        grid4.add(vField("SECURE EMAIL", emailField), 0, 0);
        grid4.add(vField("DIRECT LINE", phoneField), 1, 0);
        grid4.add(vField("AVAILABILITY WINDOW", daysField), 2, 0);

        grid4.getColumnConstraints().addAll(cc, cc, cc);
        card.getChildren().add(grid4);

        // ── Actions ──
        card.getChildren().add(Theme.styledSeparator());
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button clear = Theme.ghostButton("SCRUB FORM");
        clear.setOnAction(e -> clearFields());

        Button submit = Theme.primaryButton("AUTHORIZE STAFF ENTRY");
        submit.setOnAction(e -> handleRegistration());

        actions.getChildren().addAll(clear, submit);
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

    private void loadSpecs() {
        specCombo.getItems().clear();
        try {
            ResultSet rs = DBManager.getAllSpecializations();
            while (rs != null && rs.next()) {
                specCombo.getItems().add(rs.getString("specialization_name"));
            }
        } catch (Exception ignored) {
        }
    }

    private void clearFields() {
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        licenseField.clear();
        qualificationField.clear();
        experienceField.clear();
        departmentField.clear();
        feeField.clear();
        emailField.clear();
        phoneField.clear();
        daysField.clear();
        specCombo.setValue(null);
    }

    private void handleRegistration() {
        String fn = firstNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String spec = specCombo.getValue();
        String lic = licenseField.getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || spec == null || lic.isEmpty()) {
            Theme.showToast(main.getStage(), "Required Logistics Missing", Theme.COL_DANGER);
            return;
        }

        try {
            int exp = experienceField.getText().isEmpty() ? 0 : Integer.parseInt(experienceField.getText().trim());
            double fee = feeField.getText().isEmpty() ? 0.0 : Double.parseDouble(feeField.getText().trim());

            DBManager.addDoctor(fn, middleNameField.getText(), ln, spec, lic,
                    qualificationField.getText(), exp, departmentField.getText(), fee,
                    emailField.getText(), phoneField.getText(), daysField.getText());

            Theme.showToast(main.getStage(), "Specialist Authenticated into System", Theme.COL_SUCCESS);
            clearFields();
        } catch (Exception e) {
            Theme.showError("Registration Error", e.getMessage());
        }
    }
}