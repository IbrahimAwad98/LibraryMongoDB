package se.kth.awad.librarymongodb.Controller;

import javafx.scene.control.Alert;
import se.kth.awad.librarymongodb.model.*;
import se.kth.awad.librarymongodb.view.*;

import java.util.ArrayList;
import javafx.concurrent.Task;
import java.util.List;
import java.util.Optional;
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

    /**
     * Ansluter till databasen och laddar alla böcker.
     */
    public void connectToDb() {
        try {
            boolean isConnected = booksDb.connect("LibraryDB");
            if (isConnected) {
                List<Book> books = new ArrayList<>();
                try {
                    books = booksDb.getAllBooks();
                    booksView.displayBooks(books);
                    booksView.showAlertAndWait("Successfully connected to database 'LibraryDB'", CONFIRMATION);
                } catch (Exception e) {
                    booksView.showAlertAndWait("Error fetching table: " + e.getMessage(), ERROR);
                }
            } else {
                booksView.showAlertAndWait("Database 'LibraryDB' not found", WARNING);
            }
        } catch (Exception e) {
            String errorDetails = e.getMessage();
            if (e.getCause() != null && e.getCause().getMessage() != null) {
                errorDetails += "\n\nCause: " + e.getCause().getMessage();
            }
            booksView.showAlertAndWait(
                    "Connection failed!\n\n" + errorDetails +
                            "\n\nPlease check:\n" +
                            "1. MongoDB server is running\n" +
                            "2. Database 'library' exists\n" +
                            "3. User credentials are correct",
                    ERROR);
        }
    }

    /**
     * Kopplar från databasen och tömmer tabellen.
     */
    public void disconnect() {
        List<Book> books = new ArrayList<>();
        try {
            booksDb.disconnect();
            booksView.showAlertAndWait("Successfully disconnected from database", INFORMATION);
            booksView.displayBooks(books);
        } catch (Exception e) {
            booksView.showAlertAndWait("Error disconnecting: " + e.getMessage(), ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Söker efter böcker baserat på vald söktyp.
     */
    public void onSearchSelected(String searchFor, SearchMode mode) {
        if (searchFor == null || searchFor.isEmpty()) {
            booksView.showAlertAndWait("Enter a search string!", WARNING);
            return;
        }
        Task<List<Book>> task = new Task<List<Book>>() {
            @Override
            protected List<Book> call() throws Exception {
                switch (mode) {
                    case Title:
                        return booksDb.searchBooksByTitle(searchFor);
                    case ISBN:
                        return booksDb.searchBooksByISBN(searchFor);
                    case Author:
                        return booksDb.searchBooksByAuthor(searchFor);
                    case Rating:
                        int minRating = (int) Double.parseDouble(searchFor);
                        return booksDb.searchBooksByRating(minRating);
                    case Genre:
                        return booksDb.searchBooksByGenre(searchFor);
                    default:
                        return new ArrayList<>();
                }
            }
        };
        task.setOnSucceeded(e -> {
            List<Book> result = task.getValue();
            if (result == null || result.isEmpty()) {
                booksView.showAlertAndWait("No results found.", INFORMATION);
            } else {
                booksView.displayBooks(result); // Visa resultaten i TableView
            }
        });
        // misslyckas ! då visa fel
        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            booksView.showAlertAndWait("Database error: " + exception.getMessage(), ERROR);
            exception.printStackTrace();
        });
        // starta tråden för att inte frysa ui
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Visar dialog och lägger till ny bok.
     */
    public void addBook() {
        AddBookDialog dialog = new AddBookDialog(booksDb, currentUser);
        Optional<Book> result = dialog.showAndWait();
        if (result.isEmpty()) {
            booksView.showAlertAndWait("Book addition cancelled", INFORMATION);
            return;
        }
        Book book = result.get();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                booksDb.createBook(book);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try {
                refreshBookList();
                booksView.showAlertAndWait("Book '" + book.getTitle() + "' added successfully", CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            booksView.showAlertAndWait("Error adding book: " + exception.getMessage(), ERROR);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Visar dialog och tar bort bok.
     * Krav F: Kräver att användaren är inloggad.
     */
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
                List<Book> books = booksDb.searchBooksByTitle(title);
                if (books.isEmpty()) {
                    throw new BooksDbException("No book found with title: " + title);
                }
                Book book = books.get(0);
                booksDb.deleteBook(book.getBookId());
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            try {
                refreshBookList();
                booksView.showAlertAndWait("Book '" + title + "' deleted successfully", CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait("Error refreshing list: " + ex.getMessage(), ERROR);
            }
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            if (ex instanceof BooksDbException) {
                booksView.showAlertAndWait(ex.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait("Error removing book: " + ex.getMessage(), ERROR);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Visar dialog och lägger till/uppdaterar betyg på bok.
     * Krav G: Max ett betyg per bok per användare.
     */
    public void updateGrade() {
        UpdateGradeDialog dialog = new UpdateGradeDialog();
        Optional<UpdateGradeDialog.GradeUpdate> result = dialog.showAndWait();

        if (!result.isPresent()) {
            booksView.showAlertAndWait("Grade Update Cancelled!!!", INFORMATION);
            return;
        }

        UpdateGradeDialog.GradeUpdate gradeUpdate = result.get();
        String title = gradeUpdate.getTitle();
        int newGrade = gradeUpdate.getGrade();

        if (title == null || title.trim().isEmpty()) {
            booksView.showAlertAndWait("Title cannot be empty", WARNING);
            return;
        }
        String trimmedTitle = title.trim();

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<Book> books = booksDb.searchBooksByTitle(trimmedTitle);
                if (books.isEmpty()) {
                    throw new BooksDbException("No book found with title: " + trimmedTitle);
                }
                Book book = books.get(0);

                // Skapa review med rating
                Review review = new Review();
                review.setBookId(book.getBookId());
                review.setUserId(currentUser.getUserID());
                review.setUsername(currentUser.getUsername());
                review.setRating(newGrade);
                review.setReviewText(""); // Rating only, no text
                review.setReviewDate(java.time.LocalDate.now().toString());
                
                booksDb.addReview(review);

                return null;
            }
        };

        task.setOnSucceeded(e -> {
            try {
                refreshBookList();
                booksView.showAlertAndWait(
                        "Rating for '" + trimmedTitle + "' set to " + newGrade + " (replaced previous rating)",
                        CONFIRMATION);
            } catch (BooksDbException ex) {
                booksView.showAlertAndWait(
                        "Error refreshing list: " + ex.getMessage(),
                        ERROR);
            }

        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait(
                        "Error updating grade: " + exception.getMessage(),
                        ERROR);
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Hjälpmetod för att ladda om alla böcker från databasen.
     */
    private void refreshBookList() throws BooksDbException {
        List<Book> books = booksDb.getAllBooks();
        booksView.displayBooks(books);
    }

    /**
     * Visar inloggningsdialog.
     */
    public void showLogin() {
        LoginDialog dialog = new LoginDialog(booksDb);
        Optional<User> result = dialog.showAndWait();
        result.ifPresent(user -> {
            currentUser = user;
            booksView.updateLoginStatus(user);
            booksView.showAlertAndWait("Welcome " + user.getUsername() + "!", INFORMATION);
        });
    }

    /**
     * Loggar ut användaren.
     */
    public void logout() {
        if (currentUser != null) {
            String username = currentUser.getUsername();
            currentUser = null;
            booksView.updateLoginStatus(null);
            booksView.showAlertAndWait("You have logged out, " + username + "!", INFORMATION);
        }
    }

    /**
     * Returnerar den nuvarande inloggade användaren.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kontrollerar om en användare är inloggad.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Lägger till recension för en bok.
     * Krav H: Kräver att användaren är inloggad.
     */
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
                review.setRating(0); // No rating, just review text
                review.setReviewText(reviewText);
                review.setReviewDate(java.time.LocalDate.now().toString());
                
                booksDb.addReview(review);
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            booksView.showAlertAndWait(
                    "Review for '" + bookTitle + "' has been added!",
                    CONFIRMATION);
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            if (exception instanceof BooksDbException) {
                booksView.showAlertAndWait(exception.getMessage(), WARNING);
            } else {
                booksView.showAlertAndWait(
                        "Error adding review: " + exception.getMessage(),
                        ERROR);
            }
            exception.printStackTrace();
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Visar detaljer om en bok inklusive författare och recensioner.
     * Krav D: Använder Alert för att visa detaljerad information.
     */
    public void showBookDetails(Book book) {
        Task<DetailsData> task = new Task<DetailsData>() {
            @Override
            protected DetailsData call() throws Exception {
                List<Review> reviews = booksDb.getReviewsForBook(book.getBookId());
                // Authors are already in the book object
                List<Author> authors = book.getAuthors();
                return new DetailsData(reviews, authors);
            }
        };

        task.setOnSucceeded(e -> {
            DetailsData data = task.getValue();
            List<Review> reviews = data.reviews;
            List<Author> authors = data.authors;

            StringBuilder content = new StringBuilder();


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

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Book details");
            alert.setHeaderText(book.getTitle());
            alert.setContentText(content.toString());

            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(400);

            alert.showAndWait();
        });

        task.setOnFailed(e -> {
            // Fallback: visa åtminstone författarnamn om det går fel
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

    /**
     * Hjälpklass för att bära både recensioner och författare.
     */
    private static class DetailsData {
        final List<Review> reviews;
        final List<Author> authors;

        DetailsData(List<Review> reviews, List<Author> authors) {
            this.reviews = reviews;
            this.authors = authors;
        }
    }
}