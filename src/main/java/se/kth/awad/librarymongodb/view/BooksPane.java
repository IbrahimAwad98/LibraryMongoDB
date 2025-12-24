package se.kth.awad.librarymongodb.view;

import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    // Knappar som kräver inloggning
    private MenuItem addMenuItem;
    private MenuItem removeMenuItem;
    private MenuItem updateMenuItem;
    // Knappar som kräver för recension
    private MenuItem reviewMenuItem;

    public BooksPane() {}

    public void setController(BookDBController controller) {
        this.init(controller);
    }

    public void displayBooks(List<Book> books) {
        booksInTable.clear();
        booksInTable.addAll(books);
    }

    public void showAlertAndWait(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type, msg);
        alert.showAndWait();
    }

    private void init(BookDBController controller) {
        this.controller = controller;

        booksInTable = FXCollections.observableArrayList();
        BorderPane mainPane = new BorderPane();

        initBooksTable();
        initSearchView(controller);
        initMenus(controller);

        // Skapa login-controls
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

        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        TableColumn<Book, String> publishedCol = new TableColumn<>("Published");
        TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
        TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
        TableColumn<Book, String> authorsCol = new TableColumn<>("Authors");

        booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, genreCol, ratingCol, authorsCol);
        titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.25));
        isbnCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.20));
        publishedCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        genreCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        ratingCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.10));
        authorsCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.15));
        isbnCol.setMinWidth(140);
        ratingCol.setMinWidth(70);

        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("ISBN"));
        publishedCol.setCellValueFactory(new PropertyValueFactory<>("publishedDate"));
        genreCol.setCellValueFactory(new PropertyValueFactory<>("genresAsString"));
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

        // Välja en rad i tabellen och få upp detaljerad
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
        searchModeBox.setValue(SearchMode.Title);
        searchButton = new Button("Search");

        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SearchMode mode = searchModeBox.getValue();
                if(mode == SearchMode.Rating) {
                    double searchFor = Double.parseDouble(searchField.getText().trim());
                    controller.onSearchSelected(String.valueOf(searchFor), mode);
                    searchField.setPromptText("Search for...");
                } else {
                    String searchFor = searchField.getText();
                    controller.onSearchSelected(searchFor, mode);
                    searchField.setPromptText("Search for...");
                }
            }
        });
    }

    private void initMenus(BookDBController controller) {
        Menu fileMenu = new Menu("File");
        MenuItem connectItem = new MenuItem("Connect");
        MenuItem disconnectItem = new MenuItem("Disconnect");
        MenuItem exitItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(connectItem, disconnectItem, exitItem);

        connectItem.setOnAction(event -> {
            controller.connectToDb();
        });
        disconnectItem.setOnAction(event -> {
            controller.disconnect();
        });
        exitItem.setOnAction(event -> {
            Platform.exit();
        });

        Menu manageMenu = new Menu("Manage");

        // Spara referenser till menu items för att kunna aktivera/inaktivera dem
        addMenuItem = new MenuItem("Add");
        addMenuItem.setOnAction(event -> {
            controller.addBook();
        });

        removeMenuItem = new MenuItem("Remove");
        removeMenuItem.setOnAction(event -> {
            controller.removeBook();
        });

        updateMenuItem = new MenuItem("Update Grade");
        updateMenuItem.setOnAction(event -> {
            controller.updateGrade();
        });

        reviewMenuItem = new MenuItem("Add Review");
        reviewMenuItem.setOnAction(event -> {
            controller.addReview();
        });

        manageMenu.getItems().addAll(addMenuItem, removeMenuItem, updateMenuItem,reviewMenuItem);

        menuBar = new MenuBar();
        menuBar.getMenus().addAll(fileMenu, manageMenu);

        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("About");
            alert.setHeaderText("Book database application v1.0");
            alert.setContentText("Made by Ibrahim Awad & Ahmed El Yasini");
            alert.showAndWait();
        });
        helpMenu.getItems().add(aboutItem);
        menuBar.getMenus().add(helpMenu);
    }

    //Skapa och returnera login-controls
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

    // Hantera login/logout-knappen
    private void handleLoginButton() {
        if (controller.isLoggedIn()) {
            controller.logout();
        } else {
            controller.showLogin();
        }
    }

    // Uppdatera login-status i GUI (anropas från Controller)
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

    // Aktivera funktioner som kräver inloggning
    private void enableLoggedInFeatures() {
        if (addMenuItem != null) {
            addMenuItem.setDisable(false);
        }
        if (removeMenuItem != null) {
            removeMenuItem.setDisable(false);
        }
        if (updateMenuItem != null) {
            updateMenuItem.setDisable(false);
        }
        if (reviewMenuItem != null) {
            reviewMenuItem.setDisable(false);
        }
    }

    //login
    private void disableLoggedInFeatures() {
        if (addMenuItem != null) {
            addMenuItem.setDisable(true);
        }
        if (removeMenuItem != null) {
            removeMenuItem.setDisable(true);
        }
        if (updateMenuItem != null) {
            updateMenuItem.setDisable(true);
        }
        if (reviewMenuItem != null) {
            reviewMenuItem.setDisable(true);
        }
    }
}