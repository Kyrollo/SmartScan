package com.SmartScan.Bluetooth;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.SmartScan.App;

import com.SmartScan.Assign.AssignTags;
import com.SmartScan.ScanItems.ScanItems;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.SL_FLAG;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.api3.TriggerInfo;

import java.util.ArrayList;

import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;
import com.zebra.scannercontrol.IDcsSdkApiDelegate;
import com.zebra.scannercontrol.SDKHandler;

public class BluetoothHandler implements IDcsSdkApiDelegate, Readers.RFIDReaderEventHandler {
    final static String TAG = "RFID_SAMPLE";
    private Readers readers;
    private ArrayList<ReaderDevice> availableRFIDReaderList;
    private ReaderDevice readerDevice;
    private RFIDReader reader;
    private EventHandler eventHandler;
    private Context context;
    private SDKHandler sdkHandler;
    private ArrayList<DCSScannerInfo> scannerList;
    private int scannerID;
    static MyAsyncTask cmdExecTask = null;
    private int MAX_POWER = 270;
    String readerName = "RFD4031-G10B700-US";

    public void onCreate(Context context) {
        reader = App.get().getRfidReader();
        this.context = context;
        scannerList = new ArrayList<>();
        InitSDK();
//        checkAndInitRFIDHandler();
    }



    @Override
    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {
    }

    @Override
    public void dcssdkEventScannerDisappeared(int i) {
    }

    @Override
    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {
    }

    @Override
    public void dcssdkEventCommunicationSessionTerminated(int i) {
    }

    @Override
    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
    }

    @Override
    public void dcssdkEventImage(byte[] bytes, int i) {
    }

    @Override
    public void dcssdkEventVideo(byte[] bytes, int i) {
    }

    @Override
    public void dcssdkEventBinaryData(byte[] bytes, int i) {
    }

    @Override
    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
    }

    @Override
    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
    }

    private boolean isReaderConnected() {
        if (reader != null && reader.isConnected()){
            return true;
        }
        else {
            Log.d(TAG, "reader is not connected");
            return false;
        }
    }

    public String onResume() {
        return connect();
    }

    public void onDestroy() {
        dispose();
        App.get().setRfidReader(null);
    }

//    public void checkAndInitRFIDHandler() {
//        if (isReaderConnected()) {
//            ConfigureReader();
//            connectReader();
//            if (context instanceof BluetoothConnectionActivity) {
//                ((BluetoothConnectionActivity) context).sendToast("RFID Connected");
//                ((BluetoothConnectionActivity) context).updateRFIDStatus("RFID is already connected: " + reader.getHostName());
//            }
//        } else {
//            if (context instanceof BluetoothConnectionActivity) {
//                ((BluetoothConnectionActivity) context).sendToast("RFID not connected");
//                ((BluetoothConnectionActivity) context).updateRFIDStatus("RFID not connected");
//            }
//            InitSDK();
//        }
//    }

    private void InitSDK() {
        Log.d(TAG, "InitSDK");

        if (context instanceof BluetoothConnectionActivity) {
            ((BluetoothConnectionActivity) context).showProgressBar();
        } else if (context instanceof ScanItems) {
            ((ScanItems) context).showProgressBar();
        }if (context instanceof AssignTags) {
            ((AssignTags) context).showProgressBar();
        }


        if (readers == null) {
            if (context instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) context).sendToast("RFID not connected");
                ((BluetoothConnectionActivity) context).updateRFIDStatus("RFID not connected");
            }
            new CreateInstanceTask().execute();
        } else{
            ConfigureReader();
            connectReader();
            if (context instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) context).sendToast("RFID Connected");
                ((BluetoothConnectionActivity) context).updateRFIDStatus("RFID is already connected: " + reader.getHostName());
            }
        }
    }

    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "CreateInstanceTask");
            // Based on support available on host device choose the reader type
            InvalidUsageException invalidUsageException = null;
            readers = new Readers(context, ENUM_TRANSPORT.SERVICE_USB);
            try {
                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
            } catch (InvalidUsageException e) {
                invalidUsageException = e;
                e.printStackTrace();
            }
            if (invalidUsageException != null || availableRFIDReaderList.size() == 0) {
                readers.Dispose();
                readers = null;
                if (readers == null) {
                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
                }
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
        } else {
//            updateRFIDStatus("RFID is already connected: " + reader.getHostName());
        }
    }

    private class ConnectionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... voids) {
            Log.d(TAG, "ConnectionTask");
            GetAvailableReader();
            if (reader != null)
                return connect();
            return "Failed to find or connect reader";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (context instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) context).updateRFIDStatus(result);
            }
//            updateRFIDStatus(result);
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
                        // if single reader is available then connect it
                        if (availableRFIDReaderList.size() == 1) {
                            readerDevice = availableRFIDReaderList.get(0);
                            reader = readerDevice.getRFIDReader();
                        } else {
                            // search reader specified by name
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
        if (context instanceof BluetoothConnectionActivity) {
            ((BluetoothConnectionActivity) context).sendToast("RFID Reader Appeared");
        } else if (context instanceof AssignTags) {
            ((AssignTags) context).sendToast("RFID Reader Appeared");
        }if (context instanceof ScanItems) {
            ((ScanItems) context).sendToast("RFID Reader Appeared");
        }
        connectReader();
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Log.d(TAG, "RFIDReaderDisappeared " + readerDevice.getName());
        if (context instanceof BluetoothConnectionActivity) {
            ((BluetoothConnectionActivity) context).sendToast("RFID Reader Disappeared");
        } else if (context instanceof AssignTags) {
            ((AssignTags) context).sendToast("RFID Reader Disappeared");
        }if (context instanceof ScanItems) {
            ((ScanItems) context).sendToast("RFID Reader Disappeared");
        }
        if (readerDevice.getName().equals(reader.getHostName()))
            disconnect();
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

                        if (context instanceof BluetoothConnectionActivity) {
                            ((BluetoothConnectionActivity) context).sendToast("RFID Reader Connected");
                            ((BluetoothConnectionActivity) context).updateRFIDStatus("Connected: " + readerDevice.getName());
                            ((BluetoothConnectionActivity) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((BluetoothConnectionActivity) context).hideProgressBar();
                                }
                            });
                        }
                        else if (context instanceof AssignTags) {
                            ((AssignTags) context).sendToast("RFID Reader Connected");
                            ((AssignTags) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((AssignTags) context).hideProgressBar();
                                }
                            });
                        }
                        else if (context instanceof ScanItems) {
                            ((ScanItems) context).sendToast("RFID Reader Connected");
                            ((ScanItems) context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScanItems) context).hideProgressBar();
                                }
                            });
                        }
                        return "Connected: " + reader.getHostName();
                    }
                }
            } catch (InvalidUsageException e) {
                e.printStackTrace();
            } catch (OperationFailureException e) {
                e.printStackTrace();
                Log.d(TAG, "OperationFailureException " + e.getVendorMessage());
                String des = e.getResults().toString();


                if (context instanceof BluetoothConnectionActivity) {
                    ((BluetoothConnectionActivity) context).sendToast("RFID Reader Failed");
                    ((BluetoothConnectionActivity) context).updateRFIDStatus("Connection failed: " + e.getVendorMessage() + " " + des);
                    ((BluetoothConnectionActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((BluetoothConnectionActivity) context).hideProgressBar();
                        }
                    });
                } else if (context instanceof AssignTags) {
                    ((AssignTags) context).sendToast("RFID Reader Failed");
                    ((AssignTags) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((AssignTags) context).hideProgressBar();
                        }
                    });
                } else if (context instanceof ScanItems) {
                    ((ScanItems) context).sendToast("RFID Reader Failed");
                    ((ScanItems) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ScanItems) context).hideProgressBar();
                        }
                    });
                }

                return "Connection failed" + e.getVendorMessage() + " " + des;
            }
        }
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                if (context instanceof BluetoothConnectionActivity) {
                    ((BluetoothConnectionActivity) context).hideProgressBar();
                } else if (context instanceof AssignTags) {
                    ((AssignTags) context).hideProgressBar();
                } else if (context instanceof ScanItems) {
                    ((ScanItems) context).hideProgressBar();
                }
            }
        }, 5000);
        return "";
    }

    private void ConfigureReader() {
        Log.d(TAG, "ConfigureReader " + reader.getHostName());
        if (reader.isConnected()) {
            TriggerInfo triggerInfo = new TriggerInfo();
            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
            try {
                // receive events from reader
                if (eventHandler == null)
                    eventHandler = new EventHandler();
                reader.Events.addEventsListener(eventHandler);
                // HH event
                reader.Events.setHandheldEvent(true);
                // tag event with tag data
                reader.Events.setTagReadEvent(true);
                reader.Events.setAttachTagDataWithReadEvent(false);
                // set start and stop triggers
                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
                // power levels are index based so maximum power supported get the last one
                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
                // set antenna configurations
                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
                if (context instanceof BluetoothConnectionActivity) {
                    config.setTransmitPowerIndex(MAX_POWER);
                    config.setrfModeTableIndex(0);
                    config.setTari(0);
                } else if (context instanceof AssignTags) {
                    config.setTransmitPowerIndex(2);
                    config.setReceiveSensitivityIndex(0);
                    config.setTransmitPowerIndex(1);
                }if (context instanceof ScanItems) {
                    int average_power = MAX_POWER / 2;
                    config.setTransmitPowerIndex(average_power);
                    config.setrfModeTableIndex(0);
                    config.setTari(0);
                }
                reader.Config.Antennas.setAntennaRfConfig(1, config);
                // Set the singulation control
                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
                s1_singulationControl.setSession(SESSION.SESSION_S0);
                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
                // delete any prefilters
                reader.Actions.PreFilters.deleteAll();
                //
            } catch (InvalidUsageException | OperationFailureException e) {
                e.printStackTrace();
            }
        }
    }

    public void setupScannerSDK() {
        if (sdkHandler == null) {
            sdkHandler = new SDKHandler(context);
            //For cdc device
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);

            //For bluetooth device
            DCSSDKDefs.DCSSDK_RESULT btResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
            DCSSDKDefs.DCSSDK_RESULT btNormalResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);

            Log.d(TAG, btNormalResult + " results " + btResult);
            sdkHandler.dcssdkSetDelegate(this);

            int notifications_mask = 0;
            // We would like to subscribe to all scanner available/not-available events
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;

            // We would like to subscribe to all scanner connection events
            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;


            // We would like to subscribe to all barcode events
            // subscribe to events set in notification mask
            sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
        }
        if (sdkHandler != null) {
            ArrayList<DCSScannerInfo> availableScanners = new ArrayList<>();
            availableScanners = (ArrayList<DCSScannerInfo>) sdkHandler.dcssdkGetAvailableScannersList();

            scannerList.clear();
            if (availableScanners != null) {
                for (DCSScannerInfo scanner : availableScanners) {

                    scannerList.add(scanner);
                }
            } else
                Log.d(TAG, "Available scanners null");

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
                if (context instanceof BluetoothConnectionActivity) {
                    ((BluetoothConnectionActivity) context).sendToast("Disconnecting reader");
                    ((BluetoothConnectionActivity) context).updateRFIDStatus("Disconnected");
                } else if (context instanceof AssignTags) {
                    ((AssignTags) context).sendToast("Disconnecting reader");
                }if (context instanceof ScanItems) {
                    ((ScanItems) context).sendToast("Disconnecting reader");
                }
            }
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void dispose() {
        disconnect();
        try {
            if (reader != null) {
                //Toast.makeText(getApplicationContext(), "Disconnecting reader", Toast.LENGTH_LONG).show();
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
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    public synchronized void stopInventory() {
        try {
            reader.Actions.Inventory.stop();
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        ///private CustomProgressDialog progressDialog;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
        }
    }

    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
        if (sdkHandler != null) {
            if (outXML == null) {
                outXML = new StringBuilder();
            }
            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode, inXML, outXML, scannerID);
            Log.d(TAG, "execute command returned " + result.toString());
            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
                return true;
            else if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
                return false;
        }
        return false;
    }

    public class EventHandler implements RfidEventsListener {
        public void eventReadNotify(RfidReadEvents e) {
            TagData[] myTags = reader.Actions.getReadTags(100);
            if (myTags != null) {
                for (int index = 0; index < myTags.length; index++) {
                    Log.d(TAG, "Tag ID" + myTags[index].getTagID() + "RSSI value " + myTags[index].getPeakRSSI());
                    Log.d(TAG, "RSSI value " + myTags[index].getPeakRSSI());
                }
                new AsyncDataUpdate().execute(myTags);
            }
        }

        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "Status Notification: " + rfidStatusEvents.StatusEventData.getStatusEventType());
            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d(TAG, "HANDHELD_TRIGGER_PRESSED");
//                            context.handleTriggerPress(true);
                            if (context instanceof BluetoothConnectionActivity) {
                                ((BluetoothConnectionActivity) context).handleTriggerPress(true);
                            } else if (context instanceof AssignTags) {
                                ((AssignTags) context).handleTriggerPress(true);
                            } else if (context instanceof ScanItems) {
                                ((ScanItems) context).handleTriggerPress(true);
                            }
                            return null;
                        }
                    }.execute();
                }
                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            Log.d(TAG, "HANDHELD_TRIGGER_RELEASED");
//                            context.handleTriggerPress(false);
                            if (context instanceof BluetoothConnectionActivity) {
                                ((BluetoothConnectionActivity) context).handleTriggerPress(false);
                            } else if (context instanceof AssignTags) {
                                ((AssignTags) context).handleTriggerPress(false);
                            } else if (context instanceof ScanItems) {
                                ((ScanItems) context).handleTriggerPress(false);
                            }
                            return null;
                        }
                    }.execute();
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
//            context.handleTagdata(params);
            if (context instanceof BluetoothConnectionActivity) {
                ((BluetoothConnectionActivity) context).handleTagdata(params);
            } else if (context instanceof AssignTags) {
                ((AssignTags) context).handleTagdata(params);
            } else if (context instanceof ScanItems) {
                ((ScanItems) context).handleTagdata(params);
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Update the UI or notify the user if needed
        }

    }

    public interface RFIDHandlerBluetoothListener {
        void handleTagdata(TagData[][] tagDataArray);
        void handleTriggerPress(boolean pressed);
        void sendToast(String val);
    }
}