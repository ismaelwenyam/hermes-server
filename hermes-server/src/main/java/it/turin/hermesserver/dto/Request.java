package it.turin.hermesserver.dto;

import java.util.Map;

public class Request<T> {

    private Endpoint endpoint;
    private Map<String, Object> requestParameters;
    private T body;

    public Request (Endpoint endpoint, Map<String, Object> requestParameters, T body) {
        this.endpoint = endpoint;
        this.requestParameters = requestParameters;
        this.body = body;
    }

    public Endpoint getEndpoint() {return this.endpoint;}
    public String getRequestParameter(String par) {
        return (String) requestParameters.get(par);
    }
    public T getBody() {return this.body;}

    public boolean validParams (String... params) {
        for (String param:
                params) {
            if (param == null || param.isBlank()) return false;
        }
        return true;
    }
    public boolean validObject() {
        //TODO
        return true;
    }

    @Override
    public String toString() {
        return "Request{" +
                "endpoint=" + endpoint +
                ", requestParameters=" + requestParameters +
                ", body=" + body +
                '}';
    }
}
