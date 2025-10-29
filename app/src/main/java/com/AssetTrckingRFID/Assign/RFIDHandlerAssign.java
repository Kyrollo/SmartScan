//package com.AssetTrckingRFID.Assign;
//
//import android.os.AsyncTask;
//import android.os.Handler;
//import android.os.Looper;
//
//import com.AssetTrckingRFID.R;
//import com.zebra.rfid.api3.Antennas;
//import com.zebra.rfid.api3.ENUM_TRANSPORT;
//import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
//import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
//import com.zebra.rfid.api3.INVENTORY_STATE;
//import com.zebra.rfid.api3.InvalidUsageException;
//import com.zebra.rfid.api3.OperationFailureException;
//import com.zebra.rfid.api3.RFIDReader;
//import com.zebra.rfid.api3.ReaderDevice;
//import com.zebra.rfid.api3.Readers;
//import com.zebra.rfid.api3.RfidEventsListener;
//import com.zebra.rfid.api3.RfidReadEvents;
//import com.zebra.rfid.api3.RfidStatusEvents;
//import com.zebra.rfid.api3.SESSION;
//import com.zebra.rfid.api3.SL_FLAG;
//import com.zebra.rfid.api3.START_TRIGGER_TYPE;
//import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
//import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
//import com.zebra.rfid.api3.TagData;
//import com.zebra.rfid.api3.TriggerInfo;
//
//import java.util.ArrayList;
//
//import com.zebra.scannercontrol.DCSSDKDefs;
//import com.zebra.scannercontrol.DCSScannerInfo;
//import com.zebra.scannercontrol.FirmwareUpdateEvent;
//import com.zebra.scannercontrol.IDcsSdkApiDelegate;
//import com.zebra.scannercontrol.SDKHandler;
//
//
//public class RFIDHandlerAssign implements IDcsSdkApiDelegate, Readers.RFIDReaderEventHandler {
//    private Readers readers;
//    private ArrayList<ReaderDevice> availableRFIDReaderList;
//    private ReaderDevice readerDevice;
//    private RFIDReader reader;
//    private EventHandler eventHandler;
//    private AssignTags context;
//    private SDKHandler sdkHandler;
//    private ArrayList<DCSScannerInfo> scannerList;
//    private int scannerID;
//    static MyAsyncTask cmdExecTask = null;
//    private int MAX_POWER = 300;
//    String readerName = "RFD4031-G10B700-US";
//
//    void onCreate(AssignTags activity) {
//        context = activity;
//        scannerList = new ArrayList<>();
//        InitSDK();
//    }
//
//    @Override
//    public void dcssdkEventScannerAppeared(DCSScannerInfo dcsScannerInfo) {
//
//    }
//
//    @Override
//    public void dcssdkEventScannerDisappeared(int i) {
//
//    }
//
//    @Override
//    public void dcssdkEventCommunicationSessionEstablished(DCSScannerInfo dcsScannerInfo) {
//
//    }
//
//    @Override
//    public void dcssdkEventCommunicationSessionTerminated(int i) {
//
//    }
//
//    @Override
//    public void dcssdkEventBarcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
//    }
//
//    @Override
//    public void dcssdkEventImage(byte[] bytes, int i) {
//
//    }
//
//    @Override
//    public void dcssdkEventVideo(byte[] bytes, int i) {
//
//    }
//
//    @Override
//    public void dcssdkEventBinaryData(byte[] bytes, int i) {
//
//    }
//
//    @Override
//    public void dcssdkEventFirmwareUpdate(FirmwareUpdateEvent firmwareUpdateEvent) {
//
//    }
//
//    @Override
//    public void dcssdkEventAuxScannerAppeared(DCSScannerInfo dcsScannerInfo, DCSScannerInfo dcsScannerInfo1) {
//
//    }
//
//    private boolean isReaderConnected() {
//        if (reader != null && reader.isConnected())
//            // Reader is connected
//            return true;
//        else {
//            // Reader is not connected
//            return false;
//        }
//    }
//
//    //
//    //  Activity life cycle behavior
//    //
//
//    String onResume() {
//        return connect();
//    }
//
//    void onPause() {
//        disconnect();
//    }
//
//    void onDestroy() {
//        dispose();
//    }
//
//    //
//    // RFID SDK
//    //
//    private void InitSDK() {
//        context.showProgressBar();
//        if (readers == null) {
//            new CreateInstanceTask().execute();
//        } else
//            connectReader();
//    }
//
//    // Enumerates SDK based on host device
//    private class CreateInstanceTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... voids) {
//            // Based on support available on host device choose the reader type
//            InvalidUsageException invalidUsageException = null;
//            readers = new Readers(context, ENUM_TRANSPORT.SERVICE_USB);
//            try {
//                availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
//            } catch (InvalidUsageException e) {
//                invalidUsageException = e;
//                e.printStackTrace();
//            }
//            if (invalidUsageException != null || availableRFIDReaderList.size() == 0) {
//                readers.Dispose();
//                readers = null;
//                if (readers == null) {
//                    readers = new Readers(context, ENUM_TRANSPORT.BLUETOOTH);
//                }
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            connectReader();
//        }
//    }
//
//    private synchronized void connectReader(){
//        if(!isReaderConnected()){
//            new ConnectionTask().execute();
//        }
//    }
//
//    private class ConnectionTask extends AsyncTask<Void, Void, String> {
//        @Override
//        protected String doInBackground(Void... voids) {
//            GetAvailableReader();
//            if (reader != null)
//                return connect();
//            return "Failed to find or connect reader";
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//        }
//    }
//
//    private synchronized void GetAvailableReader() {
//        if (readers != null) {
//            readers.attach(this);
//            try {
//                if (readers.GetAvailableRFIDReaderList() != null) {
//                    availableRFIDReaderList = readers.GetAvailableRFIDReaderList();
//                    if (availableRFIDReaderList.size() != 0) {
//                        // if single reader is available then connect it
//                        if (availableRFIDReaderList.size() == 1) {
//                            readerDevice = availableRFIDReaderList.get(0);
//                            reader = readerDevice.getRFIDReader();
//                        } else {
//                            // search reader specified by name
//                            for (ReaderDevice device : availableRFIDReaderList) {
//                                if (device.getName().startsWith(readerName)) {
//
//                                    readerDevice = device;
//                                    reader = readerDevice.getRFIDReader();
//
//                                }
//                            }
//                        }
//                    }
//                }
//            }catch (InvalidUsageException ie){
//                ie.printStackTrace();
//            }
//
//        }
//    }
//
//    // handler for receiving reader appearance events
//    @Override
//    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
//        context.sendToast(context.getString(R.string.connecting_to_rfid_reader));
//        connectReader();
//    }
//
//    @Override
//    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
//        context.sendToast(context.getString(R.string.rfid_reader_disappeared));
//        if (readerDevice.getName().equals(reader.getHostName()))
//            disconnect();
//    }
//
//    private synchronized String connect() {
//        if (reader != null) {
//            try {
//                if (!reader.isConnected()) {
//                    // Establish connection to the RFID Reader
//                    reader.connect();
//                    ConfigureReader();
//
//                    // Call this function if the reader device supports scanner to setup scanner SDK
//                    setupScannerSDK();
//                    if (reader.isConnected()) {
//                        context.runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                context.hideProgressBar();
//                            }
//                        });
//                        return "Connected: " + reader.getHostName();
//                    }
//                }
//            } catch (InvalidUsageException e) {
//                e.printStackTrace();
//            } catch (OperationFailureException e) {
//                e.printStackTrace();
//                String des = e.getResults().toString();
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        context.hideProgressBar();
//                    }
//                });
//                return "Connection failed" + e.getVendorMessage() + " " + des;
//            }
//        }
//        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                context.hideProgressBar();
//            }
//        }, 5000); // 5 second delay
//        return "";
//    }
//
//    private void ConfigureReader() {
//        if (reader.isConnected()) {
//            TriggerInfo triggerInfo = new TriggerInfo();
//            triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
//            triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
//            try {
//                // receive events from reader
//                if (eventHandler == null)
//                    eventHandler = new EventHandler();
//                reader.Events.addEventsListener(eventHandler);
//                // HH event
//                reader.Events.setHandheldEvent(true);
//                // tag event with tag data
//                reader.Events.setTagReadEvent(true);
//                reader.Events.setAttachTagDataWithReadEvent(false);
//                // set trigger mode as rfid so scanner beam will not come
//                reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE, true);
//                // set start and stop triggers
//                reader.Config.setStartTrigger(triggerInfo.StartTrigger);
//                reader.Config.setStopTrigger(triggerInfo.StopTrigger);
//                // power levels are index based so maximum power supported get the last one
//                MAX_POWER = reader.ReaderCapabilities.getTransmitPowerLevelValues().length - 1;
//                // set antenna configurations
//                Antennas.AntennaRfConfig config = reader.Config.Antennas.getAntennaRfConfig(1);
//                config.setTransmitPowerIndex(2);
//                config.setReceiveSensitivityIndex(0);
//                config.setTransmitPowerIndex(1);
//                reader.Config.Antennas.setAntennaRfConfig(1, config);
//                // Set the singulation control
//                Antennas.SingulationControl s1_singulationControl = reader.Config.Antennas.getSingulationControl(1);
//                s1_singulationControl.setSession(SESSION.SESSION_S0);
//                s1_singulationControl.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
//                s1_singulationControl.Action.setSLFlag(SL_FLAG.SL_ALL);
//                reader.Config.Antennas.setSingulationControl(1, s1_singulationControl);
//                // delete any prefilters
//                reader.Actions.PreFilters.deleteAll();
//                //
//            } catch (InvalidUsageException | OperationFailureException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void setupScannerSDK(){
//        if (sdkHandler == null)
//        {
//            sdkHandler = new SDKHandler(context);
//            //For cdc device
//            DCSSDKDefs.DCSSDK_RESULT result = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);
//
//            //For bluetooth device
//            DCSSDKDefs.DCSSDK_RESULT btResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
//            DCSSDKDefs.DCSSDK_RESULT btNormalResult = sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
//
//            sdkHandler.dcssdkSetDelegate(this);
//
//            int notifications_mask = 0;
//            // We would like to subscribe to all scanner available/not-available events
//            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value;
//
//            // We would like to subscribe to all scanner connection events
//            notifications_mask |= DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value | DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value;
//
//
//            // We would like to subscribe to all barcode events
//            // subscribe to events set in notification mask
//            sdkHandler.dcssdkSubsribeForEvents(notifications_mask);
//        }
//        if (sdkHandler != null)
//        {
//            ArrayList<DCSScannerInfo> availableScanners = new ArrayList<>();
//            availableScanners  = (ArrayList<DCSScannerInfo>) sdkHandler.dcssdkGetAvailableScannersList();
//
//            scannerList.clear();
//            if (availableScanners != null)
//            {
//                for (DCSScannerInfo scanner : availableScanners)
//                {
//
//                    scannerList.add(scanner);
//                }
//            }
//
//        }
//        if (reader != null )
//        {
//            for (DCSScannerInfo device : scannerList)
//            {
//                if (device.getScannerName().contains(reader.getHostName()))
//                {
//                    try
//                    {
//                        sdkHandler.dcssdkEstablishCommunicationSession(device.getScannerID());
//                        scannerID= device.getScannerID();
//                    }
//                    catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    }
//
//    private synchronized void disconnect() {
//        try {
//            if (reader != null) {
//                if (eventHandler != null)
//                    reader.Events.removeEventsListener(eventHandler);
//                if (sdkHandler != null) {
//                    sdkHandler.dcssdkTerminateCommunicationSession(scannerID);
//                    scannerList = null;
//                }
//                reader.disconnect();
//                context.sendToast(context.getString(R.string.disconnecting_reader));
//            }
//        } catch (InvalidUsageException e) {
//            e.printStackTrace();
//        } catch (OperationFailureException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    private synchronized void dispose() {
//        disconnect();
//        try {
//            if (reader != null) {
//                reader = null;
//                readers.Dispose();
//                readers = null;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    synchronized void performInventory() {
//        try {
//            reader.Actions.Inventory.perform();
//        } catch (InvalidUsageException e) {
//            e.printStackTrace();
//        } catch (OperationFailureException e) {
//            e.printStackTrace();
//        }
//    }
//
//    synchronized void stopInventory() {
//        try {
//            reader.Actions.Inventory.stop();
//        } catch (InvalidUsageException e) {
//            e.printStackTrace();
//        } catch (OperationFailureException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void scanCode(){
//        String in_xml = "<inArgs><scannerID>" + scannerID+ "</scannerID></inArgs>";
//        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, null);
//        cmdExecTask.execute(new String[]{in_xml});
//    }
//
//    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
//        int scannerId;
//        StringBuilder outXML;
//        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
//        ///private CustomProgressDialog progressDialog;
//
//        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
//            this.scannerId = scannerId;
//            this.opcode = opcode;
//            this.outXML = outXML;
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//        }
//
//
//        @Override
//        protected Boolean doInBackground(String... strings) {
//            return executeCommand(opcode, strings[0], outXML, scannerId);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean b) {
//            super.onPostExecute(b);
//        }
//    }
//
//    public boolean executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE opCode, String inXML, StringBuilder outXML, int scannerID) {
//        if (sdkHandler != null)
//        {
//            if(outXML == null){
//                outXML = new StringBuilder();
//            }
//            DCSSDKDefs.DCSSDK_RESULT result=sdkHandler.dcssdkExecuteCommandOpCodeInXMLForScanner(opCode,inXML,outXML,scannerID);
//            if(result== DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS)
//                return true;
//            else if(result==DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE)
//                return false;
//        }
//        return false;
//    }
//    // Read/Status Notify handler
//    // Implement the RfidEventsLister class to receive event notifications
//    public class EventHandler implements RfidEventsListener {
//        // Read Event Notification
//        public void eventReadNotify(RfidReadEvents e) {
//            TagData[] myTags = reader.Actions.getReadTags(100);
//            if (myTags != null) {
////                for (int index = 0; index < myTags.length; index++) {
////                    //  Log.d(TAG, "Tag ID " + myTags[index].getTagID());
////                    Log.d(TAG, "Tag ID" + myTags[index].getTagID() +"RSSI value "+ myTags[index].getPeakRSSI());
////                    Log.d(TAG, "RSSI value "+ myTags[index].getPeakRSSI());
////                    /* To get the RSSI value*/   //   Log.d(TAG, "RSSI value "+ myTags[index].getPeakRSSI());
////
////                }
//                new AsyncDataUpdate().execute(myTags);
//            }
//        }
//
//        // Status Event Notification
//        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
//            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
//                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
//                    new AsyncTask<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            context.handleTriggerPress(true);
//                            return null;
//                        }
//                    }.execute();
//                }
//                if (rfidStatusEvents.StatusEventData.HandheldTriggerEventData.getHandheldEvent() == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
//                    new AsyncTask<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... voids) {
//                            context.handleTriggerPress(false);
//                            return null;
//                        }
//                    }.execute();
//                }
//            }
//            if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
//                new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... voids) {
//
//                        disconnect();
//                        return null;
//                    }
//                }.execute();
//            }
//        }
//    }
//
//    private class AsyncDataUpdate extends AsyncTask<TagData[], Void, Void> {
//        @Override
//        protected Void doInBackground(TagData[]... params) {
//            context.handleTagdata(params);
//
//            return null;
//        }
//    }
//
//    public interface RFIDHandlerListener {
//        void handleTagdata(TagData[][] tagDataArray);
//        void handleTriggerPress(boolean pressed);
//        void sendToast(String val);
//    }
//}
