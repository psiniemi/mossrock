package net.diibadaaba.mossrock;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.ToggleButton;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    private static final String TAG = "MossRock";
    private static final int off = R.drawable.btn_border_off;
    private static final int on = R.drawable.btn_border_on;
    private final OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            Integer lightCode = (Integer)buttonView.getTag();
            sendCommand(isChecked, lightCode.intValue());
            toggleBackround(buttonView, isChecked);
        }
    };
    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            ToggleButton btn = (ToggleButton)seekBar.getTag();
            Integer lightCode = (Integer)btn.getTag();
            if (progress == 0) {
                sendCommand(false, lightCode.intValue());
                if (btn.isChecked()) {
                    noEventSetChecked(btn, false);
                }
            } else if (progress != 0 && !btn.isChecked()) {
                noEventSetChecked(btn, true);
                sendDimCommand(progress, lightCode.intValue());
            }
        }
    };
    private final void noEventSetChecked(ToggleButton btn, boolean checked) {
        btn.setOnCheckedChangeListener(null);
        btn.setChecked(checked);
        toggleBackround(btn, checked);
        btn.setOnCheckedChangeListener(checkedChangeListener);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        getSupportActionBar().hide();
        setButton((ToggleButton)findViewById(R.id.kitchen), 11);
        setButton((ToggleButton)findViewById(R.id.entry), 12);
        setButton((ToggleButton)findViewById(R.id.nuutti), 9);
        setButton((ToggleButton)findViewById(R.id.viggo), 11);
        setButton((ToggleButton)findViewById(R.id.hallway), 13);
        setButton((ToggleButton)findViewById(R.id.venni), 10);
        setButton((ToggleButton)findViewById(R.id.balcony), 14);
        setButton((ToggleButton)findViewById(R.id.library), 15);
        setButton((ToggleButton)findViewById(R.id.bedroom), 16);
        setSeekBar((SeekBar)findViewById(R.id.nuutti_dim), (ToggleButton)findViewById(R.id.nuutti));
        setSeekBar((SeekBar)findViewById(R.id.viggo_dim), (ToggleButton)findViewById(R.id.viggo));
        setSeekBar((SeekBar)findViewById(R.id.venni_dim), (ToggleButton)findViewById(R.id.venni));
        setSeekBar((SeekBar)findViewById(R.id.balcony_dim), (ToggleButton)findViewById(R.id.balcony));
        setSeekBar((SeekBar)findViewById(R.id.library_dim), (ToggleButton)findViewById(R.id.library));
    }
    private final void setButton(ToggleButton button, int code) {
        button.setOnCheckedChangeListener(checkedChangeListener);
        button.setTag(Integer.valueOf(code));
        toggleBackround(button, button.isChecked());
    }
    private final void setSeekBar(SeekBar bar, ToggleButton btn) {
        bar.setOnSeekBarChangeListener(seekListener);
        bar.setTag(btn);
    }
    private static final void sendCommand(boolean on, int lightCode) {
        String cmd = on ? "on: " : "off: ";
        Log.i(TAG, "Send command " + cmd + "to " + lightCode);
    }
    private static final void sendDimCommand(int level, int lightCode) {
        Log.i(TAG, "Send dim command " + level + " to " + lightCode);
    }
    private final void toggleBackround(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            button.setBackground(getResources().getDrawable(on));
            button.setTextColor(0);
        } else {
            button.setBackground(getResources().getDrawable(off));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        }

    }
}