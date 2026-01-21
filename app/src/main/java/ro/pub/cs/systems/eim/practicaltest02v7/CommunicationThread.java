package ro.pub.cs.systems.eim.practicaltest02v7;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CommunicationThread extends Thread{
    private ServerThread serverThread;
    private Socket socket;
    // server timp
    private final String NIST_SERVER = "time-a-g.nist.gov";
    private final int NIST_PORT = 13;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // iau client dupa ip
            String clientIp = socket.getInetAddress().getHostAddress();

            // iau comanda de la client
            String request = reader.readLine();
            if (request == null || request.isEmpty()) {
                socket.close();
                return;
            }

            Log.d("Colocviu", "Comanda primita: " + request);
            String response = "";

            // parsez comanda
            // iau fiecare componenta, separata de virgula
            String[] parts = request.split(",");
            // comanda va fi prima parte a comenzii
            String command = parts[0].trim();

            switch (command) {
                case "set":
                    // set,min,s - setez alarma dupa min si s
                    try {
                        // iau min si s
                        int min = Integer.parseInt(parts[1].trim());
                        int sec = Integer.parseInt(parts[2].trim());

                        // calculez in milis
                        long durationInMillis = (min * 60 + sec) * 1000;

                        // timp de expirare
                        long expirationTime = System.currentTimeMillis() + durationInMillis;

                        serverThread.setAlarm(clientIp, expirationTime);
                        response = "Alarm set for " + min + "m " + sec + "s";
                    } catch (NumberFormatException e) {
                        response = "Error: Invalid number format";
                    }
                    break;

                case "reset":
                    // reset - sterg alarma pusa de client
                    serverThread.removeAlarm(clientIp);
                    response = "Alarm reset";
                    break;

                case "poll":
                    // poll - verific daca alarma mai e activa sau nu in functie de ip client
                    Long alarmTime = serverThread.getAlarm(clientIp);

                    if (alarmTime == null) {
                        response = "none";
                    } else {
                        // verific cu nist ca sa vad daca mai e activa sau nu
                        long nistTime = getNistTime();

                        if (nistTime < alarmTime) {
                            response = "active";
                        } else {
                            response = "inactive";
                        }
                    }
                    break;

                default:
                    response = "Unknown command";
                    break;
            }

            // trimit raspuns inapoi
            writer.println(response);
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Metoda de conectare la nist + luare params
    private long getNistTime() {
        Socket nistSocket = null;
        try {
            // deschid socketul de nist pe serverul 13
            nistSocket = new Socket(NIST_SERVER, NIST_PORT);
            BufferedReader nistReader = new BufferedReader(new InputStreamReader(nistSocket.getInputStream()));

            String line = "";
            while ((line = nistReader.readLine()) != null) {
                if (line.length() > 0)
                    break;
            }

            // citesc info de la nist
            if (line != null && !line.isEmpty()) {
                // parsez string-ul
                String[] timeParts = line.trim().split("\\s+");
                if (timeParts.length >= 3) {
                    String dateString = timeParts[1]; // formatul cu an, data
                    String timeString = timeParts[2]; // formatul de inters cu ore, min, s

                    // parsez data
                    SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = sdf.parse(dateString + " " + timeString);

                    if (date != null) {
                        return date.getTime();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Colocviu", "Eroare conexiune NIST: " + e.getMessage());
        } finally {
            if (nistSocket != null) {
                try { nistSocket.close(); } catch (IOException e) {}
            }
        }
        return -1; // Eroare
    }

}
