package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;


public class UpdateGradeDialog extends Dialog<UpdateGradeDialog.GradeUpdate> {

    private final ComboBox<Integer> gradeField = new ComboBox<>();
    private final TextField titleField = new TextField();


    public static class GradeUpdate {
        private final String title;
        private final int grade;

        public GradeUpdate(String title, int grade) {
            this.title = title;
            this.grade = grade;
        }

        public String getTitle() {
            return title;
        }

        public int getGrade() {
            return grade;
        }
    }

    public UpdateGradeDialog() {
        buildUpdateGradeDialog();
    }
    private void buildUpdateGradeDialog(){
        this.setTitle("Update a book grade");
        this.setResizable(false);
        gradeField.getItems().addAll(1, 2, 3, 4, 5,6,7,8,9,10);

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

        ButtonType buttonTypeOk
                = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel
                = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        this.setResultConverter(new Callback<ButtonType, GradeUpdate>() {
            @Override
            public GradeUpdate call(ButtonType b) {
                GradeUpdate result = null;
                if (b == buttonTypeOk) {
                    String title = titleField.getText().trim();
                    Integer grade = gradeField.getValue();

                    if (!title.isEmpty() && grade != null) {
                        result = new GradeUpdate(title, grade);
                    }
                }
                clearFormData();
                return result;
            }
        });
    }

    private void clearFormData() {
        titleField.setText("");
        gradeField.setValue(null);
    }
}
