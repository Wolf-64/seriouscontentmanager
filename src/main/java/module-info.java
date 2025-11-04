module seriouscontentmanager {
    requires atlantafx.base;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires java.datatransfer;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires static lombok;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;
    requires org.slf4j;
    requires java.desktop;
    requires org.kordamp.ikonli.fontawesome6;
    requires javafx.web;
    requires java.sql;
    requires java.net.http;
    requires org.apache.commons.exec;
    requires java.logging;
    requires org.apache.commons.io;

    opens com.wlf.app.preferences to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.controls to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.util to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.themes to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.wlf.app to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main.data to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics, javafx.base;
    opens com.wlf.app.main.io to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main.net to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
}