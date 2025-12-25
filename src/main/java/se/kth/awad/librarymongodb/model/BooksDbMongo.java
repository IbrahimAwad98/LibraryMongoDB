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
import java.util.regex.Pattern;

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

            database.listCollectionNames().first(); //lista alla collections och visa första (bok)

            createIndexes(); //skapa index här
            return true;
        } catch (Exception e) {
            throw new BooksDbException("Failed to connect to MongoDB: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws BooksDbException {
        try {
            if (mongoClient != null) {
                mongoClient.close(); //stäng anslutning
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
            for (Document doc : collection.find()) { //returnera dokument (ingen filter)
                books.add(documentToBook(doc)); //konvertera dokument till javabok
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

            //anväder indexering istället än regex
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

            //med hjälp av indexering
            Bson filter = Filters.eq("ISBN", isbn);

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

            //Regex search
            Bson filter = Filters.regex("genres.genreName", genre, "i");

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

            //Greater than or equal
            Bson filter = Filters.gte("averageRating", rating);

            for (Document doc : collection.find(filter)) {
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
            
            // Hämta nästa bookId från COUNTERS
            int bookId = getNextSequence("bookId");
            book.setBookId(bookId);

            Document doc = bookToDocument(book);
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new BooksDbException("Error adding book: " + e.getMessage(), e);
        }
    }

    @Override
    public void updateBook(Book book) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);
            Bson filter = Filters.eq("bookId", book.getBookId());

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
    public void deleteBook(int bookId) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(BOOKS_COLLECTION);
            Bson filter = Filters.eq("bookId", bookId);

            DeleteResult result = collection.deleteOne(filter);
            if (result.getDeletedCount() == 0) {
                throw new BooksDbException("No book found with ID: " + bookId);
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
            Bson filter = Filters.eq("bookId", bookId);

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
            
            // Hämta nästa reviewId från COUNTERS
            int reviewId = getNextSequence("reviewId");
            review.setReviewId(reviewId);

            Document doc = reviewToDocument(review);
            collection.insertOne(doc);

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
            Bson filter = Filters.eq("bookId", bookId);

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
            Bson filter = Filters.eq("username", username);

            Document doc = collection.find(filter).first();
            if (doc == null) {
                throw new BooksDbException("User not found: " + username);
            }
            return documentToUser(doc);
        } catch (Exception e) {
            throw new BooksDbException("Error getting user by username: " + e.getMessage(), e);
        }
    }

    @Override
    public void addUser(User user) throws BooksDbException {
        checkConnection();
        try {
            MongoCollection<Document> collection = database.getCollection(USERS_COLLECTION);
            
            // Kontrollera om användaren redan finns
            Bson filter = Filters.eq("username", user.getUsername());
            if (collection.find(filter).first() != null) {
                throw new BooksDbException("Username already exists: " + user.getUsername());
            }

            // Hämta nästa userId från COUNTERS
            int userId = getNextSequence("userId");
            user.setUserID(userId);

            Document doc = userToDocument(user);
            collection.insertOne(doc);
        } catch (Exception e) {
            throw new BooksDbException("Error adding user: " + e.getMessage(), e);
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
                @SuppressWarnings("unchecked")
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
                @SuppressWarnings("unchecked")
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
            Bson filter = Filters.eq("_id", sequenceName);
            Bson update = Updates.inc("seq", 1);
            
            Document result = collection.findOneAndUpdate(filter, update);
            if (result == null) {
                // Skapa ny counter om den inte finns
                Document newCounter = new Document("_id", sequenceName)
                        .append("seq", 1);
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
            Bson filter = Filters.eq("bookId", bookId);

            List<Document> reviews = reviewsCollection.find(filter).into(new ArrayList<>());
            if (reviews.isEmpty()) {
                return;
            }

            double sum = 0;
            for (Document review : reviews) {
                sum += review.getInteger("rating", 0);
            }
            double average = sum / reviews.size();

            MongoCollection<Document> booksCollection = database.getCollection(BOOKS_COLLECTION);
            Bson bookFilter = Filters.eq("bookId", bookId);
            Bson update = Updates.combine(
                    Updates.set("averageRating", average),
                    Updates.set("reviewCount", reviews.size())
            );
            booksCollection.updateOne(bookFilter, update);
        } catch (Exception e) {
            throw new BooksDbException("Error updating book average rating: " + e.getMessage(), e);
        }
    }

    private Document bookToDocument(Book book) {
        Document doc = new Document("bookId", book.getBookId())
                .append("ISBN", book.getIsbn())
                .append("title", book.getTitle())
                .append("publishedDate", book.getPublishedDate())
                .append("averageRating", book.getAverageRating())
                .append("reviewCount", book.getReviewCount());

        // Lägg till författare
        List<Document> authorDocs = new ArrayList<>();
        if (book.getAuthors() != null) {
            for (Author author : book.getAuthors()) {
                authorDocs.add(authorToDocument(author));
            }
        }
        doc.append("authors", authorDocs);

        // Lägg till genrer
        List<Document> genreDocs = new ArrayList<>();
        if (book.getGenres() != null) {
            for (Genre genre : book.getGenres()) {
                genreDocs.add(genreToDocument(genre));
            }
        }
        doc.append("genres", genreDocs);

        return doc;
    }

    private Book documentToBook(Document doc) {
        Book book = new Book();
        book.setBookId(doc.getInteger("bookId", 0));
        book.setIsbn(doc.getString("ISBN"));
        book.setTitle(doc.getString("title"));
        book.setPublishedDate(doc.getString("publishedDate"));
        
        Object avgRating = doc.get("averageRating");
        if (avgRating instanceof Number) {
            book.setAverageRating(((Number) avgRating).doubleValue());
        } else {
            book.setAverageRating(0.0);
        }
        
        book.setReviewCount(doc.getInteger("reviewCount", 0));

        // Lägg till författare
        @SuppressWarnings("unchecked")
        List<Document> authorDocs = (List<Document>) doc.get("authors");
        if (authorDocs != null) {
            for (Document authorDoc : authorDocs) {
                book.addAuthor(documentToAuthor(authorDoc));
            }
        }

        // Lägg till genrer
        @SuppressWarnings("unchecked")
        List<Document> genreDocs = (List<Document>) doc.get("genres");
        if (genreDocs != null) {
            for (Document genreDoc : genreDocs) {
                book.addGenre(documentToGenre(genreDoc));
            }
        }

        return book;
    }

    private Document authorToDocument(Author author) {
        return new Document("authorID", author.getAuthorID())
                .append("name", author.getName())
                .append("birthDate", author.getBirthDate());
    }

    private Author documentToAuthor(Document doc) {
        return new Author(
                doc.getInteger("authorID", 0),
                doc.getString("name"),
                doc.getString("birthDate")
        );
    }

    private Document genreToDocument(Genre genre) {
        return new Document("genreID", genre.getGenreID())
                .append("genreName", genre.getGenreName());
    }

    private Genre documentToGenre(Document doc) {
        return new Genre(
                doc.getInteger("genreID", 0),
                doc.getString("genreName")
        );
    }

    private Document reviewToDocument(Review review) {
        return new Document("reviewId", review.getReviewId())
                .append("bookId", review.getBookId())
                .append("userId", review.getUserId())
                .append("username", review.getUsername())
                .append("rating", review.getRating())
                .append("reviewText", review.getReviewText())
                .append("reviewDate", review.getReviewDate());
    }

    private Review documentToReview(Document doc) {
        return new Review(
                doc.getInteger("reviewId", 0),
                doc.getInteger("bookId", 0),
                doc.getInteger("userId", 0),
                doc.getString("username"),
                doc.getInteger("rating", 0),
                doc.getString("reviewText"),
                doc.getString("reviewDate")
        );
    }

    private Document userToDocument(User user) {
        return new Document("userID", user.getUserID())
                .append("username", user.getUsername());
    }

    private User documentToUser(Document doc) {
        return new User(
                doc.getInteger("userID", 0),
                doc.getString("username")
        );
    }

    /**
     * Skapar index för snabbare sökningar.
     * Körs automatiskt vid anslutning.
     */
    private void createIndexes(){
        try{
            MongoCollection<Document> booksCollection = database.getCollection(BOOKS_COLLECTION);


            booksCollection.createIndex(Indexes.text("title"));
            booksCollection.createIndex(Indexes.ascending("ISBN"));
            booksCollection.createIndex(Indexes.ascending("authors.name"));
            booksCollection.createIndex(Indexes.ascending("genres.genreName"));
            booksCollection.createIndex(Indexes.descending("averageRating"));
            booksCollection.createIndex(Indexes.ascending("bookId"), //vara unik
                    new IndexOptions().unique(true)
            );

            System.out.println("Indexes created successfully!");


        }catch (Exception e){
            System.err.println("Warning: Could not create indexes - " + e.getMessage());
        }
    }

}
