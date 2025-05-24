package com.example.chatgptapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class MainActivity extends Activity {
    private static final String OPENAI_API_KEY = "(OPENAI-API-KEY)";
    private static final String OPENAI_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private final OkHttpClient client = new OkHttpClient();

    private EditText inputEditText;
    private Button sendButton;
    private TextView chatTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputEditText = findViewById(R.id.inputEditText);
        sendButton = findViewById(R.id.sendButton);
        chatTextView = findViewById(R.id.chatTextView);

        sendButton.setOnClickListener(v -> {
            String userInput = inputEditText.getText().toString();
            chatTextView.append("你：" + userInput + "\n");
            sendToOpenAI(userInput);
            inputEditText.setText("");
        });
    }

    private void sendToOpenAI(String message) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-3.5-turbo");

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.put(userMessage);
            body.put("messages", messages);

            Request request = new Request.Builder()
                    .url(OPENAI_ENDPOINT)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> chatTextView.append("錯誤：" + e.getMessage() + "\n"));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String jsonResponse = response.body().string();
                        try {
                            JSONObject json = new JSONObject(jsonResponse);
                            String reply = json.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            runOnUiThread(() -> chatTextView.append("ChatGPT：" + reply + "\n"));
                        } catch (Exception e) {
                            runOnUiThread(() -> chatTextView.append("解析回應錯誤\n"));
                        }
                    } else {
                        String errorBody = response.body().string();  // 看錯誤細節
                        runOnUiThread(() -> chatTextView.append("API 請求失敗：" + response.code() + "\n" + errorBody + "\n"));
                    }
                }

            });
        } catch (Exception e) {
            chatTextView.append("構建請求時發生錯誤\n");
        }
    }
}
