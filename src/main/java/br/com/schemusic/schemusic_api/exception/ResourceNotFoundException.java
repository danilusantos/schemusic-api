package br.com.schemusic.schemusic_api.exception;

public class ResourceNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1975911185832064444L;

	public ResourceNotFoundException(String message) {
        super(message);
    }
}
