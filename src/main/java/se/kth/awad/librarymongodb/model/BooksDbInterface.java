package se.kth.awad.librarymongodb.model;

import java.util.List;

/**
 * Interface för databaskommunikation med LibraryDB.
 * mer Flexibilitet och Testbarhet
 */
public interface BooksDbInterface {

    /**
     * Ansluter till en databas.
     * @param database databasnamnet att ansluta till
     * @return true om anslutningen lyckades, false annars
     * @throws BooksDbException om anslutningen misslyckas
     */
    boolean connect(String database) throws BooksDbException;

    /**
     * Kopplar bort från databasen.
     * Måste anropas när applikationen stängs!
     * @return true om nedkopplingen lyckades, false annars
     * @throws BooksDbException om nedkopplingen misslyckas
     */
    boolean disconnect() throws BooksDbException;

    /**
     * Hämtar alla böcker från databasen.
     * @return lista över alla böcker
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Book> retrieveBookTable() throws BooksDbException;

    /**
     * Söker efter böcker efter titel.
     * @param searchTitle titeln att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException;

    /**
     * Söker efter böcker efter ISBN.
     * @param searchISBN ISBN att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByISBN(String searchISBN) throws BooksDbException;

    /**
     * Söker efter böcker efter författare.
     * @param searchName författarens namn att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByAuthor(String searchName) throws BooksDbException;

    /**
     * Söker efter böcker efter genre.
     * @param searchGenre genren att söka efter
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByGenre(String searchGenre) throws BooksDbException;

    /**
     * Söker efter böcker efter minimum rating/betyg.
     * @param minRating minimibetyget
     * @return lista över matchande böcker
     * @throws BooksDbException om sökningen misslyckas
     */
    List<Book> searchBooksByRating(int minRating) throws BooksDbException;

    /**
     * Lägger till en ny bok i databasen.
     * @param book boken att lägga till
     * @throws BooksDbException om insättningen misslyckas
     */
    void insertBook(Book book) throws BooksDbException;

    /**
     * Tar bort en bok från databasen.
     * @param book boken att ta bort
     * @throws BooksDbException om borttagningen misslyckas
     */
    void deleteBook(Book book) throws BooksDbException;

    /**
     * Uppdaterar en bok i databasen.
     * @param book boken med uppdaterad information
     * @throws BooksDbException om uppdateringen misslyckas
     */
    void updateBook(Book book) throws BooksDbException;

    /**
     * Lägger till ett betyg för en bok.
     * @param bookID bokens ID
     * @param ratingValue betygsvärdet (1-10)
     * @return true om betyget lades till, false annars
     * @throws BooksDbException om insättningen misslyckas
     */
    boolean addRating(int bookID, int ratingValue) throws BooksDbException;

    /**
     * Hämtar alla författare för en specifik bok.
     * @param bookID bokens ID
     * @return lista över författare
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Author> getAuthorsForBook(int bookID) throws BooksDbException;

    /**
     * Hämtar alla författare från databasen.
     * @return lista över alla författare
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Author> getAllAuthors() throws BooksDbException;

    /**
     * Hämtar alla genrer från databasen.
     * @return lista över alla genrer
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Genre> getAllGenres() throws BooksDbException;

    /**
     * Hämtar alla genrer för en specifik bok.
     * @param bookID bokens ID
     * @return lista över genrer
     * @throws BooksDbException om hämtningen misslyckas
     */
    List<Genre> getGenresForBook(int bookID) throws BooksDbException;


    /**
     * Loggar in en användare med användarnamn och lösenord.
     * @param username användarnamnet
     * @param password lösenordet (hashas automatiskt i SQL)
     * @return User-objekt om inloggningen lyckas
     * @throws BooksDbException om fel användarnamn/lösenord eller databasfel
     */
    User login(String username, String password) throws BooksDbException;

    /**
     * Skapar en ny användare i systemet.
     * @param username användarnamnet (måste vara unikt)
     * @param password lösenordet (hashas automatiskt i SQL)
     * @throws BooksDbException om användarnamnet redan finns eller databasfel
     */
    void createUser(String username, String password) throws BooksDbException;

    /**
     * Betygsätter en bok som en specifik användare.
     * En användare kan endast ge ETT betyg per bok.
     * @param bookID bokens ID
     * @param userID användarens ID
     * @param ratingValue betygsvärdet (1-10)
     * @throws BooksDbException om användaren redan betygsatt boken eller databasfel
     */
    void rateBookAsUser(int bookID, int userID, int ratingValue) throws BooksDbException;

    /**
     * Kontrollerar om en användare redan har betygsatt en bok.
     * Användbart för att visa/dölja betygsättningsknappar i GUI.
     * @param bookID bokens ID
     * @param userID användarens ID
     * @return true om användaren redan har betygsatt boken, annars false
     * @throws BooksDbException om databasfel
     */
    boolean hasUserRatedBook(int bookID, int userID) throws BooksDbException;

    /**
     * Lägger till en recension för en bok.
     * @param bookID bokens ID
     * @param userID användarens ID (vem som skriver recensionen)
     * @param reviewText recensionstexten
     * @throws BooksDbException om recensionen är tom eller databasfel
     */
    void addReview(int bookID, int userID, String reviewText) throws BooksDbException;

    /**
     * Hämtar alla recensioner för en specifik bok.
     * @param bookID bokens ID
     * @return lista med recensioner (tom lista om inga recensioner finns)
     * @throws BooksDbException om databasfel
     */
    List<Review> getReviewsForBook(int bookID) throws BooksDbException;

    /**
     * Lägger till en ny bok med spårning av vem som lade till den.
     * @param book boken att lägga till
     * @param authorIDs lista med författar-ID:n
     * @param genreNames lista med genrenamn
     * @param userID användarens ID (vem som lägger till boken)
     * @throws BooksDbException om insättningen misslyckas
     */
    void insertBookAsUser(Book book, List<Integer> authorIDs, List<String> genreNames, int userID) throws BooksDbException;

    /**
     * Lägger till en ny författare med spårning av vem som lade till.
     * @param author författaren att lägga till
     * @param userID användarens ID (vem som lägger till författaren)
     * @throws BooksDbException om insättningen misslyckas
     */
    void insertAuthorAsUser(Author author, int userID) throws BooksDbException;

    /**
     * Tar bort en bok (kräver inloggad användare).
     * @param bookID bokens ID
     * @param userID användarens ID (för logging/audit)
     * @throws BooksDbException om borttagningen misslyckas
     */
    void deleteBookAsUser(int bookID, int userID) throws BooksDbException;
}