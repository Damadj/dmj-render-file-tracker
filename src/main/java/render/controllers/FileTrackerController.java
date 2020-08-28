package render.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import render.bots.FileTrackerBot;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FileTrackerController implements Initializable {

    @FXML
    private Button btnPackage;
    @FXML
    private Button btnStart;
    @FXML
    private Button btnStop;
    @FXML
    private Button btnInfo;
    @FXML
    TextField minutes;
    @FXML
    TextField files;
    @FXML
    TextField telegramId;
    @FXML
    Label labelMinutes;
    @FXML
    Label labelFiles;
    @FXML
    Label labelTelegramId;

    String cfg = "";

    int counter = 0;

    FileTrackerBot fileTrackerBot;
    private String folder = "";
    private int currentNumberOfFiles;
    private int initialNumberOfFiles;
    private boolean firstIteration;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    @FXML
    private void handleButtonClicks(javafx.event.ActionEvent mouseEvent) {
        if (mouseEvent.getSource() == btnPackage) {
            chooseFolder();
        } else if (mouseEvent.getSource() == btnInfo) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeight(500);
            alert.setWidth(700);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText("write '/start' to telegram bot @userinfobot\n   to get your telegram id\n" + "-fill in telegram id\n" +
                    "-write '/start' to telegram bot\n   @Dmj_file_tracker_bot(only first time)\n-choose directory\n" +
                    "-fill in tracking interval(minutes)\n-fill in final number of rendered files\n" +
                    "-to start - press power button in the middle\n-to force stop - press power button again");
            alert.showAndWait();
        } else if (mouseEvent.getSource() == btnStart) {
            if (files.getText() == null || files.getText().equals("") || minutes.getText() == null || minutes.getText().equals("")
        || telegramId.getText() == null || telegramId.getText().equals("") || folder.equals("")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING");
                alert.setHeaderText("Specify all parameters before start");
                alert.setContentText("(folder, minutes, files, telegram id)");
                alert.showAndWait();
            } else if (Long.parseLong(telegramId.getText()) == 0L) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING");
                alert.setHeaderText("Connect to telegram bot first");
                alert.setContentText("press info button for more information");
                alert.showAndWait();
            } else {
                btnPackage.setDisable(true);
                btnStart.setDisable(true);
                btnStart.setOpacity(0);
                files.setDisable(true);
                telegramId.setDisable(true);
                minutes.setDisable(true);
                btnStop.setDisable(false);
                btnStop.setOpacity(1);
                counter = 0;
                Runnable trackRunnable = new Runnable() {
                    public void run() {
                        startTracking();
                    }
                };
                exec.scheduleAtFixedRate(trackRunnable, 0, Integer.parseInt(minutes.getText()), TimeUnit.MINUTES);
            }
        } else if (mouseEvent.getSource() == btnStop) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("WARNING");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to stop tracking?");
            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent()) alert.close();
            else if (result.get() == ButtonType.OK) {
                stopTracking();
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()),"Tracking was forcibly stopped on user: " + fileTrackerBot.getUserName());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        fileTrackerBot = new FileTrackerBot();
        fileTrackerBot.setUserName(System.getProperty("user.name"));
        try {
            File file = new File("trackerconfig.txt");
            file.createNewFile();
            BufferedReader reader = new BufferedReader(new FileReader("trackerconfig.txt"));
            cfg = reader.readLine();
            if (!cfg.isEmpty()) {
                String[] params = cfg.split(":");
                minutes.setText(params[0]);
                files.setText(params[1]);
                telegramId.setText(params[2]);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            cfg = "";
        }
        try {
            botsApi.registerBot(fileTrackerBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        minutes.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    minutes.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        files.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    files.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
        telegramId.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                if (!newValue.matches("\\d*")) {
                    telegramId.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });
    }


    private void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = new Stage();
        File selectedDirectory = directoryChooser.showDialog(stage);
        folder = selectedDirectory.toString();
        currentNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        initialNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        firstIteration = true;
    }

    private void startTracking() {
        try {
            FileWriter writer = new FileWriter("trackerconfig.txt");
            writer.write("");
            writer.write(minutes.getText() + ":" + files.getText() + ":" + telegramId.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int filesCap = Integer.parseInt(files.getText()) + initialNumberOfFiles;
        if (currentNumberOfFiles < filesCap) {
            if (counter >= 3) {
                stopTracking();
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()),"Files are not increasing for a long period, tracking stopped on user: " + fileTrackerBot.getUserName());
            }
            int previousAmount = currentNumberOfFiles;
            currentNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
            if (currentNumberOfFiles > previousAmount) counter = 0;
            if (previousAmount == currentNumberOfFiles && firstIteration && counter == 0) {
                firstIteration = false;
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()),"File tracker successfully started on user: " + fileTrackerBot.getUserName());
            }
            else if (previousAmount == currentNumberOfFiles && counter != 3) {
                counter++;
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()),
                        "WARNING! After last " + minutes.getText() + " minute(s) number of files("+ currentNumberOfFiles +") has not been increased on user: "
                                + fileTrackerBot.getUserName());
            }
        } else {
            stopTracking();
            fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()),"Tracking successfully finished on user: " + fileTrackerBot.getUserName()
                    + ", number of rendered files: "+ (currentNumberOfFiles - initialNumberOfFiles));
        }
    }

    private void stopTracking() {
        exec.shutdown();
        exec = Executors.newScheduledThreadPool(1);
        firstIteration = true;
        btnPackage.setDisable(false);
        btnStart.setDisable(false);
        btnStart.setOpacity(1);
        files.setDisable(false);
        minutes.setDisable(false);
        telegramId.setDisable(false);
        btnStop.setDisable(true);
        btnStop.setOpacity(0);
    }
}
