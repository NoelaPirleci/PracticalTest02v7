package ro.pub.cs.systems.eim.practicaltest02v7;

import android.os.AsyncTask;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientAsyncTask extends AsyncTask<String, Void, String> {
    private String address;
    private int port;
    private String command;
    private TextView resultTextView;

    public ClientAsyncTask(String address, int port, String command, TextView resultTextView) {
        this.address = address;
        this.port = port;
        this.command = command;
        this.resultTextView = resultTextView;
    }

    @Override
    protected String doInBackground(String... params) {
        Socket socket = null;
        String response = "";
        try {
            socket = new Socket(address, port);
            // cerere
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(command);
            // raspuns
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            response = reader.readLine();

        } catch (IOException e) {
            response = "Error: " + e.getMessage();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        // afisez si in interfata rezultatul obtinut
        if (resultTextView != null) {
            resultTextView.setText(result);
        }
    }

}
