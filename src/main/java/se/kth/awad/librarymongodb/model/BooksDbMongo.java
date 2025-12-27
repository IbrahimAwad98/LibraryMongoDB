package se.kth.awad.librarymongodb.model;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * MongoDB-implementation av BooksDbInterface.
 * Hanterar all kommunikation med MongoDB-databasen.
 */
public class BooksDbMongo implements BooksDbInterface {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private static final String CONNECTION_STRING = "mongodb://appUser:AppPass2025!@localhost:27017/library?authSource=library";
    private static final String DATABASE_NAME = "library";
    private static final String BOOKS_COLLECTION = "BOOKS";
    private static final String REVIEWS_COLLECTION = "REVIEWS";
    private static final String USERS_COLLECTION = "USERS";
    private static final String COUNTERS_COLLECTION = "COUNTERS";

    @Override
    public boolean connect(String databaseName) throws BooksDbException {
        try {
            ConnectionString connectionString = new ConnectionString(CONNECTION_STRING);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .build();
            mongoClient = MongoClients.create(settings);
            database = mongoClient.getDatabase(DATABASE_NAME);

            database.listCollectionNames().first(); // lista alla collections och visa första (bok)

            createIndexes(); // skapa index här
            return true;
        } catch (Exception e) {
            throw new BooksDbException("Failed to connect to MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        try {
            if (mongoClient != null) {
                mongoClient.close(); // stäng anslutning
                mongoClient = null;
                database = null;
            }
        } catch (Exception e) {
            throw new BooksDbException("Error disconnecting from MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> getAllBooks() throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);
            for (Document doc : collection.find()) { // returnera dokument (ingen filter)
                books.add(documentToBook(doc)); // konvertera dokument till objekt (bok)
            }
        } catch (Exception e) {
            throw new BooksDbException("Error getting all books: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByTitle(String title) throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // anväder indexering
            Bson filter = Filters.text(title);

            for (Document doc : collection.find(filter)) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            throw new BooksDbException("Error searching books by title: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByISBN(String isbn) throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // med hjälp av indexering
            Bson filter = eq("ISBN", isbn);

            for (Document doc : collection.find(filter)) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            throw new BooksDbException("Error searching books by ISBN: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByAuthor(String author) throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // Regex search
            Bson filter = Filters.regex("authors.name", author, "i");

            for (Document doc : collection.find(filter)) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            throw new BooksDbException("Error searching books by author: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByGenre(String genre) throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // Regex search
            Bson filter = Filters.regex("genres.name", genre, "i");

            for (Document doc : collection.find(filter)) {
                books.add(documentToBook(doc));
            }
        } catch (Exception e) {
            throw new BooksDbException("Error searching books by genre: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public List<Book> searchBooksByRating(int rating) throws BooksDbException {
        checkConnection();
        List<Book> books = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);
            //hämta alla (behövs ingen filter)
            for (Document doc : collection.find()) {
                Book book = documentToBook(doc);
                if (Math.round(book.getAverageRating()) == rating) {
                    books.add(book);
                }
            }
        } catch (Exception e) {
            throw new BooksDbException("Error searching books by rating: " + e.getMessage(), e);
        }
        return books;
    }

    @Override
    public void createBook(Book book) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            if (book.getAddedByUserId() <= 0) {
                throw new BooksDbException("user id must be set before adding book");
            }

            // Hämta nästa bookId från COUNTERS
            int bookId = getNextSequence("book_id");
            book.setBookId(bookId);

            // Säkerställ att nya böcker börjar med tom recensionslista
            book.setAverageRating(0.0);
            book.setReviewCount(0);

            // Konvertera Book objekt till MongoDB Document
            Document document = bookToDocument(book);
            collection.insertOne(document);// spara i MongoDB
        } catch (Exception e) {
            throw new BooksDbException("Error adding book: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateBook(Book book) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);
            Bson filter = eq("book_id", book.getBookId());

            Document doc = bookToDocument(book);
            UpdateResult result = collection.replaceOne(filter, doc);

            if (result.getMatchedCount() == 0) {
                throw new BooksDbException("No book found with ID: " + book.getBookId());
            }
        } catch (Exception e) {
            throw new BooksDbException("Error updating book: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteBook(Book book) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            Bson filter = eq("book_id", book.getBookId());

            DeleteResult result = collection.deleteOne(filter);

            if (result.getDeletedCount() == 0) {
                throw new BooksDbException("No book found with ID: " + book.getBookId());
            }

        } catch (Exception e) {
            throw new BooksDbException("Error deleting book: " + e.getMessage(), e);
        }
    }

    @Override
    public Book readBook(int bookId) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // sök filter med equlas efter id.
            Bson filter = eq("book_id", bookId);

            // ta första och enda resultat
            Document doc = collection.find(filter).first();
            if (doc == null) {
                throw new BooksDbException("No book found with ID: " + bookId);
            }
            return documentToBook(doc);
        } catch (Exception e) {
            throw new BooksDbException("Error getting book by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void addReview(Review review) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(REVIEWS_COLLECTION);

            Bson existingFilter = Filters.and(
                    eq("book_id", review.getBookId()),
                    eq("user_id", review.getUserId()));
            Document existingReview = collection.find(existingFilter).first();

            if (existingReview != null) {
                // Uppdatera befintlig review
                int existingReviewId = existingReview.getInteger("review_id");
                review.setReviewId(existingReviewId);

                // Om det redan finns text i review behåll då
                String existingText = existingReview.getString("review_text");
                if (existingText != null && !existingText.isEmpty() && 
                    (review.getReviewText() == null || review.getReviewText().isEmpty())) {
                    review.setReviewText(existingText);
                }

                // Om det redan finns rating och ny review har rating = 0, behåll den gamla ratingen
                int existingRating = existingReview.getInteger("rating", 0);
                if (existingRating > 0 && review.getRating() == 0) {
                    review.setRating(existingRating);
                }

                Document doc = reviewToDocument(review);
                Bson updateFilter = eq("review_id", existingReviewId);
                collection.replaceOne(updateFilter, doc);
            } else {
                int reviewId = getNextSequence("review_id");
                review.setReviewId(reviewId);

                Document doc = reviewToDocument(review);
                collection.insertOne(doc);
            }

            // Uppdatera genomsnittsbetyg för boken
            updateBookAverageRating(review.getBookId());
        } catch (Exception e) {
            throw new BooksDbException("Error adding review: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Review> getReviewsForBook(int bookId) throws BooksDbException {
        checkConnection();
        List<Review> reviews = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(REVIEWS_COLLECTION);

            Bson filter = eq("book_id", bookId);

            for (Document doc : collection.find(filter)) {
                reviews.add(documentToReview(doc));
            }
        } catch (Exception e) {
            throw new BooksDbException("Error getting reviews for book: " + e.getMessage(), e);
        }
        return reviews;
    }

    @Override
    public User getUserByUsername(String username) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(USERS_COLLECTION);

            Bson filter = eq("username", username);

            Document doc = collection.find(filter).first();
            if (doc == null) {
                throw new BooksDbException("User not found: " + username);
            }
            return documentToUser(doc);
        } catch (Exception e) {
            throw new BooksDbException("Error getting user by username: " + e.getMessage(), e);
        }
    }

    public String getUsernameById(int userId) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(USERS_COLLECTION);
            Bson filter = eq("userID", userId);

            Document document = collection.find(filter).first();
            if (document == null) {
                return "Unknown";
            }

            return document.getString("username");
        } catch (Exception e) {
            throw new BooksDbException("Error getting username by ID: " + e.getMessage(), e);
        }
    }

    @Override
    public void addUser(User user) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(USERS_COLLECTION);

            // Kontrollera om användaren redan finns
            Bson filter = eq("username", user.getUsername());
            if (collection.find(filter).first() != null) {
                throw new BooksDbException("Username already exists: " + user.getUsername());
            }

            // Hämta nästa userId från COUNTERS
            int userId = getNextSequence("user_id");
            user.setUserID(userId);

            Document document = userToDocument(user);
            collection.insertOne(document);
        } catch (Exception e) {
            throw new BooksDbException(e.getMessage(), e);
        }
    }

    @Override
    public List<Author> getAllAuthors() throws BooksDbException {
        checkConnection();
        List<Author> authors = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // Hämta alla unika författare från alla böcker
            for (Document bookDoc : collection.find()) {

                List<Document> authorDocs = (List<Document>) bookDoc.get("authors");
                if (authorDocs != null) {
                    for (Document authorDoc : authorDocs) {
                        Author author = documentToAuthor(authorDoc);
                        // Lägg till om den inte redan finns
                        if (!authors.contains(author)) {
                            authors.add(author);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BooksDbException("Error getting all authors: " + e.getMessage(), e);
        }
        return authors;
    }

    @Override
    public List<Genre> getAllGenres() throws BooksDbException {
        checkConnection();
        List<Genre> genres = new ArrayList<>();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);

            // Hämta alla unika genrer från alla böcker
            for (Document bookDoc : collection.find()) {
                List<Document> genreDocs = (List<Document>) bookDoc.get("genres");
                if (genreDocs != null) {
                    for (Document genreDoc : genreDocs) {
                        Genre genre = documentToGenre(genreDoc);
                        // Lägg till om den inte redan finns
                        if (!genres.contains(genre)) {
                            genres.add(genre);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new BooksDbException("Error getting all genres: " + e.getMessage(), e);
        }
        return genres;
    }

    // Hjälp metoder (endast för denna klass)
    private void checkConnection() throws BooksDbException {
        if (database == null || mongoClient == null) {
            throw new BooksDbException("Not connected to database. Call connect() first.");
        }
    }

    private int getNextSequence(String sequenceName) throws BooksDbException {
        try {
            MongoCollection<Document> collection = database.getCollection(COUNTERS_COLLECTION);
            Bson filter = eq("_id", sequenceName);
            Bson update = Updates.inc("seq", 1); //öka seq med 1
            Document result = collection.findOneAndUpdate(filter, update);
            if (result == null) {
                // Skapa ny counter om den inte finns
                Document newCounter = new Document("_id", sequenceName).append("seq", 1);
                collection.insertOne(newCounter);
                return 1;
            }
            return result.getInteger("seq", 1);
        } catch (Exception e) {
            throw new BooksDbException("Error getting next sequence: " + e.getMessage(), e);
        }
    }

    private void updateBookAverageRating(int bookId) throws BooksDbException {
        try {
            MongoCollection<Document> reviewsCollection = database.getCollection(REVIEWS_COLLECTION);
            Bson filter = eq("book_id", bookId);

            List<Document> reviews = reviewsCollection.find(filter).into(new ArrayList<>());
            if (reviews.isEmpty()) {
                // ingen reviews då rating = 0
                MongoCollection<Document> booksCollection = database.getCollection(BOOKS_COLLECTION);
                Bson bookFilter = eq("book_id", bookId);
                Bson update = Updates.combine(
                        Updates.set("average_rating", 0.0),
                        Updates.set("rating_count", 0));
                booksCollection.updateOne(bookFilter, update);
                return;
            }

            double sum = 0;
            int ratingCount = 0;
            for (Document review : reviews) {
                int rating = review.getInteger("rating", 0);
                if (rating > 0) {
                    sum += rating; //bara rating > 0
                    ratingCount++;
                }
            }

            double average;
            if(ratingCount > 0){
                average = sum / ratingCount; //finns rate då beräkna genomsnitt
            }else {
                average = 0.0;
            }

            MongoCollection<Document> booksCollection = database.getCollection(BOOKS_COLLECTION);
            Bson bookFilter = eq("book_id", bookId);
            Bson update = Updates.combine(
                    Updates.set("average_rating", average),
                    Updates.set("rating_count", ratingCount));
            booksCollection.updateOne(bookFilter, update);
        } catch (Exception e) {
            throw new BooksDbException("Error updating book average rating: " + e.getMessage(), e);
        }
    }

    private Document bookToDocument(Book book) {
        Document document = new Document("book_id", book.getBookId())
                .append("ISBN", book.getIsbn())
                .append("title", book.getTitle())
                .append("published_date", book.getPublishedDate())
                .append("added_by_user_id", book.getAddedByUserId())
                .append("average_rating", book.getAverageRating())
                .append("rating_count", book.getReviewCount());

        // Lägg till författare
        List<Document> authorDocs = new ArrayList<>();
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                authorDocs.add(authorToDocument(author));
            }
        }
        document.append("authors", authorDocs);

        // Lägg till genrer
        List<Document> genreDocs = new ArrayList<>();
        if (book.getGenres() != null) {
            for (Genre genre : book.getGenres()) {
                genreDocs.add(genreToDocument(genre));
            }
        }
        document.append("genres", genreDocs);

        return document;
    }

    private Book documentToBook(Document doc) {
        Book book = new Book();
        book.setBookId(doc.getInteger("book_id", 0));
        book.setIsbn(doc.getString("ISBN"));
        book.setTitle(doc.getString("title"));
        book.setPublishedDate(doc.getString("published_date"));
        book.setAddedByUserId(doc.getInteger("added_by_user_id", 0));

        Object avgRating = doc.get("average_rating");
        if (avgRating instanceof Number) {
            book.setAverageRating(((Number) avgRating).doubleValue());
        } else {
            book.setAverageRating(0.0);
        }

        book.setReviewCount(doc.getInteger("rating_count", 0));

        // Lägg till författare
        List<Document> authorDocs = doc.getList("authors", Document.class);
        if (authorDocs != null) {
            for (Document authorDoc : authorDocs) {
                book.addAuthor(documentToAuthor(authorDoc));
            }
        }

        // Lägg till genrer
        List<Document> genreDocs = doc.getList("genres", Document.class);
        if (genreDocs != null) {
            for (Document genreDoc : genreDocs) {
                book.addGenre(documentToGenre(genreDoc));
            }
        }
        return book;
    }

    private Document authorToDocument(Author author) {
        return new Document("author_id", author.getAuthorID()).append("name", author.getName())
                .append("birth_date", author.getBirthDate());
    }

    private Author documentToAuthor(Document doc) {
        return new Author(doc.getInteger("author_id", 0), doc.getString("name"),
                doc.getString("birth_date"));
    }

    private Document genreToDocument(Genre genre) {
        return new Document("genre_id", genre.getGenreID()).append("name", genre.getGenreName());
    }

    private Genre documentToGenre(Document doc) {
        return new Genre(doc.getInteger("genre_id", 0), doc.getString("name"));
    }

    private Document reviewToDocument(Review review) {
        return new Document("review_id", review.getReviewId())
                .append("book_id", review.getBookId())
                .append("user_id", review.getUserId())
                .append("username", review.getUsername())
                .append("rating", review.getRating())
                .append("review_text", review.getReviewText())
                .append("review_date", review.getReviewDate());
    }

    private Review documentToReview(Document doc) {
        return new Review(
                doc.getInteger("review_id", 0),
                doc.getInteger("book_id", 0),
                doc.getInteger("user_id", 0),
                doc.getString("username"),
                doc.getInteger("rating", 0),
                doc.getString("review_text"),
                doc.getString("review_date"));
    }

    private Document userToDocument(User user) {
        return new Document("userID", user.getUserID()).append("username", user.getUsername());
    }

    private User documentToUser(Document doc) {
        return new User(doc.getInteger("userID", 0), doc.getString("username"));
    }

    // Skapar index för snabbare sökningar då körs automatiskt vid anslutning.
    private void createIndexes() {
        try {
            MongoCollection<Document> booksCollection = database.getCollection(BOOKS_COLLECTION);

            // indexering för varje sökningstyp
            booksCollection.createIndex(Indexes.text("title"));
            booksCollection.createIndex(Indexes.ascending("ISBN"));
            booksCollection.createIndex(Indexes.ascending("authors.name"));
            booksCollection.createIndex(Indexes.ascending("genres.name"));
            booksCollection.createIndex(Indexes.descending("average_rating"));
            booksCollection.createIndex(Indexes.ascending("book_id"), new IndexOptions().unique(true));

        } catch (Exception e) {
            System.err.println("Warning: Could not create indexes - " + e.getMessage());
        }
    }
}
