package se.kth.awad.librarymongodb.view;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import se.kth.awad.librarymongodb.model.BooksDbException;
import se.kth.awad.librarymongodb.model.BooksDbInterface;
import se.kth.awad.librarymongodb.model.User;
import javafx.application.Platform;

/**
 * Dialog för inloggning och registrering (VG-funktionalitet).
 */
public class LoginDialog extends Dialog<User> {

    private final BooksDbInterface booksDb;
    private final TextField usernameField;
    private final PasswordField passwordField;
    private final Label messageLabel;

    public LoginDialog(BooksDbInterface booksDb) {
        this.booksDb = booksDb;

        setTitle("Log in");
        setHeaderText("Log in or create new account");

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        // Layout
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

        // Knappar
        ButtonType loginButtonType = new ButtonType("Log in", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Create", ButtonBar.ButtonData.OTHER);
        ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, cancelButtonType);

        // Fokusera på username
        Platform.runLater(() -> usernameField.requestFocus());

        // Hämta knappar
        Button loginButton = (Button) getDialogPane().lookupButton(loginButtonType);
        Button registerButton = (Button) getDialogPane().lookupButton(registerButtonType);

        // Förhindra att dialog stängs automatiskt
        loginButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleLogin();
        });

        registerButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            event.consume();
            handleRegister();
        });

        // Resultat-konverterare
        setResultConverter(dialogButton -> {
            return null;
        });
    }


     // Hanterar inloggning
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validering
        if (username.isEmpty() || password.isEmpty()) {
            showError("Fill in both username and password!");
            return;
        }

        try {
            // Försök hämta användare (enkel inloggning utan lösenordskontroll i MongoDB)
            User user = booksDb.getUserByUsername(username);

            // Lyckad inloggning!
            setResult(user);
            close();

        } catch (BooksDbException ex) {
            showError("Invalid username or user not found");
        }
    }

    // Hanterar registrering av nytt konto
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validering
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
            // Skapa nytt konto
            User newUser = new User();
            newUser.setUsername(username);
            booksDb.addUser(newUser);

            // Logga in automatiskt efter registrering
            User user = booksDb.getUserByUsername(username);

            // Visa bekräftelse
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

     //Visar felmeddelande
    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }

     //Visar success-meddelande
    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        messageLabel.setText(message);
    }
}