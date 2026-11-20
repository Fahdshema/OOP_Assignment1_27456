package com.auca.exceptions;

// Thrown when a record needed first (taxpayer, tax type, obligation...) is missing.
public class PrerequisiteMissingException extends ValidationException {
    private String prerequisite; // which record can be registered now (null = nothing to register)

    public PrerequisiteMissingException(String message) {
        super(message);
        this.prerequisite = null;
    }

    public PrerequisiteMissingException(String message, String prerequisite) {
        super(message);
        this.prerequisite = prerequisite;
    }

    public String getPrerequisite() {
        return prerequisite;
    }
}
