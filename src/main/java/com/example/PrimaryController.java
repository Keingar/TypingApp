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

    private String text = QuoteService.FALLBACK;
    private final QuoteService quoteService = new QuoteService();

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
        // Block typing while there is a mistake; deleting is still allowed
        input.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getControlNewText().length() > text.length()) {
                return null;
            }
            if (!change.getText().isEmpty() && !text.startsWith(change.getControlText())) {
                return null;
            }
            if (!change.getText().isEmpty()) {
                keystrokes += change.getText().length();
                if (!text.startsWith(change.getControlNewText())) {
                    mistakes += change.getText().length();
                }
            }
            return change;
        }));

        input.textProperty().addListener((obs, old, typed) -> {
            render(typed);
            if (startNanos == 0 && !typed.isEmpty()) {
                startNanos = System.nanoTime();
            }
            if (typed.equals(text)) {
                double minutes = (System.nanoTime() - startNanos) / 60e9;
                long wpm = Math.round(text.length() / 5.0 / minutes);
                long accuracy = Math.round(100.0 * (keystrokes - mistakes) / keystrokes);
                resultLabel.setText(wpm + " WPM  " + accuracy + "% accuracy");
                resultBox.setVisible(true);
                input.setDisable(true);
            }
        });

        loadQuote();
    }

    // Fetches off the UI thread; the quote service falls back to static text on failure
    private void loadQuote() {
        input.setDisable(true);
        targetFlow.getChildren().setAll(new Text("Loading..."));
        Thread worker = new Thread(() -> {
            String quote = quoteService.fetchQuote();
            Platform.runLater(() -> showText(quote));
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void showText(String quote) {
        text = quote;
        chars.clear();
        for (char c : text.toCharArray()) {
            Text t = new Text(String.valueOf(c));
            t.setFont(FONT);
            t.setFill(PENDING);
            chars.add(t);
        }
        targetFlow.getChildren().setAll(chars);
        input.setDisable(false);
        input.requestFocus();
    }

    private void render(String typed) {
        for (int i = 0; i < chars.size(); i++) {
            Text t = chars.get(i);
            if (i >= typed.length()) {
                t.setFill(PENDING);
                t.setUnderline(false);
            } else if (typed.charAt(i) == text.charAt(i)) {
                t.setFill(Color.BLACK);
                t.setUnderline(false);
            } else {
                t.setFill(Color.RED);
                t.setUnderline(text.charAt(i) == ' ');
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
        input.clear();
        resultBox.setVisible(false);
        loadQuote();
    }
}
