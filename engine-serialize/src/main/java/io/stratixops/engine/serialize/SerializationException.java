package io.stratixops.engine.serialize;

/**
 * Thrown when structured data cannot be read or written: a bad envelope, an
 * unsupported (newer) schema version, malformed JSON, or an I/O failure.
 *
 * <p>Content errors that must degrade rather than fail — an unknown component
 * type, a missing asset — never surface as this exception; they become an
 * {@code UnsupportedComponent} or a warning instead (ADR-0006).
 */
public class SerializationException extends RuntimeException {

    /**
     * Creates an exception with a message.
     *
     * @param message the message
     */
    public SerializationException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and cause.
     *
     * @param message the message
     * @param cause   the cause
     */
    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
