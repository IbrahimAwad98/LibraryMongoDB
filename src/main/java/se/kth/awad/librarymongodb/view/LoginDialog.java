package se.kth.awad.librarymongodb.view;

import se.kth.awad.librarymongodb.model.BooksDbException;
import se.kth.awad.librarymongodb.model.BooksDbInterface;
import se.kth.awad.librarymongodb.model.User;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.application.Platform;

/**
 * Användare kan logga in eller skapa nytt konto.
 */
public class LoginDialog extends Dialog<User> {

    private final BooksDbInterface booksDb;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Label messageLabel;

    public LoginDialog(BooksDbInterface booksDb) {
        this.booksDb = booksDb;

        setTitle("Login");
        setHeaderText("Login or create new account");

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // Skapa layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(messageLabel, 1, 2);

        getDialogPane().setContent(grid);

        // Skapa knappar
        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Create", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, cancelButtonType);

        // Sätt fokus på användarnamnsfältet
        Platform.runLater(usernameField::requestFocus);

        // Hämta knappar för att kunna hantera klick
        Button loginButton = (Button) getDialogPane().lookupButton(loginButtonType);
        Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);

        // Förhindra att dialog stängs automatiskt, hantera klick manuellt
        loginButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleLogin();
        });

        registerButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleRegister();
        });

        // Resultat-konverterare (hanteras manuellt i handleLogin/handleRegister)
        setResultConverter(dialogButton -> null);
    }

    // Hanterar inloggning när användaren klickar "Log in"
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Fill in both username and password!");
            return;
        }

        try {
            // Hämta användare från databas (enkel inloggning utan lösenordskontroll)
            User user = booksDb.getUserByUsername(username);
            setResult(user);
            close();

        } catch (BooksDbException ex) {
            showError(ex.getMessage());
        }
    }

    // Hanterar registrering av nytt konto
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();


        if (username.isEmpty() || password.isEmpty()) {
            showError("Fill in both username and password!");
            return;
        }

        if (username.length() < 3) {
            showError("The username must be at least 3 characters long!");
            return;
        }

        if (password.length() < 4) {
            showError("The password must be at least 4 characters long!");
            return;
        }

        try {
            // Skapa nytt användarkonto
            User newUser = new User();
            newUser.setUsername(username);
            booksDb.addUser(newUser);

            // Hämta den nyskapade användaren och logga in automatiskt
            User user = booksDb.getUserByUsername(username);

            // Visa bekräftelsemeddelande
            showSuccess("Account created! You are now logged in as " + username);

            // Stäng dialogen efter kort fördröjning
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        setResult(user);
                        close();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (BooksDbException ex) {
            showError(ex.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }
}