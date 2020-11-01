package org.converter.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import netscape.javascript.JSObject;
import org.converter.model.MediaFile;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.cert.Extension;
import java.util.List;

public class Converter {

    @FXML
    private TableView videoTable;

    @FXML
    private TableColumn<Object, Object> titleColumn;

    @FXML
    private TableColumn<Object, Object> durationColumn;

    @FXML
    private TableColumn<Object, Object> fileSizeColumn;

    @FXML
    private TableColumn<Object, Object> fileFormatColumn;

    @FXML
    private TableColumn<Object, Object> videoCodecColumn;

    @FXML
    private TableColumn<Object, Object> informationColumn;

    @FXML
    private ComboBox qualityComboBox;

    @FXML
    private ComboBox framerateComboBox;

    @FXML
    private ComboBox keyframeComboBox;

    @FXML
    private ComboBox videoCodecComboBox;

    @FXML
    private ComboBox bitrateVideoComboBox;

    @FXML
    private ComboBox audioCodecComboBox;

    @FXML
    private ComboBox bitrateAudioComboBox;

    @FXML
    private TextField resolutionWidthTextField;

    @FXML
    private TextField resolutionHeightTextField;

    @FXML
    private TextField outputDirectoryTextField;

    @FXML
    private CheckBox keepOriginalResolutionCheckBox;

    public Converter() { }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        fileFormatColumn.setCellValueFactory(new PropertyValueFactory<>("fileFormat"));
        videoCodecColumn.setCellValueFactory(new PropertyValueFactory<>("videoCodec"));
        informationColumn.setCellValueFactory(new PropertyValueFactory<>("information"));

        ObservableList<String> qualities = FXCollections.observableArrayList("ultrafast", "fast", "medium", "slow", "veryslow");
        qualityComboBox.setItems(qualities);
        qualityComboBox.getSelectionModel().select(1);

        ObservableList<String> framerates = FXCollections.observableArrayList("1", "5", "24", "25", "29.97", "30", "48", "50", "59.94", "60", "120", "144");
        framerateComboBox.setItems(framerates);
        framerateComboBox.getSelectionModel().select(5);

        ObservableList<String> keyframes = FXCollections.observableArrayList("5", "30", "60", "120");
        keyframeComboBox.setItems(keyframes);
        keyframeComboBox.getSelectionModel().select(0);

        ObservableList<String> videoCodecs = FXCollections.observableArrayList("h264");
        videoCodecComboBox.setItems(videoCodecs);
        videoCodecComboBox.getSelectionModel().select(0);

        ObservableList<String> videoBitrates = FXCollections.observableArrayList("512k", "1M", "2M", "3M", "5M", "10M", "15M", "30M");
        bitrateVideoComboBox.setItems(videoBitrates);
        bitrateVideoComboBox.getSelectionModel().select(2);

        ObservableList<String> audioCodecs = FXCollections.observableArrayList("aac", "ac3", "flac");
        audioCodecComboBox.setItems(audioCodecs);
        audioCodecComboBox.getSelectionModel().select(0);

        ObservableList<String> audioBitrates = FXCollections.observableArrayList("32k", "128k", "192k", "256k");
        bitrateAudioComboBox.setItems(audioBitrates);
        bitrateAudioComboBox.getSelectionModel().select(1);

        resolutionWidthTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) return;
            resolutionWidthTextField.setText(newValue.replaceAll("[^\\d]", ""));
        });

        resolutionHeightTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*")) return;
            resolutionHeightTextField.setText(newValue.replaceAll("[^\\d]", ""));
        });

        keepOriginalResolutionCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            resolutionWidthTextField.setDisable(newValue);
            resolutionHeightTextField.setDisable(newValue);
        });
    }

    @FXML
    public void onImportFiles(ActionEvent e) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select files");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Video", "*.mp4", "*.mov", "*.avi", "*.webm", "*.mkv", "*.wmv", "*.flv");
        chooser.getExtensionFilters().add(extFilter);
        List<File> files = chooser.showOpenMultipleDialog(new Stage());

        for (File file: files) {
            String[] cmd={"C:\\ffmpeg\\ffprobe.exe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", file.getAbsolutePath()};

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(cmd);

            BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

            String jsonData = "";
            for (String line = ""; (line = stdInput.readLine()) != null; jsonData+=line);

            JsonObject jsonObject = new JsonParser().parse(jsonData).getAsJsonObject();
            JsonObject formatData = jsonObject.get("format").getAsJsonObject();
            JsonArray streamsData = jsonObject.get("streams").getAsJsonArray();

            String duration = formatData.get("duration").getAsString();
            String fileFormat = formatData.get("format_long_name").getAsString();
            String size = formatData.get("size").getAsString();
            String bitrate = formatData.get("bit_rate").getAsString();

            JsonObject videoStream = null;
            for (int i = 0; i < streamsData.size(); i++) {
                JsonObject stream = streamsData.get(i).getAsJsonObject();

                if (stream.get("codec_type").getAsString().equals("video")) {
                    videoStream = streamsData.get(i).getAsJsonObject();
                    break;
                }
            }

            if (videoStream == null) return;

            String videoCodec = videoStream.get("codec_long_name").getAsString();
            String width = videoStream.get("width").getAsString();
            String height = videoStream.get("height").getAsString();

            String formattedBitrate = Math.round(Integer.parseInt(bitrate)/1024) + " kbps";
            String formattedDuration = duration + "s";
            String formattedFileSize = Math.round(Integer.parseInt(size)/(1024*1024)) + " MB";
            String formattedInfo = String.format("%sx%s, %s", width, height, formattedBitrate);

            MediaFile mf = new MediaFile(file.getName(), formattedDuration, formattedFileSize, fileFormat, videoCodec, formattedInfo, file.getAbsolutePath());
            videoTable.getItems().add(mf);
        }
    }

    @FXML
    public void onSelectOutputDirectory(ActionEvent e) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select output directory");
        File selectedDirectory = chooser.showDialog(new Stage());
        outputDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
    }
}
