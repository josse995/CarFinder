package fdi.ucm.carfinder.connection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Mauri on 24/04/2017.
 */

public class Conexion {

    private final String url = "http://147.96.110.183/carfinder/";

    protected JSONObject ejecutar(String peticion, String modulo) {
        HttpURLConnection client = null;
        JSONObject result = new JSONObject();
        try {
            URL urlConnection = new URL(this.url + modulo);
            client = (HttpURLConnection) urlConnection.openConnection();
            client.setReadTimeout(10000);
            client.setConnectTimeout(10000);
            client.setRequestMethod("POST");
            client.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            client.setDoOutput(true);
            client.setDoInput(true);
            StringBuilder response = new StringBuilder();

            DataOutputStream wr = new DataOutputStream(client.getOutputStream());
            wr.writeBytes(peticion);
            wr.flush();
            wr.close();

            InputStream in = new BufferedInputStream(client.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            result = new JSONObject(response.toString());
        } catch (IOException e) {
            try {
                result.put("errorno", 404);
                result.put("errorMessage", "Existe un problema de conexion con el servidor");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (client != null )
                client.disconnect();
            return result;
        }
    }
}
