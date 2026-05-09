package it.turin.hermesserver.dto;

import java.io.Serial;
import java.io.Serializable;

public class Response<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -21548922357797L;
    int statusCode;
    T responseBody;

    public Response(int statusCode, T responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {return this.statusCode;}
    public T getResponseBody() {return this.responseBody;}

}