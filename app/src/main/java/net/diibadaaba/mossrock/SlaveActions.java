package net.diibadaaba.mossrock;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class SlaveActions implements ActionRegistrar {
    private static final String TAG = "MossRockSlave";
    private Thread sender;
    private class Sender implements Runnable {
        @Override
        public void run() {
            while (true) {
                MRMessage next = null;
                try {
                    next = messageQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) { }
                if (next != null) {
                    Log.i(TAG, "Sending " + next.command);
                    getAction(next.command);
                    new Thread(next.onSent).start();
                }
            }
        }
    }

    private SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int value = seekBar.getProgress();
            String name = (String)seekBar.getTag();
            String action = "dim/";
            try {
                messageQueue.put(new MRMessage(statusTask, action + name + "?" + value));
            } catch (InterruptedException e) {
            }
        }
    };
    private CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            String action = isChecked ? "on/" : "off/";
            String name = (String)buttonView.getTag();
            try {
                messageQueue.put(new MRMessage(statusTask, action + name));
            } catch (InterruptedException e) {
            }
            toggleBackround(buttonView, isChecked);
        }
    };

    private class SceneEnabler implements View.OnClickListener {
        private final String scene;

        public SceneEnabler(String scene) {
            this.scene = scene;
        }

        @Override
        public void onClick(View v) {
            try {
                messageQueue.put(new MRMessage(statusTask, "scene/" + scene));
            } catch (InterruptedException e) {
            }
        }
    }
    Button.OnClickListener allOn = new SceneEnabler("all_on");
    Button.OnClickListener allOff = new SceneEnabler("all_off");
    Button.OnClickListener movie = new SceneEnabler("movie");
    @Override
    public void registerActions(MossRockActivity activity) {
        setButton(activity, "viggo");
        setButton(activity, "nuutti");
        setButton(activity, "venni");
        setButton(activity, "kitchen");
        setButton(activity, "entry");
        setButton(activity, "hallway");
        setButton(activity, "balcony");
        setButton(activity, "library");
        setButton(activity, "bedroom");
        setSeekBar(activity, "viggo");
        setSeekBar(activity, "nuutti");
        setSeekBar(activity, "venni");
        setSeekBar(activity, "balcony");
        setSeekBar(activity, "library");
        ((Button)activity.scenes.get("all_on")).setOnClickListener(allOn);
        ((Button)activity.scenes.get("all_off")).setOnClickListener(allOff);
        ((Button)activity.scenes.get("movie")).setOnClickListener(movie);
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateStatus();
            }
        }).start();
        sender = new Thread(new Sender());
        sender.start();
    }

    private void setSeekBar(MossRockActivity activity, String seekBar) {
        SeekBar bar = activity.seekBars.get(seekBar);
        bar.setTag(seekBar);
        bar.setOnSeekBarChangeListener(seekListener);
    }

    private void setButton(MossRockActivity activity, String btn) {
        CompoundButton button = activity.buttons.get(btn);
        button.setTag(btn);
        button.setOnCheckedChangeListener(checkedChangeListener);
    }

    @Override
    public SeekBar.OnSeekBarChangeListener getSeekListener() {
        return seekListener;
    }

    @Override
    public CompoundButton.OnCheckedChangeListener getCheckedChangeListener() {
        return checkedChangeListener;
    }
    public void updateStatus() {
        updateStatus(getAction("status"));
    }
    public void updateStatus(String status) {
        try {
            Log.i(TAG, "Got status: " + status);
            final JSONObject json = new JSONObject(status);
            for (Iterator<String> it = json.keys(); it.hasNext(); ) {
                String key = it.next();
                if (MossRockActivity.getInstance().buttons.containsKey(key)) {
                    boolean checked = json.getBoolean(key);
                    Log.i(TAG, "Setting button " + key + " to " + (checked ? "on" : "off"));
                    ToggleButton button = MossRockActivity.getInstance().buttons.get(key);
                    noEventSetChecked(button, checked);
                }
            }
        } catch (JSONException e) {
        }
    }
    private String getAction(String path)  {
        try {
            URL url = new URL("http://192.168.86.167:8088/" + path);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
                return readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            return "{}";
        }
    }

    private String readStream(InputStream in) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = in.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString("UTF-8");
    }
    Runnable statusTask = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                for (int i = 0; i < 5; i++) {
                    updateStatus();
                    try {
                        this.wait(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    };
}
