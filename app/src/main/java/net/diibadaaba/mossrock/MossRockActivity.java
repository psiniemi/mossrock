package net.diibadaaba.mossrock;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MossRockActivity extends AppCompatActivity {
    private static final String TAG = "MossRock";
    private static final int off = R.drawable.btn_border_off;
    private static final int on = R.drawable.btn_border_on;
    public final Map<String, ToggleButton> buttons = new LinkedHashMap<>();
    public final Map<String, SeekBar> seekBars = new LinkedHashMap<>();
    public final Map<String, Button> scenes = new LinkedHashMap<>();
    private static MossRockActivity instance;
    public ActionRegistrar registrar;
    public MossRockActivity() {
        super();
        instance = this;
    }
    public static MossRockActivity getInstance() {
        return instance;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        getSupportActionBar().hide();
        buttons.put("viggo", (ToggleButton)findViewById(R.id.viggo));
        buttons.put("nuutti", (ToggleButton)findViewById(R.id.nuutti));
        buttons.put("venni", (ToggleButton)findViewById(R.id.venni));
        buttons.put("kitchen", (ToggleButton)findViewById(R.id.kitchen));
        buttons.put("entry", (ToggleButton)findViewById(R.id.entry));
        buttons.put("hallway", (ToggleButton)findViewById(R.id.hallway));
        buttons.put("balcony", (ToggleButton)findViewById(R.id.balcony));
        buttons.put("library", (ToggleButton)findViewById(R.id.library));
        buttons.put("bedroom", (ToggleButton)findViewById(R.id.bedroom));
        seekBars.put("viggo", (SeekBar)findViewById(R.id.viggo_dim));
        seekBars.put("nuutti", (SeekBar)findViewById(R.id.nuutti_dim));
        seekBars.put("venni", (SeekBar)findViewById(R.id.venni_dim));
        seekBars.put("balcony", (SeekBar)findViewById(R.id.balcony_dim));
        seekBars.put("library", (SeekBar)findViewById(R.id.library_dim));
        scenes.put("all_on", (Button)findViewById(R.id.all_on));
        scenes.put("all_off", (Button)findViewById(R.id.all_off));
        scenes.put("movie", (Button)findViewById(R.id.movie));
        registrar = new SlaveActions();
        registrar.registerActions(this);
    }
    public void toggleBackround(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            button.setBackground(getResources().getDrawable(on));
            button.setTextColor(Color.parseColor("#000000"));
        } else {
            button.setBackground(getResources().getDrawable(off));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
}