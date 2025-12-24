package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.control.Dialog;

public class ReviewDialog extends Dialog<String> {

    private final TextField bookTitleField;
    private final TextArea reviewTextArea;

    public ReviewDialog() {
        setTitle("Write a review");
        setHeaderText("Write a review for a book");

        bookTitleField = new TextField();
        bookTitleField.setPromptText("Book title");

        reviewTextArea = new TextArea();
        reviewTextArea.setPromptText("Write your review here...");
        reviewTextArea.setPrefRowCount(8);
        reviewTextArea.setPrefColumnCount(40);
        reviewTextArea.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Book title:"), 0, 0);
        grid.add(bookTitleField, 1, 0);
        grid.add(new Label("Review:"), 0, 1);
        grid.add(reviewTextArea, 1, 1);

        GridPane.setHgrow(bookTitleField, Priority.ALWAYS);
        GridPane.setHgrow(reviewTextArea, Priority.ALWAYS);
        GridPane.setVgrow(reviewTextArea, Priority.ALWAYS);

        getDialogPane().setContent(grid);

        ButtonType submitButton = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(submitButton, cancelButton);

        javafx.application.Platform.runLater(() -> bookTitleField.requestFocus());

        Button submitBtn = (Button) getDialogPane().lookupButton(submitButton);
        submitBtn.setDisable(true);

        bookTitleField.textProperty().addListener((obs, oldVal, newVal) -> {
            submitBtn.setDisable(newVal.trim().isEmpty() || reviewTextArea.getText().trim().isEmpty());
        });

        reviewTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
            submitBtn.setDisable(newVal.trim().isEmpty() || bookTitleField.getText().trim().isEmpty());
        });

        setResultConverter(dialogButton -> {
            if (dialogButton == submitButton) {
                return reviewTextArea.getText().trim();
            }
            return null;
        });
    }

    public String getBookTitle() {
        return bookTitleField.getText().trim();
    }

    public String getReviewText() {
        return reviewTextArea.getText().trim();
    }
}
