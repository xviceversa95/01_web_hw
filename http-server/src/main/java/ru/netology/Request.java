package ru.netology;

import java.util.ArrayList;

public class Request {
    public String method;
    public String path;
    public String[] headers;
    public String body;

    public Request(String method, String path, String[] headers, String body) {
        this.method = method;
        this.path = path;
        this.headers = headers;
        this.body = body;
    }

    public static class RequestBuilder {
        public String method;
        public String path;
        public String[] headers;
        public String body;


        public RequestBuilder setBody(String body) {
            this.body = body;
            return this;
        }

        public RequestBuilder setHeaders(String[] headers) {
            this.headers = headers;
            return this;
        }

        public RequestBuilder setMethod(String method) {
            this.method = method;
            return this;
        }

        public RequestBuilder setPath(String path){
            this.path = path;
            return this;
        }

        public Request build() {
            return new Request(method, path, headers, body);
        }
    }
}
