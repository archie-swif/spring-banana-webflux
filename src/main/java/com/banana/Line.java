package com.banana;

public class Line {
    String value;

    public Line(String value) {
        this.value = value;
        if (value.equals("4")) {
            throw new RuntimeException("Throwing error on #4");
        }
    }

    public Line() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Line{" +
                "value='" + value + '\'' +
                '}';
    }
}
