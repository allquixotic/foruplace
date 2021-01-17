package com.github.allquixotic.foruplace;

import lombok.val;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class Main {

    public static final Logger log = Logger.getGlobal();
    public static final Path tempDir;
    private static final Pattern urlRx = Pattern.compile("\\[\\s*img\\s*\\]\\s*((?:https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\\s*\\[\\s*/img\\s*\\]", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    static {
        try {
            tempDir = Files.createTempDirectory("foruplace");
            tempDir.toFile().deleteOnExit();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        val config = Config.readConfig();
        val s3u = new S3Upload(config.getEndpoint(), config.getAccessKey(), config.getSecretAccessKey(), config.getBucket());
        val orig = Files.readString(Path.of(args[0]));
        val dest = args[1];
        val m = urlRx.matcher(orig);
        val result = m.replaceAll((mr) -> {
            val fo = new FileObject(mr.group(1));
            val fno = fo.getFileName().replaceAll("/", "");
            val tempPath = fo.getTempPath();
            try {
                s3u.upload(String.format("%s/%s", config.getDirectory(), fno), tempPath);
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
            fo.setS3Path(String.format("%s/%s/%s/%s", config.getEndpoint(), config.getBucket(), config.getDirectory(), fno));
            return String.format("[img]%s[/img]", fo.getS3Path());
        });
        Files.writeString(Path.of(dest), result);
        log.info(String.format("Wrote output ...\n--------\n%s\n--------\n ... to file '%s'", result, dest));
        try{
            Desktop.getDesktop().open(new File(dest));
        }
        catch(Exception e){}

        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
        System.exit(0);
    }

    public static String getThrowableInfo(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println(t.getMessage());
        t.printStackTrace(pw);
        return sw.toString();
    }

    public static void logSevere(String context, Throwable t) {
        log.severe(String.format("%s\n%s", context, getThrowableInfo(t)));
    }

    public static void logSevere(Throwable t) {
        log.severe(getThrowableInfo(t));
    }

    public static void sleepOrExit(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch(InterruptedException ie) {
            logSevere(ie);
            System.exit(1);
        }
    }

    public static void sleepOrExit(int seconds, int exitCode) {
        try {
            Thread.sleep(seconds * 1000);
        }
        catch(InterruptedException ie) {
            logSevere(ie);
            System.exit(exitCode);
        }
    }
}
