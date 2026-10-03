package gui;

import db.DBManager;
import util.Theme;
import util.ThemeManager;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.*;

public class SettingsPanel {
    private final MainWindow main;
    private ComboBox<String> modeCombo;
    private ColorPicker colorPicker;
    private Slider sizeSlider;
    private String selectedBgPath = null;
    private Label bgStatus;

    public SettingsPanel(MainWindow m) {
        this.main = m;
        this.selectedBgPath = ThemeManager.getInstance().getBgImage();
    }

    public Node build() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.TOP_LEFT);

        Label title = Theme.titleLabel("System Configuration");
        Label sub = Theme.bodyLabel("Personalize your MediLab environment and aesthetic preferences.");
        root.getChildren().addAll(title, sub, Theme.styledSeparator());

        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(25);
        grid.setPadding(new Insets(20, 0, 0, 0));

        // 1. Theme Mode
        modeCombo = new ComboBox<>();
        modeCombo.getItems().addAll("GOATED", "DARK", "LIGHT");
        modeCombo.setValue(ThemeManager.getInstance().getMode());
        modeCombo.setPrefWidth(200);
        grid.add(Theme.formLabel("VISUAL MODE"), 0, 0);
        grid.add(modeCombo, 1, 0);

        // 2. Accent Color
        colorPicker = new ColorPicker(Color.web(ThemeManager.getInstance().getAccentColor()));
        colorPicker.setPrefWidth(200);
        grid.add(Theme.formLabel("ACCENT PALETTE"), 0, 1);
        grid.add(colorPicker, 1, 1);

        // 3. Font Size
        sizeSlider = new Slider(10, 20, ThemeManager.getInstance().getFontSize());
        sizeSlider.setShowTickLabels(true);
        sizeSlider.setShowTickMarks(true);
        sizeSlider.setMajorTickUnit(2);
        sizeSlider.setMinorTickCount(1);
        grid.add(Theme.formLabel("TYPOGRAPHY SIZE"), 0, 2);
        grid.add(sizeSlider, 1, 2);

        // 4. Background Image
        bgStatus = new Label(
                selectedBgPath != null ? "Current: " + new File(selectedBgPath).getName() : "Using System Default");
        bgStatus.setTextFill(Color.GRAY);
        bgStatus.setFont(Font.font(11));

        Button uploadBtn = Theme.ghostButton("📁 UPLOAD IMAGE");
        uploadBtn.setOnAction(e -> handleFileUpload());

        Button clearBtn = Theme.ghostButton("🗑️ CLEAR");
        clearBtn.setStyle(clearBtn.getStyle() + "-fx-text-fill: " + Theme.COL_DANGER + ";");
        clearBtn.setOnAction(e -> {
            selectedBgPath = null;
            bgStatus.setText("System Default Restoration Scheduled");
        });

        HBox bgActions = new HBox(10, uploadBtn, clearBtn);
        bgActions.setAlignment(Pos.CENTER_LEFT);

        grid.add(Theme.formLabel("CUSTOM BACKGROUND"), 0, 3);
        grid.add(new VBox(5, bgActions, bgStatus), 1, 3);

        root.getChildren().add(grid);

        // Save Button
        Button saveBtn = Theme.primaryButton("APPLY & PERSIST SETTINGS");
        saveBtn.setPadding(new Insets(12, 30, 12, 30));
        saveBtn.setOnAction(e -> handleSave());

        HBox footer = new HBox(saveBtn);
        footer.setPadding(new Insets(40, 0, 0, 0));
        root.getChildren().add(footer);

        return root;
    }

    private void handleFileUpload() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Background Image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fc.showOpenDialog(main.getStage());
        if (selectedFile != null) {
            try {
                File uploadDir = new File("uploads/backgrounds");
                if (!uploadDir.exists())
                    uploadDir.mkdirs();

                String ext = selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."));
                File dest = new File(uploadDir, main.getUser() + "_bg" + ext);
                Files.copy(selectedFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                this.selectedBgPath = dest.getAbsolutePath();
                bgStatus.setText("Ready to Apply: " + selectedFile.getName());
                bgStatus.setTextFill(Color.web(Theme.COL_CYAN));
            } catch (Exception e) {
                Theme.showError("Upload Fault", "Failed to stage image: " + e.getMessage());
            }
        }
    }

    private void handleSave() {
        try {
            String mode = modeCombo.getValue();
            String color = "#" + Integer.toHexString(colorPicker.getValue().hashCode()).substring(0, 6);
            int size = (int) sizeSlider.getValue();

            ThemeManager.getInstance().setMode(mode);
            ThemeManager.getInstance().setAccentColor(color);
            ThemeManager.getInstance().setFontSize(size);
            ThemeManager.getInstance().setBgImage(selectedBgPath);

            DBManager.saveUserSettings(main.getUser(), mode, color, size, selectedBgPath);

            Theme.showToast(main.getStage(), "Security Protocol: Theme Synchronized", Theme.COL_SUCCESS);

            // Refresh the entire Window to apply new Theme
            main.refreshTheme();

        } catch (Exception e) {
            Theme.showError("Sync Fault", "Failed to persist settings: " + e.getMessage());
        }
    }
}
