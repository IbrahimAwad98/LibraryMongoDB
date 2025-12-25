package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/**
 * I MongoDB lagras detta i en separat REVIEWS-samling.
 */
public class Review {
    private int reviewId;
    private int bookId; //referens till Book
    private int userId; //referens till user
    private String username;
    private int rating;
    private String reviewText;
    private String reviewDate;

    // för mongoDb ska konvertera document till ett review-objekt
    public Review() {
    }

    // läser från databas
    public Review(int reviewId, int bookId, int userId, String username,
                  int rating, String reviewText, String reviewDate) {
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    //getters
    public int getReviewId() {
        return reviewId;
    }
    public int getBookId() {
        return bookId;
    }
    public int getUserId() {
        return userId;
    }
    public String getUsername() {
        return username;
    }
    public int getRating() {
        return rating;
    }
    public String getReviewText() {
        return reviewText;
    }
    public String getReviewDate() {
        return reviewDate;
    }

    //setters
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setRating(int rating) {
        this.rating = rating;
    }
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
    public void setReviewDate(String reviewDate) {
        this.reviewDate = reviewDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Review review = (Review) o;
        return reviewId == review.reviewId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewId);
    }

    @Override
    public String toString() {
        return username + " - Rating: " + rating + "/10";
    }
}