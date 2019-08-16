package net.diibadaaba.mossrock;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.mac.Mac;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;
import static com.github.mob41.blapi.HexUtil.bytes2hex;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.mob41.blapi.HexUtil.bytes2hex;
import static java.net.InetAddress.getByName;

public class ITGWActions implements ActionRegistrar {
    private static final String TAG = "MossRockITGW";
    private static final String MOSS_ROCK_CODE = "w2r2 ";
    private static final int GW_PORT = 49880;
    private final AtomicBoolean ackLock = new AtomicBoolean(false);
    private Thread receiver;
    private Thread sender;
    private Thread irSender;
    private HttpServer server;
    byte[][] VOLUMES = new byte[][] {
            IRCommands.HK_VOL_DOWN_30DB,
            IRCommands.HK_VOL_DOWN_20DB,
            IRCommands.HK_VOL_DOWN_10DB,
            IRCommands.HK_VOL_DOWN_5DB,
            null,
            IRCommands.HK_VOL_UP_5DB,
            IRCommands.HK_VOL_UP_10DB,
            IRCommands.HK_VOL_UP_20DB,
            IRCommands.HK_VOL_UP_30DB
    };
    private static DatagramSocket GW_SOCKET;
    private static ITGWActions instance;
    final BlockingQueue<byte[]> irQueue = new LinkedBlockingDeque<>();
    private RM2Device dev;
    private static final Set<String> ampScenes = new HashSet<>(Arrays.asList("venom", "wii", "ps4", "steam"));
    private ToggleButton activeAmpScene;

    private RM2Device getDev() {
        if (dev == null) {
            try {
                dev = (RM2Device) RM2Device.discoverDevices(InetAddress.getByAddress(new byte[] {0,0,0,0}), 0, 0)[0];
                dev.auth();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dev;
    }
    public  ITGWActions() {
        instance = this;
    }
    public ITGWActions getInstance() {
        return instance;
    }
    public SeekBar.OnSeekBarChangeListener getSeekListener() {
        return seekListener;
    }
    private final CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
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
    private final SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            int progress = seekBar.getProgress();
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
            for (ToggleButton btn : MossRockActivity.getInstance().buttons.values()) {
                if (btn.getId() == R.id.balcony || btn.getId() == R.id.library ||
                    btn.getId() == R.id.hallway || btn.getId() == R.id.entry ||
                    btn.getId() == R.id.kitchen) {
                    if (!btn.isChecked()) {
                        btn.setChecked(true);
                    }
                }
            }
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
            try {
                irQueue.put(IRCommands.HK_AUX);
                irQueue.put(IRCommands.HK_VOL_UP_10DB);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (ToggleButton btn : MossRockActivity.getInstance().buttons.values()) {
                if (btn.getId() == R.id.balcony) {
                    SeekBar seekBar = MossRockActivity.getInstance().seekBars.get("balcony");
                    seekBar.setProgress(0);
                    getSeekListener().onStopTrackingTouch(seekBar);
                } else if (btn.getId() == R.id.library) {
                    SeekBar seekBar = MossRockActivity.getInstance().seekBars.get("library");
                    seekBar.setProgress(0);
                    getSeekListener().onStopTrackingTouch(seekBar);
                } else {
                    if (btn.isChecked()) {
                        btn.setChecked(false);
                    }
                }
            }
        }
    };
    View.OnClickListener tv = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            try {
                irQueue.put(IRCommands.LG_ON_OFF);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };
    CompoundButton.OnCheckedChangeListener ampScene = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (!isChecked) {
                noEventSetChecked((ToggleButton) buttonView, true);
                return;
            }
            AmpSceneTag newTag = (AmpSceneTag) buttonView.getTag();
            if (activeAmpScene != null) {
                noEventSetChecked(activeAmpScene, false);
                AmpSceneTag oldTag = (AmpSceneTag) activeAmpScene.getTag();
                for (byte[] command : newTag.commands) {
                    if (!oldTag.commands.contains(command)) {
                        try {
                            irQueue.put(command);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                toggleBackround(buttonView, true);
            }
            activeAmpScene = (ToggleButton) buttonView;
            for (String next : newTag.others) {
                noEventSetChecked((ToggleButton) MossRockActivity.getInstance().scenes.get(next), false);
            }
            if (newTag.postAction != null) {
                newTag.postAction.run();
            }
        }
    };
    private final SeekBar.OnSeekBarChangeListener volumeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (VOLUMES[progress] != null) {
                try {
                    irQueue.put(VOLUMES[progress]);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            seekBar.setProgress(4);
        }
    };
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
                            MossRockActivity.getInstance().runOnUiThread(next.onSent);
                        }
                    } catch (InterruptedException e) {}
                }
            }
        }
    }
    private class IRSender implements Runnable {
        @Override
        public void run() {
            while (true) {
                byte[] next = null;
                try {
                    next = irQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                }
                if (next != null && next.length > 0) {
                    try {
                        RM2Device dev = getDev();
                        Log.i(TAG, "Sending " + bytes2hex(next));
                        dev.sendCmdPkt(new SendDataCmdPayload(next));
                    } catch (IOException e) {
                        Log.i(TAG, "Failed to send IR command", e);
                    }
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
    public void registerActions(MossRockActivity activity) {
        setButton(activity.buttons.get("viggo"), 8);
        setButton(activity.buttons.get("nuutti"), 9);
        setButton(activity.buttons.get("venni"), 10);
        setButton(activity.buttons.get("kitchen"), 11);
        setButton(activity.buttons.get("entry"), 12);
        setButton(activity.buttons.get("hallway"), 13);
        setButton(activity.buttons.get("balcony"), 14);
        setButton(activity.buttons.get("library"), 15);
        setButton(activity.buttons.get("bedroom"), 16);
        setSeekBar(activity.seekBars.get("viggo"), activity.buttons.get("viggo"));
        setSeekBar(activity.seekBars.get("nuutti"), activity.buttons.get("nuutti"));
        setSeekBar(activity.seekBars.get("venni"), activity.buttons.get("venni"));
        setSeekBar(activity.seekBars.get("balcony"), activity.buttons.get("balcony"));
        setSeekBar(activity.seekBars.get("library"), activity.buttons.get("library"));
        activity.scenes.get("all_on").setOnClickListener(allOn);
        activity.scenes.get("all_off").setOnClickListener(allOff);
        activity.scenes.get("movie").setOnClickListener(movie);
        activity.scenes.get("tv").setOnClickListener(tv);
        setAmpScene("venom", null, IRCommands.HK_AUX, IRCommands.HK_VOL_UP_30DB);
        setAmpScene("wii", new Runnable() {
            @Override
            public void run() {
                SeekBar balcony = MossRockActivity.getInstance().seekBars.get("balcony");
                balcony.setProgress(0);
                ActionRegistrar.getListener(balcony).onStopTrackingTouch(balcony);
            }
        }, IRCommands.HK_STB, IRCommands.HK_VOL_DOWN_30DB);
        setAmpScene("ps4", null, IRCommands.HK_GAME, IRCommands.HK_VOL_DOWN_30DB);
        setAmpScene("steam", null, IRCommands.HK_SERVER, IRCommands.HK_VOL_DOWN_30DB);
        try {
            getGwSocket();
            server = new HttpServer();
        } catch (IOException e) {
        }
        receiver = new Thread(new Receiver());
        sender = new Thread(new Sender());
        irSender = new Thread(new IRSender());
        receiver.start();
        sender.start();
        irSender.start();
        ToggleButton venom = (ToggleButton)activity.scenes.get("venom");
        venom.setChecked(true);
        toggleBackround(venom, true);
        SeekBar volume = MossRockActivity.getInstance().findViewById(R.id.volume);
        volume.setOnSeekBarChangeListener(volumeListener);
    }
    private static class AmpSceneTag {
        public final List<byte[]> commands;
        public final Set<String> others;
        public final Runnable postAction;
        AmpSceneTag(List<byte[]> commands, Set<String> others, Runnable postAction) {
            this.commands = commands;
            this.others = others;
            this.postAction = postAction;
        }
    }
    private void setAmpScene(String name, Runnable postAction, byte[] ... commands) {
        CompoundButton button = (CompoundButton) MossRockActivity.getInstance().scenes.get(name);
        button.setChecked(false);
        Set<String> others = new HashSet<>(ampScenes);
        others.remove(name);
        button.setTag(new AmpSceneTag(Arrays.asList(commands), others, postAction));
        button.setOnCheckedChangeListener(ampScene);
        toggleBackround(button, false);
    }
    private void setButton(final ToggleButton button, int code) {
        button.setOnCheckedChangeListener(checkedChangeListener);
        button.setTag(code);
        toggleBackround(button, button.isChecked());
    }
    private void setSeekBar(SeekBar bar, ToggleButton btn) {
        bar.setOnSeekBarChangeListener(seekListener);
        bar.setTag(btn);
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
    private void setAll(boolean on) {
        for (ToggleButton btn : MossRockActivity.getInstance().buttons.values()) {
            if (btn.isChecked() ^ on) {
                btn.setChecked(on);
            }
        }
    }
}
