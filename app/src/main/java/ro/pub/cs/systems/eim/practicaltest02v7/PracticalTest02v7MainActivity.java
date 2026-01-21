package ro.pub.cs.systems.eim.practicaltest02v7;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PracticalTest02v7MainActivity extends AppCompatActivity {


    // Declararea componentelor grafice
    private EditText serverPortEditText;
    private Button connectButton;

    private EditText clientAddressEditText;
    private EditText clientPortEditText;
    private EditText cerereEditText;
    private Button setAlarmButton;
    private TextView alarmInfoTextView;

    // Referință la thread-ul serverului
    private ServerThread serverThread = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practical_test02v7_main); // Asigură-te că XML-ul se numește activity_main.xml

        // 1. Inițializare elemente Server
        serverPortEditText = findViewById(R.id.server_port_edit_text);
        connectButton = findViewById(R.id.connect_button);

        // Logică buton pornire Server
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverPort = serverPortEditText.getText().toString();
                if (serverPort == null || serverPort.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Portul serverului trebuie completat!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pornim serverul
                if (serverThread == null || !serverThread.isAlive()) {
                    serverThread = new ServerThread(Integer.parseInt(serverPort));
                    serverThread.start();
                    Toast.makeText(getApplicationContext(), "Server started!", Toast.LENGTH_SHORT).show();
                    Log.v("Colocviu", "Server started on port " + serverPort);
                }
            }
        });

        // 2. Inițializare elemente Client
        clientAddressEditText = findViewById(R.id.client_address_edit_text);
        clientPortEditText = findViewById(R.id.client_port_edit_text);
        cerereEditText = findViewById(R.id.cerere_edit_text);
        setAlarmButton = findViewById(R.id.set_alarm_button);
        alarmInfoTextView = findViewById(R.id.alarm_info_text_view);

        // Logică buton trimitere comandă Client
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String clientAddress = clientAddressEditText.getText().toString();
                String clientPort = clientPortEditText.getText().toString();
                String comanda = cerereEditText.getText().toString(); // ex: "set,1,5" sau "poll"

                if (clientAddress.isEmpty() || clientPort.isEmpty() || comanda.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Toate câmpurile clientului sunt obligatorii!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Pornim task-ul clientului pentru a trimite comanda
                // Parametrii sunt: Adresa, Port, Comanda, TextView-ul unde afisam raspunsul
                ClientAsyncTask clientAsyncTask = new ClientAsyncTask(
                        clientAddress,
                        Integer.parseInt(clientPort),
                        comanda,
                        alarmInfoTextView
                );
                clientAsyncTask.execute();
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.i("Colocviu", "onDestroy() callback method was invoked");
        // Oprim serverul când închidem aplicația pentru a elibera portul
        if (serverThread != null) {
            serverThread.stopServer();
        }
        super.onDestroy();
    }

}