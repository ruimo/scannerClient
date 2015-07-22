package com.ruimo.samples.scannerclient;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import static java.util.Objects.requireNonNull;

public class VisitRecord {
    private final String id;
    private final SimpleStringProperty count;

    public VisitRecord(String id) {
        this.id = requireNonNull(id);
        count = new SimpleStringProperty("検索中...");
    }

    public String getId() {
        return id;
    }

    public StringProperty countProperty() {
        return count;
    }
}

