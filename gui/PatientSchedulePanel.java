/*
 * MediLab Pro — PatientSchedulePanel
 * Goated UI for Clinical Appointment Booking
 * Doctors/Admins get read-only view; Patients can book.
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

public class PatientSchedulePanel {

    private final MainWindow main;
    private VBox appointmentsList;

    public PatientSchedulePanel(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox root = new VBox(30);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: transparent;");

        // Header
        VBox header = new VBox(10);
        header.setPadding(new Insets(30));
        header.setStyle(Theme.glassStyle());
        header.setEffect(Theme.cardShadow());

        String role = main.getRole();
        boolean isPatient = "PATIENT".equals(role);

        header.getChildren().addAll(
                Theme.titleLabel("Clinical Appointments"),
                Theme.bodyLabel(isPatient
                        ? "Manage your medical sessions and schedule new consultations"
                        : "Read-only view of scheduled patient appointments"));
        root.getChildren().add(header);

        HBox split = new HBox(25);

        // ── Active Schedule (Left) ──
        VBox left = new VBox(20);
        left.setPadding(new Insets(25));
        left.setStyle(Theme.glassStyle());
        left.setEffect(Theme.cardShadow());
        HBox.setHgrow(left, Priority.ALWAYS);

        left.getChildren().addAll(Theme.sectionLabel("Upcoming Sessions"), Theme.styledSeparator());

        appointmentsList = new VBox(12);
        loadAppointments();
        left.getChildren().add(appointmentsList);

        split.getChildren().add(left);

        // ── Booking Module (Right) — Patients only ──
        if (isPatient) {
            VBox right = new VBox(20);
            right.setPrefWidth(350);
            right.setPadding(new Insets(25));
            right.setStyle(Theme.glassStyle());
            right.setEffect(Theme.cardShadow());

            right.getChildren().addAll(Theme.sectionLabel("New Requisition"), Theme.styledSeparator());

            ComboBox<String> doctorCombo = Theme.styledCombo();
            doctorCombo.setPromptText("Select Specialist");
            doctorCombo.setMaxWidth(Double.MAX_VALUE);
            loadDoctors(doctorCombo);

            DatePicker datePicker = new DatePicker();
            datePicker.setMaxWidth(Double.MAX_VALUE);
            datePicker.setStyle("-fx-control-inner-background: " + Theme.COL_BG_DEEP + "; -fx-text-fill: white;");

            TextField timeField = Theme.styledField("HH:mm (e.g. 14:30)");
            TextArea reasonArea = new TextArea();
            reasonArea.setPromptText("Reason for appointment...");
            reasonArea.setPrefHeight(80);
            reasonArea.setStyle("-fx-control-inner-background: " + Theme.COL_BG_DEEP
                    + "; -fx-text-fill: white; -fx-border-color: " + Theme.COL_BORDER + ";");

            Button submit = Theme.primaryButton("BOOK SESSION");
            submit.setMaxWidth(Double.MAX_VALUE);
            submit.setOnAction(e -> {
                try {
                    String docSel = doctorCombo.getValue();
                    if (docSel == null || datePicker.getValue() == null || timeField.getText().isEmpty()) {
                        Theme.showToast(main.getStage(), "Validation Failed: Required Fields Missing",
                                Theme.COL_DANGER);
                        return;
                    }

                    int docId = Integer.parseInt(docSel.substring(docSel.indexOf("[") + 1, docSel.indexOf("]")));
                    String timeStr = timeField.getText().trim();
                    if (!timeStr.matches("\\d{2}:\\d{2}")) {
                        Theme.showToast(main.getStage(), "Invalid time format — use HH:mm (e.g. 14:30)",
                                Theme.COL_DANGER);
                        return;
                    }
                    String dateTime = datePicker.getValue().toString() + " " + timeStr + ":00";

                    int pId = main.getLinkedId();
                    if (pId == -1) {
                        pId = DBManager.getPatientIdByUsername(main.getUsername());
                    }
                    
                    if (pId == -1) {
                        Theme.showError("Identity Fault",
                                "Your account is not yet linked to a patient record.\n" +
                                        "Please contact your administrator to resolve this.");
                        return;
                    }

                    DBManager.addAppointment(pId, docId, dateTime, reasonArea.getText());
                    util.SpecialEffects.playClinicalSuccess(submit);
                    Theme.showToast(main.getStage(), "✔  Appointment Booked Successfully", Theme.COL_SUCCESS);
                    loadAppointments();
                    reasonArea.clear();
                    timeField.clear();
                } catch (Exception ex) {
                    Theme.showError("Booking Fault", ex.getMessage());
                }
            });

            right.getChildren().addAll(
                    vField("TARGET SPECIALIST", doctorCombo),
                    vField("DATE", datePicker),
                    vField("TIME", timeField),
                    vField("REASON", reasonArea),
                    submit);

            split.getChildren().add(right);
        }

        root.getChildren().add(split);
        sp.setContent(root);
        return sp;
    }

    private VBox vField(String l, Control f) {
        VBox b = new VBox(8);
        b.getChildren().addAll(Theme.formLabel(l), f);
        return b;
    }

    private void loadAppointments() {
        appointmentsList.getChildren().clear();
        try {
            String role = main.getRole();
            ResultSet rs;

            if ("PATIENT".equals(role)) {
                int pId = DBManager.getPatientIdByUsername(main.getUsername());
                if (pId == -1) {
                    appointmentsList.getChildren()
                            .add(Theme.bodyLabel("⚠ Your account is not linked to a patient record. Contact admin."));
                    return;
                }
                rs = DBManager.getAppointmentsForPatient(pId);
            } else if ("DOCTOR".equals(role)) {
                int dId = DBManager.getDoctorIdByUsername(main.getUsername());
                rs = (dId != -1) ? DBManager.getScheduledQueue(dId) : null;
            } else {
                // Admin: show all appointments
                rs = DBManager.getAllAppointments();
            }

            boolean empty = true;
            while (rs != null && rs.next()) {
                empty = false;
                HBox card = new HBox(15);
                card.setPadding(new Insets(15));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.03); -fx-background-radius: 10; -fx-border-color: rgba(255,255,255,0.05); -fx-border-radius: 10;");

                VBox details = new VBox(4);
                String primary, secondary;

                if ("PATIENT".equals(role)) {
                    primary = "Dr. " + rs.getString("d_first") + " " + rs.getString("d_last");
                    secondary = rs.getString("specialization");
                } else if ("DOCTOR".equals(role)) {
                    primary = rs.getString("first_name") + " " + rs.getString("last_name");
                    secondary = rs.getString("reason") != null ? rs.getString("reason") : "No reason specified";
                } else {
                    // Admin: show patient name and doctor
                    primary = rs.getString("patient_name");
                    secondary = "Dr. " + rs.getString("doctor_name") + " — "
                            + (rs.getString("reason") != null ? rs.getString("reason") : "");
                }

                Label primaryLbl = new Label(primary);
                primaryLbl.setFont(Font.font("Inter", FontWeight.BOLD, 14));
                primaryLbl.setTextFill(Color.WHITE);

                Label secondaryLbl = new Label(secondary);
                secondaryLbl.setFont(Font.font("Inter", FontWeight.BOLD, 10));
                secondaryLbl.setTextFill(Color.web(Theme.COL_CYAN));

                details.getChildren().addAll(primaryLbl, secondaryLbl);

                Label date = new Label(rs.getString("app_date"));
                date.setFont(Font.font("Monospaced", FontWeight.BOLD, 12));
                date.setTextFill(Color.web(Theme.COL_TEXT_SOFT));

                Region r = new Region();
                HBox.setHgrow(r, Priority.ALWAYS);

                String st = rs.getString("status");
                Label status = new Label("  " + st + "  ");
                status.setFont(Font.font("Inter", FontWeight.BLACK, 10));
                String color = "PENDING".equals(st) ? Theme.COL_WARNING
                        : ("APPROVED".equals(st) || "COMPLETED".equals(st)) ? Theme.COL_SUCCESS
                                : Theme.COL_DANGER;
                status.setTextFill(Color.web(color));
                status.setPadding(new Insets(4, 10, 4, 10));
                status.setStyle("-fx-background-color: " + color + "22; -fx-background-radius: 12;");

                card.getChildren().addAll(details, date, r, status);

                // Add Actions for Doctors/Admins
                if (!"PATIENT".equals(role)) {
                    final int currentAppId = rs.getInt("app_id");
                    HBox itemActions = new HBox(8);
                    itemActions.setAlignment(Pos.CENTER_RIGHT);
                    
                    if ("PENDING".equals(st)) {
                        Button appBtn = Theme.successButton("✔");
                        appBtn.setTooltip(new Tooltip("Approve Session"));
                        appBtn.setOnAction(e -> {
                            try {
                                DBManager.updateAppointmentStatus(currentAppId, "APPROVED");
                                loadAppointments();
                            } catch (Exception ex) { Theme.showError("Update Fault", ex.getMessage()); }
                        });
                        itemActions.getChildren().add(appBtn);
                    } else if ("APPROVED".equals(st)) {
                        Button compBtn = Theme.primaryButton("DONE");
                        compBtn.setTooltip(new Tooltip("Mark as Completed"));
                        compBtn.setOnAction(e -> {
                            try {
                                DBManager.updateAppointmentStatus(currentAppId, "COMPLETED");
                                loadAppointments();
                            } catch (Exception ex) { Theme.showError("Update Fault", ex.getMessage()); }
                        });
                        itemActions.getChildren().add(compBtn);
                    }

                    if (!"CANCELLED".equals(st) && !"COMPLETED".equals(st)) {
                        Button cancBtn = Theme.dangerButton("✖");
                        cancBtn.setTooltip(new Tooltip("Cancel Session"));
                        cancBtn.setOnAction(e -> {
                            try {
                                DBManager.updateAppointmentStatus(currentAppId, "CANCELLED");
                                loadAppointments();
                            } catch (Exception ex) { Theme.showError("Update Fault", ex.getMessage()); }
                        });
                        itemActions.getChildren().add(cancBtn);
                    }
                    card.getChildren().add(itemActions);
                }

                appointmentsList.getChildren().add(card);
            }

            if (empty) {
                appointmentsList.getChildren().add(Theme.bodyLabel("No upcoming medical sessions synchronized."));
            }
        } catch (Exception e) {
            appointmentsList.getChildren().add(Theme.bodyLabel("Schedule fault: " + e.getMessage()));
        }
    }

    private void loadDoctors(ComboBox<String> combo) {
        try {
            ResultSet rs = DBManager.getAllDoctors();
            while (rs != null && rs.next()) {
                combo.getItems().add("[" + rs.getInt("doctor_id") + "] Dr. " + rs.getString("last_name") + " ("
                        + rs.getString("specialization") + ")");
            }
        } catch (Exception ignored) {
        }
    }
}
