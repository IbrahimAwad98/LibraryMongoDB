package se.kth.awad.librarymongodb.view;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import se.kth.awad.librarymongodb.Controller.BookDBController;
import se.kth.awad.librarymongodb.model.*;
import javafx.application.Platform;
import se.kth.awad.librarymongodb.model.User;

/**
 * Huvudvyn för applikationen.
 * Visar tabell med böcker, sökfunktioner och menyer.
 */
public class BooksPane extends VBox {

    private TableView<Book> booksTable;
    private ObservableList<Book> booksInTable;
    private ComboBox<SearchMode> searchModeBox;
    private TextField searchField;
    private Button searchButton;
    private MenuBar menuBar;
    private Button loginButton;
    private Label userStatusLabel;
    private BookDBController controller;
    // Menyalternativ som kräver inloggning
    private MenuItem addMenuItem;
    private MenuItem removeMenuItem;
    private MenuItem updateMenuItem;
    private MenuItem reviewMenuItem;

    public BooksPane() {
    }

    // Sätter controller och initialiserar vyn
    public void setController(BookDBController controller) {
        this.init(controller);
    }

    // Uppdaterar tabellen med nya böcker
    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    public void showAlertAndWait(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    // Initialiserar hela vyn
    private void init(BookDBController controller) {
        this.controller = controller;

        booksInTable = FXCollections.observableArrayList();
        BorderPane mainPane = new BorderPane();

        initBooksTable();
        initSearchView(controller);
        initMenus(controller);

        HBox loginBox = setupLoginControls();

        FlowPane bottomPane = new FlowPane();
        bottomPane.setHgap(10);
        bottomPane.setPadding(new Insets(10, 10, 10, 10));
        bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

        mainPane.setCenter(booksTable);
        mainPane.setBottom(bottomPane);
        mainPane.setPadding(new Insets(10, 10, 10, 10));

        this.getChildren().addAll(menuBar, loginBox, mainPane);
        VBox.setVgrow(mainPane, Priority.ALWAYS);

        // Starta med inaktiverade funktioner (ej inloggad)
        disableLoggedInFeatures();
    }

    private void initBooksTable() {
        booksTable = new TableView<>();
        booksTable.setEditable(false);
        booksTable.setPlaceholder(new Label("No rows to display"));

        // Skapa kolumner för tabellen
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, String> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, String> authorsCol = new TableColumn<>("Authors");

        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, genreCol, ratingCol, authorsCol);

        // Sätt kolumnbredder som procent av tabellbredd
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.25));
        isbnCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.20));
        publishedCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        genreCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        ratingCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.10));
        authorsCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        isbnCol.setMinWidth(140);
        ratingCol.setMinWidth(70);

        // Koppla kolumner till Book-objektets egenskaper
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genresAsString"));
        // Rating behöver specialhantering för att runda av till heltal
        ratingCol.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            return new javafx.beans.property.SimpleObjectProperty<>((int) Math.round(book.getAverageRating()));
        });
        authorsCol.setCellValueFactory(new PropertyValueFactory<>("authorsAsString"));

        ratingCol.setCellFactory(column -> new TableCell<Book, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(item));
                }
            }
        });

        booksTable.setItems(booksInTable);

        // När användaren klickar på en rad, visa detaljer
        TableView.TableViewSelectionModel<Book> selectionModel = booksTable.getSelectionModel();
        selectionModel.setSelectionMode(SelectionMode.SINGLE);
        selectionModel.selectedItemProperty().addListener((obs, oldBook, newBook) -> {
            if (newBook != null && controller != null) {
                controller.showBookDetails(newBook);
            }
        });
    }

    private void initSearchView(BookDBController controller) {
        searchField = new TextField();
        searchField.setPromptText("Search for...");
        searchModeBox = new ComboBox<>();
        searchModeBox.getItems().addAll(SearchMode.values());
        searchModeBox.setValue(SearchMode.Title); // Standard: sök på titel
        searchButton = new Button("Search");

        // När sökknappen klickas, skicka sökning till controller
        searchButton.setOnAction(e -> {
            SearchMode mode = searchModeBox.getValue();
            // Om rating-sökning, konvertera till nummer
            String searchFor;
            if (mode == SearchMode.Rating) {
                String input = searchField.getText().trim();
                double ratingValue = Double.parseDouble(input);
                searchFor = String.valueOf(ratingValue);
            } else {
                searchFor = searchField.getText();
            }
            controller.onSearchSelected(searchFor, mode);
        });
    }

    // Skapar menyer med alla menyval
    private void initMenus(BookDBController controller) {
        Menu fileMenu = new Menu("File");
        MenuItem connectItem = new MenuItem("Connect");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(connectItem, disconnectItem, exitItem);

        connectItem.setOnAction(e -> controller.connectToDatabase());
        disconnectItem.setOnAction(e -> controller.disconnect());
        exitItem.setOnAction(e -> Platform.exit());

        Menu manageMenu = new Menu("Manage");
        addMenuItem = new MenuItem("Add book");
        addMenuItem.setOnAction(e -> controller.addBook());
        removeMenuItem = new MenuItem("Remove book");
        removeMenuItem.setOnAction(e -> controller.removeBook());
        updateMenuItem = new MenuItem("Update Rating");
        updateMenuItem.setOnAction(e -> controller.updateGrade());
        reviewMenuItem = new MenuItem("Add Review");
        reviewMenuItem.setOnAction(e -> controller.addReview());
        manageMenu.getItems().addAll(addMenuItem, removeMenuItem, updateMenuItem, reviewMenuItem);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, manageMenu);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Library application v2.0");
            alert.setContentText("Made by Ibrahim Awad & Ahmed El Yasini");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);
    }

    // Skapar inloggningskontroller (label och knapp)
    private HBox setupLoginControls() {
        userStatusLabel = new Label("Not logged in");
        userStatusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        loginButton = new Button("Login");
        loginButton.setOnAction(e -> handleLoginButton());

        HBox loginBox = new HBox(10);
        loginBox.setPadding(new Insets(10));
        loginBox.setAlignment(Pos.CENTER_LEFT);
        loginBox.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 0 1 0;");
        loginBox.getChildren().addAll(userStatusLabel, loginButton);

        return loginBox;
    }

    // Hanterar klick på login/logout-knappen
    private void handleLoginButton() {
        if (controller.isLoggedIn()) {
            controller.logout();
        } else {
            controller.showLogin();
        }
    }

    // Uppdaterar inloggningsstatus i GUI (anropas från Controller)
    public void updateLoginStatus(User user) {
        if (user != null) {
            userStatusLabel.setText("Login as: " + user.getUsername());
            loginButton.setText("Logout");
            enableLoggedInFeatures();
        } else {
            userStatusLabel.setText("Not logged in");
            loginButton.setText("Login");
            disableLoggedInFeatures();
        }
    }

    // Aktiverar funktioner som kräver inloggning
    private void enableLoggedInFeatures() {
        setMenuItemsEnabled(false);
    }

    // Inaktiverar funktioner som kräver inloggning
    private void disableLoggedInFeatures() {
        setMenuItemsEnabled(true);
    }

    // Sätter om menyval ska vara aktiverade eller inaktiverade
    private void setMenuItemsEnabled(boolean disabled) {
        if (addMenuItem != null)
            addMenuItem.setDisable(disabled);
        if (removeMenuItem != null)
            removeMenuItem.setDisable(disabled);
        if (updateMenuItem != null)
            updateMenuItem.setDisable(disabled);
        if (reviewMenuItem != null)
            reviewMenuItem.setDisable(disabled);
    }
}