package net.diibadaaba.mossrock;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.TypefaceSpan;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.LinkedHashMap;
import java.util.Map;

public class MossRockActivity extends AppCompatActivity {
    private static final String TAG = "MossRock";
    private static final int off = R.drawable.btn_border_off;
    private static final int on = R.drawable.btn_border_on;
    public final Map<String, ToggleButton> buttons = new LinkedHashMap<>();
    public final Map<String, SeekBar> seekBars = new LinkedHashMap<>();
    public final Map<String, Button> scenes = new LinkedHashMap<>();
    public SeekBar volume;
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
        buttons.put("viggo", findViewById(R.id.viggo));
        buttons.put("nuutti", findViewById(R.id.nuutti));
        buttons.put("venni", findViewById(R.id.venni));
        buttons.put("kitchen", findViewById(R.id.kitchen));
        buttons.put("entry", findViewById(R.id.entry));
        buttons.put("hallway", findViewById(R.id.hallway));
        buttons.put("balcony", findViewById(R.id.balcony));
        buttons.put("library", findViewById(R.id.library));
        buttons.put("bedroom", findViewById(R.id.bedroom));
        seekBars.put("viggo", findViewById(R.id.viggo_dim));
        seekBars.put("nuutti", findViewById(R.id.nuutti_dim));
        seekBars.put("venni", findViewById(R.id.venni_dim));
        seekBars.put("balcony", findViewById(R.id.balcony_dim));
        seekBars.put("library", findViewById(R.id.library_dim));
        scenes.put("all_on", findViewById(R.id.all_on));
        scenes.put("all_off", findViewById(R.id.all_off));
        scenes.put("movie", findViewById(R.id.movie));
        scenes.put("tv", findViewById(R.id.tv));
        scenes.put("venom", findViewById(R.id.venom));
        scenes.put("wii", findViewById(R.id.wii));
        scenes.put("ps4", findViewById(R.id.ps4));
        scenes.put("steam", findViewById(R.id.steam));
        volume = findViewById(R.id.volume);
        registrar = new ITGWActions();
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