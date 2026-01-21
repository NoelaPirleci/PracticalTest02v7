package ro.pub.cs.systems.eim.practicaltest02v7;

import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import ro.pub.cs.systems.eim.practicaltest02v7.CommunicationThread;

public class ServerThread extends Thread {

    private int port;
    private ServerSocket serverSocket;

    // Mapare între IP-ul clientului și Timpul de expirare (in milisecunde)
    // Folosim IP-ul ca identificator unic pentru sesiune simplă
    private HashMap<String, Long> clientAlarms;

    public ServerThread(int port) {
        this.port = port;
        this.clientAlarms = new HashMap<>();
    }

    // Metode sincronizate pentru accesul la date (Thread-Safe)
    public synchronized void setAlarm(String clientIp, long expirationTime) {
        clientAlarms.put(clientIp, expirationTime);
    }

    public synchronized Long getAlarm(String clientIp) {
        return clientAlarms.get(clientIp);
    }

    public synchronized void removeAlarm(String clientIp) {
        clientAlarms.remove(clientIp);
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            Log.v("Colocviu", "Server started on port " + port);

            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                Log.v("Colocviu", "A client has connected from " + socket.getInetAddress());

                // Pornim firul de comunicație
                new CommunicationThread(this, socket).start();
            }
        } catch (IOException ioException) {
            Log.e("Colocviu", "Server exception: " + ioException.getMessage());
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
}
