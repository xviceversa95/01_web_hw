package ru.netology;


import java.io.BufferedOutputStream;
import java.io.IOException;

public class Main {
  public static void main(String[] args) {
    Server server = new Server(9999, 64);

//здесь просто добавляем хэндлер
    server.addHandler("GET", "/default-get.html", new Handler() {
        public void handle(Request request, BufferedOutputStream responseStream) throws IOException {
            String responseBody = "<h1>Hello from custom handler!</h1>";
            byte[] responseBytes = responseBody.getBytes();

            responseStream.write(("HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Content-Length: " + responseBytes.length + "\r\n" +
                    "Connection: close\r\n" +
                    "\r\n").getBytes());

            responseStream.write(responseBytes);
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
