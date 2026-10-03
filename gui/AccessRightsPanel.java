/*
 * MediLab Pro — AccessRightsPanel
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

public class AccessRightsPanel {

    private final MainWindow main;

    public AccessRightsPanel(MainWindow m) {
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

        VBox card = new VBox(25);
        card.setPadding(new Insets(40));
        card.setStyle(Theme.glassStyle());
        card.setEffect(Theme.cardShadow());

        // Header
        VBox hdr = new VBox(5);
        hdr.getChildren().addAll(
                Theme.titleLabel("Security Protocol: Access Matrix"),
                Theme.bodyLabel("Define hierarchical module permissions for global system roles"));
        card.getChildren().add(hdr);
        card.getChildren().add(Theme.styledSeparator());

        if (!DBManager.isConnected()) {
            card.getChildren().add(Theme.bodyLabel("System Offline: Database unreachable."));
            root.getChildren().add(card);
            sp.setContent(root);
            return sp;
        }

        try {
            String[] roles = { "ADMIN", "DOCTOR", "PATIENT" };
            String[] modules = { "Dashboard", "Registration", "Reports", "MasterDetails", "Administration" };

            for (String role : roles) {
                VBox roleBox = new VBox(15);
                roleBox.setPadding(new Insets(10, 0, 10, 0));

                Label roleTitle = Theme.sectionLabel("Role Identifier: " + role);
                roleBox.getChildren().add(roleTitle);

                GridPane grid = new GridPane();
                grid.setHgap(30);
                grid.setVgap(12);
                grid.setPadding(new Insets(15, 20, 15, 20));
                grid.setStyle(
                        "-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 12; -fx-border-color: "
                                + Theme.COL_BORDER + "; -fx-border-radius: 12;");

                // Headers
                grid.add(makeHeader("PROTOCOL MODULE"), 0, 0);
                grid.add(makeHeader("VISIBILITY"), 1, 0);
                grid.add(makeHeader("AUTHORITY"), 2, 0);

                for (int i = 0; i < modules.length; i++) {
                    final String mod = modules[i];
                    final String r = role;

                    ResultSet rs = DBManager.getAccessRights();
                    int canView = 0, canEdit = 0;
                    while (rs != null && rs.next()) {
                        if (rs.getString("role").equals(role) && rs.getString("module_name").equals(mod)) {
                            canView = rs.getInt("can_view");
                            canEdit = rs.getInt("can_edit");
                        }
                    }

                    Label modLbl = new Label(mod.toUpperCase());
                    modLbl.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 12));
                    modLbl.setTextFill(Color.WHITE);
                    modLbl.setMinWidth(200);

                    CheckBox viewCb = new CheckBox("VIEW");
                    viewCb.setSelected(canView == 1);
                    viewCb.setStyle(
                            "-fx-text-fill: " + Theme.COL_TEXT_SOFT + "; -fx-font-size: 10px; -fx-cursor: hand;");

                    CheckBox editCb = new CheckBox("EDIT");
                    editCb.setSelected(canEdit == 1);
                    editCb.setStyle(
                            "-fx-text-fill: " + Theme.COL_TEXT_SOFT + "; -fx-font-size: 10px; -fx-cursor: hand;");

                    viewCb.setOnAction(e -> update(r, mod, viewCb, editCb));
                    editCb.setOnAction(e -> update(r, mod, viewCb, editCb));

                    grid.add(modLbl, 0, i + 1);
                    grid.add(viewCb, 1, i + 1);
                    grid.add(editCb, 2, i + 1);
                }

                roleBox.getChildren().add(grid);
                card.getChildren().add(roleBox);
            }
        } catch (Exception e) {
            Theme.showError("Security Audit Fault", e.getMessage());
        }

        root.getChildren().add(card);
        sp.setContent(root);
        return sp;
    }

    private Label makeHeader(String t) {
        Label l = new Label(t);
        l.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BLACK, 10));
        l.setTextFill(Color.web(Theme.COL_CYAN));
        l.setOpacity(0.7);
        return l;
    }

    private void update(String role, String mod, CheckBox v, CheckBox e) {
        try {
            DBManager.updateAccessRight(role, mod, v.isSelected() ? 1 : 0, e.isSelected() ? 1 : 0);
            Theme.showToast(main.getStage(), "Security Policy Updated", Theme.COL_SUCCESS);
        } catch (Exception ex) {
            Theme.showError("Policy Update Error", ex.getMessage());
        }
    }
}