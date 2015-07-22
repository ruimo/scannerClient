package com.ruimo.samples.scannerclient;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController {
    static final int MAX_RECORDS_TO_SHOW = 5;

    @FXML private Label inputArea;
    @FXML private Label reportArea;
    @FXML private TableView<VisitRecord> visitorTable;

    public void reportQueryCustomerVisitStart(String id) {
        System.err.println("Customer visit " + id);
        removeObsoleteRecords();
        visitorTable.getItems().add(0, new VisitRecord(id));
    }

    public void reportQueryCustomerVisitCompleted(String id, int count) {
        System.err.println("Customer visit " + id + ", " + count);
        visitorTable.getItems().stream().filter(e -> e.getId().equals(id)).findFirst().map((VisitRecord e) -> {
           e.countProperty().set(count + "回目");
           return null;
        });
    }

    public void reportQueryCustomerUnregistered(String id) {
        System.err.println("Customer unregistered " + id);
        visitorTable.getItems().stream().filter(e -> e.getId().equals(id)).findFirst().map((VisitRecord e) -> {
           e.countProperty().set("未登録ユーザー");
           return null;
        });
    }

    public void reportInputChanged(String input) {
        inputArea.setText(input);
    }

    public void reportNotification(String report) {
        reportArea.setText(report);
    }

    void removeObsoleteRecords() {
        ObservableList<VisitRecord> records = visitorTable.getItems();
        int size = records.size();
        if (size > MAX_RECORDS_TO_SHOW) {
            records.remove(MAX_RECORDS_TO_SHOW, size);
        }
    }
}
