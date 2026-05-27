package it.turin.hermesserver.dto;

/**
 * Endpoint applicativi supportati dal server Hermes.
 *
 * <p>I valori dell'enum vengono usati nel JSON delle richieste per selezionare
 * l'operazione da eseguire nel {@code RequestDispatcher}.</p>
 */
public enum Endpoint {
    /** Verifica l'esistenza di un account. */
    GET_USER,

    /** Recupera una pagina di email da una mailbox. */
    GET_EMAILS,

    /** Elimina un'email da una mailbox. */
    DELETE_EMAIL,

    /** Salva e recapita una nuova email. */
    POST_EMAIL,

    /** Verifica la raggiungibilita' del server per un account. */
    PING,

    /** Restituisce il numero di email presenti in una mailbox. */
    COUNT
}
