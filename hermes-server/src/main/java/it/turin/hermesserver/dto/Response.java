package it.turin.hermesserver.dto;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO generico usato per restituire una risposta al client.
 *
 * <p>Contiene un codice di stato applicativo e un corpo tipizzato. La classe e'
 * serializzabile per poter essere trasmessa o persistita se necessario, anche
 * se nel server viene normalmente convertita in JSON tramite Gson.</p>
 *
 * @param <T> tipo del corpo della risposta
 */
public class Response<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = -21548922357797L;
    int statusCode;
    T responseBody;

    /**
     * Crea una risposta applicativa.
     *
     * @param statusCode codice di stato della risposta
     * @param responseBody corpo della risposta
     */
    public Response(int statusCode, T responseBody) {
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    /**
     * Restituisce il codice di stato.
     *
     * @return codice di stato applicativo
     */
    public int getStatusCode() {return this.statusCode;}

    /**
     * Restituisce il corpo della risposta.
     *
     * @return corpo tipizzato della risposta
     */
    public T getResponseBody() {return this.responseBody;}

}
