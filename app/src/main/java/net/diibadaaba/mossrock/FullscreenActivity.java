package net.diibadaaba.mossrock;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen);
        getSupportActionBar().hide();
        setButton((ToggleButton)findViewById(R.id.kitchen), 11);
    }
    private final void setButton(ToggleButton button, int code) {
        button.setOnCheckedChangeListener(checkedChangeListener);
        button.setTag(Integer.valueOf(code));
        toggleBackround(button, button.isChecked());
    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }
    private static final void sendCommand(boolean on, int lightCode) {
        String cmd = on ? "on: " : "off: ";
        Log.i(TAG, "Send command " + cmd + lightCode);
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