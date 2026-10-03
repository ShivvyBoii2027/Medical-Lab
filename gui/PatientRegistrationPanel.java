/*
 * MediLab Pro — PatientRegistrationPanel
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
import javafx.scene.text.*;

public class PatientRegistrationPanel {

    private final MainWindow main;
    private TextField firstNameField, middleNameField, lastNameField;
    private TextField ageField, phoneField, emailField, bloodGroupField;
    private TextField usernameField;
    private ComboBox<String> genderCombo;
    private TextArea addressArea, allergiesArea;
    private TextField emergencyArea;

    public PatientRegistrationPanel(MainWindow m) {
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
        Label icon = new Label("👤");
        icon.setFont(Font.font(40));
        icon.setEffect(Theme.glowShadow(Color.web(Theme.COL_CYAN)));
        VBox texts = new VBox(5,
                Theme.titleLabel("Patient Bio-Registry"),
                Theme.bodyLabel("Create advanced clinical profile for diagnostic tracking"));
        hdr.getChildren().addAll(icon, texts);
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        // ── Personal Info ──
        card.getChildren().add(Theme.sectionLabel("Biometric Data"));
        GridPane grid1 = new GridPane();
        grid1.setHgap(20);
        grid1.setVgap(20);

        firstNameField = Theme.styledField("First Name");
        middleNameField = Theme.styledField("Middle Name (Optional)");
        lastNameField = Theme.styledField("Surname");

        grid1.add(vField("GIVEN NAME *", firstNameField), 0, 0);
        grid1.add(vField("MIDDLE INITIAL", middleNameField), 1, 0);
        grid1.add(vField("SURNAME *", lastNameField), 2, 0);

        ageField = Theme.styledField("Age");
        genderCombo = Theme.styledCombo();
        genderCombo.getItems().addAll("Female", "Male", "Non-Binary", "System Neutral");
        genderCombo.setMaxWidth(Double.MAX_VALUE);

        grid1.add(vField("CHRONOLOGICAL AGE *", ageField), 0, 1);
        grid1.add(vField("GENDER IDENTITY *", genderCombo), 1, 1);

        bloodGroupField = Theme.styledField("e.g. O+, AB-");
        grid1.add(vField("BLOOD TYPE", bloodGroupField), 2, 1);

        usernameField = Theme.styledField("Login username (leave blank if no portal access)");
        grid1.add(vField("PORTAL USERNAME (OPTIONAL)", usernameField), 0, 2, 3, 1);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setHgrow(Priority.ALWAYS);
        grid1.getColumnConstraints().addAll(cc, cc, cc);
        card.getChildren().add(grid1);

        // ── Contact Info ──
        card.getChildren().add(Theme.styledSeparator());
        card.getChildren().add(Theme.sectionLabel("Communication & Address"));
        GridPane grid2 = new GridPane();
        grid2.setHgap(20);
        grid2.setVgap(20);

        phoneField = Theme.styledField("+1 (xxx) xxx-xxxx");
        emailField = Theme.styledField("protocol@medilab.net");
        addressArea = new TextArea();
        addressArea.setPromptText("Physical Residency Address");
        addressArea.setPrefHeight(80);
        addressArea.setStyle("-fx-control-inner-background: " + Theme.COL_BG_DEEP
                + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-border-color: " + Theme.COL_BORDER + ";");

        grid2.add(vField("PRIMARY CONTACT", phoneField), 0, 0);
        grid2.add(vField("ENCRYPTED EMAIL", emailField), 1, 0);
        grid2.add(vField("RESIDENCY", addressArea), 0, 1, 2, 1);

        grid2.getColumnConstraints().addAll(cc, cc);
        card.getChildren().add(grid2);

        // ── Clinical Info ──
        card.getChildren().add(Theme.styledSeparator());
        card.getChildren().add(Theme.sectionLabel("Medical Risk & Emergency"));
        GridPane grid3 = new GridPane();
        grid3.setHgap(20);
        grid3.setVgap(20);

        allergiesArea = new TextArea();
        allergiesArea.setPromptText("Known Hypersensitivities");
        allergiesArea.setPrefHeight(80);
        allergiesArea.setStyle(addressArea.getStyle());

        emergencyArea = Theme.styledField("Emergency Contact Name & Phone");

        grid3.add(vField("ALLERGIC RECORDS", allergiesArea), 0, 0);
        grid3.add(vField("EMERGENCY PROTOCOL", emergencyArea), 1, 0);

        grid3.getColumnConstraints().addAll(cc, cc);
        card.getChildren().add(grid3);

        // ── Footer Actions ──
        card.getChildren().add(Theme.styledSeparator());
        HBox actions = new HBox(15);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button clear = Theme.ghostButton("RESET ENGINE");
        clear.setOnAction(e -> clearFields());

        Button submit = Theme.primaryButton("EXECUTE REGISTRATION");
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

    private void clearFields() {
        firstNameField.clear();
        middleNameField.clear();
        lastNameField.clear();
        ageField.clear();
        bloodGroupField.clear();
        phoneField.clear();
        emailField.clear();
        addressArea.clear();
        allergiesArea.clear();
        emergencyArea.clear();
        usernameField.clear();
        genderCombo.setValue(null);
    }

    private void handleRegistration() {
        String fn = firstNameField.getText().trim();
        String mn = middleNameField.getText().trim();
        String ln = lastNameField.getText().trim();
        String ageStr = ageField.getText().trim();
        String gender = genderCombo.getValue();
        String username = usernameField.getText().trim();

        if (fn.isEmpty() || ln.isEmpty() || ageStr.isEmpty() || gender == null) {
            Theme.showToast(main.getStage(), "Required Biometrics Missing", Theme.COL_DANGER);
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            // Link username if provided, else store null
            String linkedUser = username.isEmpty() ? null : username;
            DBManager.addPatientWithUser(fn, mn, ln, gender, age, phoneField.getText(), emailField.getText(),
                    addressArea.getText(), bloodGroupField.getText(), allergiesArea.getText(),
                    emergencyArea.getText(), linkedUser);
            Theme.showToast(main.getStage(),
                    "Patient Profile Synchronized" + (linkedUser != null ? " (linked to: " + linkedUser + ")" : ""),
                    Theme.COL_SUCCESS);
            clearFields();
        } catch (NumberFormatException e) {
            Theme.showToast(main.getStage(), "Invalid Age Metric", Theme.COL_DANGER);
        } catch (Exception e) {
            Theme.showError("Registry Fault", e.getMessage());
        }
    }
}