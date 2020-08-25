module ffmpeg.gui.converter {
    requires javafx.controls;
    requires javafx.fxml;
    opens converter;
    opens converter.model;
    opens converter.controller;
}
