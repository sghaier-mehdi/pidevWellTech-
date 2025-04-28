package com.welltech.util;

import java.io.IOException;

public class SimpleTTS {

    public static void speak(String text) {
        try {
            String command = "PowerShell -Command \"Add-Type –AssemblyName System.Speech; " +
                    "$speak = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                    "$speak.SelectVoice('Microsoft David Desktop'); " + // Force English Male Voice
                    "$speak.Speak('" + text.replace("'", "''") + "');\"";
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
