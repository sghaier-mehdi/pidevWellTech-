package com.welltech.util;

import java.io.IOException;

public class SimpleTTS {

    public static Process currentSpeechProcess = null;

    public static void speak(String text) {
        try {
            // Escape single quotes ' -> '' for PowerShell
            String safeText = text.replace("'", "''");

            String command = "powershell.exe -Command \"Add-Type -AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.SelectVoice('Microsoft David Desktop'); " +
                    "$speak.Speak('" + safeText + "');\"";

            // Kill previous speaking if necessary
            if (currentSpeechProcess != null && currentSpeechProcess.isAlive()) {
                currentSpeechProcess.destroy();
            }

            currentSpeechProcess = Runtime.getRuntime().exec(command);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void stop() {
        if (currentSpeechProcess != null && currentSpeechProcess.isAlive()) {
            currentSpeechProcess.destroy();
        }
    }
}
