package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/**
 * I MongoDB lagras detta som ett subdokument inom dokument.
 */
public class Genre {
    private int genreID;
    private String genreName;

    // för mongoDB ska konvertera dokument till ett genre-object
    public Genre(){}

    // Läser från databasen och har alla fält
    public Genre(int genreID, String name) {
        this.genreID = genreID;
        this.genreName = name;
    }

    //getters
    public int getGenreID() {
        return genreID;
    }
    public String getGenreName() {
        return genreName;
    }

    //setters
    public void setGenreID(int newID) {
        this.genreID = newID;
    }
    public void setGenreName(String newName) {
        this.genreName = newName;
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

    @Override
    public String toString() {
        return "Genre{" +
                "genreID=" + genreID +
                ", genreName='" + genreName + '\'' +
                '}';
    }
}
