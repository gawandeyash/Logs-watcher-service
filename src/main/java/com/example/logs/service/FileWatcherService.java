//package com.example.logs.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.ByteBuffer;
//import java.nio.channels.FileChannel;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class FileWatcherService {
//
//    private static final Path LOG_FILE = Paths.get("C:/Users/z004fy4n/Desktop/PP/BS/logs/logs/data.txt");
//    private static final String DESTINATION = "/topic/log";
//    private long filePointer = 0L; // Tracks the byte position
//
//    @Autowired
//    private SimpMessagingTemplate messagingTemplate;
//
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    @Scheduled(fixedDelay = 1000, initialDelay = 500)
//    public void sendUpdates() throws IOException {
//        if (!Files.exists(LOG_FILE)) {
//            return;
//        }
//
//        try (FileChannel channel = FileChannel.open(LOG_FILE, StandardOpenOption.READ)) {
//            long fileSize = channel.size();
//
//            if (fileSize < filePointer) {
//                filePointer = 0; // file reset or truncated
//            }
//
//            channel.position(filePointer);
//
//            ByteBuffer buffer = ByteBuffer.allocate((int) (fileSize - filePointer));
//            int bytesRead = channel.read(buffer);
//
//            if (bytesRead > 0) {
//                buffer.flip();
//                String newData = StandardCharsets.UTF_8.decode(buffer).toString();
//                String[] lines = newData.split("\\r?\\n");
//
//                // Prepare list of maps for JSON serialization
//                List<Map<String, String>> batch = new ArrayList<>();
//                for (String line : lines) {
//                    if (!line.isEmpty()) {
//                        batch.add(Map.of("content", line));
//                    }
//                }
//
//                if (!batch.isEmpty()) {
//                    // Convert list to JSON array string
//                    String payload = mapper.writeValueAsString(batch);
//                    messagingTemplate.convertAndSend(DESTINATION, payload);
//                }
//
//                filePointer = fileSize;
//            }
//        }
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
    private final static String READ_MODE = "r";
    public static final String DESTINATION = "/topic/log";
    private long offset;

    private final RandomAccessFile randomAccessFile;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public FileWatcherService() throws IOException {
        randomAccessFile = new RandomAccessFile(String.valueOf(FILE_NAME), READ_MODE);

        offset = initialOffset();
    }

//    @Scheduled(fixedDelay = 100, initialDelay = 5000)
//    public void sendUpdates() throws IOException {
//        long fileLength = randomAccessFile.length();
//        long pointer = fileLength - 1;
//        int lines = 0;
//        while (pointer >= 0 && lines <= 10) {
//            randomAccessFile.seek(pointer--);
//            if (randomAccessFile.readByte() == '\n') lines++;
//        }
//
//        while (randomAccessFile.getFilePointer() < fileLength) {
//            String latestFileData = randomAccessFile.readLine();
//            String payload = "{\"content\":\"" + latestFileData + "\"}";
//
//            messagingTemplate
//                    .convertAndSend(DESTINATION, payload);
//        }
//
//        offset = fileLength;
//    }
@Scheduled(fixedDelay = 100, initialDelay = 5000)
public void sendUpdates() throws IOException {
    long fileLength = randomAccessFile.length();

    // File was truncated or recreated
    if (fileLength < offset) {
        offset = 0;
    }

    // Go to last position
    randomAccessFile.seek(offset);

    List<String> newLines = new ArrayList<>();
    String line;

    while ((line = randomAccessFile.readLine()) != null) {
        if (!line.isEmpty()) {
            newLines.add(line);
        }
    }

    if (!newLines.isEmpty()) {
        // Send only the last 10 lines
        messagingTemplate.convertAndSend(
                DESTINATION,
                newLines.subList(Math.max(0, newLines.size() - 10), newLines.size())
        );

        // Update offset
        offset = randomAccessFile.getFilePointer();
    }
}



    private String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n");
    }



    private long initialOffset() throws IOException {
        int lineCount = 0;

        while (randomAccessFile.readLine() != null) {
            lineCount++;
        }

        if(lineCount > 10) {
            offset = lineCount - 10;
        }

        return offset;
    }
}
