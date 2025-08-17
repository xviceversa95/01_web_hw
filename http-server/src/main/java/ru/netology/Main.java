package ru.netology;


import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(9999, 64);

//здесь просто добавляем хэндлер
    server.addHandler("GET", "/default-get.html", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
            final var filePath = Path.of(".", "public", request.path);
            final var mimeType = Files.probeContentType(filePath);
            final var length = Files.size(filePath);

            responseStream.write((
                    "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: " + mimeType + "\r\n" +
                            "Content-Length: " + length + "\r\n" +
                            "Connection: close\r\n" +
                            "\r\n"
            ).getBytes());
            Files.copy(filePath, responseStream);
            responseStream.flush();

        }
    });

    if (server.handlersList.size() == 1) {
        System.out.println("Добавили хэндлер:");
    } else {
        System.out.println("Ошибка добавления хэндлера");
    }

    server.start();
  }
}
