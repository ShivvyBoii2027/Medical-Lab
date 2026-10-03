package gui;

import util.Theme;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;

public class PatientDashboard {
    private final MainWindow main;

    public PatientDashboard(MainWindow m) {
        this.main = m;
    }

    public Node build() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_LEFT);

        VBox welcome = new VBox(10);
        welcome.setPadding(new Insets(35));
        welcome.setStyle(Theme.glassStyle());
        welcome.getChildren().addAll(
                Theme.titleLabel("Patient Personal Health Hub"),
                Theme.bodyLabel("Security Handshake Complete. Welcome back to your personal clinical vault, "
                        + main.getDisplayName() + "."));
        root.getChildren().add(welcome);

        HBox stats = new HBox(20);
        stats.getChildren().addAll(
                pStatCard("COMPLETED ANALYSES", "14", Theme.COL_SUCCESS),
                pStatCard("PENDING RESULTS", "01", Theme.COL_WARNING),
                pStatCard("HEALTH SCORE", "92%", Theme.COL_CYAN));
        for (Node n : stats.getChildren())
            HBox.setHgrow(n, Priority.ALWAYS);
        root.getChildren().add(stats);

        HBox bottomSplit = new HBox(20);
        VBox history = new VBox(15);
        history.setPadding(new Insets(25));
        history.setStyle(Theme.glassStyle());
        history.getChildren().addAll(Theme.sectionLabel("My Recent Diagnostic Results"), Theme.styledSeparator());

        // Mocking patient history
        VBox hList = new VBox(12);
        String[][] tests = {
                { "Apr 15", "CBC Blood Panel", "NORMAL" },
                { "Mar 02", "Lipid Profile", "NORMAL" },
                { "Jan 20", "Urinalysis", "CHECK-UP" }
        };
        for (String[] t : tests) {
            HBox row = new HBox(20);
            row.setPadding(new Insets(12));
            row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 10;");
            row.getChildren().addAll(
                    new Label(t[0]) {
                        {
                            setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                            setMinWidth(80);
                        }
                    },
                    new Label(t[1]) {
                        {
                            setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                            setMinWidth(180);
                        }
                    },
                    new Label(t[2]) {
                        {
                            setStyle("-fx-text-fill: " + (t[2].equals("NORMAL") ? Theme.COL_SUCCESS : Theme.COL_WARNING)
                                    + "; -fx-font-weight: black; -fx-font-size: 10px;");
                        }
                    });
            hList.getChildren().add(row);
        }
        history.getChildren().add(hList);

        VBox instructions = new VBox(15);
        instructions.setPrefWidth(300);
        instructions.setPadding(new Insets(25));
        instructions.setStyle(Theme.glassStyle());
        instructions.getChildren().addAll(Theme.sectionLabel("Medical Directives"), Theme.styledSeparator());
        instructions.getChildren().add(new Label("● Fast for 12h before next test") {
            {
                setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            }
        });
        instructions.getChildren().add(new Label("● Maintain hydration protocols") {
            {
                setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");
            }
        });

        HBox.setHgrow(history, Priority.ALWAYS);
        bottomSplit.getChildren().addAll(history, instructions);
        root.getChildren().add(bottomSplit);

        return new ScrollPane(root) {
            {
                setFitToWidth(true);
                setStyle("-fx-background: transparent; -fx-background-color: transparent;");
            }
        };
    }

    private VBox pStatCard(String t, String v, String c) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(25));
        card.setStyle("-fx-background-color: " + Theme.cBgMid() + "; -fx-background-radius: 20; -fx-border-color: " + c
                + "33;");
        Label tl = Theme.sectionLabel(t);
        tl.setStyle("-fx-font-size: 9px;");
        Label vl = new Label(v);
        vl.setFont(Font.font("Inter", FontWeight.BLACK, 30));
        vl.setTextFill(Color.web(c));
        card.getChildren().addAll(tl, vl);
        return card;
    }
}
