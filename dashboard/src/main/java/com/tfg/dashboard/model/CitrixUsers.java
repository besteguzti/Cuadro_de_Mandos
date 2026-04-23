package com.tfg.dashboard.model;

public class CitrixUsers {

    private int connectedUsers;

    public CitrixUsers(int connectedUsers) {
        this.connectedUsers = connectedUsers;
    }

    public int getConnectedUsers() {
        return connectedUsers;
    }

    public void setConnectedUsers(int connectedUsers) {
        this.connectedUsers = connectedUsers;
    }
}