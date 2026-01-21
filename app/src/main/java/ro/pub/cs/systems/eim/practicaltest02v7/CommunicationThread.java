package ro.pub.cs.systems.eim.practicaltest02v7;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class CommunicationThread extends Thread{
    private ServerThread serverThread;
    private Socket socket;

    // Constante API
    final String WEB_SERVICE_ADDRESS = "https://time-a-g.nist.gov";

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // 1. Pregătim Reader/Writer pentru client
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // 2. Citim cererea clientului (cerere alarma)
            String cerere = reader.readLine();

            if (cerere == null || cerere.isEmpty()) {
                Log.e("Colocviu", "Error receiving parameters from client");
                return;
            }

            // 3. Verificăm Cache-ul (HashMap-ul din ServerThread)
            GetCerereInfo data = serverThread.getData(cerere);

            // Daca NU avem datele, facem Request HTTP
            if (data == null) {
                Log.v("Colocviu", "Getting data from HTTP for " + city);

                // Construim URL-ul
                String urlString = WEB_SERVICE_ADDRESS + city + "&appid=" + API_KEY + "&units=metric";

                // Facem Request-ul HTTP (Native Java)
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    content.append(line);
                }
                bufferedReader.close();
                urlConnection.disconnect();

                // Parsăm JSON-ul
                JSONObject contentJson = new JSONObject(content.toString());

                // Extragem datele "main"
                JSONObject main = contentJson.getJSONObject("main");
                String temperature = main.getString("temp");
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");

                // Extragem "wind"
                JSONObject wind = contentJson.getJSONObject("wind");
                String windSpeed = wind.getString("speed");

                // Extragem "weather" (care e array)
                JSONObject weather = contentJson.getJSONArray("weather").getJSONObject(0);
                String condition = weather.getString("main");

                // Creăm obiectul și îl salvăm în Cache
                data = new GetWeatherInfo(temperature, windSpeed, condition, pressure, humidity);
                serverThread.setData(city, data);
            } else {
                Log.v("Colocviu", "Getting data from CACHE for " + city);
            }

            // 4. Construim răspunsul în funcție de ce a cerut clientul
            String result = "";
            switch (informationType) {
                case "all":
                    result = data.toString();
                    break;
                case "temperature":
                    result = data.getTemperature();
                    break;
                case "wind":
                    result = data.getWindSpeed();
                    break;
                case "condition":
                    result = data.getCondition();
                    break;
                case "humidity":
                    result = data.getHumidity();
                    break;
                case "pressure":
                    result = data.getPressure();
                    break;
                default:
                    result = "Wrong information type";
            }

            // 5. Trimitem răspunsul la client
            writer.println(result);
            socket.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.e("Colocviu", "Error in CommunicationThread: " + e.getMessage());
        }
    }
}
