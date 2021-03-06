package net.diibadaaba.mossrock;

import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST;
import static fi.iki.elonen.NanoHTTPD.Response.Status.INTERNAL_ERROR;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "MossRockServer";
    private static final String CONTENT_TYPE = "application/json";
    private static final String RESPONSE_WRONG_METHOD = "{\"status\":\"error\",\"reason\":\"Invalid HTTP method\"}";
    private static final String RESPONSE_BAD_REQUEST = "{\"status\":\"error\",\"reason\":\"Invalid request\"}";
    private static final String RESPONSE_ERROR = "{\"status\":\"error\",\"reason\":\"Internal error\"}";
    private static final Map<String, Integer> VOLUMES = new HashMap<>();
    static {
        VOLUMES.put("down5", 3);
        VOLUMES.put("down10", 2);
        VOLUMES.put("down20", 1);
        VOLUMES.put("down30", 0);
        VOLUMES.put("up5", 5);
        VOLUMES.put("up10", 6);
        VOLUMES.put("up20", 7);
        VOLUMES.put("up30", 8);
    }
    private static final Set<String> actions = new HashSet<>();
    public HttpServer() throws IOException {
        super(8088);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
    }
    @Override
    public Response serve(IHTTPSession session) {
        if (session.getMethod() != Method.GET) {
            return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_WRONG_METHOD);
        }
        /*
         *   /on/nuutti
         *   /off/nuutti
         *   /dim/nuutti?5
         *   /scene/wii
         *   /volume/up5
         *   /status
         */
        final String[] command = session.getUri().split("/");
        Log.i(TAG, Arrays.deepToString(command));
        if (!(command.length == 3 || (command.length == 2 && "status".equals(command[1])))) {
            return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
        }
        switch (command[1]) {
            case "on":
            case "off":
                if (!buttons().containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                ToggleButton button = buttons().get(command[2]);
                boolean checked = "on".equals(command[1]);
                MossRockActivity.getInstance().registrar.setChecked(button, checked);
                break;
            case "dim":
                if (!seekBars().containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                int value = -1;
                try {
                    value = Integer.parseInt(session.getQueryParameterString());
                } catch (Exception e) {}
                if (value < 0 || value > 15) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                SeekBar target = seekBars().get(command[2]);
                target.setProgress(value);
                MossRockActivity.getInstance()
                        .registrar.setProgress(target, value);
                break;
            case "scene":
                if (!scenes().containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                final Button scene = scenes().get(command[2]);
                if (scene instanceof ToggleButton) {
                    MossRockActivity.getInstance().registrar.setChecked((CompoundButton) scene, true);
                } else {
                    MossRockActivity.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scene.callOnClick();
                        }
                    });
                }
                break;
            case "status":
                break;
            case "volume":
                if (!VOLUMES.containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                SeekBar volume = MossRockActivity.getInstance().volume;
                int progress = VOLUMES.get(command[2]);
                volume.setProgress(progress);
                MossRockActivity.getInstance().registrar.setProgress(volume, progress);
                break;
            default:
                return newFixedLengthResponse(INTERNAL_ERROR, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
        }
        try {
            JSONObject response = new JSONObject().put("status", "ok");
            for (Map.Entry<String, ToggleButton> next : MossRockActivity.getInstance().buttons.entrySet()) {
                boolean checked = ((ToggleButton)MossRockActivity.getInstance().findViewById(next.getValue().getId())).isChecked();
                response.put(next.getKey(), checked);
            }
            for (Map.Entry<String, Button> next : MossRockActivity.getInstance().scenes.entrySet()) {
                if (next.getValue() instanceof ToggleButton) {
                    boolean checked = ((ToggleButton) MossRockActivity.getInstance().findViewById(next.getValue().getId())).isChecked();
                    response.put(next.getKey(), checked);
                }
            }
            return newFixedLengthResponse(OK, CONTENT_TYPE, response.toString());
        } catch (JSONException e) {
            return newFixedLengthResponse(INTERNAL_ERROR, CONTENT_TYPE, RESPONSE_ERROR);
        }
    }
    private Map<String, ToggleButton> buttons() {
        return MossRockActivity.getInstance().buttons;
    }
    private Map<String, SeekBar> seekBars() {
        return MossRockActivity.getInstance().seekBars;
    }
    private Map<String, Button> scenes() {
        return MossRockActivity.getInstance().scenes;
    }
}
