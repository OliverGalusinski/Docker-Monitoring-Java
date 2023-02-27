package org.example;

public class Main {
    public static void main(String[] args) {
        CheckForNewContainer checkForNewContainer = new CheckForNewContainer();
        checkForNewContainer.setName("CheckForNewContainer-Thread");
        checkForNewContainer.start();
    }
}
