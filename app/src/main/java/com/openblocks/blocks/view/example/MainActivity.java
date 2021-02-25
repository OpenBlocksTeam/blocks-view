package com.openblocks.blocks.view.example;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.openblocks.blocks.view.SketchwareBlocksParser;
import com.openblocks.blocks.view.SketchwareBlocksView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SketchwareBlocksView blocksView = findViewById(R.id.blocks_view);
    }
}