package net.diibadaaba.mossrock;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.net.InetAddress.getByName;

public class MossRockActivity extends AppCompatActivity {
    private static final String TAG = "MossRock";
    private static final int off = R.drawable.btn_border_off;
    private static final int on = R.drawable.btn_border_on;
    private static final String MOSS_ROCK_CODE = "w2r2 ";
    public final Map<String, ToggleButton> buttons = new LinkedHashMap<>();
    public final Map<String, SeekBar> seekBars = new LinkedHashMap<>();
    public final Map<String, Button> scenes = new LinkedHashMap<>();
    private static final int GW_PORT = 49880;
    private static DatagramSocket GW_SOCKET;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static MossRockActivity instance;
    private final AtomicBoolean ackLock = new AtomicBoolean(false);
    private final BlockingQueue<MRMessage> messageQueue = new LinkedBlockingDeque<>(10);
    private Thread receiver;
    private Thread sender;
    private HttpServer server;
    private static class MRMessage {
        public final Runnable onSent;
        public final String command;
        public MRMessage(Runnable onSent, String command) {
            this.onSent = onSent;
            this.command = command;
        }
    }
    private class Sender implements Runnable {
        @Override
        public void run() {
            while (true) {
                MRMessage next = null;
                try {
                    next = messageQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) { }
                ackLock.set(false);
                while (next != null && doSend(next.command) && !ackLock.get()) {
                    try {
                        synchronized (ackLock) {
                            ackLock.wait(4000);
                        }
                        if (ackLock.get()) {
                            runOnUiThread(next.onSent);
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
    private class Receiver implements Runnable {
        byte[] buffer = new byte[256];
        @Override
        public void run() {
            while (true) {
                DatagramPacket datagramPacket = new DatagramPacket(this.buffer, this.buffer.length);
                try {
                    getGwSocket().receive(datagramPacket);
                    String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    Log.i(TAG, "Received: " + message);
                    if (message.startsWith("HCGW:")) {
                        ackLock.set(true);
                        synchronized (ackLock) {
                            ackLock.notifyAll();
                        }
                    }
                } catch (IOException e) {
                }
            }
        }
    }
    public MossRockActivity() {
        super();
        instance = this;
    }
    public static MossRockActivity getInstance() {
        return instance;
    }
    private static InetAddress getGwAddress() throws UnknownHostException {
        return getByName("192.168.86.37");
    }
    private static DatagramSocket getGwSocket() throws SocketException {
        if (GW_SOCKET != null && !GW_SOCKET.isClosed()) {
            return GW_SOCKET;
        }
        Log.d(TAG, "Not connected");
        GW_SOCKET = new DatagramSocket();
        GW_SOCKET.setSoTimeout(2000);
        return GW_SOCKET;
    }
    public final OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
            Integer lightCode = (Integer)buttonView.getTag();
            sendCommand(isChecked, lightCode, new Runnable() {
                @Override
                public void run() {
                    toggleBackround(buttonView, isChecked);
                }
            });
        }
    };
    public final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            int progress = seekBar.getProgress() + 1;
            final ToggleButton btn = (ToggleButton)seekBar.getTag();
            Integer lightCode = (Integer)btn.getTag();
            sendDimCommand(progress, lightCode, new Runnable() {
                @Override
                public void run() {
                    if (!btn.isChecked()) {
                        noEventSetChecked(btn, true);
                    }

                }
            });
        }
    };
    View.OnClickListener allOn = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setAll(true);
        }
    };
    View.OnClickListener allOff = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setAll(false);
        }
    };
    View.OnClickListener movie = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            for (ToggleButton btn : buttons.values()) {
                if (btn.getId() == R.id.balcony) {
                    SeekBar seekBar = (SeekBar)findViewById(R.id.balcony_dim);
                    seekBar.setProgress(0);
                    seekListener.onStopTrackingTouch(seekBar);
                } else if (btn.getId() == R.id.library) {
                    SeekBar seekBar = (SeekBar)findViewById(R.id.library_dim);
                    seekBar.setProgress(0);
                    seekListener.onStopTrackingTouch(seekBar);
                } else {
                    if (btn.isChecked()) {
                        btn.setChecked(false);
                    }
                }
            }
        }
    };
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
        setButton((ToggleButton)findViewById(R.id.viggo), 8);
        setButton((ToggleButton)findViewById(R.id.nuutti), 9);
        setButton((ToggleButton)findViewById(R.id.venni), 10);
        setButton((ToggleButton)findViewById(R.id.kitchen), 11);
        setButton((ToggleButton)findViewById(R.id.entry), 12);
        setButton((ToggleButton)findViewById(R.id.hallway), 13);
        setButton((ToggleButton)findViewById(R.id.balcony), 14);
        setButton((ToggleButton)findViewById(R.id.library), 15);
        setButton((ToggleButton)findViewById(R.id.bedroom), 16);
        setSeekBar((SeekBar)findViewById(R.id.viggo_dim), (ToggleButton)findViewById(R.id.viggo));
        setSeekBar((SeekBar)findViewById(R.id.nuutti_dim), (ToggleButton)findViewById(R.id.nuutti));
        setSeekBar((SeekBar)findViewById(R.id.venni_dim), (ToggleButton)findViewById(R.id.venni));
        setSeekBar((SeekBar)findViewById(R.id.balcony_dim), (ToggleButton)findViewById(R.id.balcony));
        setSeekBar((SeekBar)findViewById(R.id.library_dim), (ToggleButton)findViewById(R.id.library));
        ((Button)findViewById(R.id.all_on)).setOnClickListener(allOn);
        ((Button)findViewById(R.id.all_off)).setOnClickListener(allOff);
        ((Button)findViewById(R.id.movie)).setOnClickListener(movie);
        try {
            getGwSocket();
            server = new HttpServer();
        } catch (IOException e) {
        }
        receiver = new Thread(new Receiver());
        sender = new Thread(new Sender());
        receiver.start();
        sender.start();
    }
    private void setButton(ToggleButton button, int code) {
        button.setOnCheckedChangeListener(checkedChangeListener);
        button.setTag(code);
        toggleBackround(button, button.isChecked());
    }
    private void setSeekBar(SeekBar bar, ToggleButton btn) {
        bar.setOnSeekBarChangeListener(seekListener);
        bar.setTag(btn);
    }
    private void noEventSetChecked(ToggleButton btn, boolean checked) {
        btn.setOnCheckedChangeListener(null);
        btn.setChecked(checked);
        toggleBackround(btn, checked);
        btn.setOnCheckedChangeListener(checkedChangeListener);
    }
    private void setAll(boolean on) {
        for (ToggleButton btn : buttons.values()) {
            if (btn.isChecked() ^ on) {
                btn.setChecked(on);
            }
        }
    }
    private void sendCommand(boolean on, int lightCode, Runnable onSent) {
        String cmd = on ? "on: " : "off: ";
        Log.i(TAG, "Send command " + cmd + "to " + lightCode);
        String command = Command.commandLearn(MOSS_ROCK_CODE, lightCode, on);
        Log.i(TAG, command);
        try {
            messageQueue.put(new MRMessage(onSent, command));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void sendDimCommand(int level, int lightCode, Runnable onSent) {
        Log.i(TAG, "Send dim command " + level + " to " + lightCode);
        String command = Command.commandDim(MOSS_ROCK_CODE, lightCode, level);
        Log.i(TAG, command);
        try {
            messageQueue.put(new MRMessage(onSent, command));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private static boolean doSend(final String command) {
        byte[] bytes = command.getBytes();
        try {
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, getGwAddress(), GW_PORT);
            getGwSocket().send(packet);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    private void toggleBackround(CompoundButton button, boolean isChecked) {
        if (isChecked) {
            button.setBackground(getResources().getDrawable(on));
            button.setTextColor(Color.parseColor("#000000"));
        } else {
            button.setBackground(getResources().getDrawable(off));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        }
    }
}