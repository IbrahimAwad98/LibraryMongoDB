package se.kth.awad.librarymongodb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representerar en bok i biblioteket
 * där authors, genres är subdokument i den här dokument (bok klass).
 */
public class Book {
    private int bookId;
    private String isbn;
    private String title;
    private String publishedDate;
    private List<Author> authors;
    private List<Genre> genres;
    private double averageRating;
    private int reviewCount;
    private int addedByUserId;

    // för mongoDb ska konvertera document till ett bok-objekt
    public Book() {
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.averageRating = 0.0;
        this.reviewCount = 0;
    }

    // löser från databas
    public Book(int bookId, String isbn, String title, String publishedDate) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.publishedDate = publishedDate;
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
        this.averageRating = 0.0;
        this.reviewCount = 0;
    }

    //getters
    public int getBookId() {
        return bookId;
    }
    public String getIsbn() {
        return isbn;
    }
    public String getTitle() {
        return title;
    }
    public String getPublishedDate() {
        return publishedDate;
    }
    public List<Author> getAuthors() {
        return authors;
    }
    public List<Genre> getGenres() {
        return genres;
    }
    public double getAverageRating() {
        return averageRating;
    }
    public int getReviewCount() {
        return reviewCount;
    }
    public int getAddedByUserId(){return addedByUserId;}

    //setters
    public void setBookId(int bookId) {this.bookId = bookId;}
    public void setIsbn(String isbn) {this.isbn = isbn;}
    public void setTitle(String title) {this.title = title;}
    public void setPublishedDate(String publishedDate) {this.publishedDate = publishedDate;}
    public void setAuthors(List<Author> authors) {this.authors = authors;}
    public void setGenres(List<Genre> genres) {this.genres = genres;}
    public void setAverageRating(double averageRating) {this.averageRating = averageRating;}
    public void setReviewCount(int reviewCount) {this.reviewCount = reviewCount;}
    public void setAddedByUserId(int addedByUserId){this.addedByUserId = addedByUserId;}

    // lägger till författare i listan
    public void addAuthor(Author author) {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        this.authors.add(author);
    }

    // lägger till genre i listan
    public void addGenre(Genre genre) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genre);
    }

    //Rreturnerar alla författare som en sträng för GUI
    public String getAuthorsAsString() {
        if (authors == null || authors.isEmpty()) {
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

    // Returnerar alla genre som en sträng för GUI
    public String getGenresAsString() {
        if (genres == null || genres.isEmpty()) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return bookId == book.bookId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }

    @Override
    public String toString() {
        return title + " by " + getAuthorsAsString() +
                " (Rating: " + String.format("%.1f", averageRating) + ")";
    }
}