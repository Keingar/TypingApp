package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class PrimaryController {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog and runs far away.";

    @FXML private Label targetLabel;
    @FXML private TextField input;
    @FXML private HBox resultBox;
    @FXML private Label resultLabel;

    private long startNanos;

    @FXML
    private void initialize() {
        targetLabel.setText(TEXT);
        input.textProperty().addListener((obs, old, text) -> {
            if (startNanos == 0 && !text.isEmpty()) {
                startNanos = System.nanoTime();
            }
            if (text.equals(TEXT)) {
                double minutes = (System.nanoTime() - startNanos) / 60e9;
                long wpm = Math.round(TEXT.length() / 5.0 / minutes);
                resultLabel.setText(wpm + " WPM");
                resultBox.setVisible(true);
                input.setDisable(true);
            }
        });
    }

    @FXML
    private void again() {
        startNanos = 0;
        input.setDisable(false);
        input.clear();
        resultBox.setVisible(false);
        input.requestFocus();
    }
}
