package com.example.managerclient;

public class Point {
    private String id;
    private String name;
    private String type;
    private double x;
    private double y;

    public Point(String id, String name, String type, double x, double y) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return name; // 用于下拉框显示
    }
} 