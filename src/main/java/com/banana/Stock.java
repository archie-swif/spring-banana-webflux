package com.banana;

public class Stock {
    String name;
    String value;

    public Stock(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        return "Stock{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
