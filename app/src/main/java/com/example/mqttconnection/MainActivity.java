package com.example.mqttconnection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;

import java.nio.charset.StandardCharsets;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;

public class MainActivity extends AppCompatActivity {

    private EditText editTextTopic;
    private EditText editTextMessage;
    private Button buttonSubscribe;
    private Button buttonSend;
    private TextView textViewMessages;

    private Mqtt5BlockingClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextTopic = findViewById(R.id.editTextTopic);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSubscribe = findViewById(R.id.buttonSubscribe);
        buttonSend = findViewById(R.id.buttonSend);
        textViewMessages = findViewById(R.id.textViewMessages);

        buttonSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToTopic();
            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String host = "ba7331bfbac94a9ca8426144b709ee41.s1.eu.hivemq.cloud";
                    final String username = "eric.carneiro@fatec.sp.gov.br";
                    final String password = "sccp1910";

                    // create an MQTT client
                    client = MqttClient.builder()
                            .useMqttVersion5()
                            .serverHost(host)
                            .serverPort(8883)
                            .sslWithDefaultConfig()
                            .buildBlocking();

                    // connect to HiveMQ Cloud with TLS and username/pw
                    client.connectWith()
                            .simpleAuth()
                            .username(username)
                            .password(StandardCharsets.UTF_8.encode(password))
                            .applySimpleAuth()
                            .send();

                    if (client.getState().isConnected()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Conexão bem-sucedida", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Falha na conexão", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    // set a callback that is called when a message is received (using the async API style)
                    client.toAsync().publishes(ALL, (publish) -> {
                        onMessageReceived(publish);
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void subscribeToTopic() {
        String topic = editTextTopic.getText().toString();
        try {
            client.subscribeWith()
                    .topicFilter(topic)
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String topic = editTextTopic.getText().toString();
        String message = editTextMessage.getText().toString();
        try {
            client.publishWith()
                    .topic(topic)
                    .payload(StandardCharsets.UTF_8.encode(message))
                    .send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onMessageReceived(Mqtt5Publish publish) {
        final String message = publish.getTopic() + " -> " + new String(publish.getPayloadAsBytes(), StandardCharsets.UTF_8);

        // Update UI on the UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewMessages.append(message + "\n");
            }
        });
    }
}