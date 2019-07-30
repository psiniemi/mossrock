package net.diibadaaba.mossrock;

import android.widget.CompoundButton;
import android.widget.SeekBar;

public interface ActionRegistrar {
    void registerActions(MossRockActivity activity);
    SeekBar.OnSeekBarChangeListener getSeekListener();
    CompoundButton.OnCheckedChangeListener getCheckedChangeListener();
}
