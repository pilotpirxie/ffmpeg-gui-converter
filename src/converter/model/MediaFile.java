package converter.model;

public class MediaFile {
    private String title;
    private String duration;
    private String type;
    private String information;

    public MediaFile(String title, String duration, String type, String information) {
        this.title = title;
        this.duration = duration;
        this.type = type;
        this.information = information;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

    public String getInformation() {
        return information;
    }
}
