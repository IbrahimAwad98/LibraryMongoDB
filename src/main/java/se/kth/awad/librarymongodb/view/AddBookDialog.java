package se.kth.awad.librarymongodb.view;

import se.kth.awad.librarymongodb.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import java.util.List;
import java.util.Optional;

public class AddBookDialog extends Dialog<Book> {

    private final TextField titleField = new TextField();
    private final TextField isbnField = new TextField();
    private final DatePicker publishedField = new DatePicker();
    private final ListView<Author> authorListView = new ListView<>();
    private final ObservableList<Author> availableAuthors = FXCollections.observableArrayList();
    private final TextField genreField = new TextField();
    private final BooksDbInterface booksDb;
    private final User currentUser;

    public AddBookDialog(BooksDbInterface booksDb, User currentUser) {
        this.booksDb = booksDb;
        this.currentUser = currentUser;
        buildAddBookDialog();
        loadAuthors();
    }

    private void buildAddBookDialog() {
        this.setTitle("Add a new book");
        this.setResizable(false);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));

        grid.add(new Label("Title "), 1, 1);
        grid.add(titleField, 2, 1);
        grid.add(new Label("ISBN "), 1, 2);
        grid.add(isbnField, 2, 2);
        grid.add(new Label("Published Date "), 1, 3);
        grid.add(publishedField, 2, 3);
        grid.add(new Label("Genre(s) "), 1, 4);
        grid.add(genreField, 2, 4);
        genreField.setPromptText("Comma-separated (e.g., Fantasy, Young Adult)");

        grid.add(new Label("Author(s) "), 1, 5);
        authorListView.setItems(availableAuthors);
        authorListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        authorListView.setPrefHeight(150);
        authorListView.setPrefWidth(300);
        authorListView.setPlaceholder(new Label("Loading authors..."));

        // Custom cell factory för att visa författarnamn
        authorListView.setCellFactory(param -> new ListCell<Author>() {
            @Override
            protected void updateItem(Author author, boolean empty) {
                super.updateItem(author, empty);
                if (empty || author == null) {
                    setText(null);
                } else {
                    String text = author.getName();
                    if (author.getBirthDate() != null && !author.getBirthDate().isEmpty()) {
                        text += " (" + author.getBirthDate() + ")";
                    }
                    setText(text);
                }
            }
        });

        VBox authorBox = new VBox(5);
        authorBox.getChildren().add(authorListView);

        Button addAuthorButton = new Button("Add New Author");
        addAuthorButton.setOnAction(e -> handleAddAuthor());
        authorBox.getChildren().add(addAuthorButton);

        Label authorHint = new Label("Select one or more authors (hold Ctrl/Cmd to select multiple)");
        authorHint.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
        authorBox.getChildren().add(authorHint);
        grid.add(authorBox, 2, 5);

        this.getDialogPane().setContent(grid);

        ButtonType buttonTypeOk = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        this.getDialogPane().getButtonTypes().add(buttonTypeOk);
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        this.getDialogPane().getButtonTypes().add(buttonTypeCancel);

        this.setResultConverter(new Callback<ButtonType, Book>() {
            @Override
            public Book call(ButtonType b) {
                Book result = null;
                if (b == buttonTypeOk) {
                    if (isValidData()) {
                        String publishedDate = publishedField.getValue() != null
                                ? publishedField.getValue().toString()
                                : "";
                        result = new Book();
                        result.setIsbn(isbnField.getText().trim());
                        result.setTitle(titleField.getText().trim());
                        result.setPublishedDate(publishedDate);
                        result.setAddedByUserId(currentUser.getUserID());

                        ObservableList<Author> selectedAuthors = authorListView.getSelectionModel().getSelectedItems();
                        for (Author author : selectedAuthors) {
                            result.addAuthor(author);
                        }

                        // Hantera genrer
                        String genresText = genreField.getText().trim();
                        if (!genresText.isEmpty()) {
                            String[] genreNames = genresText.split(",");
                            for (String genreName : genreNames) {
                                genreName = genreName.trim();
                                if (!genreName.isEmpty()) {
                                    Genre genre = new Genre();
                                    genre.setGenreName(genreName);
                                    result.addGenre(genre);
                                }
                            }
                        }
                    }
                }
                clearFormData();
                return result;
            }
        });

        Button okButton = (Button) this.getDialogPane().lookupButton(buttonTypeOk);
        okButton.addEventFilter(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!isValidData()) {
                    event.consume();
                    showErrorAlert("Form error", "Please fill in all required fields correctly.");
                }
            }
        });
    }

    private void loadAuthors() {
        Task<List<Author>> task = new Task<List<Author>>() {
            @Override
            protected List<Author> call() throws Exception {
                return booksDb.getAllAuthors();
            }
        };

        task.setOnSucceeded(e -> {
            List<Author> authors = task.getValue();
            availableAuthors.clear();
            availableAuthors.addAll(authors);
            if (authors.isEmpty()) {
                authorListView.setPlaceholder(new Label("No authors available in database"));
            }
        });

        task.setOnFailed(e -> {
            authorListView.setPlaceholder(new Label("Error loading authors"));
            Throwable ex = task.getException();
            ex.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void handleAddAuthor() {
        Dialog<Author> authorDialog = new Dialog<>();
        authorDialog.setTitle("Add New Author");
        authorDialog.setHeaderText("Enter author information");

        TextField nameField = new TextField();
        nameField.setPromptText("Author name");
        DatePicker birthDatePicker = new DatePicker();
        birthDatePicker.setPromptText("Birth date (optional)");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Birth Date:"), 0, 1);
        grid.add(birthDatePicker, 1, 1);

        authorDialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        authorDialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

        authorDialog.setResultConverter(buttonType -> {
            if (buttonType == addButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    return null;
                }
                String birthDate = birthDatePicker.getValue() != null
                        ? birthDatePicker.getValue().toString()
                        : null;
                Author author = new Author();
                author.setName(name);
                author.setBirthDate(birthDate);
                return author;
            }
            return null;
        });

        Optional<Author> result = authorDialog.showAndWait();
        result.ifPresent(author -> {
            Task<Author> addTask = new Task<Author>() {
                @Override
                protected Author call() throws Exception {
                    // Author will be added when book is saved
                    // For now, just return the author
                    return author;
                }
            };

            addTask.setOnSucceeded(e -> {
                Author addedAuthor = addTask.getValue();
                availableAuthors.add(addedAuthor);
                authorListView.getSelectionModel().select(addedAuthor);
            });

            addTask.setOnFailed(e -> {
                Throwable ex = addTask.getException();
                showErrorAlert("Error adding author", ex.getMessage());
                ex.printStackTrace();
            });

            Thread thread = new Thread(addTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    private boolean isValidData() {
        if (titleField.getText().trim().isEmpty()) {
            return false;
        }
        if (isbnField.getText().trim().isEmpty()) {
            return false;
        }
        if (publishedField.getValue() == null) {
            return false;
        }
        if (authorListView.getSelectionModel().getSelectedItems().isEmpty()) {
            return false;
        }

        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty() || isbn.length() < 10) {
            return false;
        }

        return true;
    }

    private void clearFormData() {
        titleField.setText("");
        isbnField.setText("");
        publishedField.setValue(null);
        genreField.setText("");
        authorListView.getSelectionModel().clearSelection();
    }

    private final Alert errorAlert = new Alert(Alert.AlertType.ERROR);

    private void showErrorAlert(String title, String info) {
        errorAlert.setTitle(title);
        errorAlert.setHeaderText(null);
        errorAlert.setContentText(info);
        errorAlert.show();
    }
}
