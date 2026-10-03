/*
 * MediLab Pro — RegisterScreen
 * Goated Account Creation Interface
 */
package gui;

import db.DBManager;
import util.Theme;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RegisterScreen {

    private final Stage stage;
    private final Stage parentStage;
    private ComboBox<String> roleCombo;
    private TextField userField, emailField, phoneField, fnField, lnField;
    private PasswordField passField;
    private TextField roleSpecificField; // Specialization for doctors, Age for patients
    private Label roleSpecificLabel; //I love you avika

    public RegisterScreen(Stage parent) {
        this.parentStage = parent;
        this.stage = new Stage();
        stage.setTitle("MediLab — Create New Identity");
    }

    public void show() {
        VBox root = new VBox(0);
        root.setBackground(Theme.mainBackground());
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40));

        VBox card = new VBox(25);
        card.setMaxWidth(500);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        // Header
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.getChildren().addAll(
                Theme.titleLabel("New Entity Registration"),
                Theme.bodyLabel("Initialize your clinical credentials in the MediLab ecosystem"));
        card.getChildren().addAll(header, Theme.styledSeparator());

        // Form
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);

        roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("PATIENT", "DOCTOR");
        roleCombo.setValue("PATIENT");
        roleCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-padding: 5;");

        grid.add(Theme.formLabel("IDENTITY TYPE"), 0, 0);
        grid.add(roleCombo, 1, 0);

        userField = Theme.styledField("Account Username");
        passField = Theme.styledPassword("Secret Password");
        emailField = Theme.styledField("Primary Email");
        phoneField = Theme.styledField("Contact Phone");
        fnField = Theme.styledField("First Name");
        lnField = Theme.styledField("Last Name");
        roleSpecificField = Theme.styledField("Enter Details");
        roleSpecificLabel = Theme.formLabel("AGE (YEARS)");

        grid.add(Theme.formLabel("USERNAME"), 0, 1);
        grid.add(userField, 1, 1);
        grid.add(Theme.formLabel("PASSWORD"), 0, 2);
        grid.add(passField, 1, 2);
        grid.add(Theme.formLabel("EMAIL"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(Theme.formLabel("PHONE"), 0, 4);
        grid.add(phoneField, 1, 4);
        grid.add(Theme.formLabel("FIRST NAME"), 0, 5);
        grid.add(fnField, 1, 5);
        grid.add(Theme.formLabel("LAST NAME"), 0, 6);
        grid.add(lnField, 1, 6);
        grid.add(roleSpecificLabel, 0, 7);
        grid.add(roleSpecificField, 1, 7);

        roleCombo.setOnAction(e -> {
            if ("DOCTOR".equals(roleCombo.getValue())) {
                roleSpecificLabel.setText("SPECIALIZATION");
                roleSpecificField.setPromptText("e.g. Cardiology");
            } else {
                roleSpecificLabel.setText("AGE (YEARS)");
                roleSpecificField.setPromptText("e.g. 25");
            }
        });

        card.getChildren().add(grid);

        // Footer Buttons
        HBox btns = new HBox(15);
        btns.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = Theme.ghostButton("ABORT");
        cancel.setOnAction(e -> stage.close());

        Button join = Theme.primaryButton("INITIALIZE ACCOUNT");
        join.setOnAction(e -> handleRegister());
        btns.getChildren().addAll(cancel, join);

        card.getChildren().addAll(Theme.styledSeparator(), btns);

        root.getChildren().add(card);
        Scene scene = new Scene(root, 700, 850);
        stage.setScene(scene);
        stage.show();
    }

    private void handleRegister() {
        try {
            String role = roleCombo.getValue();
            String u = userField.getText().trim();
            String p = passField.getText();
            String e = emailField.getText().trim();
            String ph = phoneField.getText().trim();
            String fn = fnField.getText().trim();
            String ln = lnField.getText().trim();
            String specOrAge = roleSpecificField.getText().trim();

            if (u.isEmpty() || p.isEmpty() || e.isEmpty() || fn.isEmpty() || ln.isEmpty()) {
                Theme.showToast(stage, "Incomplete Data Protocol", Theme.COL_DANGER);
                return;
            }

            if ("PATIENT".equals(role)) {
                int age = Integer.parseInt(specOrAge);
                int pid = DBManager.registerPatientAccount(u, p, e, ph, fn, ln, "M", age);
                Theme.showInfo("Registration Successful", "Patient ID: " + pid + "\nUsername: " + u
                        + "\n\nPlease note your Patient ID for faster secure login.");
            } else {
                DBManager.registerDoctorAccount(u, p, e, ph, fn, ln, specOrAge);
                Theme.showInfo("Success", "Doctor account established. You may now access the portal.");
            }
            stage.close();

        } catch (NumberFormatException ex) {
            Theme.showToast(stage, "Age must be a numeric value", Theme.COL_WARNING);
        } catch (Exception ex) {
            Theme.showError("Registration Fault", ex.getMessage());
        }
    }
}
