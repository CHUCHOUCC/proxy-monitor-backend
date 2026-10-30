package com.ucc.proxymonitor.service;

import com.ucc.proxymonitor.exception.InvalidArgumentsException;

import java.util.Map;

/**
 * Typed access to the named arguments of an operation.
 * The REST layer sends a single JSON object as params[0].
 */
public class OperationArgs {

    private final Map<?, ?> values;

    private OperationArgs(Map<?, ?> values) {
        this.values = values;
    }

    public static OperationArgs from(Object... params) {
        if (params == null || params.length == 0 || params[0] == null) {
            return new OperationArgs(Map.of());
        }
        if (params[0] instanceof Map<?, ?> map) {
            return new OperationArgs(map);
        }
        throw new InvalidArgumentsException("Params must be a JSON object with named arguments");
    }

    public String getString(String key) {
        Object value = require(key);
        String text = value.toString().trim();
        if (text.isEmpty()) {
            throw new InvalidArgumentsException("Argument '" + key + "' cannot be empty");
        }
        return text;
    }

    public String getString(String key, String defaultValue) {
        Object value = values.get(key);
        return value == null ? defaultValue : value.toString();
    }

    public int getPositiveInt(String key) {
        int number = (int) toNumber(key);
        if (number <= 0) {
            throw new InvalidArgumentsException("Argument '" + key + "' must be greater than 0");
        }
        return number;
    }

    public double getPositiveDouble(String key) {
        double number = toNumber(key);
        if (number <= 0) {
            throw new InvalidArgumentsException("Argument '" + key + "' must be greater than 0");
        }
        return number;
    }

    private double toNumber(String key) {
        Object value = require(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException ex) {
            throw new InvalidArgumentsException("Argument '" + key + "' must be a number");
        }
    }

    private Object require(String key) {
        Object value = values.get(key);
        if (value == null) {
            throw new InvalidArgumentsException("Missing argument '" + key + "'");
        }
        return value;
    }
}
