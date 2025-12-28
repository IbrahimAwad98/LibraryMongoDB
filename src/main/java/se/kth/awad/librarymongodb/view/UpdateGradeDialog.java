package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Dialog för att uppdatera betyg på en bok.
 */
public class UpdateGradeDialog extends Dialog<GradeUpdate> {

    private final ComboBox<Integer> gradeField = new ComboBox<>(); // Dropdown för betyg (1-10)
    private final TextField titleField = new TextField(); // Fält för boktitel

    public UpdateGradeDialog() {
        buildUpdateGradeDialog();
    }

    // Bygger dialogens användargränssnitt
    private void buildUpdateGradeDialog() {
        this.setTitle("Update a book grade");
        this.setResizable(false);
        gradeField.getItems().addAll(1, 2, 3, 4, 5, 6, 7, 8, 9, 10); // Betyg från 1 till 10

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);
        grid.add(new Label("Grade "), 1, 2);
        grid.add(gradeField, 2, 2);

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        // Returnerar GradeUpdate-objekt med titel och betyg när användaren klickar Update
        this.setResultConverter(b -> {
            if (b != buttonTypeOk) {
                titleField.setText("");
                gradeField.setValue(null);
                return null;
            }

            String title = titleField.getText().trim();
            Integer grade = gradeField.getValue();
            GradeUpdate result;
            if (!title.isEmpty() && grade != null) {
                result = new GradeUpdate(title, grade);
            } else {
                result = null;
            }
            
            titleField.setText(""); // Rensa fälten
            gradeField.setValue(null);
            return result;
        });
    }
}