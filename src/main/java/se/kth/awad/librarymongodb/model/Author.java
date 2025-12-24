package se.kth.awad.librarymongodb.model;

import java.util.Objects;

/**
 * Representerar en författare i bibliotekssystemet.
 * I MongoDB lagras detta som ett underdokument inom bokdokument.
 */
public class Author {
    private int authorID;
    private String name;
    private String birthDate;


    public Author(){}

    /**
     * Konstruktor för att skapa författare från databasen (med ID).
     */
    public Author(int authorID, String name, String birthDate) {
        this.authorID = authorID;
        this.name = name;
        this.birthDate = birthDate;
    }

    public Author(int authorID, String name){
        this(authorID,name,null);
    }


    public int getAuthorID() {
        return authorID;
    }

    public String getName() {
        return name;
    }

    public String getBirthDate() {
        return birthDate;
    }

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