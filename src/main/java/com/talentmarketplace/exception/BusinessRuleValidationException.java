package com.talentmarketplace.exception;

public class BusinessRuleValidationException extends RuntimeException {

    public BusinessRuleValidationException(String message) {
        super(message);
    }
}
