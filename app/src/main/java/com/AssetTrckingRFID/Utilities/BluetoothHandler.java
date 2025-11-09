package com.AssetTrckingRFID.Utilities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import com.AssetTrckingRFID.Activities.BluetoothConnectionActivity;
import com.AssetTrckingRFID.App;
import com.AssetTrckingRFID.R;
import com.AssetTrckingRFID.Activities.ScanItems;
import com.zebra.rfid.api3.*;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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

    private volatile boolean userInitiatedConnect = false;
    private BroadcastReceiver bluetoothStateReceiver;
    private boolean isReceiverRegistered = false;
    private final Object connectionGuard = new Object();
    private volatile boolean isConnecting = false;
    private final AtomicBoolean isTearingDown = new AtomicBoolean(false);
    private volatile int lastBtState = BluetoothAdapter.ERROR;
    private volatile boolean isInventoryRunning = false;



    String readerName = "RFD4031-G10B700-US";

    public synchronized void onCreate(Context context) {
        reader = App.get().getRfidReader();
        updateContext(context);
        scannerList = new ArrayList<>();
        registerBluetoothStateReceiver();
        checkRFIDConnectionStatusSilently();
    }

    private void registerBluetoothStateReceiver() {
        if (isReceiverRegistered) return;

        bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == lastBtState) return;
                    lastBtState = state;

                    if (state == BluetoothAdapter.STATE_OFF) {
                        Log.d(TAG, "Bluetooth OFF -> teardown");
                        handleBluetoothDisabled();
                    } else if (state == BluetoothAdapter.STATE_ON) {
                        Log.d(TAG, "Bluetooth ON -> reconnect (reset teardown flag)");
                        isTearingDown.set(false);
                        onBluetoothEnabled();
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        Context ctx = getCurrentContext();
        if (ctx != null) {
            ctx.registerReceiver(bluetoothStateReceiver, filter);
            isReceiverRegistered = true;
            Log.d(TAG, "Bluetooth state receiver registered");
        }
    }


    private void onBluetoothEnabled() {
        new Thread(() -> {
            synchronized (connectionGuard) {
                if (isReaderConnected()) return;
                if (isConnecting) return;
                userInitiatedConnect = false;
                InitSDK();
            }
        }).start();
    }



    private void handleBluetoothDisabled() {
        if (!isTearingDown.compareAndSet(false, true)) {
            Log.d(TAG, "Teardown already in progress, skipping");
            return;
        }

        new Thread(() -> {
            synchronized (this) {
                try {
                    if (isInventoryRunning) {
                        try {
                            if (reader != null && reader.isConnected()) {
                                reader.Actions.Inventory.stop();
                            }
                        } catch (Exception e) {
                            Log.w(TAG, "Ignoring inventory stop error during teardown: " + e.getMessage());
                        } finally {
                            isInventoryRunning = false;
                        }
                    }

                    try {
                        if (reader != null && reader.isConnected()) {
                            reader.disconnect();
                        }
                    } catch (InvalidUsageException | OperationFailureException e) {
                        Log.w(TAG, "Ignoring reader disconnect error: " + e.getMessage());
                    } catch (Throwable t) {
                        Log.w(TAG, "Ignoring unexpected disconnect error: " + t.getMessage());
                    }

                    reader = null;
                    readerDevice = null;
                    App.get().setRfidReader(null);

                    try {
                        if (readers != null) {
                            readers.Dispose();
                        }
                    } catch (Throwable t) {
                        Log.w(TAG, "Ignoring readers dispose error: " + t.getMessage());
                    } finally {
                        readers = null;
                    }

                    isConnecting = false;
                    Log.d(TAG, "RFID reader teardown complete (Bluetooth off)");
                } catch (Exception e) {
                    Log.e(TAG, "Error disconnecting RFID: " + e.getMessage(), e);
                }
            }
        }).start();
    }

    private void unregisterBluetoothStateReceiver() {
        if (isReceiverRegistered && bluetoothStateReceiver != null) {
            try {
                Context ctx = getCurrentContext();
                if (ctx != null) {
                    ctx.unregisterReceiver(bluetoothStateReceiver);
                    isReceiverRegistered = false;
                    Log.d(TAG, "Bluetooth state receiver unregistered");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
    }

    public void checkRFIDConnectionStatusSilently() {
        userInitiatedConnect = false;
        new CheckConnectionTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckConnectionTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            if (userInitiatedConnect) {
                handleConnecting();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean isActuallyConnected = false;

            if (reader != null) {
                try {
                    isActuallyConnected = reader.isConnected();

                    if (isActuallyConnected) {
                        reader.getHostName();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Connection verification failed: " + e.getMessage());
                    isActuallyConnected = false;

                    if (reader != null) {
                        try {
                            reader.disconnect();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        reader = null;
                    }
                }
            }

            return isActuallyConnected;
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                handleAlreadyConnected();
            } else {
                InitSDK();
            }
        }
    }

    private void handleAlreadyConnected() {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            String statusMsg = ctx.getString(R.string.rfid_is_already_connected) + ": " + (reader != null ? reader.getHostName() : "");
            activity.updateRFIDStatus(statusMsg);
            String connectedMsg = ctx.getString(R.string.rfid_is_already_connected);
            activity.sendToast(connectedMsg);
            activity.runOnUiThread(activity::hideProgressBar);
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            String connectedMsg = ctx.getString(R.string.rfid_is_already_connected);
            activity.sendToast(connectedMsg);
            activity.runOnUiThread(activity::hideProgressBar);
        }

        userInitiatedConnect = false;
    }

    private void handleConnecting() {
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
            activity.updateRFIDStatus(ctx.getString(R.string.wait_for_connection));
            activity.runOnUiThread(activity::showProgressBar);
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            activity.runOnUiThread(activity::showProgressBar);
        }
    }

    private void InitSDK() {
        Log.d(TAG, "InitSDK");

        if (readers == null) {
            new CreateInstanceTask().execute();
        } else {
            ConfigureReader();
            connectReader();
        }
    }

    public synchronized void closeAndResetConnection() {
        Log.d(TAG, "closeAndResetConnection - starting");

        // Cancel any pending command task
        if (cmdExecTask != null && !cmdExecTask.isCancelled()) {
            try {
                cmdExecTask.cancel(true);
            } catch (Exception ignored) {}
            cmdExecTask = null;
        }

        // Stop inventory if running
        try {
            if (reader != null && reader.isConnected() && isInventoryRunning) {
                try {
                    reader.Actions.Inventory.stop();
                } catch (Exception e) {
                    Log.w(TAG, "Error stopping inventory during close: " + e.getMessage());
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "Ignoring inventory-stop check error: " + t.getMessage());
        } finally {
            isInventoryRunning = false;
        }

        // Disconnect reader
        try {
            if (reader != null) {
                try {
                    if (reader.isConnected()) {
                        reader.disconnect();
                    }
                } catch (Exception e) {
                    Log.w(TAG, "Error disconnecting reader: " + e.getMessage());
                }
                reader = null;
                readerDevice = null;
                App.get().setRfidReader(null);
            }
        } catch (Throwable t) {
            Log.w(TAG, "Ignoring reader disconnect error: " + t.getMessage());
        }

        // Dispose readers object
        try {
            if (readers != null) {
                readers.Dispose();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Ignoring readers dispose error: " + t.getMessage());
        } finally {
            readers = null;
        }

        // Clear SDK handler reference (close scanner session if needed externally)
        try {
            if (sdkHandler != null) {
                // If specific termination is needed, add it here. For safety clear reference.
                sdkHandler = null;
                scannerList = null;
                scannerID = 0;
            }
        } catch (Throwable t) {
            Log.w(TAG, "Ignoring sdkHandler cleanup error: " + t.getMessage());
        }

        // Reset connection flags so reconnection can proceed
        isConnecting = false;
        isTearingDown.set(false);

        Log.d(TAG, "closeAndResetConnection - complete");
    }

    @SuppressLint("StaticFieldLeak")
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
            if (invalidUsageException != null || availableRFIDReaderList.isEmpty()) {
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
        if (isReaderConnected()) return;
        if (isConnecting) return;
        isConnecting = true;
        new ConnectionTask().execute();
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {

        ConnectionTask() {}

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (reader == null) {
                return false;
            }
            return connect();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            try {
                Context ctx = getCurrentContext();
                if (ctx instanceof BluetoothConnectionActivity) {
                    BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
                    if (success) {
                        String statusMsg = ctx.getString(R.string.connected_to) + (readerDevice != null ? readerDevice.getName() : "");
                        activity.updateRFIDStatus(statusMsg);
                        activity.runOnUiThread(activity::hideProgressBar);
                    } else {
                        activity.updateRFIDStatus(ctx.getString(R.string.rfid_not_connected));
                        activity.sendToast(ctx.getString(R.string.rfid_not_connected));
                        activity.runOnUiThread(activity::hideProgressBar);
                    }
                } else if (ctx instanceof ScanItems) {
                    ((ScanItems) ctx).onConnectionStatusChanged(success, !success);
                }
            } finally {
                isConnecting = false;
                userInitiatedConnect = false;
            }
        }
    }

    private synchronized void GetAvailableReader() {
        Log.d(TAG, "GetAvailableReader");
        if (readers != null) {
            readers.attach(this);
            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
                if (availableRFIDReaderList != null && !availableRFIDReaderList.isEmpty()) {
                    readerDevice = null;
                    for (ReaderDevice device : availableRFIDReaderList) {
                        Log.d(TAG, "device: " + device.getName());
                        if (device.getName() != null && device.getName().startsWith(readerName)) {
                            readerDevice = device;
                            break;
                        }
                    }
                    if (readerDevice == null) {
                        readerDevice = availableRFIDReaderList.get(0);
                    }
                    reader = (readerDevice != null) ? readerDevice.getRFIDReader() : null;
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
        if (reader != null && readerDevice.getName().equals(reader.getHostName()))
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

    private boolean connect() {
        if (reader != null) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    reader.connect();

                    if (reader.isConnected()) {
                        ConfigureReader();
                        App.get().setRfidReader(reader);

                        Context ctx = getCurrentContext();
                        if (ctx != null) {
                            if (ctx instanceof BluetoothConnectionActivity) {
                                ((BluetoothConnectionActivity) ctx).runOnUiThread(this::setupScannerSDK);
                            } else if (ctx instanceof ScanItems) {
                                ((ScanItems) ctx).runOnUiThread(this::setupScannerSDK);
                            }
                        } else {
                            Log.w(TAG, "Context is null, skipping scanner SDK setup");
                        }

                        return true;
                    } else {
                        return false;
                    }
                }
                return true;
            } catch (InvalidUsageException e) {
                e.printStackTrace();
                Log.e(TAG, "InvalidUsageException: " + e.getMessage());
                return false;
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.e(TAG, "OperationFailureException: " + e.getVendorMessage());
                return false;
            }
        }
        return false;
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
                int MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;

                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);

                if (ctx instanceof BluetoothConnectionActivity) {
                    int average_power = (int) (MAX_POWER * 0.65);
                    config.setTransmitPowerIndex(average_power);
                    config.setrfModeTableIndex(0);
                    config.setTari(0);
                } else if (ctx instanceof ScanItems) {
                    int average_power = (int) (MAX_POWER * 0.65);
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
            if (context instanceof ScanItems || context instanceof BluetoothConnectionActivity) {
                Context oldContext = this.context;
                this.context = context;
                Log.d(TAG, "Context updated from: " + (oldContext != null ? oldContext.getClass().getSimpleName() : "null") +
                        " to: " + context.getClass().getSimpleName());

                if (cmdExecTask != null && !cmdExecTask.isCancelled()) {
                    cmdExecTask.cancel(true);
                    cmdExecTask = null;
                }

                // Re-register receiver with new context
                unregisterBluetoothStateReceiver();
                registerBluetoothStateReceiver();

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

    public void onResume() {
        if (isReaderConnected()) {
            reader.getHostName();
        }
    }

    public void setupScannerSDK() {
        Context ctx = getCurrentContext();
        if (ctx == null) {
            Log.e(TAG, "Cannot setup scanner SDK - context is null");
            return;
        }

        Context appContext = ctx.getApplicationContext();

        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(appContext);
            DCSSDKDefs.DCSSDK_RESULT btResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
            DCSSDKDefs.DCSSDK_RESULT btNormalResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
            Log.d(TAG, btNormalResult + " results " + btResult);
            sdkHandler.dcssdkSetDelegate(this);

            int notifications_mask = 0;
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
                    | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value
                    | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
                    | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
            sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        }

        if (scannerList == null) {
            scannerList = new ArrayList<>();
        } else {
            scannerList.clear();
        }

        if (sdkHandler != null) {
            ArrayList<DCSScannerInfo> availableScanners = (ArrayList<DCSScannerInfo>) sdkHandler.dcssdkGetAvailableScannersList();
            if (availableScanners != null) {
                scannerList.addAll(availableScanners);
            } else {
                Log.d(TAG, "Available scanners null");
            }
        }

        if (reader != null && scannerList != null) {
            for (DCSScannerInfo device : scannerList) {
                if (device.getScannerName() != null && device.getScannerName().contains(reader.getHostName())) {
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

    public synchronized void disconnect() {
        Log.d(TAG, "Disconnect");
        Context ctx = getCurrentContext();
        if (ctx instanceof BluetoothConnectionActivity) {
            ((BluetoothConnectionActivity) ctx).sendToast(ctx.getString(R.string.disconnecting_reader));
            ((BluetoothConnectionActivity) ctx).updateRFIDStatus(ctx.getString(R.string.disconnected));
        } else if (ctx instanceof ScanItems) {
            ((ScanItems) ctx).sendToast(ctx.getString(R.string.disconnecting_reader));
        }

        closeAndResetConnection();
    }

    public synchronized void performInventory() {
        if (reader == null || !reader.isConnected()) {
            Log.w(TAG, "Cannot perform inventory - reader not connected");
            return;
        }

        if (isInventoryRunning) {
            Log.d(TAG, "Inventory already running");
            return;
        }

        try {
            reader.Actions.Inventory.perform();
            isInventoryRunning = true;
            Log.d(TAG, "Inventory started");
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, "Error performing inventory: " + e.getMessage(), e);
            isInventoryRunning = false;
        }
    }

    public synchronized void stopInventory() {
        if (reader == null || !reader.isConnected()) {
            Log.w(TAG, "Cannot stop inventory - reader not connected");
            isInventoryRunning = false;
            return;
        }

        if (!isInventoryRunning) {
            Log.d(TAG, "Inventory not running, nothing to stop");
            return;
        }

        try {
            reader.Actions.Inventory.stop();
            isInventoryRunning = false;
            Log.d(TAG, "Inventory stopped");
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.e(TAG, "Error stopping inventory: " + e.getMessage(), e);
            isInventoryRunning = false;
        }
    }

    @SuppressLint("StaticFieldLeak")
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
            if (myTags != null && myTags.length > 0) {
                // Create a defensive copy to avoid concurrent modification
                TagData[] tagsCopy = new TagData[myTags.length];
                System.arraycopy(myTags, 0, tagsCopy, 0, myTags.length);

                for (TagData tag : tagsCopy) {
                    Log.d(TAG, "Tag ID: " + tag.getTagID() + " RSSI value " + tag.getPeakRSSI());
                }

                Context ctx = getCurrentContext();
                if (ctx != null) {
                    Log.d(TAG, "Context: " + ctx.getClass().getSimpleName());
                    new AsyncDataUpdate().execute(tagsCopy);
                } else {
                    Log.w(TAG, "Context is null, skipping tag update");
                }
            }
        }

        @SuppressLint("StaticFieldLeak")
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());

            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                Context ctx = getCurrentContext();
                if (ctx == null) {
                    Log.w(TAG, "Context is null, cannot handle trigger event");
                    return;
                }

                boolean isPressed = rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() ==
                        HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED;

                Log.d(TAG, isPressed ? "HANDHELD_TRIGGER_PRESSED" : "HANDHELD_TRIGGER_RELEASED");
                Log.d(TAG, "Context: " + ctx.getClass().getSimpleName());

                if (ctx instanceof BluetoothConnectionActivity) {
                    BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.runOnUiThread(() -> activity.handleTriggerPress(isPressed));
                    }
                } else if (ctx instanceof ScanItems) {
                    ScanItems activity = (ScanItems) ctx;
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

    @SuppressLint("StaticFieldLeak")
    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
        @Override
        protected Void doInBackground(TagData[]... params) {
            if (params == null || params.length == 0) {
                return null;
            }

            Context ctx = getCurrentContext();
            if (ctx == null) {
                Log.w(TAG, "Context is null in AsyncDataUpdate");
                return null;
            }

            try {
                if (ctx instanceof BluetoothConnectionActivity) {
                    BluetoothConnectionActivity activity = (BluetoothConnectionActivity) ctx;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.handleTagdata(params);
                    }
                } else if (ctx instanceof ScanItems) {
                    ScanItems activity = (ScanItems) ctx;
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.handleTagdata(params);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error updating tag data: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.d(TAG, "AsyncDataUpdate cancelled");
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
        void onConnectionStatusChanged(boolean isConnected, boolean isFailed);
    }
}
