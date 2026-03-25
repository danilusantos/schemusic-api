package br.com.schemusic.schemusic_api.exception;

@SuppressWarnings("serial")
public class AuthenticationException extends RuntimeException {

    public AuthenticationException(String message) {
        super(message);
    }
}
