package com.gusya.mv.simpleadid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *<p>
 *     Helper class that retrieves Ad ID and Limited Ad Tracking flag from Google Play Services
 *     but without using any dependencies from external GMS libraries.
 *</p>
 *
 */
public final class SimpleAdId {

    private static final String GP_PACKAGE = "com.android.vending";
    private static final String GP_PACKAGE_NOT_FOUND = "package 'com.android.vending' not found";
    private static final String GMS_ACTION = "com.google.android.gms.ads.identifier.service.START";
    private static final String GMS_PACKAGE = "com.google.android.gms";

    private final SimpleAdListener mListener;
    private final Handler mHandler;

    protected SimpleAdId(SimpleAdListener listener, Handler handler){
        this.mListener = listener;
        this.mHandler = handler;
    }

    protected void getGoogleAdIdInfo(Context context){
        PackageManager packageManager = context.getPackageManager();
        try {
            // is google play installed
            packageManager.getPackageInfo(GP_PACKAGE, 0);
        } catch (PackageManager.NameNotFoundException e){
            // google play is not installed
            PackageManager.NameNotFoundException exception =
                    new PackageManager.NameNotFoundException(GP_PACKAGE_NOT_FOUND);
            onErrorOnUI(exception);
            return;
        }

        // service binding intent
        Intent intent = new Intent(GMS_ACTION);
        intent.setPackage(GMS_PACKAGE);
        AdIdConnection serviceConnection = new AdIdConnection();
        try {
            // if connection is successful
            if (context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)) {
                AdIdInterface adIdInterface = new AdIdInterface(serviceConnection.getBinder());
                String adId = adIdInterface.getAdId();
                boolean isAdTrackingEnabled = adIdInterface.isAdIdTrackingEnabled();
                if (TextUtils.isEmpty(adId)) {
                    // empty ad id, something went wrong
                    onErrorOnUI(new IllegalStateException("Ad ID is null or empty"));
                } else {
                    // everything is ok, call listener
                    onSuccessOnUI(new AdIdInfo(adId, isAdTrackingEnabled));
                }
            } else {
                // connection to service was not successful
                onErrorOnUI(new IllegalStateException("Bad GMS service connection"));
            }
        } catch (IllegalStateException | RemoteException e){
            // can't process IBinder object
            onErrorOnUI(e);
        } finally {
            // finally unbind from service
            context.unbindService(serviceConnection);
        }
    }

    protected void onSuccessOnUI(final AdIdInfo info){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onSuccess(info);
            }
        });
    }

    protected void onErrorOnUI(final Exception exception){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mListener.onException(exception);
            }
        });
    }

    /**
     * <p>
     *      Retrieve 'Ad ID' and 'Is Limited Ad Tracking' flag.
     *      Listener methods are invoked on the UI thread
     *</p>
     *
     * @param context Application context
     * @param simpleAdListener callback
     */
    public static final void getAdInfo(final Context context, final SimpleAdListener simpleAdListener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(Looper.getMainLooper());
                new SimpleAdId(simpleAdListener, handler).getGoogleAdIdInfo(context);
            }
        }).start();
    }

    /**
     * <p>
     *      Client side callback interface
     * </p>
     */
    public interface SimpleAdListener {
        /**
         * Successfully retrieved Ad info
         * @param info AdIdInfo object containing Ad ID and Tracking flag
         */
        void onSuccess(AdIdInfo info);

        /**
         * Something happened trying to get Ad info
         * @param exception Exception object that was thrown on error
         */
        void onException(Exception exception);
    }

    /**
     * Holds 'Ad ID and 'Is Limited Ad Tracking' flag
     */
    public static class AdIdInfo{
        private final String adId;
        private final boolean isAdTrackingEnabled;

        private AdIdInfo(String adId, boolean isAdTrackingEnabled){
            this.adId = adId;
            this.isAdTrackingEnabled = isAdTrackingEnabled;
        }

        public String getAdId() {
            return adId;
        }

        public boolean isAdTrackingEnabled() {
            return isAdTrackingEnabled;
        }
    }

    /**
     * Service connection that retrieves Binder object from connected service
     */
    private static class AdIdConnection implements ServiceConnection{

        private final BlockingQueue<IBinder> queue = new ArrayBlockingQueue<>(1);

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) throws IllegalStateException {
            try{
                this.queue.put(iBinder);
            }
            catch (InterruptedException ex){
                throw new IllegalStateException("Exception trying to parse GMS connection");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }

        public IBinder getBinder() throws IllegalStateException {
            try{
                return queue.take();
            }
            catch (InterruptedException e){
                throw new IllegalStateException("Exception trying to retrieve GMS connection");
            }
        }
    }

    /**
     * Interface that deals with advertising service's Binder
     */
    private static class AdIdInterface implements IInterface{

        private static final String INTERFACE_TOKEN = "com.google.android.gms.ads.identifier.internal.IAdvertisingIdService";
        private static final int AD_ID_TRANSACTION_CODE = 1;
        private static final int AD_TRACKING_TRANSACTION_CODE = 2;

        private final IBinder mIBinder;
        private AdIdInterface(IBinder binder){
            this.mIBinder = binder;
        }

        @Override
        public IBinder asBinder() {
            return mIBinder;
        }

        private String getAdId() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            String adId;
            try {
                data.writeInterfaceToken(INTERFACE_TOKEN);
                mIBinder.transact(AD_ID_TRANSACTION_CODE, data, reply, 0);
                reply.readException();
                adId = reply.readString();
            }
            finally {
                data.recycle();
                reply.recycle();
            }
            return adId;
        }

        private boolean isAdIdTrackingEnabled() throws RemoteException{
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            boolean limitedTrackingEnabled;
            try {
                data.writeInterfaceToken(INTERFACE_TOKEN);
                data.writeInt(1);
                mIBinder.transact(AD_TRACKING_TRANSACTION_CODE, data, reply, 0);
                reply.readException();
                limitedTrackingEnabled = 0 != reply.readInt();
            }
            finally {
                data.recycle();
                reply.recycle();
            }
            return limitedTrackingEnabled;
        }
    }
}
