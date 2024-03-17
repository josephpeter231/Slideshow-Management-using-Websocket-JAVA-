import java.io.Serializable;

public class ImageInfo implements Serializable {
    private String filename;
    private int duration; // Duration in seconds

    public ImageInfo(String filename, int duration) {
        this.filename = filename;
        this.duration = duration;
    }

    public String getFilename() {
        return filename;
    }

    public int getDuration() {
        return duration;
    }
}
