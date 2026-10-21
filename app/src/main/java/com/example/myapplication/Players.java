package com.example.myapplication;

public class Players {
    private String fullName;
    private String number;
    private int elo;
    public Players() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }
    @Override
    public String toString(){
        return "\n\n*******************\n full name: " + this.getFullName() + "\n phone number: " + this.getNumber() + "\n current elo " + this.getElo() + "\n**********************\n\n";
    }
}
