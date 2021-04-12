package render;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/filetracker.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        stage.setResizable(false);
        stage.getIcons().add(new Image("/icons/folder.png"));
        stage.setTitle("Damadj's file tracker");
        stage.setScene(new Scene(root));
        stage.show();
        stage.setOnCloseRequest(e->{
            Platform.exit();
            System.exit(0);
        });

        /** minimise to tray */
//        Platform.setImplicitExit(false);
//        URL url = System.class.getResource("/icons/tray.png");
//        java.awt.Image image = Toolkit.getDefaultToolkit().getImage(url);
//        final TrayIcon trayIcon = new TrayIcon(image);
//        final PopupMenu popup = new PopupMenu();
//        popup.addNotify();
//        final SystemTray tray = SystemTray.getSystemTray();
//        MenuItem aboutItem = new MenuItem("About");
//        MenuItem exitItem = new MenuItem("Exit");
//        stage.setOnCloseRequest(event -> {
//            stage.hide();
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
//
////        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
////            @Override
////            public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) {
////                if (!SystemTray.isSupported()) {
////                    System.out.println("SystemTray is not supported");
////                    return;
////                }
////                if (tray.getTrayIcons().length == 0) {
////                    // Create a pop-up menu components
////                    //Add components to pop-up menu
////                    popup.add(aboutItem);
////                    popup.addSeparator();
////                    popup.add(exitItem);
////                    trayIcon.setPopupMenu(popup);
////                    try {
////                        tray.add(trayIcon);
////                    } catch (AWTException e) {
////                        System.out.println("TrayIcon could not be added.");
////                    }
////                }
////            }
////        });
//        exitItem.addActionListener(event -> {
//            Platform.exit();
//            System.exit(0);
//            tray.remove(trayIcon);
//        });
//        trayIcon.addActionListener(e -> {
//            Platform.runLater(() -> stage.show());
//        });
    }



    public static void main(String[] args) {
        launch(args);
    }
}
