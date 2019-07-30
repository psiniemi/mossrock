package net.diibadaaba.mossrock;

import android.util.Log;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;

import static fi.iki.elonen.NanoHTTPD.Response.Status.BAD_REQUEST;
import static fi.iki.elonen.NanoHTTPD.Response.Status.OK;

public class HttpServer extends NanoHTTPD {
    private static final String TAG = "MossRockServer";
    private static final String CONTENT_TYPE = "application/json";
    private static final String RESPONSE_OK = "{\"status\":\"ok\"}";
    private static final String RESPONSE_WRONG_METHOD = "{\"status\":\"error\",\"reason\":\"Invalid HTTP method\"}";
    private static final String RESPONSE_BAD_REQUEST = "{\"status\":\"error\",\"reason\":\"Invalid request\"}";
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
         */
        final String[] command = session.getUri().split("/");
        Log.i(TAG, Arrays.deepToString(command));
        if (command.length != 3) {
            return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
        }
        switch (command[1]) {
            case "on":
            case "off":
                if (!buttons().containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                MossRockActivity.getInstance()
                        .registrar.getCheckedChangeListener()
                        .onCheckedChanged(buttons().get(command[2]), "on".equals(command[1]));
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
                        .registrar.getSeekListener()
                        .onStopTrackingTouch(target);
                break;
            case "scene":
                if (!scenes().containsKey(command[2])) {
                    return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
                }
                MossRockActivity.getInstance().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scenes().get(command[2]).callOnClick();
                    }
                });
                break;
            default:
                return newFixedLengthResponse(BAD_REQUEST, CONTENT_TYPE, RESPONSE_BAD_REQUEST);
        }
        return newFixedLengthResponse(OK, CONTENT_TYPE, RESPONSE_OK);
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
