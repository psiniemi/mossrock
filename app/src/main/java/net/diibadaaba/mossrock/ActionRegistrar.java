package net.diibadaaba.mossrock;

import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.lang.reflect.Field;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public interface ActionRegistrar {
    void registerActions(MossRockActivity activity);
    final BlockingQueue<MRMessage> messageQueue = new LinkedBlockingDeque<>(10);
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
    static CompoundButton.OnCheckedChangeListener getListener(CompoundButton button) {
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
    static SeekBar.OnSeekBarChangeListener getListener(SeekBar seekBar) {
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
}
