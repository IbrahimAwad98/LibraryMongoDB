package se.kth.awad.librarymongodb.model;

import java.time.LocalDate;

public class Review {
    private int reviewID;
    private int bookID;
    private User user;
    private String reviewText;
    private LocalDate reviewDate;

    public Review(int reviewID, int bookID, User user,
            String reviewText, LocalDate reviewDate) {
        this.reviewID = reviewID;
        this.bookID = bookID;
        this.user = user;
        this.reviewText = reviewText;
        this.reviewDate = reviewDate;
    }

    public Review(int bookID, User user, String reviewText, LocalDate reviewDate) {
        this(-1, bookID, user, reviewText, reviewDate);
    }

    public Review(int bookID, User user, String reviewText) {
        this(-1, bookID, user, reviewText, LocalDate.now());
    }

    public int getReviewID() {
        return reviewID;
    }

    public int getBookID() {
        return bookID;
    }

    public User getUser() {
        return user;
    }

    public String getReviewText() {
        return reviewText;
    }

    public LocalDate getReviewDate() {
        return reviewDate;
    }

    public void setReviewID(int reviewID) {
        this.reviewID = reviewID;
    }

    @Override
    public String toString() {
        return reviewText + "\n" + "- " + user.getUsername() + ", " + reviewDate;
    }

}
