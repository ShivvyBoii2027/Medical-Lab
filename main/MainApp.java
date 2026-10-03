/*
 * MediLab Pro — Main Entry Point
 * Professional Portal Launch
 */
package main;

import gui.LoginScreen;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        LoginScreen login = new LoginScreen();
        login.show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}