package com.smartrent.Controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FireBaseAuth {

    public static boolean signWithEmailAndPassword(String email, String password, String signupOrSign) {
        String API_KEY = "AIzaSyDGvAbkR67e1idjDTnNaFiuNOC-kXQlV4k";
        String firebaseUrl = "https://identitytoolkit.googleapis.com/v1/accounts:";

        try {
            //  Prepare full URL
            URL url;
            if (signupOrSign.equalsIgnoreCase("signup")) {
                url = new URL(firebaseUrl + "signUp?key=" + API_KEY);
            } else {
                url = new URL(firebaseUrl + "signInWithPassword?key=" + API_KEY);
            }

            //  Setup connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

             //Prepare JSON payload
            String payload = String.format(
                "{\"email\":\"%s\",\"password\":\"%s\",\"returnSecureToken\":true}",
                email, password
            );

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes());
                os.flush();
            }

            //  Check response
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                return true;
            } else {
                // Read error response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();
                System.out.println("Error: " + response.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        //  Return false if failure
        return false;
    }
}
