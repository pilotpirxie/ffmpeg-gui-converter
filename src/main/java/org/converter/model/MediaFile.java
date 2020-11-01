package org.converter.model;

public class MediaFile {
    private String title;
    private String duration;
    private String fileSize;
    private String fileFormat;
    private String videoCodec;
    private String information;
    private String filepath;

    public MediaFile(String title, String duration, String fileSize, String fileFormat, String videoCodec, String information, String filepath) {
        this.title = title;
        this.duration = duration;
        this.fileSize = fileSize;
        this.fileFormat = fileFormat;
        this.videoCodec = videoCodec;
        this.information = information;
        this.filepath = filepath;
    }

    public String getTitle() {
        return title;
    }

    public String getDuration() {
        return duration;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public String getVideoCodec() {
        return videoCodec;
    }

    public String getInformation() {
        return information;
    }

    public String getFilepath() {
        return filepath;
    }
}
