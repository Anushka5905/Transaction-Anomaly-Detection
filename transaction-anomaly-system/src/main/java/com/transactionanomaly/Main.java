package com.transactionanomaly;

import com.transactionanomaly.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application entry point. Run this class (or `mvn javafx:run`) to launch
 * the dashboard.
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        DashboardView dashboard = new DashboardView();

        Scene scene = new Scene(dashboard, 1200, 700);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        stage.setTitle("AI Transaction Anomaly Visualization System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
