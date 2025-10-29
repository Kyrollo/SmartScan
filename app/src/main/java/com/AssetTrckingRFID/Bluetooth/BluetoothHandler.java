package com.AssetTrckingRFID.Bluetooth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
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

    String readerName = "RFD4031-G10B700-US";

    public synchronized void onCreate(Context context) {
        reader = App.get().getRfidReader();
        updateContext(context);
        scannerList = new ArrayList<>();
        checkRFIDConnectionStatus();
    }

    public void checkRFIDConnectionStatus() {
        new CheckConnectionTask().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckConnectionTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            handleConnecting();
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
            String connectedMsg = ctx.getString(R.string.rfid_connected);
            String statusMsg = ctx.getString(R.string.rfid_is_already_connected) + (reader != null ? reader.getHostName() : "");
            activity.sendToast(connectedMsg);
            activity.updateRFIDStatus(statusMsg);
            activity.runOnUiThread(activity::hideProgressBar);
        } else if (ctx instanceof ScanItems) {
            ScanItems activity = (ScanItems) ctx;
            String connectedMsg = ctx.getString(R.string.rfid_connected);
            activity.sendToast(connectedMsg);
            activity.runOnUiThread(activity::hideProgressBar);
        }
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
        if (!isReaderConnected()) {
            if (reader == null) {
                new ConnectionTask().execute();
            }
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private String errorMessage = "";

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();

            if (reader == null) {
                errorMessage = getCurrentContext().getString(R.string.rfid_not_connected);
                return false;
            }

            return connect();
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
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
                ScanItems activity = (ScanItems) ctx;

                if (success) {
                    String statusMsg = ctx.getString(R.string.connected_to) + (readerDevice != null ? readerDevice.getName() : "");
                    activity.sendToast(statusMsg);
                    activity.runOnUiThread(activity::hideProgressBar);
                } else {
                    activity.sendToast(ctx.getString(R.string.rfid_not_connected));
                    activity.runOnUiThread(activity::hideProgressBar);
                }
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
                    if (!availableRFIDReaderList.isEmpty()) {
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

    private synchronized boolean connect() {
        if (reader != null) {
            Log.d(TAG, "connect " + reader.getHostName());
            try {
                if (!reader.isConnected()) {
                    reader.connect();

                    if (reader.isConnected()) {
                        ConfigureReader();
                        setupScannerSDK();
                        App.get().setRfidReader(reader);
                        return true;
                    } else {
                        return false;
                    }
                }
                return true; // Already connected
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
            if (context instanceof ScanItems || context instanceof BluetoothConnectionActivity) {
                Context oldContext = this.context;
                this.context = context;
                Log.d(TAG, "Context updated from: " + (oldContext != null ? oldContext.getClass().getSimpleName() : "null") +
                           " to: " + context.getClass().getSimpleName());

                if (cmdExecTask != null && !cmdExecTask.isCancelled()) {
                    cmdExecTask.cancel(true);
                    cmdExecTask = null;
                }

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

    public void onDestroy() {
        dispose();
        App.get().setRfidReader(null);
    }

    public void setupScannerSDK() {
        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(getCurrentContext());
//            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);
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