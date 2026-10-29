package com.example.myapplication;

public class Games {
    private String name;
    private int priority;
    private double hours;
    private String imageBase64;

    public Games() {
    }

    public Games(String name, int priority, double hours, String imageBase64) {
        this.name = name;
        this.priority = priority;
        this.hours = hours;
        this.imageBase64 = imageBase64;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public double getHours() {
        return hours;
    }

    public void setHours(double hours) {
        this.hours = hours;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    @Override
    public String toString() {
        return name + " | Priority: " + priority + " | Hours: " + hours;
    }
}