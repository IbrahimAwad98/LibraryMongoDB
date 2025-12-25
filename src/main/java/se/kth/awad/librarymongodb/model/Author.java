package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/**
 * Representerar en författare i bibliotekssystemet.
 * I MongoDB lagras detta som ett subdokument i ett dokument
 */
public class Author {
    private int authorID;
    private String name;
    private String birthDate;

    // för mongoDb ska konvertera document till ett Author-objekt
    public Author(){}

    // Läser från databasen och har alla fält
    public Author(int authorID, String name, String birthDate) {
        this.authorID = authorID;
        this.name = name;
        this.birthDate = birthDate;
    }
    // eftersom födelsedatum är optional
    public Author(int authorID, String name){
        this(authorID,name,null);
    }

    //getters
    public int getAuthorID() {
        return authorID;
    }
    public String getName() {
        return name;
    }
    public String getBirthDate() {
        return birthDate;
    }

    //setters
    public void setAuthorID(int authorID) {
        this.authorID = authorID;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Author author = (Author) o;
        return authorID == author.authorID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorID);
    }

    @Override
    public String toString() {
        return "Author{" +
                "authorID=" + authorID +
                ", name='" + name + '\'' +
                ", birthDate=" + birthDate +
                '}';
    }
}