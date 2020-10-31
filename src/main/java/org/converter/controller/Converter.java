package org.converter.controller;

import org.converter.model.MediaFile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Converter {

    @FXML
    private TableView videoTable;

    @FXML
    private TableColumn<Object, Object> titleColumn;

    @FXML
    private TableColumn<Object, Object> durationColumn;

    @FXML
    private TableColumn<Object, Object> typeColumn;

    @FXML
    private TableColumn<Object, Object> informationColumn;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private MenuItem convertMenuItem;

    public Converter() { }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        informationColumn.setCellValueFactory(new PropertyValueFactory<>("information"));

        MediaFile mf = new MediaFile("A", "B", "C", "D");
        videoTable.getItems().add(mf);
    }

    @FXML
    public void onImportFiles(ActionEvent e) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
        List<File> files = chooser.showOpenMultipleDialog(new Stage());
        for (File file: files) {

            String[] cmd={"C:\\ffmpeg\\ffprobe.exe", "-v", "quiet", "-print_format", "json", "-show_format", file.getAbsolutePath()};

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            MediaFile mf = new MediaFile(file.getName(), "B", "C", "D");
            videoTable.getItems().add(mf);
        }
    }
}
