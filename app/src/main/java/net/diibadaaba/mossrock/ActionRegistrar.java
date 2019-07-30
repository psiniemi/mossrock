package net.diibadaaba.mossrock;

import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

public interface ActionRegistrar {
    void registerActions(MossRockActivity activity);
    SeekBar.OnSeekBarChangeListener getSeekListener();
    CompoundButton.OnCheckedChangeListener getCheckedChangeListener();
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
                    getSeekListener().onStopTrackingTouch(seekBar);
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
                btn.setOnCheckedChangeListener(null);
                btn.setChecked(checked);
                MossRockActivity.getInstance().toggleBackround(btn, checked);
                btn.setOnCheckedChangeListener(getCheckedChangeListener());
            }
        });
    }

}
