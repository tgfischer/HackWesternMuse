package com.fischer.tom.hackwesternmuse;

import android.app.Activity;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.interaxon.libmuse.ConnectionState;
import com.interaxon.libmuse.Eeg;
import com.interaxon.libmuse.Muse;
import com.interaxon.libmuse.MuseArtifactPacket;
import com.interaxon.libmuse.MuseConnectionListener;
import com.interaxon.libmuse.MuseConnectionPacket;
import com.interaxon.libmuse.MuseDataListener;
import com.interaxon.libmuse.MuseDataPacket;
import com.interaxon.libmuse.MuseDataPacketType;
import com.interaxon.libmuse.MuseManager;
import com.interaxon.libmuse.MusePreset;
import com.interaxon.libmuse.MuseVersion;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * Created by Tom on 2015-03-28.
 */
public class ConnectionsFragment extends Fragment implements View.OnClickListener {
    View rootView;

    class ConnectionListener extends MuseConnectionListener {

        final WeakReference<Activity> activityRef;

        ConnectionListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseConnectionPacket(MuseConnectionPacket p) {
            final ConnectionState current = p.getCurrentConnectionState();
            final String status = p.getPreviousConnectionState().toString() + " -> " + current;
            final String full = "Muse " + p.getSource().getMacAddress() + " " + status;

            Log.i("Muse Headband", full);
            Activity activity = activityRef.get();
            // UI thread is used here only because we need to update
            // TextView values. You don't have to use another thread, unless
            // you want to run disconnect() or connect() from connection packet
            // handler. In this case creating another thread is required.
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView statusText = (TextView) findViewById(R.id.con_status);
                        //statusText.setText(status);
                        //TextView museVersionText = (TextView) findViewById(R.id.version);
                        if (current == ConnectionState.CONNECTED) {
                            MuseVersion museVersion = muse.getMuseVersion();
                            String version = museVersion.getFirmwareType() +
                                    " - " + museVersion.getFirmwareVersion() +
                                    " - " + Integer.toString(
                                    museVersion.getProtocolVersion());
                            //museVersionText.setText(version);
                        } else {
                            //museVersionText.setText(R.string.undefined);
                        }
                    }
                });
            }
        }
    }

    /**
     * Data listener will be registered to listen for: Accelerometer,
     * Eeg and Relative Alpha bandpower packets. In all cases we will
     * update UI with new values.
     * We also will log message if Artifact packets contains "blink" flag.
     * DataListener methods will be called from execution thread. If you are
     * implementing "serious" processing algorithms inside those listeners,
     * consider to create another thread.
     */
    class DataListener extends MuseDataListener {

        final WeakReference<Activity> activityRef;

        DataListener(final WeakReference<Activity> activityRef) {
            this.activityRef = activityRef;
        }

        @Override
        public void receiveMuseDataPacket(MuseDataPacket p) {
            switch (p.getPacketType()) {
                case EEG:
                    updateEeg(p.getValues());
                    break;
                case ACCELEROMETER:
                    updateAccelerometer(p.getValues());
                    break;
                case ALPHA_RELATIVE:
                    updateAlphaRelative(p.getValues());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void receiveMuseArtifactPacket(MuseArtifactPacket p) {
            if (p.getHeadbandOn() && p.getBlink()) {
                Log.i("Artifacts", "blink");
            }
        }

        private void updateAccelerometer(final ArrayList<Double> data) {
            /*Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView acc_x = (TextView) findViewById(R.id.acc_x);
                        TextView acc_y = (TextView) findViewById(R.id.acc_y);
                        TextView acc_z = (TextView) findViewById(R.id.acc_z);
                        acc_x.setText(String.format(
                            "%6.2f", data.get(Accelerometer.FORWARD_BACKWARD.ordinal())));
                        acc_y.setText(String.format(
                            "%6.2f", data.get(Accelerometer.UP_DOWN.ordinal())));
                        acc_z.setText(String.format(
                            "%6.2f", data.get(Accelerometer.LEFT_RIGHT.ordinal())));
                    }
                });
            }*/
        }

        private void updateEeg(final ArrayList<Double> data) {
            getView().post(new Runnable() {
                public void run() {
                    //if (!Thread.currentThread().isInterrupted()) {
                    try {
                        if (Thread.currentThread().isInterrupted()) {
                            throw new InterruptedException();
                        }

                        TextView tp9 = (TextView) getView().findViewById(R.id.eeg_tp9);
                        TextView fp1 = (TextView) getView().findViewById(R.id.eeg_fp1);
                        TextView fp2 = (TextView) getView().findViewById(R.id.eeg_fp2);
                        TextView tp10 = (TextView) getView().findViewById(R.id.eeg_tp10);
                        tp9.setText(String.format(
                                "%6.2f", data.get(Eeg.TP9.ordinal())));
                        fp1.setText(String.format(
                                "%6.2f", data.get(Eeg.FP1.ordinal())));
                        fp2.setText(String.format(
                                "%6.2f", data.get(Eeg.FP2.ordinal())));
                        tp10.setText(String.format(
                                "%6.2f", data.get(Eeg.TP10.ordinal())));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    //}
                }
            });
        }

        private void updateAlphaRelative(final ArrayList<Double> data) {
            /*Activity activity = activityRef.get();
            if (activity != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                         TextView elem1 = (TextView) findViewById(R.id.elem1);
                         TextView elem2 = (TextView) findViewById(R.id.elem2);
                         TextView elem3 = (TextView) findViewById(R.id.elem3);
                         TextView elem4 = (TextView) findViewById(R.id.elem4);
                         elem1.setText(String.format(
                            "%6.2f", data.get(Eeg.TP9.ordinal())));
                         elem2.setText(String.format(
                            "%6.2f", data.get(Eeg.FP1.ordinal())));
                         elem3.setText(String.format(
                            "%6.2f", data.get(Eeg.FP2.ordinal())));
                         elem4.setText(String.format(
                            "%6.2f", data.get(Eeg.TP10.ordinal())));
                    }
                });
            }*/
        }
    }

    private Muse muse = null;
    private ConnectionListener connectionListener = null;
    private DataListener dataListener = null;
    private boolean dataTransmission = true;

    public ConnectionsFragment() {
        // Create listeners and pass reference to activity to them
        WeakReference<Activity> weakActivity = new WeakReference<Activity>(this.getActivity());
        connectionListener = new ConnectionListener(weakActivity);
        dataListener = new DataListener(weakActivity);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.connections_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        ImageButton refreshButton = (ImageButton) getView().findViewById(R.id.refresh);
        refreshButton.setOnClickListener(this);
        Button connectButton = (Button) getView().findViewById(R.id.connect);
        connectButton.setOnClickListener(this);
        Button disconnectButton = (Button) getView().findViewById(R.id.disconnect);
        disconnectButton.setOnClickListener(this);
        Button pauseButton = (Button) getView().findViewById(R.id.pause);
        pauseButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Spinner musesSpinner = (Spinner)getView().findViewById(R.id.muses_spinner);
        if (v.getId() == R.id.refresh) {
            MuseManager.refreshPairedMuses();
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            List<String> spinnerItems = new ArrayList<String>();
            for (Muse m: pairedMuses) {
                String dev_id = m.getName() + "-" + m.getMacAddress();
                Log.i("Muse Headband", dev_id);
                spinnerItems.add(dev_id);
            }
            ArrayAdapter<String> adapterArray = new ArrayAdapter<String> (this.getActivity(), android.R.layout.simple_spinner_item, spinnerItems);
            musesSpinner.setAdapter(adapterArray);
        }
        else if (v.getId() == R.id.connect) {
            List<Muse> pairedMuses = MuseManager.getPairedMuses();
            if (pairedMuses.size() < 1 ||
                    musesSpinner.getAdapter().getCount() < 1) {
                Log.w("Muse Headband", "There is nothing to connect to");
            }
            else {
                muse = pairedMuses.get(musesSpinner.getSelectedItemPosition());
                ConnectionState state = muse.getConnectionState();
                if (state == ConnectionState.CONNECTED ||
                        state == ConnectionState.CONNECTING) {
                    Log.w("Muse Headband", "doesn't make sense to connect second time to the same muse");
                    return;
                }

                LinearLayout statusBar = (LinearLayout)getView().findViewById(R.id.statusBar);
                statusBar.setBackgroundColor(Color.parseColor("#4CAF50"));
                TextView statusLabel = (TextView)getView().findViewById(R.id.statusLabel);
                statusLabel.setText("CONNECTED");

                configure_library();
                /**
                 * In most cases libmuse native library takes care about
                 * exceptions and recovery mechanism, but native code still
                 * may throw in some unexpected situations (like bad bluetooth
                 * connection). Print all exceptions here.
                 */
                try {
                    muse.runAsynchronously();
                } catch (Exception e) {
                    Log.e("Muse Headband", e.toString());
                }
            }
        }
        else if (v.getId() == R.id.disconnect) {
            if (muse != null) {
                /**
                 * true flag will force libmuse to unregister all listeners,
                 * BUT AFTER disconnecting and sending disconnection event.
                 * If you don't want to receive disconnection event (for ex.
                 * you call disconnect when application is closed), then
                 * unregister listeners first and then call disconnect:
                 * muse.unregisterAllListeners();
                 * muse.disconnect(false);
                 */
                LinearLayout statusBar = (LinearLayout)getView().findViewById(R.id.statusBar);
                statusBar.setBackgroundColor(Color.parseColor("#F44336"));
                TextView statusLabel = (TextView)getView().findViewById(R.id.statusLabel);
                statusLabel.setText("DISCONNECTED");

                muse.disconnect(true);
            }
        }
        else if (v.getId() == R.id.pause) {
            dataTransmission = !dataTransmission;
            if (muse != null) {
                muse.enableDataTransmission(dataTransmission);
            }
        }
    }

    private void configure_library() {
        muse.registerConnectionListener(connectionListener);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ACCELEROMETER);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.EEG);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ALPHA_RELATIVE);
        muse.registerDataListener(dataListener,
                MuseDataPacketType.ARTIFACTS);
        muse.setPreset(MusePreset.PRESET_14);
        muse.enableDataTransmission(dataTransmission);
    }
}
