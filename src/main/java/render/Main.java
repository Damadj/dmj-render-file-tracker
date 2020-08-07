package render;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{

        Parent root = FXMLLoader.load(getClass().getResource("/filetracker.fxml"));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("/icons/folder.png"));
        primaryStage.setTitle("Damadj's file tracker");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });

        /** minimise to tray, exit does not stop all threads */
//        Platform.setImplicitExit(false);
//        URL url = System.class.getResource("/icons/folder1.png");
//        java.awt.Image image = Toolkit.getDefaultToolkit().getImage(url);
//        final TrayIcon trayIcon = new TrayIcon(image);
//        final PopupMenu popup = new PopupMenu();
//        final SystemTray tray = SystemTray.getSystemTray();
//        MenuItem aboutItem = new MenuItem("About");
//        MenuItem exitItem = new MenuItem("Exit");
//        primaryStage.setOnCloseRequest(event -> {
//            if (!SystemTray.isSupported()) {
//                System.out.println("SystemTray is not supported");
//                return;
//            }
//            if (tray.getTrayIcons().length == 0) {
//                // Create a pop-up menu components
//                //Add components to pop-up menu
//                popup.add(aboutItem);
//                popup.addSeparator();
//                popup.add(exitItem);
//                trayIcon.setPopupMenu(popup);
//                try {
//                    tray.add(trayIcon);
//                } catch (AWTException e) {
//                    System.out.println("TrayIcon could not be added.");
//                }
//            }
//        });
//        exitItem.addActionListener(event -> {
//            Platform.exit();
//            tray.remove(trayIcon);
//        });
//        trayIcon.addActionListener(e -> {
//            Platform.runLater(() -> primaryStage.show());
//        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}
