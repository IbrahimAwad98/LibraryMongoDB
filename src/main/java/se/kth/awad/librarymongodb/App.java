package se.kth.awad.librarymongodb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.kth.awad.librarymongodb.Controller.BookDBController;
import se.kth.awad.librarymongodb.model.BooksDb;
import se.kth.awad.librarymongodb.view.BooksPane;
import javafx.scene.image.Image;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {

        BooksDb booksDb = new BooksDb();
        BooksPane booksPane = new BooksPane();
        BookDBController controller = new BookDBController(booksDb, booksPane);

        booksPane.setController(controller);

        Scene scene = new Scene(booksPane, 1280, 720);
        primaryStage.setTitle("Books Database Client v1.0");

        Image logo = new Image(getClass().getResourceAsStream("/se/kth/awad/librarymongodb/logo.png"));
        primaryStage.getIcons().add(logo);

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