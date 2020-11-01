package org.converter.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import netscape.javascript.JSObject;
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
    private TableColumn<Object, Object> fileSizeColumn;

    @FXML
    private TableColumn<Object, Object> fileFormatColumn;

    @FXML
    private TableColumn<Object, Object> videoCodecColumn;

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
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        fileFormatColumn.setCellValueFactory(new PropertyValueFactory<>("fileFormat"));
        videoCodecColumn.setCellValueFactory(new PropertyValueFactory<>("videoCodec"));
        informationColumn.setCellValueFactory(new PropertyValueFactory<>("information"));
    }

    @FXML
    public void onImportFiles(ActionEvent e) throws IOException {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Open File");
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
}
