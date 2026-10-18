package GetServlet.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MapFile {
    private String name;
    private String extension;
    private byte[] content;
    
    public MapFile(String name, String extension, byte[] content) {
        this.name = name;
        this.extension = extension;
        this.content = content;
    }

    public MapFile() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
