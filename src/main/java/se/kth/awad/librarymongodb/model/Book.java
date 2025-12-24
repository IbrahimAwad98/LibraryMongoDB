package se.kth.awad.librarymongodb.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a book in the library system.
 * In MongoDB, authors and genres are stored as subdocuments within the Book document.
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

    // Constructors
    public Book() {
        this.authors = new ArrayList<>();
        this.genres = new ArrayList<>();
    }

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

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public List<Author> getAuthors() {
        return authors;
    }

    public void setAuthors(List<Author> authors) {
        this.authors = authors;
    }

    public void addAuthor(Author author) {
        if (this.authors == null) {
            this.authors = new ArrayList<>();
        }
        this.authors.add(author);
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public void addGenre(Genre genre) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genre);
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(int reviewCount) {
        this.reviewCount = reviewCount;
    }

    // Helper method to get authors as comma-separated string
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

    // Helper method to get genres as comma-separated string
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
    public String toString() {
        return title + " by " + getAuthorsAsString() +
                " (Rating: " + String.format("%.1f", averageRating) + ")";
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
}