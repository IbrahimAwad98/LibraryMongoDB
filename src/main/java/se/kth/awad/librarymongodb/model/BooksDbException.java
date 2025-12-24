package se.kth.awad.librarymongodb.model;

/**
 * Denna exception-klass används för att hantera fel som uppstår vid kommunikation med databasen
 * Skilja databasfel från andra typer av fel
 * tydligare felmeddelanden
 */
public class BooksDbException extends Exception {

    public BooksDbException(String msg, Exception cause) {
        super(msg, cause);
    }

    public BooksDbException(String msg) {
        super(msg);
    }

    public BooksDbException() {
        super();
    }
}
