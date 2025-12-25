package se.kth.awad.librarymongodb.model;

import java.util.List;

/**
 * Gränssnitt som definierar alla databasoperationer för bibliotekssystemet.
 * BooksDbMongo kommer att implementera detta gränssnitt.
 */
public interface BooksDbInterface {

    // Anslutning
    boolean connect(String database) throws BooksDbException;
    void disconnect() throws BooksDbException;

    // Sökning (READ operations)
    List<Book> getAllBooks() throws BooksDbException;
    List<Book> searchBooksByTitle(String title) throws BooksDbException;
    List<Book> searchBooksByISBN(String isbn) throws BooksDbException;
    List<Book> searchBooksByAuthor(String author) throws BooksDbException;
    List<Book> searchBooksByGenre(String genre) throws BooksDbException;
    List<Book> searchBooksByRating(int rating) throws BooksDbException;

    // CRUD för böcker
    void createBook(Book book) throws BooksDbException;
    Book readBook(int bookId) throws BooksDbException;
    void updateBook(Book book) throws BooksDbException;
    void deleteBook(int bookId) throws BooksDbException;

    // Review operations
    void addReview(Review review) throws BooksDbException;
    List<Review> getReviewsForBook(int bookId) throws BooksDbException;

    // User operations
    User getUserByUsername(String username) throws BooksDbException;
    void addUser(User user) throws BooksDbException;

    // Helper operations
    List<Author> getAllAuthors() throws BooksDbException;
    List<Genre> getAllGenres() throws BooksDbException;
}