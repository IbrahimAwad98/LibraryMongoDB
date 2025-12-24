package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import javafx.scene.control.*;


public class RemoveBookDialog extends Dialog<String> {
    private final TextField titleField = new TextField();

    public RemoveBookDialog() {
        buildRemoveBookDialog();
    }
    private void buildRemoveBookDialog(){
        this.setTitle("Delete a book");
        this.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk
                = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel
                = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        this.setResultConverter(new Callback<ButtonType, String>() {
            @Override
            public String call(ButtonType b) {
                String result = null;
                if (b == buttonTypeOk) {
                    result = titleField.getText().trim();
                }
                clearFormData();
                return result;
            }
        });
    }

    private void clearFormData() {
        titleField.setText("");
    }
}

