//package com.example.logs.service;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//import java.io.IOException;
//import java.io.RandomAccessFile;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class FileWatcherService {
//    private static final Path FILE_NAME = Paths.get("C:/Users/z004fy4n/Desktop/PP/BS/logs/logs/data.txt");
//    public static final String DESTINATION = "/topic/log";
//    private long offset;
//    private final RandomAccessFile randomAccessFile;
//    private boolean firstRun = true;
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    public FileWatcherService() throws IOException {
//        randomAccessFile = new RandomAccessFile(String.valueOf(FILE_NAME), "r");
//        offset = randomAccessFile.length();
//    }
//
//    @Scheduled(fixedDelay = 100, initialDelay = 5000)
//    public void sendUpdates() throws IOException {
//        long fileLength = randomAccessFile.length();
//
//        if (fileLength < offset) offset = 0;
//        if (fileLength <= offset && !firstRun) return;
//
//        seekToLast10Lines(firstRun ? 0 : offset, fileLength);
//
//        List<String> lines = new ArrayList<>();
//        String line;
//        while ((line = randomAccessFile.readLine()) != null && lines.size() < 10) {
//            if (!line.isEmpty()) lines.add(line);
//        }
//
//        if (!lines.isEmpty()) {
//            messagingTemplate.convertAndSend(DESTINATION, lines);
//            offset = randomAccessFile.getFilePointer();
//            firstRun = false;
//        }
//    }
//
//    private void seekToLast10Lines(long start, long end) throws IOException {
//        if (end <= start) return;
//
//        long pos = end - 1;
//        int lines = 0;
//
//        while (pos >= start && lines < 10) {
//            randomAccessFile.seek(pos--);
//            if (randomAccessFile.read() == '\n') lines++;
//        }
//
//        randomAccessFile.seek(pos < start ? start : pos + 2);
//    }
//}


package com.example.logs.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Component
public class FileWatcherService {
    private static final Path FILE_NAME = Paths.get("C:/Users/z004fy4n/Desktop/PP/BS/logs/logs/data.txt");
    public static final String DESTINATION = "/topic/log";
    private long offset;
    private final RandomAccessFile randomAccessFile;
    private boolean firstRun = true;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public FileWatcherService() throws IOException {
        randomAccessFile = new RandomAccessFile(String.valueOf(FILE_NAME), "r");
        offset = randomAccessFile.length();
    }

    @Scheduled(fixedDelay = 100, initialDelay = 5000)
    public void sendUpdates() throws IOException {
        long fileLength = randomAccessFile.length();

        if (fileLength < offset) offset = 0;
        if (fileLength <= offset && !firstRun) return;

        seekToLast10Lines(firstRun ? 0 : offset, fileLength);

        List<String> lines = new ArrayList<>();
        String line;
        while ((line = randomAccessFile.readLine()) != null && lines.size() < 10) {
            if (!line.isEmpty()) {
                lines.add(line);
            }
        }

        // Send each line individually
        for (String logLine : lines) {
            messagingTemplate.convertAndSend(DESTINATION, logLine);
        }

        offset = randomAccessFile.getFilePointer();
        firstRun = false;
    }

    private void seekToLast10Lines(long start, long end) throws IOException {
        if (end <= start) return;

        long pos = end - 1;
        int lines = 0;

        while (pos >= start && lines < 10) {
            randomAccessFile.seek(pos--);
            if (randomAccessFile.read() == '\n') lines++;
        }

        randomAccessFile.seek(pos < start ? start : pos + 2);
    }
}