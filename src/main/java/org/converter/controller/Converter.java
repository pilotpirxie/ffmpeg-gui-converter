package org.converter.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.converter.Command;
import org.converter.model.MediaFile;
import javafx.fxml.FXML;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class Converter {

    @FXML
    private TableView<MediaFile> videoTable;

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
    private ComboBox<String> qualityComboBox;

    @FXML
    private ComboBox<String> framerateComboBox;

    @FXML
    private ComboBox<String> keyframeComboBox;

    @FXML
    private ComboBox<String> videoCodecComboBox;

    @FXML
    private ComboBox<String> bitrateVideoComboBox;

    @FXML
    private ComboBox<String> audioCodecComboBox;

    @FXML
    private ComboBox<String> bitrateAudioComboBox;

    @FXML
    private ComboBox<String> containerComboBox;

    @FXML
    private TextField resolutionWidthTextField;

    @FXML
    private TextField resolutionHeightTextField;

    @FXML
    private TextField outputDirectoryTextField;

    @FXML
    private TextField additionalArgumentsTextField;

    @FXML
    private TextField ffmpegTextField;

    @FXML
    private TextField ffprobeTextField;

    @FXML
    private CheckBox keepOriginalResolutionCheckBox;

    @FXML
    private MenuBar menuBar;

    @FXML
    private ProgressIndicator inProgress;

    @FXML
    private Button convertButton;

    private LinkedList<String[]> convertQueue = new LinkedList<>();

    public Converter() { }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        fileSizeColumn.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
        fileFormatColumn.setCellValueFactory(new PropertyValueFactory<>("fileFormat"));
        videoCodecColumn.setCellValueFactory(new PropertyValueFactory<>("videoCodec"));
        informationColumn.setCellValueFactory(new PropertyValueFactory<>("information"));

        ObservableList<String> containers = FXCollections.observableArrayList("mp4", "mkv", "avi");
        containerComboBox.setItems(containers);
        containerComboBox.getSelectionModel().select(0);

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

        ObservableList<String> videoBitrates = FXCollections.observableArrayList("512k", "1M", "2M", "3M", "5M", "10M");
        bitrateVideoComboBox.setItems(videoBitrates);
        bitrateVideoComboBox.getSelectionModel().select(2);

        ObservableList<String> audioCodecs = FXCollections.observableArrayList("aac", "ac3");
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
    public void onImportFiles() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select files");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Video", "*.mp4", "*.mov", "*.avi", "*.webm", "*.mkv", "*.wmv", "*.flv");
        chooser.getExtensionFilters().add(extFilter);
        List<File> files = chooser.showOpenMultipleDialog(new Stage());

        File ffprobeFile = new File(ffprobeTextField.getText());
        if (!ffprobeFile.exists() || !ffprobeFile.canExecute() || !ffprobeTextField.getText().contains("ffprobe")) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "You don't specify FFprobe file.");
            alert.showAndWait();
            return;
        };

        for (File file : files) {
            String[] cmd = {ffprobeTextField.getText(), "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", file.getAbsolutePath()};

            Command command = new Command(cmd);

            command.setOnSucceeded(t -> {
                String jsonData = command.getValue();

                MediaFile mf = createMediaFile(file, jsonData);

                if (mf != null) videoTable.getItems().add(mf);
            });

            new Thread(command).start();
        }
    }

    private MediaFile createMediaFile(File file, String jsonData) {
        JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
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

        if (videoStream == null) return null;

        String videoCodec = videoStream.get("codec_long_name").getAsString();
        String width = videoStream.get("width").getAsString();
        String height = videoStream.get("height").getAsString();

        String formattedBitrate = Math.round(Integer.parseInt(bitrate) / 1024f) + " kbps";
        String formattedDuration = duration + "s";
        String formattedFileSize = Math.round(Integer.parseInt(size) / (1024f * 1024f)) + " MB";
        String formattedInfo = String.format("%sx%s, %s", width, height, formattedBitrate);

        return new MediaFile(file.getName(), formattedDuration, formattedFileSize, fileFormat, videoCodec, formattedInfo, file.getAbsolutePath());
    }

    @FXML
    public void onSelectOutputDirectory() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select output directory");
        File selectedDirectory = chooser.showDialog(new Stage());
        outputDirectoryTextField.setText(selectedDirectory.getAbsolutePath());
    }

    @FXML
    public void onConvert() {
        if (videoTable.getItems().size() > 0) {
            File ffmpegFile = new File(ffmpegTextField.getText());
            if (!ffmpegFile.exists() || !ffmpegFile.canExecute() || !ffmpegTextField.getText().contains("ffmpeg")) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "You don't specify FFmpeg file.");
                alert.showAndWait();
                return;
            };

            onStartConversion();

            for (MediaFile file : videoTable.getItems()) {
                String[] cmd = getCommandString(file);

                if (cmd == null) return;

                convertQueue.add(cmd);
            }

            if (!convertQueue.isEmpty()) {
                runConversionProcess(convertQueue.poll());
            }
        }
    }

    private void runConversionProcess(String[] cmd) {
        Command command = new Command(cmd);
        Thread thread = new Thread(command);
        System.out.println("Running: " + Arrays.toString(cmd));
        command.setOnSucceeded(t -> {
            onFinishedConversion();
        });

        command.setOnFailed(t -> {
            onFinishedConversion();
        });

        thread.start();
    }

    private String[] getCommandString(MediaFile file) {
        boolean changeResolution = !keepOriginalResolutionCheckBox.isSelected();
        String width = resolutionWidthTextField.getText();
        String height = resolutionHeightTextField.getText();
        if (changeResolution && (!width.equals("") || !height.equals(""))) return null;

        String filters = "\"scale=" + width + ":" + height + "\"";
        String preset = qualityComboBox.getSelectionModel().getSelectedItem();
        String keyframes = keyframeComboBox.getSelectionModel().getSelectedItem();
        String videoBitrate = bitrateVideoComboBox.getSelectionModel().getSelectedItem();
        String framerate = framerateComboBox.getSelectionModel().getSelectedItem();
        String audioCodec = audioCodecComboBox.getSelectionModel().getSelectedItem();
        String audioBitrate = bitrateAudioComboBox.getSelectionModel().getSelectedItem();
        String container = containerComboBox.getSelectionModel().getSelectedItem();
        String additionalArguments = additionalArgumentsTextField.getText();
        String suffix = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5);
        String filename = file.getTitle().replaceAll("\\.\\w+", "") + "_" + suffix + "." + container;
        String output = Paths.get(outputDirectoryTextField.getText(), filename).toString();

        return new String[]{"C:\\ffmpeg\\ffmpeg.exe", "-progress", "-", "-nostats", "-i", file.getFilepath(), changeResolution ? "-vf" : "", changeResolution ? filters : "", "-c:v", "libx264", "-preset", preset, "-g", keyframes, "-keyint_min", keyframes, "-maxrate", videoBitrate, "-bufsize", videoBitrate, "-r", framerate, "-c:a", audioCodec, "-b:a", audioBitrate, additionalArguments, output};
    }

    private void onStartConversion() {
        menuBar.setDisable(true);
        inProgress.setVisible(true);
        convertButton.setText("In progress...");
        convertButton.setDisable(true);
    }

    private void onFinishedConversion() {
        if (!convertQueue.isEmpty()) {
            runConversionProcess(convertQueue.poll());
        } else {
            menuBar.setDisable(false);
            inProgress.setVisible(false);
            convertButton.setText("Convert Files");
            convertButton.setDisable(false);
        }
    }

    @FXML
    public void onSelectFFMPEG() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select FFmpeg file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("FFmpeg", "ffmpeg.exe", "ffmpeg");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(new Stage());
        ffmpegTextField.setText(file.getAbsolutePath());
    }

    @FXML
    public void onSelectFFPROBE() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select FFprobe file");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("FFprobe", "ffprobe.exe", "ffprobe");
        chooser.getExtensionFilters().add(extFilter);
        File file = chooser.showOpenDialog(new Stage());
        ffprobeTextField.setText(file.getAbsolutePath());
    }
}
