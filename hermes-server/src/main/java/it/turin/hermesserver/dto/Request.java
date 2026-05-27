package it.turin.hermesserver.dto;

import java.util.Map;

/**
 * DTO generico che rappresenta una richiesta ricevuta dal client.
 *
 * <p>Una richiesta contiene l'endpoint da invocare, una mappa di parametri
 * testuali e un eventuale corpo tipizzato. Il dispatcher usa questa classe come
 * rappresentazione intermedia dopo la deserializzazione JSON.</p>
 *
 * @param <T> tipo del corpo della richiesta
 */
public class Request<T> {

    private Endpoint endpoint;
    private Map<String, Object> requestParameters;
    private T body;

    /**
     * Crea una richiesta applicativa.
     *
     * @param endpoint endpoint da invocare
     * @param requestParameters parametri associati alla richiesta
     * @param body corpo della richiesta
     */
    public Request (Endpoint endpoint, Map<String, Object> requestParameters, T body) {
        this.endpoint = endpoint;
        this.requestParameters = requestParameters;
        this.body = body;
    }

    /**
     * Restituisce l'endpoint richiesto.
     *
     * @return endpoint della richiesta
     */
    public Endpoint getEndpoint() {return this.endpoint;}

    /**
     * Restituisce un parametro della richiesta come stringa.
     *
     * @param par nome del parametro
     * @return valore del parametro, oppure {@code null} se assente
     */
    public String getRequestParameter(String par) {
        return (String) requestParameters.get(par);
    }

    /**
     * Restituisce il corpo della richiesta.
     *
     * @return corpo tipizzato della richiesta
     */
    public T getBody() {return this.body;}

    /**
     * Verifica che i parametri indicati siano valorizzati.
     *
     * <p>Il metodo controlla i valori passati come argomento, non la presenza
     * delle chiavi nella mappa interna.</p>
     *
     * @param params valori da validare
     * @return {@code true} se tutti i valori sono non nulli e non vuoti,
     *         {@code false} altrimenti
     */
    public boolean validParams (String... params) {
        for (String param:
                params) {
            if (param == null || param.isBlank()) return false;
        }
        return true;
    }

    /**
     * Verifica la validita' del corpo della richiesta.
     *
     * <p>Attualmente il metodo e' un segnaposto e considera valido qualsiasi
     * corpo.</p>
     *
     * @return sempre {@code true}
     */
    public boolean validObject() {
        //TODO
        return true;
    }

    /**
     * Restituisce una rappresentazione testuale della richiesta.
     *
     * @return stringa descrittiva della richiesta
     */
    @Override
    public String toString() {
        return "Request{" +
                "endpoint=" + endpoint +
                ", requestParameters=" + requestParameters +
                ", body=" + body +
                '}';
    }
}
