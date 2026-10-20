package com.example;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class PrimaryController {

    private static final String TEXT = "The quick brown fox jumps over the lazy dog and runs far away.";

    private static final Color PENDING = Color.web("#b5b5b5");
    private static final Font FONT = Font.font(20);

    @FXML private TextFlow targetFlow;
    @FXML private TextField input;
    private final List<Text> chars = new ArrayList<>();
    @FXML private HBox resultBox;
    @FXML private Label resultLabel;

    private long startNanos;
    private int keystrokes;
    private int mistakes;

    @FXML
    private void initialize() {
        for (char c : TEXT.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setFont(FONT);
            t.setFill(PENDING);
            chars.add(t);
        }
        targetFlow.getChildren().addAll(chars);

        // Block typing while there is a mistake; deleting is still allowed
        input.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > TEXT.length()) {
                return null;
            }
            if (!change.getText().isEmpty() && !TEXT.startsWith(change.getControlText())) {
                return null;
            }
            if (!change.getText().isEmpty()) {
                keystrokes += change.getText().length();
                if (!TEXT.startsWith(change.getControlNewText())) {
                    mistakes += change.getText().length();
                }
            }
            return change;
        }));
        Platform.runLater(input::requestFocus);

        input.textProperty().addListener((obs, old, text) -> {
            render(text);
            if (startNanos == 0 && !text.isEmpty()) {
                startNanos = System.nanoTime();
            }
            if (text.equals(TEXT)) {
                double minutes = (System.nanoTime() - startNanos) / 60e9;
                long wpm = Math.round(TEXT.length() / 5.0 / minutes);
                long accuracy = Math.round(100.0 * (keystrokes - mistakes) / keystrokes);
                resultLabel.setText(wpm + " WPM  " + accuracy + "% accuracy");
                resultBox.setVisible(true);
                input.setDisable(true);
            }
        });
    }

    private void render(String text) {
        for (int i = 0; i < chars.size(); i++) {
            Text t = chars.get(i);
            if (i >= text.length()) {
                t.setFill(PENDING);
                t.setUnderline(false);
            } else if (text.charAt(i) == TEXT.charAt(i)) {
                t.setFill(Color.BLACK);
                t.setUnderline(false);
            } else {
                t.setFill(Color.RED);
                t.setUnderline(TEXT.charAt(i) == ' ');
            }
        }
    }

    @FXML
    private void focusInput() {
        input.requestFocus();
    }

    @FXML
    private void again() {
        startNanos = 0;
        keystrokes = 0;
        mistakes = 0;
        input.setDisable(false);
        input.clear();
        resultBox.setVisible(false);
        input.requestFocus();
    }
}
