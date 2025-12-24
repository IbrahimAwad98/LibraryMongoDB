module se.kth.awad.librarymongodb {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;

    opens se.kth.awad.librarymongodb to javafx.fxml;
    opens se.kth.awad.librarymongodb.view to javafx.fxml;
    opens se.kth.awad.librarymongodb.Controller to javafx.fxml;
    exports se.kth.awad.librarymongodb;
    exports se.kth.awad.librarymongodb.model;
    exports se.kth.awad.librarymongodb.view;
    exports se.kth.awad.librarymongodb.Controller;
}