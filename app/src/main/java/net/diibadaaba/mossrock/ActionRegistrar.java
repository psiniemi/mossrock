package net.diibadaaba.mossrock;

import android.util.Log;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.github.mob41.blapi.RM2Device;
import com.github.mob41.blapi.pkt.cmd.rm2.SendDataCmdPayload;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.mob41.blapi.HexUtil.bytes2hex;

public interface ActionRegistrar {
    final String TAG = "ActionRegistrar";
    void registerActions(MossRockActivity activity);
    final BlockingQueue<MRMessage> messageQueue = new LinkedBlockingDeque<>(10);
    final BlockingQueue<byte[]> irQueue = new LinkedBlockingDeque<>();
    Thread irSender = new Thread(new IRSender());
    static AtomicReference<RM2Device> dev = new AtomicReference<>(null);
    class IRSender implements Runnable {
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
                        RM2Device dev = ActionRegistrar.getDev();
                        Log.i(TAG, "Sending " + bytes2hex(next));
                        dev.sendCmdPkt(new SendDataCmdPayload(next));
                    } catch (IOException e) {
                        Log.i(TAG, "Failed to send IR command", e);
                    }
                }
            }
        }
    }
    static RM2Device getDev() {
        if (dev.get() == null) {
            try {
                dev.set((RM2Device) RM2Device.discoverDevices(InetAddress.getByAddress(new byte[] {0,0,0,0}), 0, 0)[0]);
                dev.get().auth();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return dev.get();
    }
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
    static class MRMessage {
        public final Runnable onSent;
        public final String command;
        public MRMessage(Runnable onSent, String command) {
            this.onSent = onSent;
            this.command = command;
        }
    }
    default void setChecked(final CompoundButton btn, final boolean checked) {
        Runnable toggleAction = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    btn.setChecked(checked);
                    this.notify();
                }
            }
        };
        synchronized (toggleAction) {
            MossRockActivity.getInstance().runOnUiThread(toggleAction);
            try {
                toggleAction.wait();
            } catch (InterruptedException e) {
            }
        }
    }
    default void setProgress(final SeekBar seekBar, int progress) {
        Runnable seekAction = new Runnable() {
            @Override
            public void run() {
                synchronized (this) {
                    seekBar.setProgress(progress);
                    getListener(seekBar).onStopTrackingTouch(seekBar);
                    this.notify();
                }
            }
        };
        synchronized (seekAction) {
            MossRockActivity.getInstance().runOnUiThread(seekAction);
            try {
                seekAction.wait();
            } catch (InterruptedException e) {
            }
        }
    }
    default void toggleBackround(final CompoundButton btn, final boolean checked) {
        MossRockActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MossRockActivity.getInstance().toggleBackround(btn, checked);
            }
        });
    }
    default void noEventSetChecked(final ToggleButton btn, boolean checked) {
        MossRockActivity.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                CompoundButton.OnCheckedChangeListener listener = getListener(btn);
                btn.setOnCheckedChangeListener(null);
                btn.setChecked(checked);
                MossRockActivity.getInstance().toggleBackround(btn, checked);
                btn.setOnCheckedChangeListener(listener);
            }
        });
    }
    public static CompoundButton.OnCheckedChangeListener getListener(CompoundButton button) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        try {
            listenerInfoField = Class.forName("android.widget.CompoundButton").getDeclaredField("mOnCheckedChangeListener");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
            }
            return (CompoundButton.OnCheckedChangeListener) listenerInfoField.get(button);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static SeekBar.OnSeekBarChangeListener getListener(SeekBar seekBar) {
        // get the nested class `android.view.View$ListenerInfo`
        Field listenerInfoField = null;
        try {
            listenerInfoField = Class.forName("android.widget.SeekBar").getDeclaredField("mOnSeekBarChangeListener");
            if (listenerInfoField != null) {
                listenerInfoField.setAccessible(true);
            }
            return (SeekBar.OnSeekBarChangeListener) listenerInfoField.get(seekBar);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
    final SeekBar.OnSeekBarChangeListener volumeListener = new SeekBar.OnSeekBarChangeListener() {
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

}
