package com.android.inputmethod.latin;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.inputmethod.libs.mozc.session.MozcJNI;

import org.mozc.android.inputmethod.japanese.protobuf.ProtoCandidateWindow;
import org.mozc.android.inputmethod.japanese.protobuf.ProtoCommands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class MozcEngine {

    private static final String TAG = "mozcDebug";

    private SharedPreferences prefs;
    private Resources res;
    private long sessionId;
    private ProtoCommands.Output lastOutput;
    private static final MozcEngine sInstance = new MozcEngine();

    public static MozcEngine getInstance() {
        sInstance.checkInitialized();
        return sInstance;
    }

    public static void init(final Context context) throws IOException {
        sInstance.initInternal(context);
    }

    private boolean isInitialized() {
        return prefs != null;
    }

    private void checkInitialized() {
        if (!isInitialized()) {
            throw new RuntimeException(MozcEngine.class.getSimpleName() + " is used before initialization");
        }
    }

    private void initInternal(Context ctx) throws IOException {
        if (!MozcJNI.initialize()) {
            throw new IllegalStateException("Failed to initialize mozc JNI");
        }

        // Copy mozc.data from assets
        File outFile = new File(ctx.getFilesDir(), "mozc.data");
        try (InputStream input = ctx.getAssets().open("mozc.data");
             FileOutputStream output = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[8192];
            int length;
            while ((length = input.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }
        }

        Log.d(TAG, "mozc.data exists: " + outFile.exists());
        Log.d(TAG, "mozc.data length = " + outFile.length() + " bytes");

        String profileDir = ctx.getFilesDir().getAbsolutePath();
        String dataFilePath = outFile.getAbsolutePath();

        boolean ok = MozcJNI.onPostLoad(profileDir, dataFilePath);
        if (!ok) {
            throw new IllegalStateException("mozc engine init failed");
        }

        Log.d(TAG, MozcJNI.getDataVersion());

        ProtoCommands.Command createCommand =
                ProtoCommands.Command.newBuilder()
                        .setInput(
                                ProtoCommands.Input.newBuilder()
                                        .setType(ProtoCommands.Input.CommandType.CREATE_SESSION)
                        )
                        .build();

        ProtoCommands.Command createResponse =
                ProtoCommands.Command.parseFrom(
                        MozcJNI.evalCommand(createCommand.toByteArray())
                );

        this.sessionId = createResponse.getOutput().getId();

        ProtoCommands.Request.Builder builder = ProtoCommands.Request.newBuilder()
                .setKeyboardName("QWERTY_KANA" + '-' + 0 + '.' + 4 + '.' + 0 + '-' + getDeviceOrientationString(ctx.getResources().getConfiguration()))
                .setSpecialRomanjiTable(ProtoCommands.Request.SpecialRomanjiTable.QWERTY_MOBILE_TO_HIRAGANA)
                .setSpaceOnAlphanumeric(ProtoCommands.Request.SpaceOnAlphanumeric.SPACE_OR_CONVERT_KEEPING_COMPOSITION)
                .setKanaModifierInsensitiveConversion(false)
                .setCrossingEdgeBehavior(ProtoCommands.Request.CrossingEdgeBehavior.DO_NOTHING)
                .setMixedConversion(true)
                .setZeroQuerySuggestion(true)
                .setUpdateInputModeFromSurroundingText(false)
                .setAutoPartialSuggestion(true);

        ProtoCommands.Input.Builder inputBuilder = ProtoCommands.Input.newBuilder()
                .setRequest(builder)
                .addAllTouchEvents(Collections.emptyList());
        ProtoCommands.Input input = inputBuilder
                .setId(sessionId)
                .setType(ProtoCommands.Input.CommandType.SET_REQUEST)
                .setRequest(inputBuilder.getRequest())
                .build();
        ProtoCommands.Command inCommand = ProtoCommands.Command.newBuilder()
                .setInput(input)
                .build();
        MozcJNI.evalCommand(inCommand.toByteArray());
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        res = ctx.getResources();
    }

    public void setCompositionMode(ProtoCommands.CompositionMode mode) {
        ProtoCommands.SessionCommand modeCommand = ProtoCommands.SessionCommand.newBuilder()
                .setType(ProtoCommands.SessionCommand.CommandType.SWITCH_COMPOSITION_MODE)
                .setCompositionMode(mode)
                .build();

        ProtoCommands.Command modeRequest = ProtoCommands.Command.newBuilder()
                .setInput(
                        ProtoCommands.Input.newBuilder()
                                .setType(ProtoCommands.Input.CommandType.SEND_COMMAND)
                                .setId(sessionId)
                                .setCommand(modeCommand)
                )
                .build();

        MozcJNI.evalCommand(modeRequest.toByteArray());
        if (mode == ProtoCommands.CompositionMode.HIRAGANA) {
            ProtoCommands.Request.Builder builder = ProtoCommands.Request.newBuilder()
                    .setKeyboardName("QWERTY_KANA" + '-' + 0 + '.' + 4 + '.' + 0 + '-' + getDeviceOrientationString(res.getConfiguration()))
                    .setSpecialRomanjiTable(ProtoCommands.Request.SpecialRomanjiTable.QWERTY_MOBILE_TO_HIRAGANA)
                    .setSpaceOnAlphanumeric(ProtoCommands.Request.SpaceOnAlphanumeric.SPACE_OR_CONVERT_KEEPING_COMPOSITION)
                    .setKanaModifierInsensitiveConversion(false)
                    .setCrossingEdgeBehavior(ProtoCommands.Request.CrossingEdgeBehavior.DO_NOTHING)
                    .setMixedConversion(true)
                    .setZeroQuerySuggestion(true)
                    .setUpdateInputModeFromSurroundingText(false)
                    .setAutoPartialSuggestion(true);

            ProtoCommands.Input.Builder inputBuilder = ProtoCommands.Input.newBuilder()
                    .setRequest(builder)
                    .addAllTouchEvents(Collections.emptyList());
            ProtoCommands.Input input = inputBuilder
                    .setId(sessionId)
                    .setType(ProtoCommands.Input.CommandType.SET_REQUEST)
                    .setRequest(inputBuilder.getRequest())
                    .build();
            ProtoCommands.Command inCommand = ProtoCommands.Command.newBuilder()
                    .setInput(input)
                    .build();
            MozcJNI.evalCommand(inCommand.toByteArray());
        } else {
            ProtoCommands.Request.Builder builder = ProtoCommands.Request.newBuilder()
                    .setKeyboardName("QWERTY_ALPHABET" + '-' + 0 + '.' + 5 + '.' + 0 + '-' + getDeviceOrientationString(res.getConfiguration()))
                    .setSpecialRomanjiTable(ProtoCommands.Request.SpecialRomanjiTable.QWERTY_MOBILE_TO_HALFWIDTHASCII)
                    .setSpaceOnAlphanumeric(ProtoCommands.Request.SpaceOnAlphanumeric.COMMIT)
                    .setKanaModifierInsensitiveConversion(false)
                    .setCrossingEdgeBehavior(ProtoCommands.Request.CrossingEdgeBehavior.DO_NOTHING)
                    .setMixedConversion(true)
                    .setZeroQuerySuggestion(true)
                    .setUpdateInputModeFromSurroundingText(false)
                    .setAutoPartialSuggestion(true);

            ProtoCommands.Input.Builder inputBuilder = ProtoCommands.Input.newBuilder()
                    .setRequest(builder)
                    .addAllTouchEvents(Collections.emptyList());
            ProtoCommands.Input input = inputBuilder
                    .setId(sessionId)
                    .setType(ProtoCommands.Input.CommandType.SET_REQUEST)
                    .setRequest(inputBuilder.getRequest())
                    .build();
            ProtoCommands.Command inCommand = ProtoCommands.Command.newBuilder()
                    .setInput(input)
                    .build();
            MozcJNI.evalCommand(inCommand.toByteArray());
        }
        prefs.edit().putInt("libmozc_enabled", mode.getNumber()).apply();
    }

    public ProtoCommands.CompositionMode getCompositionMode() {
        ProtoCommands.CompositionMode fallback = ProtoCommands.CompositionMode.forNumber(prefs.getInt("libmozc_enabled", ProtoCommands.CompositionMode.HIRAGANA.getNumber()));
        try {
            ProtoCommands.SessionCommand modeCommand = ProtoCommands.SessionCommand.newBuilder()
                    .setType(ProtoCommands.SessionCommand.CommandType.GET_STATUS)
                    .build();
            ProtoCommands.Command keyRequest = ProtoCommands.Command.newBuilder()
                    .setInput(
                            ProtoCommands.Input.newBuilder()
                                    .setType(ProtoCommands.Input.CommandType.SEND_COMMAND)
                                    .setId(sessionId)
                                    .setCommand(modeCommand)
                    )
                    .build();

            byte[] bytes = MozcJNI.evalCommand(keyRequest.toByteArray());
            if (bytes == null || bytes.length == 0) {
                Log.e(TAG, "mozc returned empty response");
                return fallback;
            }

            ProtoCommands.Command response = ProtoCommands.Command.parseFrom(bytes);
            ProtoCommands.Output output = response.hasOutput() ? response.getOutput() : null;
            if (output == null) return fallback;
            if (output.hasMode()) {
                Log.d(TAG, String.valueOf(output.getMode().getNumber()));
                return output.getMode();
            } else {
                return fallback;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return fallback;
        }
    }

    public void deleteSession() {
        if (sessionId == 0) return;

        try {
            ProtoCommands.Command deleteRequest =
                    ProtoCommands.Command.newBuilder()
                            .setInput(
                                    ProtoCommands.Input.newBuilder()
                                            .setType(ProtoCommands.Input.CommandType.DELETE_SESSION)
                                            .setId(sessionId)
                            )
                            .build();

            MozcJNI.evalCommand(deleteRequest.toByteArray());

        } catch (Exception e) {
            Log.e(TAG, "Failed to delete mozc session", e);
        }

        sessionId = 0;
        lastOutput = null;
    }

    public void sendMozcKey(char ch) {
        try {
            Log.d(TAG, "keycode: " + ch + " as int: " + (int) ch);
            ProtoCommands.KeyEvent keyEvent = ProtoCommands.KeyEvent.newBuilder()
                    .setKeyCode(ch)
                    .setActivated(true)
                    .build();

            ProtoCommands.Command keyRequest = ProtoCommands.Command.newBuilder()
                    .setInput(
                            ProtoCommands.Input.newBuilder()
                                    .setType(ProtoCommands.Input.CommandType.SEND_KEY)
                                    .setId(sessionId)
                                    .setKey(keyEvent)
                    )
                    .build();

            byte[] bytes = MozcJNI.evalCommand(keyRequest.toByteArray());
            if (bytes == null || bytes.length == 0) {
                Log.e(TAG, "mozc returned empty response");
                return;
            }

            ProtoCommands.Command response = ProtoCommands.Command.parseFrom(bytes);
            ProtoCommands.Output output = response.hasOutput() ? response.getOutput() : null;
            if (output == null) return;
            lastOutput = output;

            // Read preedit
            String preedit = "";
            if (output.hasPreedit()) {
                preedit = output.getPreedit().getSegmentList()
                        .stream()
                        .map(seg -> seg.getValue())
                        .reduce("", String::concat);
            }

            // Read candidates (optional)
            String candidates = "";
            if (output.hasCandidateWindow()) {
                StringBuilder sb = new StringBuilder();
                int idx = 1;
                for (ProtoCandidateWindow.CandidateWindow.Candidate cand : output.getCandidateWindow().getCandidateList()) {
                    sb.append(idx++).append(". ").append(cand.getValue()).append("\n");
                }
                candidates = sb.toString();
            }

            // Read committed text
            String committed = output.hasResult() ? output.getResult().getValue() : "";

            Log.d(TAG, "Preedit: " + preedit);
            Log.d(TAG, "Committed: " + committed);
            Log.d(TAG, "Candidates:\n" + candidates);
            Log.d(TAG, "ComositionMode: " + lastOutput.getStatus().getMode());
        } catch (Exception e) {
            Log.e(TAG, "Failed to send key to mozc: " + ch, e);
        }
    }

    public void sendMozcKey(ProtoCommands.KeyEvent.SpecialKey code) {
        try {
            ProtoCommands.KeyEvent keyEvent = ProtoCommands.KeyEvent.newBuilder()
                    .setSpecialKey(code)
                    .setActivated(true)
                    .build();

            ProtoCommands.Command keyRequest = ProtoCommands.Command.newBuilder()
                    .setInput(
                            ProtoCommands.Input.newBuilder()
                                    .setType(ProtoCommands.Input.CommandType.SEND_KEY)
                                    .setId(sessionId)
                                    .setKey(keyEvent)
                    )
                    .build();

            byte[] bytes = MozcJNI.evalCommand(keyRequest.toByteArray());
            if (bytes == null || bytes.length == 0) {
                Log.e(TAG, "mozc returned empty response");
                return;
            }

            ProtoCommands.Command response = ProtoCommands.Command.parseFrom(bytes);
            ProtoCommands.Output output = response.hasOutput() ? response.getOutput() : null;
            if (output == null) return;
            lastOutput = output;

            // Read preedit
            String preedit = "";
            if (output.hasPreedit()) {
                preedit = output.getPreedit().getSegmentList()
                        .stream()
                        .map(seg -> seg.getValue())
                        .reduce("", String::concat);
            }

            // Read candidates (optional)
            String candidates = "";
            if (output.hasCandidateWindow()) {
                StringBuilder sb = new StringBuilder();
                int idx = 1;
                for (ProtoCandidateWindow.CandidateWindow.Candidate cand : output.getCandidateWindow().getCandidateList()) {
                    sb.append(idx++).append(". ").append(cand.getValue()).append("\n");
                }
                candidates = sb.toString();
            }

            // Read committed text
            String committed = output.hasResult() ? output.getResult().getValue() : "";

            Log.d(TAG, "Preedit: " + preedit);
            Log.d(TAG, "Committed: " + committed);
            Log.d(TAG, "Candidates:\n" + candidates);
        } catch (Exception e) {
            Log.e(TAG, "Failed to send special key to mozc: " + code, e);
        }
    }

    public List<ProtoCandidateWindow.CandidateWindow.Candidate> getCandidates() {
        ProtoCommands.Output output = lastOutput;
        if (output == null) return Collections.emptyList();
        if (output.hasCandidateWindow()) {
            return output.getCandidateWindow().getCandidateList();
        } else {
            return Collections.emptyList();
        }
    }

    public String getPreedit() {
        ProtoCommands.Output output = lastOutput;
        if (output == null) return "";
        if (output.hasPreedit()) {
            return output.getPreedit().getSegmentList()
                    .stream()
                    .map(seg -> seg.getValue())
                    .reduce("", String::concat);
        } else {
            return "";
        }
    }

    public void resetSession() throws IOException {
        deleteSession();
        ProtoCommands.Command createCommand =
                ProtoCommands.Command.newBuilder()
                        .setInput(
                                ProtoCommands.Input.newBuilder()
                                        .setType(ProtoCommands.Input.CommandType.CREATE_SESSION)
                        )
                        .build();

        ProtoCommands.Command createResponse =
                ProtoCommands.Command.parseFrom(
                        MozcJNI.evalCommand(createCommand.toByteArray())
                );

        this.sessionId = createResponse.getOutput().getId();

        setCompositionMode(ProtoCommands.CompositionMode.forNumber(prefs.getInt("libmozc_enabled", ProtoCommands.CompositionMode.HIRAGANA.getNumber())));
    }
    public static String getDeviceOrientationString(Configuration configuration) {
        switch (configuration.orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                return "PORTRAIT";
            case Configuration.ORIENTATION_LANDSCAPE:
                return "LANDSCAPE";
            case Configuration.ORIENTATION_SQUARE:
                return "SQUARE";
            case Configuration.ORIENTATION_UNDEFINED:
                return "UNDEFINED";
        }
        // If none of above is matched to the orientation, we return "UNKNOWN".
        return "UNKNOWN";
    }

}