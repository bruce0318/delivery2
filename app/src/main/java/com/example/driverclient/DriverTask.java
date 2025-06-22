package com.example.driverclient;

public class DriverTask {
    private String startPointName;
    private String endPointName;
    private String date;
    private int driverOrder;
    private String startId;
    private String endId;
    private String taskId;
    private double startX, startY, endX, endY;

    public DriverTask(String startId, String endId, String date, int driverOrder) {
        this.startId = startId;
        this.endId = endId;
        this.date = date;
        this.driverOrder = driverOrder;
    }

    public DriverTask(String taskId, String startId, String endId, String startPointName, String endPointName, String date, int driverOrder, double startX, double startY, double endX, double endY) {
        this.taskId = taskId;
        this.startId = startId;
        this.endId = endId;
        this.startPointName = startPointName;
        this.endPointName = endPointName;
        this.date = date;
        this.driverOrder = driverOrder;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    // Getters
    public String getTaskId() { return taskId; }
    public String getStartPointName() { return startPointName; }
    public String getEndPointName() { return endPointName; }
    public String getDate() { return date; }
    public int getDriverOrder() { return driverOrder; }
    public String getStartId() { return startId; }
    public String getEndId() { return endId; }
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public double getEndX() { return endX; }
    public double getEndY() { return endY; }

    // Setters
    public void setStartPointName(String startPointName) { this.startPointName = startPointName; }
    public void setEndPointName(String endPointName) { this.endPointName = endPointName; }
    public void setStartCoords(double x, double y) {
        this.startX = x;
        this.startY = y;
    }
    public void setEndCoords(double x, double y) {
        this.endX = x;
        this.endY = y;
    }
} 