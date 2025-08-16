package ru.netology;

import java.io.BufferedOutputStream;
import java.io.IOException;

@FunctionalInterface
public interface Handler {
     void handle(Request request, BufferedOutputStream responseStream) throws IOException;
}

//     handlers = {
//      "GET": {
//          "/messages": какой-то handler
//      },
//      "POST": {
//          "/messages": какой-то handler
//      }
//  }
//
// Формат Map: <String method<String path, Handler handler>>


