package com.jim.ktv.ktvcontroller.app;


import android.content.Context;

import java.io.IOException;

/*
 * 
 * @author Jim
 * 
 */
public class AppContext {

    private static AppContext instance = null;

    private Context nativeContext = null;
    private Context activeContext = null;
    private Context gcmReceiverContext = null;

    public static String ktvServerPort = "8888";
    private static String ktvServerIP = null;


    @Override
    protected void finalize() throws Throwable {

    }


    public static int initAtStartup(Context act_ctx) {
        if (act_ctx == null)
            return -1;


        AppContext app_ctx = AppContext.getInstance();


        //AAppContext.getInstance().setNativeContext(act_ctx.getApplicationContext());
        app_ctx.nativeContext = act_ctx.getApplicationContext();
        app_ctx.activeContext = act_ctx;

        return 0;
    }


    private AppContext() {

    }


    public static AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }

        return instance;
    }


    public Context getNativeContext() {
        return nativeContext;
    }


    public int setNativeContext(Context val) {
        nativeContext = val;
        return 0;
    }


    public synchronized Context getActiveContext() {
        return activeContext;
    }


    public synchronized int setActiveContext(Context val) {
        activeContext = val;
        return 0;
    }

    public String getKTVServerIP()
    {
        return ktvServerIP;
    }

    public void setKTVServerIP(String ip){
        ktvServerIP = ip;
    }

    public boolean pingServer(){
        System.out.println("executeCommand");
        Runtime runtime = Runtime.getRuntime();
        try
        {
            Process  mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + ktvServerIP);
            int mExitValue = mIpAddrProcess.waitFor();
            System.out.println(" mExitValue "+mExitValue);
            if(mExitValue==0){
                return true;
            }else{
                return false;
            }
        }
        catch (InterruptedException ignore)
        {
            ignore.printStackTrace();
            System.out.println(" Exception:"+ignore);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.out.println(" Exception:"+e);
        }
        return false;
    }
}
