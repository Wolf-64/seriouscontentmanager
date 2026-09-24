module lightweightFX {
    requires static lombok;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires java.datatransfer;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires atlantafx.base;
    requires com.dlsc.gemsfx;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires java.desktop;
    requires org.kordamp.ikonli.fontawesome6;
    requires com.dlsc.atlantafx.themes;

    opens com.wlf.app.preferences to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.controls to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.util to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.themes to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.wlf.app to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.logging to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
}