package se.kth.awad.librarymongodb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.kth.awad.librarymongodb.Controller.BookDBController;
import se.kth.awad.librarymongodb.model.BooksDbMongo;
import se.kth.awad.librarymongodb.view.BooksPane;
import javafx.scene.image.Image;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        BooksDbMongo booksDb = new BooksDbMongo();
        BooksPane booksPane = new BooksPane();
        BookDBController controller = new BookDBController(booksDb, booksPane);

        booksPane.setController(controller);

        Scene scene = new Scene(booksPane, 1280, 720);
        primaryStage.setTitle("Library Database Client v2.0");

        // Lägg till logotyp om den finns
        try {
            java.io.InputStream logoStream = getClass().getResourceAsStream("/se/kth/awad/librarymongodb/Logo.png");
            if (logoStream != null) {
                Image logo = new Image(logoStream);
                primaryStage.getIcons().add(logo);
            }
        } catch (Exception e) {
            // Ignorera om logotypen inte finns
            System.out.println("Logo not found, continuing without icon");
        }

        primaryStage.setOnCloseRequest(event -> {
            try {
                booksDb.disconnect();
                System.out.println("Application closed, database disconnected");
            } catch (Exception e) {
                System.err.println("Error during shutdown: " + e.getMessage());
            }
        });
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}