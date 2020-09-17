package render.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class SettingsController  {

    private Stage thisStage;
    private final FileTrackerController fileTrackerController;

    public SettingsController(FileTrackerController fileTrackerController) {
        this.fileTrackerController = fileTrackerController;
        thisStage = new Stage();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/settings.fxml"));
            loader.setController(this);
            thisStage.setScene(new Scene(loader.load()));
            thisStage.setTitle("Report settings");
            thisStage.initModality(Modality.APPLICATION_MODAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void showStage() {
        thisStage.showAndWait();
    }


    @FXML
    private CheckBox user;
    @FXML
    private CheckBox totalTime;
    @FXML
    private CheckBox averageTime;
    @FXML
    private CheckBox renderFilesSinceReport;
    @FXML
    private CheckBox totalRenderFiles;
    @FXML
    private CheckBox extensions;
    @FXML
    private CheckBox totalSize;
    @FXML
    private CheckBox freeSpace;
    @FXML
    private Button btnSave;

    @FXML
    void handleSaveButton(ActionEvent event) {
        fileTrackerController.checkBoxSettings.put("user", user.isSelected());
        fileTrackerController.checkBoxSettings.put("totalTime", totalTime.isSelected());
        fileTrackerController.checkBoxSettings.put("averageTime", averageTime.isSelected());
        fileTrackerController.checkBoxSettings.put("renderFilesSinceReport", renderFilesSinceReport.isSelected());
        fileTrackerController.checkBoxSettings.put("totalRenderFiles", totalRenderFiles.isSelected());
        fileTrackerController.checkBoxSettings.put("extensions", extensions.isSelected());
        fileTrackerController.checkBoxSettings.put("totalSize", totalSize.isSelected());
        fileTrackerController.checkBoxSettings.put("freeSpace", freeSpace.isSelected());
        closeStage(event);
    }



    private void closeStage(ActionEvent event) {
        Node  source = (Node)  event.getSource();
        Stage stage  = (Stage) source.getScene().getWindow();
        stage.close();
    }

}