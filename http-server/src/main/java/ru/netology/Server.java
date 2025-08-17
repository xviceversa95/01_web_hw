package ru.netology;

import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Server {

    final ExecutorService threadPool;
    final int port;
    final int poolSize;
    static final List<String> validPaths = List.of("/index.html", "/spring.svg", "/spring.png",
            "/resources.html", "/styles.css", "/app.js",
            "/links.html", "/forms.html", "/classic.html",
            "/events.html", "/events.js");
    HashMap<String, HashMap<String, Handler>> handlersList = new HashMap<>();


    public Server (int port, int poolSize) {
        this.port = port;
        this.poolSize = poolSize;
        this.threadPool = Executors.newFixedThreadPool(poolSize);
    }

    // запускаем сервер
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on " + port);
             while (true) {
                Socket socket = serverSocket.accept();
                 System.out.println("Соединение установлено на порту: " + port);
                threadPool.submit(() -> handleConnection(socket));
            }
        } catch (IOException e) {
            e.getMessage();
        } finally {
            threadPool.shutdown();
        }
    }

    //обработка подключения
    public void handleConnection (Socket socket) {

        // создаем потоки входящий, исходящий
        try (final var in = new BufferedInputStream(socket.getInputStream());
             final var out = new BufferedOutputStream(socket.getOutputStream());
             ) {
            {
                Request request = parseRequest(in);

                if (request.method == null || request.path == null ) {
                    badRequest(out);
                    out.flush();
                    return;
                }

                if (!request.path.startsWith("/")) {
                    badRequest(out);
                    out.flush();
                    return;

                }

                if (request.headers == null) {
                    badRequest(out);
                    out.flush();
                    return;
                }

                if (!validPaths.contains(request.path)) {
                    badRequest(out);
                    out.flush();
                    return;
                }

                Handler activeHandler = findHandler(request);

                if (activeHandler == null) {
                    badRequest(out);
                    out.flush();
                    return;
                }

                activeHandler.handle(request, out);

            }

        } catch (IOException e) {
            e.getMessage();
        }
    }


    public static Request parseRequest(BufferedInputStream in) throws IOException {
        final var limit = 4096;

        in.mark(limit);
        byte[] buffer = new byte[limit];
        int read = in.read(buffer);

        // ищем индекс первого вхождения разделителя в массив байтов in, т.е. конец requestLine
        byte[] requestLineDelimeter = new byte[]{'\r', '\n'};
        int requestLineEnd = indexOf(buffer, requestLineDelimeter, 0, read);

        //читаем requestLine в массив String
        String[] requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");

        //GET /users/3 HTTP/1.1
        String method = requestLine[0];
        System.out.println("Метод " + method);
        System.out.println("Путь " + requestLine[1]);
        System.out.println("Протокол " + requestLine[2]);

        String path = requestLine[1];

        //то где заканчиваются заголовки, разделитель
        byte[] headersDelimeter = new byte[]{'\r', '\n', '\r', '\n'};
        //тут начинаются заголовки (конец reqLine + длина разделителя)
        int headersStart = requestLineEnd + requestLineDelimeter.length;
        int headersEnd = indexOf(buffer, headersDelimeter, headersStart, read);

        in.reset();
        in.skip(headersStart);

        byte[] headerBytes = in.readNBytes(headersEnd - headersStart);
        String[] headers = new String(headerBytes).split("\r\n");
        for (String header : headers) {
            System.out.println(header);
        }
        System.out.println("Напечатали заголовки");


        String body = null;
        if (!method.equals("GET")) {
            in.skip(headersDelimeter.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = extractHeader(List.of(headers), "Content-Length");

            if (contentLength.isPresent()) {
                Integer length = Integer.parseInt(contentLength.get());
                byte[] bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                System.out.println(body);
            }
        }

        Request request = new Request.RequestBuilder()
                .setMethod(method)
                .setPath(path)
                .setBody(body)
                .setHeaders(headers)
                .build();
        return request;
    }

    //Метод будет искать подстроку в строке = индекс начала вхождения
    public static int indexOf(byte[] array, byte[] target, int start, int end){
        outer:
        for (int i = start; i < end - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
            }
        return -1;
    }

    private static Optional<String> extractHeader(List<String> headers, String header) {
        return headers.stream()
                .filter(o -> o.startsWith(header))
                .map(o -> o.substring(o.indexOf(" ")))
                .map(String::trim)
                .findFirst();
    }

    private static void badRequest(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 400 Bad Request\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void addHandler(String method, String path, Handler handler) {
        handlersList.computeIfAbsent(method, k -> new HashMap<>());
        handlersList.get(method).put(path, handler);
    }

    public Handler findHandler(Request request){
        Handler handler = handlersList.get(request.method).get(request.path);
        return handler;
    }
}
