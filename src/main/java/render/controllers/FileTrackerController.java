package render.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    private Button btnSettings;
    @FXML
    TextField minutes;
    @FXML
    Label folderPath;
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

    public Map<String, Boolean> checkBoxSettings = new HashMap<>();
    private FileTrackerBot fileTrackerBot;
    private File selectedDirectory;
    private String folder = "";
    private int currentNumberOfFiles;
    private int previousNumberOfFiles;
    private int initialNumberOfFiles;
    private int regularReportCounter;
    private int filesCap;
    private int intervalInSeconds;
    private long startTime;
    private boolean started = false;
    private ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    @FXML
    private void handleButtonClicks(ActionEvent mouseEvent) {
        if (mouseEvent.getSource() == btnPackage) {
            chooseFolder();
        } else if (mouseEvent.getSource() == btnSettings) {
            openSettings();
        } else if (mouseEvent.getSource() == btnInfo) {
            openInformation();
        } else if (mouseEvent.getSource() == btnStart) {
            if (files.getText() == null || files.getText().equals("") || telegramId.getText() == null || telegramId.getText().equals("") || folder.equals("")) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("WARNING");
                alert.setHeaderText("Specify all parameters before start");
                alert.setContentText("(folder, files, telegram/group id)");
                alert.showAndWait();
            } else {
                prepareForStartTracker();
                Runnable trackRunnable;
                if (intervalInSeconds == 0) trackRunnable = this::startTrackingWithoutNotifications;
                else trackRunnable = this::startTrackingWithNotifications;
                exec.scheduleAtFixedRate(trackRunnable, 0, 1, TimeUnit.SECONDS);
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()), "File tracker successfully started on: " + fileTrackerBot.getUserName());
            }
        } else if (mouseEvent.getSource() == btnStop) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("WARNING");
            alert.setHeaderText(null);
            alert.setContentText("Are you sure you want to stop tracker?");
            Optional<ButtonType> result = alert.showAndWait();
            if (!result.isPresent()) alert.close();
            else if (result.get() == ButtonType.OK) {
                stopTracking();
                fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()), "File tracker was forcibly stopped on: " + fileTrackerBot.getUserName());
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        checkBoxSettings.put("user", true);
        checkBoxSettings.put("totalTime", true);
        checkBoxSettings.put("averageTime", true);
        checkBoxSettings.put("renderFilesSinceReport", true);
        checkBoxSettings.put("totalRenderFiles", true);
        checkBoxSettings.put("extensions", true);
        checkBoxSettings.put("totalSize", true);
        checkBoxSettings.put("freeSpace", true);
        ApiContextInitializer.init();
        TelegramBotsApi botsApi = new TelegramBotsApi();
        fileTrackerBot = new FileTrackerBot(this);
        fileTrackerBot.setUserName(System.getProperty("user.name"));
        try {
            botsApi.registerBot(fileTrackerBot);
            botsApi.registerBot(fileTrackerBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        File file = new File("trackerconfig.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedReader reader = new BufferedReader(new FileReader("trackerconfig.txt"))) {


            cfg = reader.readLine();
            if (!cfg.isEmpty()) {
                String[] params = cfg.split("/");
                minutes.setText(params[0]);
                files.setText(params[1]);
                telegramId.setText(params[2]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            cfg = "";
        }

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
                if (!newValue.matches("\\d*|\\-*")) {
                    telegramId.setText(newValue.replaceAll("[^\\d\\-]", ""));
                }
            }
        });
    }

    private void chooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        Stage stage = new Stage();
        selectedDirectory = directoryChooser.showDialog(stage);
        folder = selectedDirectory.toString();
        currentNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        initialNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        folderPath.setText(shortenFolderPath());
    }

    private void openSettings() {
        SettingsController settingsController = new SettingsController(this);
        settingsController.showStage();
    }

    private void openInformation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeight(500);
        alert.setWidth(700);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(
                "-find telegram bot @Dmj_file_tracker_bot\n" +
                        "-write '/id' directly to bot or add bot to group\n" +
                        "   and write '/id' there to get your/group telegram id\n" +
                        "-fill in your/group telegram id\n" +
                        "-write '/start'(only first time)\n" +
                        "-choose directory\n" +
                        "-fill in report interval\n" +
                        "   or leave it '00m00s', if you want only final report\n" +
                        "-fill in final number of rendered files\n" +
                        "-use settings button to specify report options\n" +
                        "-to start - press power button\n" +
                        "-to force stop - press power button again\n" +
                        "-write '/report' to get actual report any time\n" +
                        "-for steady working\n" +
                        "   do not move files from selected folder while tracker is on\n\n" +
                        "Thanks for using, for any problems or suggestions\n" +
                        "   contact me through damadj2@gmail.com");
        alert.showAndWait();
    }

    private void startTrackingWithoutNotifications() {
        currentNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        if (currentNumberOfFiles >= filesCap) {
            stopTracking();
            fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()), "File tracker successfully finished" + buildReport());
        }
    }

    private void startTrackingWithNotifications() {
        currentNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        regularReportCounter++;
        if (currentNumberOfFiles >= filesCap) {
            stopTracking();
            fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()), "File tracker successfully finished" + buildReport());
        } else if (regularReportCounter >= intervalInSeconds) {
            fileTrackerBot.sendMessage(Long.parseLong(telegramId.getText()), buildReport());
            regularReportCounter = 0;
            previousNumberOfFiles = currentNumberOfFiles;
        }
    }

    private void prepareForStartTracker() {
        try (FileWriter writer = new FileWriter("trackerconfig.txt")) {

            writer.write("");
            writer.write(minutes.getText() + "/" + files.getText() + "/" + telegramId.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnPackage.setDisable(true);
        btnStart.setDisable(true);
        btnStart.setOpacity(0);
        files.setDisable(true);
        telegramId.setDisable(true);
        minutes.setDisable(true);
        btnStop.setDisable(false);
        btnStop.setOpacity(1);
        intervalInSeconds = convertToSeconds(minutes.getText());
        previousNumberOfFiles = initialNumberOfFiles;
        counter = 0;
        initialNumberOfFiles = Objects.requireNonNull(new File(folder).listFiles()).length;
        filesCap = Integer.parseInt(files.getText()) + initialNumberOfFiles;
        startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.MIN);
        regularReportCounter = 0;
        started = true;
    }

    private void stopTracking() {
        exec.shutdown();
        exec = Executors.newScheduledThreadPool(1);
        btnPackage.setDisable(false);
        btnStart.setDisable(false);
        btnStart.setOpacity(1);
        files.setDisable(false);
        minutes.setDisable(false);
        telegramId.setDisable(false);
        btnStop.setDisable(true);
        btnStop.setOpacity(0);
        started = false;
    }

    private int convertToSeconds(String interval) {
        String[] seconds = interval.split("m");
        return (Integer.parseInt(seconds[0]) * 60 + Integer.parseInt(seconds[1].replace("s", "")));
    }

    private String convertToMinutes(int secs) {
        int mins = secs / 60;
        int modulo = secs % 60;
        return (mins + "m" + modulo + "s");
    }

    private String parseFileNamesForExtensions() {
        Set<String> set = new HashSet<>();
        if (Objects.requireNonNull(selectedDirectory.listFiles()).length > 0) {
            for (File t : Objects.requireNonNull(selectedDirectory.listFiles())) {
                if (t.isFile())
                    if (t.toString().contains(".")) set.add(t.toString().substring(t.toString().lastIndexOf(".")));
            }
            String extensions = set.toString();
            return extensions.substring(1, extensions.length() - 1);
        }
        return "no files";
    }

    private String shortenFolderPath() {
        if (folder.split("/").length > 4) {
            String beginning = folder.substring(1);
            return "/" + beginning.substring(0, beginning.indexOf("/")) + "/..." + folder.substring(folder.lastIndexOf("/"));
        } else if (folder.split(Pattern.quote("\\")).length > 4) {
            return folder.substring(0, folder.indexOf("\\")) + "\\..." + folder.substring(folder.lastIndexOf("\\"));
        } else return folder;
    }

    private String countFolderSize() {
        long length = 0;
        for (File file : Objects.requireNonNull(selectedDirectory.listFiles()))
            if (file.isFile()) length += file.length();
        return (Math.floor(length * 100.0 / 1048576) / 100) + "Mb";
    }

    private String countFreeSpace() {
        return (Math.floor(selectedDirectory.getFreeSpace() * 100.0 / 1048576) / 100) + "Mb";
    }

    public String buildReport() {
        String report = "";
        if (checkBoxSettings.get("user")) report += "\npc username: " + fileTrackerBot.getUserName();
        if (checkBoxSettings.get("totalTime"))
            report += "\ntotal render time: " + convertToMinutes((int) ((LocalDateTime.now().toEpochSecond(ZoneOffset.MIN) - startTime)));
        if (checkBoxSettings.get("averageTime"))
            report += "\naverage render time: " + convertToMinutes(currentNumberOfFiles == initialNumberOfFiles ? 0 :
                    (int) ((LocalDateTime.now().toEpochSecond(ZoneOffset.MIN) - startTime) / (currentNumberOfFiles - initialNumberOfFiles)));
        if (checkBoxSettings.get("renderFilesSinceReport"))
            report += "\nrendered files since last report: " + (currentNumberOfFiles - previousNumberOfFiles);
        if (checkBoxSettings.get("totalRenderFiles"))
            report += "\nnumber of rendered files: " + (currentNumberOfFiles - initialNumberOfFiles);
        if (checkBoxSettings.get("extensions")) report += "\nfile formats: " + parseFileNamesForExtensions();
        if (checkBoxSettings.get("totalSize")) report += "\ntotal size of files: " + countFolderSize();
        if (checkBoxSettings.get("freeSpace")) report += "\nfree space left: " + countFreeSpace();
        return report;
    }

    public boolean isStarted() {
        return started;
    }

    public TextField getTelegramId() {
        return telegramId;
    }
}
