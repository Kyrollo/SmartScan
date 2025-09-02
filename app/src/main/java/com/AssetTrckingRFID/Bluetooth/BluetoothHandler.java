package com.AssetTrckingRFID.Bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.ScanItems.ScanItems;
import com.zebra.rfid.api3.*;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;

public class BluetoothHandler implements IDcsSdkApiDelegate, Readers.RFIDReaderEventHandler {
    final static String TAG = "RFID_SAMPLE";
    private Readers readers;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice readerDevice;
    private RFIDReader reader;
    private EventHandler eventHandler;
    private volatile Context context;
    private SDKHandler sdkHandler;
    private ArrayList<DCSScannerInfo> scannerList;
    private int scannerID;
    static MyAsyncTask cmdExecTask = null;
    private int MAX_POWER = 300;
    String readerName = "RFD4031-G10B700-US";

    public synchronized void onCreate(Context context) {
        reader = App.get().getRfidReader();
        updateContext(context);
        scannerList = new ArrayList<>();
        checkRFIDConnectionStatus();
    }

    public void checkRFIDConnectionStatus() {
        if (reader != null && reader.isConnected()) {
            handleRFIDEvent("connected");
        } else {
            handleRFIDEvent("connecting");
            InitSDK();
        }
    }

    private void handleRFIDEvent(String eventType) {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            switch (eventType) {
                case "connected":
                    activity.sendToast(ctx.getString(R.string.rfid_connected));
                    activity.updateRFIDStatus(ctx.getString(R.string.rfid_is_already_connected) + (reader != null ? reader.getHostName() : ""));
                    break;
                case "connecting":
                    activity.sendToast(ctx.getString(R.string.connecting_to_rfid_reader));
                    activity.updateRFIDStatus(ctx.getString(R.string.wait_for_connection));
                    break;
            }
        }
    }

    private void InitSDK() {
        Log.d(TAG, "InitSDK");

        handleProgressBar(true);

        if (readers == null) {
            handleConnectionStatus("not_connected");
            new CreateInstanceTask().execute();
        } else {
            ConfigureReader();
            connectReader();
            handleConnectionStatus("already_connected");
        }
    }

    private void handleProgressBar(boolean show) {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            if (show) {
                activity.showProgressBar();
            } else {
                activity.hideProgressBar();
            }
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            if (show) {
                activity.showProgressBar();
            } else {
                activity.hideProgressBar();
            }
        }
    }

    private void handleConnectionStatus(String status) {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            switch (status) {
                case "not_connected":
                    activity.sendToast(ctx.getString(R.string.rfid_not_connected));
                    activity.updateRFIDStatus(ctx.getString(R.string.rfid_not_connected));
                    break;
                case "already_connected":
                    activity.sendToast(ctx.getString(R.string.rfid_connected));
                    String statusMsg = reader != null && reader.getHostName() != null ?
                                     ctx.getString(R.string.rfid_is_already_connected) + reader.getHostName() :
                                     ctx.getString(R.string.rfid_connected);
                    activity.updateRFIDStatus(statusMsg);
                    break;
            }
        }
    }

    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "CreateInstanceTask");
            InvalidUsageException invalidUsageException = null;
            readers = new Readers(getCurrentContext(), ENUM_TRANSPORT.SERVICE_USB);
            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
            } catch (InvalidUsageException e) {
                invalidUsageException = e;
                e.printStackTrace();
            }
            if (invalidUsageException != null || availableRFIDReaderList.size() == 0) {
                readers.Dispose();
                readers = null;
                readers = new Readers(getCurrentContext(), ENUM_TRANSPORT.BLUETOOTH);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            connectReader();
        }
    }

    private synchronized void connectReader() {
        if (!isReaderConnected()) {
            if (reader == null) {
                new ConnectionTask().execute();
            }
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (reader != null)
                return connect();
            return getCurrentContext().getString(R.string.failed_to_find_or_connect_reader);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Context ctx = getCurrentContext();
            if (ctx instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) ctx).updateRFIDStatus(result);
            }
        }
    }

    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        if (readers != null) {
            readers.attach(this);
            try {
                if (readers.GetAvailableRFIDReaderList() != null) {
                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                    if (availableRFIDReaderList.size() != 0) {
                        if (availableRFIDReaderList.size() == 1) {
                            readerDevice = availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                        } else {
                            for (ReaderDevice device : availableRFIDReaderList) {
                                Log.d(TAG, "device: " + device.getName());
                                if (device.getName().startsWith(readerName)) {
                                    readerDevice = device;
                                    reader = readerDevice.getRFIDReader();
                                }
                            }
                        }
                    }
                }
            } catch (InvalidUsageException ie) {
                ie.printStackTrace();
            }
        }
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderAppeared " + readerDevice.getName());
        handleToast(R.string.rfid_reader_appeared);
        connectReader();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        handleToast(R.string.rfid_reader_disappeared);
        if (readerDevice.getName().equals(reader.getHostName()))
            disconnect();
    }

    private void handleToast(int stringResourceId) {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            ((BluetoothConnectionActivity) ctx).sendToast(ctx.getString(stringResourceId));
        } else if (ctx instanceof ScanItems) {
            ((ScanItems) ctx).sendToast(ctx.getString(stringResourceId));
        }
    }

    private synchronized String connect() {
        if (reader != null) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    reader.connect();
                    ConfigureReader();
                    setupScannerSDK();
                    if (reader.isConnected()) {
                        App.get().setRfidReader(reader);
                        handleConnectionSuccess();
                        return "Connected: " + reader.getHostName();
                    }
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                handleConnectionFailure();
                return "Connection failed: " + e.getVendorMessage() + " " + e.getResults().toString();
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> handleProgressBar(false), 5000);
        return "";
    }

    private void handleConnectionSuccess() {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            activity.sendToast(ctx.getString(R.string.rfid_reader_connected));
            activity.runOnUiThread(() -> activity.hideProgressBar());
            activity.updateRFIDStatus(ctx.getString(R.string.connected_to) + readerDevice.getName());
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            activity.sendToast(ctx.getString(R.string.rfid_reader_connected));
            activity.runOnUiThread(() -> activity.hideProgressBar());
        }
    }

    private void handleConnectionFailure() {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            activity.sendToast(ctx.getString(R.string.rfid_reader_failed));
            activity.runOnUiThread(() -> activity.hideProgressBar());
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            activity.sendToast(ctx.getString(R.string.rfid_reader_failed));
            activity.runOnUiThread(() -> activity.hideProgressBar());
        }
    }

    private void ConfigureReader() {
        if (reader == null) {
            Log.e(TAG, "ConfigureReader: reader is null");
            return;
        }

        Log.d(TAG, "ConfigureReader " + reader.getHostName());
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                if (eventHandler != null) {
                    reader.Events.removeEventsListener(eventHandler);
                }
                eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);

                Context ctx = getCurrentContext();
                reader.Events.setHandheldEvent(true);
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;

                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);

                if (ctx instanceof BluetoothConnectionActivity) {
                    int average_power = MAX_POWER / 2;
                    config.setTransmitPowerIndex(average_power);
                    config.setrfModeTableIndex(0);
                    config.setTari(0);
                } else if (ctx instanceof ScanItems) {
                    int average_power = MAX_POWER / 2;
                    config.setTransmitPowerIndex(average_power);
                    config.setrfModeTableIndex(0);
                    config.setTari(0);
                }

                reader.Config.Antennas.setAntennaRfConfig(1, config);

                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                reader.Actions.PreFilters.deleteAll();

            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public void assignBluetoothConnectionContext(BluetoothConnectionActivity bluetoothConnectionActivity) {
        updateContext(bluetoothConnectionActivity);
    }

    public void assignScanItemsContext(ScanItems scanItemsActivity) {
        updateContext(scanItemsActivity);
    }

    public synchronized void removeContext(Context context) {
        if (this.context == context) {
            Log.d(TAG, "Removing context: " + context.getClass().getSimpleName());
            this.context = null;
        }
    }

    public synchronized void updateContext(Context context) {
        if (context != null) {
            // Only accept ScanItems and BluetoothConnectionActivity
            if (context instanceof ScanItems || context instanceof BluetoothConnectionActivity) {
                Context oldContext = this.context;
                this.context = context;
                Log.d(TAG, "Context updated from: " + (oldContext != null ? oldContext.getClass().getSimpleName() : "null") +
                           " to: " + context.getClass().getSimpleName());

                if (cmdExecTask != null && !cmdExecTask.isCancelled()) {
                    cmdExecTask.cancel(true);
                    cmdExecTask = null;
                }

                // Reconfigure reader with new context
                if (reader != null && reader.isConnected()) {
                    ConfigureReader();
                }
            } else {
                Log.w(TAG, "Unsupported activity type: " + context.getClass().getSimpleName());
            }
        }
    }

    private synchronized Context getCurrentContext() {
        return this.context;
    }

    private boolean isReaderConnected() {
        return reader != null && reader.isConnected();
    }

    public String onResume() {
        return connect();
    }

    public void onDestroy() {
        dispose();
        App.get().setRfidReader(null);
    }

    public void setupScannerSDK() {
        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(getCurrentContext());
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);
            DCSSDKDefs.DCSSDK_RESULT btResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
            DCSSDKDefs.DCSSDK_RESULT btNormalResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

            Log.d(TAG, btNormalResult + " results " + btResult);
            sdkHandler.dcssdkSetDelegate(this);

            int notifications_mask = 0;
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
            sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        }

        if (sdkHandler != null) {
            ArrayList<DCSScannerInfo> availableScanners = (ArrayList<DCSScannerInfo>) sdkHandler.dcssdkGetAvailableScannersList();
            scannerList.clear();
            if (availableScanners != null) {
                scannerList.addAll(availableScanners);
            } else {
                Log.d(TAG, "Available scanners null");
            }
        }

        if (reader != null) {
            for (DCSScannerInfo device : scannerList) {
                if (device.getScannerName().contains(reader.getHostName())) {
                    try {
                        sdkHandler.dcssdkEstablishCommunicationSession(device.getScannerID());
                        scannerID = device.getScannerID();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private synchronized void disconnect() {
        Log.d(TAG, "Disconnect");
        try {
            if (reader != null) {
                if (eventHandler != null)
                    reader.Events.removeEventsListener(eventHandler);
                if (sdkHandler != null) {
                    sdkHandler.dcssdkTerminateCommunicationSession(scannerID);
                    scannerList = null;
                }
                reader.disconnect();

                Context ctx = getCurrentContext();
                if (ctx instanceof BluetoothConnectionActivity) {
                    ((BluetoothConnectionActivity) ctx).sendToast(ctx.getString(R.string.disconnecting_reader));
                    ((BluetoothConnectionActivity) ctx).updateRFIDStatus(ctx.getString(R.string.disconnected));
                } else if (ctx instanceof ScanItems) {
                    ((ScanItems) ctx).sendToast(ctx.getString(R.string.disconnecting_reader));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        disconnect();
        try {
            if (reader != null) {
                reader = null;
                readers.Dispose();
                readers = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void performInventory() {
        try {
            reader.Actions.Inventory.perform();
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopInventory() {
        try {
            reader.Actions.Inventory.stop();
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }
    }

    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        if (sdkHandler != null) {
            if (outXML == null) {
                outXML = new StringBuilder();
            }
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode, inXML, outXML, scannerID);
            Log.d(TAG, "execute command returned " + result.toString());
            return result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS;
        }
        return false;
    }

    public class EventHandler implements RfidEventsListener {
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                for (TagData tag : myTags) {
                    Log.d(TAG, "Tag ID: " + tag.getTagID() + " RSSI value " + tag.getPeakRSSI());
                    Log.d(TAG, "Context: " + getCurrentContext());
                }
                new AsyncDataUpdate().execute(myTags);
            }
        }

        @SuppressLint("StaticFieldLeak")
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());

            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                Context ctx = getCurrentContext();
                boolean isPressed = rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() ==
                                   HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;

                Log.d(TAG, isPressed ? "HANDHELD_TRIGGER_PRESSED" : "HANDHELD_TRIGGER_RELEASED");
                Log.d(TAG, "Context: " + (ctx != null ? ctx.getClass().getSimpleName() : "null"));

                // Only handle trigger for the current active activity
                if (ctx instanceof BluetoothConnectionActivity) {
                    BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
                    // Check if the activity is still active and not finishing
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.runOnUiThread(() -> activity.handleTriggerPress(isPressed));
                    }
                } else if (ctx instanceof ScanItems) {
                    ScanItems activity = (ScanItems) ctx;
                    // Check if the activity is still active and not finishing
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.runOnUiThread(() -> activity.handleTriggerPress(isPressed));
                    }
                }
            }

            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        disconnect();
                        return null;
                    }
                }.execute();
            }
        }

    }

    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            Context ctx = getCurrentContext();
            if (ctx instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) ctx).handleTagdata(params);
            } else if (ctx instanceof ScanItems) {
                ((ScanItems) ctx).handleTagdata(params);
            }
            return null;
        }
    }

    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {}

    @Override
    public void dcssdkEventScannerDisappeared(int i) {}

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {}

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int i) {}

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {}

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {}

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {}

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {}

    public interface RFIDHandlerBluetoothListener {
        void handleTagdata(TagData[][] tagDataArray);
        void handleTriggerPress(boolean pressed);
        void sendToast(String val);
    }
}