module se.kth.awad.librarymongodb {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.bson;


    opens se.kth.awad.librarymongodb to javafx.fxml;
    exports se.kth.awad.librarymongodb;
}