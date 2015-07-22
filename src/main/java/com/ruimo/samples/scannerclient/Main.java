package com.ruimo.samples.scannerclient;

import javafx.application.Platform;
import javafx.application.Application;
import javafx.stage.*;
import javafx.scene.input.KeyEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.geometry.Rectangle2D;
import javafx.fxml.FXMLLoader;
import javafx.event.EventDispatcher;
import javafx.event.EventDispatchChain;
import javafx.event.Event;
import javafx.event.EventType;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        Pane pane = loader.load();
        MainController controller = loader.getController();

        final JavaAdapter adapter = new JavaAdapter() {
            @Override public void onQueryCustomerVisitStart(String id) {
                Platform.runLater(() -> {
                  controller.reportQueryCustomerVisitStart(id);
                });
            }

            @Override public void onQueryCustomerVisitCompleted(String id, int count) {
                Platform.runLater(() -> {
                    controller.reportQueryCustomerVisitCompleted(id, count);
                });
            }

            @Override public void onQueryCustomerUnregistered(String id) {
                Platform.runLater(() -> {
                    controller.reportQueryCustomerUnregistered(id);
                });
            }

            @Override public void onNotifyReport(String report) {
                Platform.runLater(() -> {
                    controller.reportNotification(report);
                });                
            }

            @Override public void onInputChanged(String input) {
                Platform.runLater(() -> {
                    controller.reportInputChanged(input);
                });                
            }
        };
        captcherAllKeyEvent(stage, adapter);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
        stage.setFullScreen(true);
        adapter.initialize();
    }

    void captcherAllKeyEvent(Stage stage, JavaAdapter adapter) {
        stage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            adapter.reportKeyEvent(e);
            e.consume();
        });
    }
}
