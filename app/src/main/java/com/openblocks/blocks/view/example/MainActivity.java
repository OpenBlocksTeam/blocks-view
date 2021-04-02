package com.openblocks.blocks.view.example;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.openblocks.blocks.view.SketchwareBlocksParser;
import com.openblocks.blocks.view.BlocksView;
import com.openblocks.blocks.view.BlocksViewEvent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    final int PICK_EVENT_REC_CODE = 10;
    ArrayList<BlocksViewEvent> events = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_EVENT_REC_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    InputStream stream = getContentResolver().openInputStream(uri);

                    int count;
                    byte[] buffer = new byte[1024];
                    ByteArrayOutputStream byteStream =
                            new ByteArrayOutputStream(stream.available());

                    while (true) {
                        count = stream.read(buffer);
                        if (count <= 0)
                            break;
                        byteStream.write(buffer, 0, count);
                    }

                    stream.close();

                    events = new SketchwareBlocksParser(decrypt(byteStream.toByteArray())).parse();

                    byteStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pickEvent();
            }
        }
    }

    private void pickFile() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i   .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*");

        startActivityForResult(i, PICK_EVENT_REC_CODE);

        Toast.makeText(this, "Navigate to a logic file", Toast.LENGTH_LONG).show();
    }

    private void pickEvent() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Choose an event to display");

        String[] items = new String[events.size()];

        for (int i = 0; i < events.size(); i++) {
            items[i] = events.get(i).activity_name + ": " + events.get(i).name;
        }

        alertDialog.setItems(items, (dialog, which) -> {
            BlocksView blocksView = findViewById(R.id.blocks_view);
            blocksView.setEvent(events.get(which));
            Log.d("MainActivity", "pickEvent: event: " + events);
            blocksView.invalidate();
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    public static String decrypt(byte[] data) {
        try {
            Cipher instance = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] bytes = "sketchwaresecure".getBytes();
            instance.init(2, new SecretKeySpec(bytes, "AES"), new IvParameterSpec(bytes));

            return new String(instance.doFinal(data));
        } catch (Exception e) {
            Log.e("Decryptor", "Error while decrypting");
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.open_file) {
            pickFile();

            return true;
        } else if (id == R.id.open_event) {
            pickEvent();
            
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}