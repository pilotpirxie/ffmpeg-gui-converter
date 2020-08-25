package converter.controller;

import converter.model.MediaFile;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

    public Converter() {
    }

    @FXML
    private void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        informationColumn.setCellValueFactory(new PropertyValueFactory<>("information"));

        MediaFile mf = new MediaFile("A", "B", "C", "D");
        videoTable.getItems().add(mf);
    }
}
