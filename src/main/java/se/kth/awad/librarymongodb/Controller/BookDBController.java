package se.kth.awad.librarymongodb.Controller;

import se.kth.awad.librarymongodb.model.*;
import se.kth.awad.librarymongodb.view.*;
import javafx.scene.control.Alert;
import javafx.concurrent.Task;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import static javafx.scene.control.Alert.AlertType.*;

/**
 * Controller i MVC-mönstret.
 * Hanterar all logik mellan View (BooksPane) och Model (BooksDb).
 */
public class BookDBController {

    private final BooksPane booksView;
    private final BooksDbInterface booksDb;
    private User currentUser = null; // Null om ingen användare är inloggad

    public BookDBController(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    // Ansluter till databasen och laddar alla böcker.
    public void connectToDatabase() {
        try {
            booksDb.connect("LibraryDB");
            List<Book> books = booksDb.getAllBooks();
            booksView.displayBooks(books);
            booksView.showAlertAndWait("Successfully connected to LibraryDB!", CONFIRMATION);
        } catch (Exception e) {
            booksView.showAlertAndWait("Connection failed: " + e.getMessage(), ERROR);
        }
    }

    // Kopplar från databasen och tömmer tabellen.
    public void disconnect() {
        try {
            booksDb.disconnect();
            booksView.displayBooks(new ArrayList<>());
            booksView.showAlertAndWait("Disconnected from database", INFORMATION);
        } catch (Exception e) {
            booksView.showAlertAndWait("Error disconnecting: " + e.getMessage(), ERROR);
        }
    }

    // Söker efter böcker baserat på vald söktyp.
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if (searchFor == null || searchFor.isEmpty()) {
            booksView.showAlertAndWait("Enter a search string!", WARNING);
            return;
        }

        Task<List<Book>> task = new Task<>() {
            @Override
            protected List<Book> call() throws Exception {
                return switch (mode) {
                    case Title -> booksDb.searchBooksByTitle(searchFor);
                    case ISBN -> booksDb.searchBooksByISBN(searchFor);
                    case Author -> booksDb.searchBooksByAuthor(searchFor);
                    case Rating -> booksDb.searchBooksByRating((int) Double.parseDouble(searchFor));
                    case Genre -> booksDb.searchBooksByGenre(searchFor);
                    default -> new ArrayList<>();
                };
            }
        };
        task.setOnSucceeded(e -> {
            List<Book> result = task.getValue();
            if (result == null || result.isEmpty()) {
                booksView.showAlertAndWait("No results found.", INFORMATION);
            } else {
                booksView.displayBooks(result);
            }
        });
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Database error: " + exception.getMessage(), ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Visar dialog och lägger till ny bok.
    public void addBook() {
        AddBookDialog dialog = new AddBookDialog(booksDb, currentUser);
        Optional<Book> result = dialog.showAndWait();
        if (result.isEmpty()) {
            booksView.showAlertAndWait("Book addition cancelled", INFORMATION);
            return;
        }

        Book book = result.get();
        book.setAddedByUserId(currentUser.getUserID());

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                booksDb.createBook(book);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try {
                List<Book> books = booksDb.getAllBooks();
                booksView.displayBooks(books);
                booksView.showAlertAndWait("Book '" + book.getTitle() + "' added successfully", CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Error: " + exception.getMessage(), ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Visar dialog och tar bort bok.
    public void removeBook() {
        if (!isLoggedIn()) {
            booksView.showAlertAndWait("You must be logged in to remove books!", WARNING);
            return;
        }

        RemoveBookDialog dialog = new RemoveBookDialog();
        Optional<String> titleResult = dialog.showAndWait();

        if (titleResult.isEmpty() || titleResult.get().isBlank()) {
            booksView.showAlertAndWait("Book deletion cancelled", INFORMATION);
            return;
        }

        String title = titleResult.get().trim();
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> allBooks = booksDb.getAllBooks();
                Book bookToDelete = null;
                for (Book book : allBooks) {
                    if (book.getTitle().equalsIgnoreCase(title)) {
                        bookToDelete = book;
                        break;
                    }
                }
                if (bookToDelete == null) {
                    throw new BooksDbException("No book found with exact title: " + title);
                }
                booksDb.deleteBook(bookToDelete);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try {
                List<Book> books = booksDb.getAllBooks();
                booksView.displayBooks(books);
                booksView.showAlertAndWait("Book '" + title + "' deleted successfully", CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Error: " + exception.getMessage(), ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Visar dialog och lägger till/uppdaterar betyg på bok.
    public void updateGrade() {
        UpdateGradeDialog dialog = new UpdateGradeDialog();
        Optional<GradeUpdate> result = dialog.showAndWait();

        if (result.isEmpty()) {
            booksView.showAlertAndWait("Rate Update Cancelled!!!", INFORMATION);
            return;
        }

        GradeUpdate gradeUpdate = result.get();
        String title = gradeUpdate.getTitle().trim();

        if (title.isEmpty()) {
            booksView.showAlertAndWait("Title cannot be empty", WARNING);
            return;
        }

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> books = booksDb.searchBooksByTitle(title);
                if (books.isEmpty()) {
                    throw new BooksDbException("No book found with title: " + title);
                }
                Book book = books.get(0);

                Review review = new Review();
                review.setBookId(book.getBookId());
                review.setUserId(currentUser.getUserID());
                review.setUsername(currentUser.getUsername());
                review.setRating(gradeUpdate.getGrade());
                review.setReviewText("");
                review.setReviewDate(java.time.LocalDate.now().toString());
                booksDb.addReview(review);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try {
                List<Book> books = booksDb.getAllBooks();
                booksView.displayBooks(books);
                booksView.showAlertAndWait(
                        "Rating for '" + title + "' set to " + gradeUpdate.getGrade() + " (replaced previous rating)",
                        CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Error: " + exception.getMessage(), ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Visar inloggningsdialog.
    public void showLogin() {
        LoginDialog dialog = new LoginDialog(booksDb);
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            currentUser = user;
            booksView.updateLoginStatus(user);
            booksView.showAlertAndWait("Welcome " + user.getUsername() + "!", INFORMATION);
        });
    }

    // Loggar ut användaren.
    public void logout() {
        if (currentUser != null) {
            String username = currentUser.getUsername();
            currentUser = null;
            booksView.updateLoginStatus(null);
            booksView.showAlertAndWait("You have logged out, " + username + "!", INFORMATION);
        }
    }

    // Returnerar den nuvarande inloggade användaren.
    public User getCurrentUser() {
        return currentUser;
    }

    // Kontrollerar om en användare är inloggad.
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    // Lägger till recension för en bok.
    public void addReview() {
        if (!isLoggedIn()) {
            booksView.showAlertAndWait("You must be logged in to write reviews!", WARNING);
            return;
        }

        ReviewDialog dialog = new ReviewDialog();
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) {
            booksView.showAlertAndWait("Review canceled", INFORMATION);
            return;
        }

        String bookTitle = dialog.getBookTitle();
        String reviewText = result.get();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> books = booksDb.searchBooksByTitle(bookTitle);
                if (books.isEmpty()) {
                    throw new BooksDbException("No book found with the title: " + bookTitle);
                }

                Book book = books.get(0);
                Review review = new Review();
                review.setBookId(book.getBookId());
                review.setUserId(currentUser.getUserID());
                review.setUsername(currentUser.getUsername());
                review.setRating(0);
                review.setReviewText(reviewText);
                // MongoDb förstår inte Date objekt därför bli som string
                review.setReviewDate(java.time.LocalDate.now().toString());
                booksDb.addReview(review);
                return null;
            }
        };

        task.setOnSucceeded(
                e -> booksView.showAlertAndWait("Review for '" + bookTitle + "' has been added!", CONFIRMATION));
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Error: " + exception.getMessage(), ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // Visar alla detaljer om en bok
    public void showBookDetails(Book book) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Review> reviews = booksDb.getReviewsForBook(book.getBookId());
                List<Author> authors = book.getAuthors();

                // Hämta vem som la till boken
                String addedBy = "Unknown";
                if (book.getAddedByUserId() > 0) {
                    try {
                        addedBy = booksDb.getUsernameById(book.getAddedByUserId());
                    } catch (BooksDbException e) {
                        addedBy = "User ID: " + book.getAddedByUserId();
                    }
                }

                // Bygg innehållet direkt här
                StringBuilder content = new StringBuilder();
                content.append("ADDED BY:\n");
                content.append("• ").append(addedBy).append("\n\n");

                content.append("AUTHORS:\n");
                if (authors.isEmpty()) {
                    content.append("No authors found");
                } else {
                    for (int i = 0; i < authors.size(); i++) {
                        Author author = authors.get(i);
                        content.append("• ").append(author.getName());
                        if (author.getBirthDate() != null) {
                            content.append(" (Born: ").append(author.getBirthDate()).append(")");
                        }
                        if (i < authors.size() - 1) {
                            content.append("\n");
                        }
                    }
                }
                content.append("\n\n");

                content.append("RECENSIONER:\n");
                if (reviews.isEmpty()) {
                    content.append("No reviews yet. Be the first to review!");
                } else {
                    for (Review review : reviews) {
                        content.append("─────────────────────\n");
                        content.append(review.getUsername());
                        if (review.getRating() > 0) {
                            content.append(" - Rating: ").append(review.getRating()).append("/10");
                        }
                        content.append(" (").append(review.getReviewDate()).append("):\n");
                        if (review.getReviewText() != null && !review.getReviewText().isEmpty()) {
                            content.append(review.getReviewText());
                        }
                        content.append("\n\n");
                    }
                }

                // Spara innehållet i Task för att använda i onSucceeded
                updateMessage(content.toString());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            String content = task.getMessage();
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Book Details");
            alert.setHeaderText(book.getTitle());
            alert.setContentText(content);
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(400);
            alert.showAndWait();
        });

        task.setOnFailed(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Authors");
            alert.setHeaderText("Authors for: " + book.getTitle());
            alert.setContentText(book.getAuthorsAsString());
            alert.showAndWait();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}