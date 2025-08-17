package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Request {
    public String method;
    public String fullPath;
    public String[] headers;
    public String body;
    public List<NameValuePair> params;

    public Request(String method, String fullPath, String[] headers, String body) {
        this.method = method;
        this.fullPath = fullPath;
        this.headers = headers;
        this.body = body;
    }

    public Optional<NameValuePair> getQueryParam(String name) {
        Optional<NameValuePair> param = params.stream()
                .filter(e -> e.getName().equals(name))
                .findFirst();
        System.out.println(param);
        return param;
    }

    public void getQueryParams(){
        int index = fullPath.indexOf('?');
        String queryString = fullPath.substring(index+1);
        params = URLEncodedUtils.parse(queryString, Charset.forName("UTF-8"));
        System.out.println("Выводим параметры:");
        System.out.println(params);
    }

    public String getClearPath(){
        int index = fullPath.indexOf('?');
        String clearPath = fullPath.substring(0, index-1);
        return clearPath;
    }


    public static class RequestBuilder {
        public String method;
        public String fullPath;
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

        public RequestBuilder setPath(String fullPath){
            this.fullPath = fullPath;
            return this;
        }

        public Request build() {
            return new Request(method, fullPath, headers, body);
        }
    }
}
