module com.schedule.demolist {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires kotlin.stdlib;
    requires kotlinx.serialization.core.jvm;
    requires kotlinx.serialization.json.jvm;
    requires java.net.http;
    requires fuel;
    requires com.google.gson;
    requires fuel.gson;
    requires org.controlsfx.controls;
    requires java.desktop;
    requires result;

    opens com.schedule.demolist to javafx.fxml;
    exports com.schedule.demolist;
    exports com.schedule.demolist.persistence.schemas;
    exports com.schedule.demolist.model;
    opens com.schedule.demolist.persistence.schemas to com.google.gson;
}