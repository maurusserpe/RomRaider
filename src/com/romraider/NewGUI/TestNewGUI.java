package com.romraider.NewGUI;

import com.romraider.util.LogManager;

public class TestNewGUI {
    public static void main(String[] args) {
        LogManager.initLogging();
        NewGUI.getInstance().setVisible(true);
    }
}