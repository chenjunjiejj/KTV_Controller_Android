package com.jim.ktv.ktvcontroller;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.ktv.ktvcontroller.app.AppContext;
import com.jim.ktv.ktvcontroller.http.okHttp;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "ktvmain";
    private static final String SERVER_DEVISION = "HOSTIP";
    private static final int UDPPORT = 42574;


    private MainActivity instance = null;
    private DatagramSocket udpSocket = null;
    private ProgressDialog progressDialog = null;
    private Button connectBtn = null;
    private Button manualConnectBtn = null;
    private EditText serverIPEdit = null;
    private String ktvServerIP = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Remove title bar


        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        instance = this;
        // Init.
        int res = AppContext.initAtStartup(this);
        Log.v(LOG_TAG, "app start !");

        // find view
        findView();

        // set listener
        connectBtn.setOnClickListener(connectClick);
        manualConnectBtn.setOnClickListener(manualConnectClick);

        Log.v(LOG_TAG, "ktv server port = " + AppContext.ktvServerPort);


    }

    private void findView() {

        this.connectBtn = (Button) findViewById(R.id.connect);
        this.manualConnectBtn = (Button) findViewById(R.id.manualconnect);
        this.serverIPEdit = (EditText) findViewById(R.id.iptext);
    }

    private View.OnClickListener manualConnectClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String manualID = serverIPEdit.getText().toString();
            if (manualID == null) {
                Toast toast = Toast.makeText(
                        MainActivity.this,
                        getString(R.string.please_keyin_id), Toast.LENGTH_LONG);
                toast.show();
            } else if (manualID.equals("")) {
                Toast toast = Toast.makeText(
                        MainActivity.this,
                        getString(R.string.please_keyin_id), Toast.LENGTH_LONG);
                toast.show();
            } else {
                try {
                    showProgressDialog(getString(R.string.connecting));


                    //InetAddress address = getBroadcastAddress();
                    //String hostAddress = address.getHostAddress().toString();
                    //ktvServerIP = hostAddress.replace("255", manualID);
                    String privateIP = getLocalIpAddress();
                    String[] strbuf = privateIP.split("\\.");
                    ktvServerIP = strbuf[0] + "." + strbuf[1] + "." + strbuf[2] + "." + manualID;


                    AppContext ctx = AppContext.getInstance();
                    ctx.setKTVServerIP(ktvServerIP);

                    httpServerRequest();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "e = " + Log.getStackTraceString(e));
                }

            }
        }
    };


    private View.OnClickListener connectClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ktvServerIP = null;
            //String res = MainActivity.searchKTVServer();
            //Log.v(LOG_TAG, "search ktv server res = " + res);

            showProgressDialog(getString(R.string.connecting));

            // check after 5 seconds
            Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    instance.runOnUiThread(new Runnable() {
                        public void run() {
                            if (ktvServerIP != null)
                                return;
                            else {
//                                if (progressDialog != null)
//                                    if (progressDialog.isShowing())
//                                        progressDialog.dismiss();

//                               Toast toast = Toast.makeText(
//                                       MainActivity.this,
//                                       getString(R.string.usemanualconnect), Toast.LENGTH_LONG);
//                               toast.show();
                                blindRequestIP();
                            }
                        }
                    });
                }
            }, 5000);

            new Thread(new Runnable() {
                @Override
                public void run() {

                    boolean onlySendBroadCast = false;

                    try {
                        Log.v(LOG_TAG, "Send GETIP");
                        byte[] data = "GETIP".getBytes();
                        udpSocket = new DatagramSocket();
                        udpSocket.setBroadcast(true);
                        InetAddress address = getBroadcastAddress();
                        //InetAddress address = InetAddress.getByName("192.168.2.255");
                        DatagramPacket packet = new DatagramPacket(data, data.length, address, UDPPORT);
                        udpSocket.send(packet);
                    } catch (Exception e) {
                        Log.e(LOG_TAG, "e = " + Log.getStackTraceString(e));
                    }


                    // receive udp data
                    Log.v(LOG_TAG, "Server: Message received loop start ");
                    while (true) {
                        if (ktvServerIP != null)
                            break;
                        byte[] recbuf = new byte[255];
                        DatagramPacket recpacket = new DatagramPacket(recbuf,
                                recbuf.length);
                        try {
                            udpSocket.receive(recpacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String receivedMsg = new String(recpacket.getData());
                        Log.v(LOG_TAG, "Server: Message received: " + receivedMsg);

                        // pasing the ip
                        try {
                            ktvServerIP = receivedMsg.split(SERVER_DEVISION)[1];
                            Log.v(LOG_TAG, "Server: Message received ktv server ip =  " + ktvServerIP);
                            Log.v(LOG_TAG, "Server: Message received loop leave");

                            AppContext ctx = AppContext.getInstance();
                            ctx.setKTVServerIP(ktvServerIP);

                            udpSocket.close();
                            udpSocket = null;

                            httpServerRequest();
                            return;
                        } catch (Exception e) {
                            Log.e(LOG_TAG, "e = " + Log.getStackTraceString(e));
                        }

                    }
                }
            }).start();
        }
    };

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    private void blindRequestIP() {
        String privateIP = getLocalIpAddress();
        String[] strbuf = privateIP.split("\\.");

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (ktvServerIP != null)
                            return;
                        else {
                            if (progressDialog != null)
                                if (progressDialog.isShowing())
                                    progressDialog.dismiss();

                               Toast toast = Toast.makeText(
                                       MainActivity.this,
                                       getString(R.string.usemanualconnect), Toast.LENGTH_LONG);
                            toast.show();

                        }
                    }
                });
            }
        }, 30000);

        for (int i = 1; i < 255; i++) {
            final String ip = strbuf[0] + "." + strbuf[1] + "." + strbuf[2] + "." + i;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AppContext ctx = AppContext.getInstance();
                        okHttp okhttpClient = new okHttp();
                        String url;
                        url = "http://" + ip + ":" + AppContext.ktvServerPort + "/hello";
                        String response = okhttpClient.runGetSynch(url, null, null);

                        Log.v(LOG_TAG, "response = " + response);

                        if(response.indexOf("hello server") != -1)
                        {
                            ktvServerIP = ip;
                            Log.v(LOG_TAG, "ktvServerIP = " + ktvServerIP);
                            ctx.setKTVServerIP(ktvServerIP);

                            httpServerRequest();
                        }
                    } catch (Exception e) {
                        Log.v(LOG_TAG, "test failed ip = " + ip);

                    }

                }
            }).start();

        }

    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    System.out.println("ip1--:" + inetAddress);
                    System.out.println("ip2--:" + inetAddress.getHostAddress());

                    // for getting IPV4 format
                    // if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        String ip = inetAddress.getHostAddress().toString();
                        System.out.println("ip---::" + ip);

                        // return inetAddress.getHostAddress().toString();
                        return ip;
                    }
                }
            }
        } catch (Exception ex) {
            Log.e("IP Address", ex.toString());
        }
        return null;
    }

    private void showProgressDialog(String string) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setProgressStyle(0);

            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {

                }
            });

            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    //if(easyWifiThreadRunning)
                    //easyWifiThreadRunning = false;
                }
            });
        }
        if (progressDialog.isShowing())
            return;
        ;
        progressDialog.setMessage(string);
        progressDialog.show();
    }

    private void goToControllerAAO() {
        //AppContext ctx = AppContext.getInstance();
        //ctx.pingServer();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        Intent intent = new Intent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setClass(MainActivity.this, ControllerAAO.class);
                        startActivity(intent);
                    }
                });
            }
        });


    }

    private void httpServerRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AppContext ctx = AppContext.getInstance();
                    okHttp okhttpClient = new okHttp();
                    String serverIp = ctx.getKTVServerIP();

                    String url;
                    url = "http://" + serverIp + ":" + AppContext.ktvServerPort + "/hello";
                    String response = okhttpClient.runGetSynch(url, null, null);

                    Log.v(LOG_TAG, "response = " + response);

                    goToControllerAAO();

                } catch (Exception e) {
                    Log.v(LOG_TAG, "e = " + Log.getStackTraceString(e));

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            instance.runOnUiThread(new Runnable() {
                                public void run() {
                                    if (progressDialog != null)
                                        if (progressDialog.isShowing())
                                            progressDialog.dismiss();
                                    Toast toast = Toast.makeText(
                                            MainActivity.this,
                                            getString(R.string.connect_failed), Toast.LENGTH_LONG);
                                    toast.show();
                                }
                            });
                        }
                    });

                }

            }
        }).start();

    }

    // JNI
    public native static String searchKTVServer();

    static {
        System.loadLibrary("angel");
    }
}
