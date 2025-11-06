module seriouscontentmanager {
    requires java.sql;
    requires java.logging;
    requires java.net.http;
    requires java.desktop;
    requires java.naming;
    requires java.datatransfer;
    requires static lombok;
    requires javafx.base;
    requires javafx.fxml;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.media;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires atlantafx.base;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires org.kordamp.ikonli.fontawesome6;
    requires org.apache.commons.exec;
    requires org.apache.commons.io;
    requires org.xerial.sqlitejdbc;
    requires jdk.zipfs;
    requires jakarta.persistence;
    requires jakarta.transaction;
    requires org.mapstruct;

    opens com.wlf.app.preferences to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.controls to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.util to com.fasterxml.jackson.databind,javafx.fxml;
    opens com.wlf.common.themes to com.fasterxml.jackson.databind, javafx.fxml;
    opens com.wlf.app to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main.data;
    opens com.wlf.app.main.io to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;
    opens com.wlf.app.main.net to com.fasterxml.jackson.databind, javafx.fxml, javafx.graphics;

}