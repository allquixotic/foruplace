package com.github.allquixotic.foruplace;

import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.http.client.fluent.Request;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

public class FileObject {
    @Getter
    private final String origPath;

    @Getter
    private final URL origUrl;

    @Getter
    private final String tempPath;

    @Getter @Setter
    private String s3Path;

    public FileObject(String origPath) {

        this.origPath = origPath;
        origUrl = pathToURL(origPath);
        tempPath = download();
    }

    private static URL pathToURL(String path) {
        try {
            return new URL(path);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getFileName() {
        return new File(getOrigUrl().getPath()).getName();
    }

    public String download() {
        try {
            val retval = Path.of(Main.tempDir.toString(), getFileName()).toString();
            Request.Get(origPath).execute().saveContent(new File(retval));
            Main.log.info(String.format("Downloaded '%s' to '%s'", origPath, retval));
            return retval;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}
