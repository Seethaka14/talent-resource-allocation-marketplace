package com.talentmarketplace.exception;

public class AiAssistantException extends RuntimeException {

    private final boolean connectionFailure;

    public AiAssistantException(String message) {
        super(message);
        this.connectionFailure = false;
    }

    public AiAssistantException(String message, Throwable cause) {
        super(message, cause);
        this.connectionFailure = false;
    }

    public AiAssistantException(String message, boolean connectionFailure) {
        super(message);
        this.connectionFailure = connectionFailure;
    }

    public AiAssistantException(String message, Throwable cause, boolean connectionFailure) {
        super(message, cause);
        this.connectionFailure = connectionFailure;
    }

    public boolean isConnectionFailure() {
        return connectionFailure;
    }
}
