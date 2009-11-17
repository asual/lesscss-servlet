package com.asual.lesscss;

import java.io.IOException;

public class ResourceNotFoundException extends IOException {
    
    private static final long serialVersionUID = 5718684064058983151L;

    public ResourceNotFoundException() {
        super();
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }

}