package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.control.*;

/**
 * Dialog för att ta bort en bok. Endast med title.
 */
public class RemoveBookDialog extends Dialog<String> {
    private final TextField titleField = new TextField(); // Fält för boktitel

    public RemoveBookDialog() {
        buildRemoveBookDialog();
    }

    private void buildRemoveBookDialog() {
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

        // Returnerar boktiteln om användaren klickar Delete, annars null
        this.setResultConverter(b -> {
            String result = b == buttonTypeOk ? titleField.getText().trim() : null;
            titleField.setText(""); // Rensa fältet
            return result;
        });
    }
}

