package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/**
 * Representerar en genre från T_Genre tabellen i LibraryDB.
 */
public class Genre {
    private int genreID;
    private String genreName;

    // Konstruktor för att läsa från databasen (med ID).
    public Genre(int genreID, String name) {
        this.genreID = genreID;
        this.genreName = name;
    }

    public Genre(String genreName) {
        this(-1, genreName); // inget id än
    }

    public int getGenreID() {
        return genreID;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreID(int newID) {
        this.genreID = newID;
    }

    public void setGenreName(String newName) {
        this.genreName = newName;
    }

    public String toString() {
        return genreName;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Genre genre = (Genre) obj;
        return genreID == genre.genreID;
    }

    public int hashCode() {
        return Objects.hash(genreID);
    }
}
