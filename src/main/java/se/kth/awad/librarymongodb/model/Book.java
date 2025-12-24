package se.kth.awad.librarymongodb.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Representerar en bok från T_Book-tabellen i LibraryDB.
 * Har relationer till Genre, Author och Rating.
 */
public class Book {
    private int bookID;
    private String ISBN, title;
    private Date publishedDate;
    // M:N-relationer (många-till-många)
    private ArrayList<Author> authors;
    private ArrayList<Genre> genres;
    private ArrayList<Review> reviews;
    // Betygsinformation från T_Rating
    private int averageRating;
    private int numRatings;
    // Användarspårning (Väl godkänd)
    private int addedByUserID;
    private String addedByUsername;

    /**
     * Konstruktor för att skapa bok från databasen (med ID).
     */
    public Book(int bookID,
            String ISBN,
            String title,
            Date publishedDate) {
        this.bookID = bookID;
        this.ISBN = ISBN;
        this.title = title;
        this.publishedDate = publishedDate;
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.averageRating = 0;
        this.numRatings = 0;
        this.reviews = new ArrayList<>();
    }

    /**
     * Konstruktor för att skapa ny bok (utan ID).
     */
    public Book(String ISBN, String title, Date publishedDate) {
        this(-1, ISBN, title, publishedDate);
    }

    public int getBookID() {
        return bookID;
    }

    public String getISBN() {
        return ISBN;
    }

    public String getTitle() {
        return title;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public int getAddedByUserID() {
        return addedByUserID;
    }

    public String getAddedByUsername() {
        return addedByUsername;
    }

    /**
     * Returnerar författarlistan (direkt referens för enkel modifiering).
     * 
     * @return lista över författare
     */
    public ArrayList<Author> getAuthors() {
        return authors;
    }

    public ArrayList<Genre> getGenres() {
        return genres;
    }

    public int getAverageRating() {
        return averageRating;
    }

    public int getNumRatings() {
        return numRatings;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setBookID(int newID) {
        this.bookID = newID;
    }

    public void setISBN(String newISBN) {
        this.ISBN = newISBN;
    }

    public void setTitle(String newTitle) {
        this.title = newTitle;
    }

    public void setPublishedDate(Date newDate) {
        this.publishedDate = newDate;
    }

    public void setAuthors(ArrayList<Author> newAuthors) {
        this.authors = newAuthors;
    }

    public void setGenres(ArrayList<Genre> genres) {
        this.genres = genres;
    }

    public void setAverageRating(int averageRating) {
        this.averageRating = averageRating;
    }

    public void setNumRatings(int numRatings) {
        this.numRatings = numRatings;
    }

    public void setReviews(ArrayList<Review> newReviews) {
        this.reviews = newReviews;
    }

    public void setAddedByUserID(int addedByUserID) {
        this.addedByUserID = addedByUserID;
    }

    public void setAddedByUsername(String addedByUsername) {
        this.addedByUsername = addedByUsername;
    }

    /**
     * Lägger till en författare om den inte redan finns (undviker dubbletter).
     */
    public void addAuthor(Author author) {
        boolean exists = false;
        for (Author existingAuthor : this.authors) {
            if (existingAuthor.getName().equalsIgnoreCase(author.getName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.authors.add(author);
        }
    }

    /**
     * Lägger till en genre om den inte redan finns (undviker dubbletter).
     */
    public void addGenre(Genre genre) {
        boolean exists = false;
        for (Genre existingGenre : this.genres) {
            if (existingGenre.getGenreName().equalsIgnoreCase(genre.getGenreName())) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.genres.add(genre);
        }
    }

    /**
     * Lägger till en recension om den inte redan finns (undviker dubbletter).
     */
    public void addReview(Review review) {
        boolean exists = false;
        for (Review existingReview : this.reviews) {
            if (existingReview.getReviewID() == review.getReviewID()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            this.reviews.add(review);
        }
    }

    /**
     * Returnerar författarnamn som en kommaseparerad sträng för visning i
     * TableView.
     */
    public String getAuthorsAsString() {
        if (authors.isEmpty()) {
            return "Unknown";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < authors.size(); i++) {
            sb.append(authors.get(i).getName());
            if (i < authors.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Returnerar genrenamn som en kommaseparerad sträng för visning i TableView.
     */
    public String getGenresAsString() {
        if (genres.isEmpty()) {
            return "Unknown";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            sb.append(genres.get(i).getGenreName());
            if (i < genres.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Returnerar recensioner som formaterad sträng (begränsad till 50 tecken per
     * recension).
     */
    public String getReviewsAsString() {
        if (reviews.isEmpty()) {
            return "No reviews yet";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < reviews.size(); i++) {
            Review r = reviews.get(i);
            sb.append(r.getReviewDate()).append(": ");
            sb.append(r.getReviewText().substring(0, Math.min(50, r.getReviewText().length())));
            if (r.getReviewText().length() > 50) {
                sb.append("...");
            }
            if (i < reviews.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    /**
     * Returnerar utgivningsåret som sträng.
     */
    public String getPublishedYear() {
        if (publishedDate == null) {
            return "N/A";
        }
        return String.valueOf(publishedDate.toLocalDate().getYear());
    }

    /**
     * Returnerar antalet recensioner för denna bok.
     */
    public int getNumReviews() {
        return reviews.size();
    }

    // Object-metoder (equals, hashCode, toString)
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Book book = (Book) obj;
        return bookID == book.bookID;
    }

    public int hashCode() {
        return Objects.hash(bookID);
    }

    public String toString() {
        return "Book{" +
                "bookID=" + bookID +
                ", ISBN='" + ISBN + '\'' +
                ", title='" + title + '\'' +
                ", avgRating=" + averageRating +
                ", numRatings=" + numRatings +
                ", numReviews=" + reviews.size() +
                ", authors=" + authors.size() +
                ", genres=" + genres.size() +
                '}';
    }
}