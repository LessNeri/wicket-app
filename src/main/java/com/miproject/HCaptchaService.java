package com.miproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class HCaptchaService {
    private static final String VERIFY_URL = "https://hcaptcha.com/siteverify";
    private static final String SECRET_KEY = "ES_65d6.."; // Reemplaza con tu Secret Key
    
    public static boolean verifyCaptcha(String captchaResponse) {
        if (captchaResponse == null || captchaResponse.trim().isEmpty()) {
            return false;
        }
        
        try {
            URL url = new URL(VERIFY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            
            String postData = "secret=" + SECRET_KEY + "&response=" + captchaResponse;
            
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                return false;
            }
            
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            
            // La respuesta es un JSON, buscamos "success": true
            return response.toString().contains("\"success\":true");
            
        } catch (Exception e) {
            System.err.println("Error verificando hCaptcha: " + e.getMessage());
            return false;
        }
    }
}