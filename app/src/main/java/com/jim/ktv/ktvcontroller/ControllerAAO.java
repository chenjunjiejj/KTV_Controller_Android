package com.jim.ktv.ktvcontroller;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jim.ktv.ktvcontroller.app.AppContext;
import com.jim.ktv.ktvcontroller.http.okHttp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ControllerAAO extends AppCompatActivity {

    private static final String LOG_TAG = "ktvcontrolleraao";

    private int audioSource = MediaRecorder.AudioSource.MIC;
    private int samplingRate = 8000;//44100; /* in Hz*/
    private int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int bufferSize = 2048;//AudioRecord.getMinBufferSize(samplingRate, channelConfig, audioFormat);
    private int sampleNumBits = 16;
    private int numChannels = 1;

    private boolean isRecording = false;

    private ControllerAAO instance = null;
    private ProgressDialog progressDialog = null;
    private WebSocketClient mWebSocketClient = null;
    private WebView ktvView = null;
    private Button showImageBtn = null;
    private Button pauseBtn = null;
    private Button cutBtn = null;
    private Button originalSingBtn = null;
    private Button musicOnlyBtn = null;
    private Button helpSingBtn = null;
    private Button downKeyBtn = null;
    private Button originalKeyBtn = null;
    private Button upKeyBtn = null;
    private Button terminateBtn = null;
    private Button iwantSingBtn = null;
    private Button requestSongBtn = null;

    private int audioIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Remove title bar


        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.control_activiity);
        instance = this;

        // find view
        findView();

        // set Listener
        setListener();

        connectWebSocket();


    }

    @Override
    protected void onStart() {
        super.onStart();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int iconSize = screenWidth / 5;


        setButtonSize(showImageBtn, iconSize);
        setButtonSize(pauseBtn, iconSize);
        setButtonSize(cutBtn, iconSize);
        setButtonSize(originalSingBtn, iconSize);
        setButtonSize(helpSingBtn, iconSize);
        setButtonSize(musicOnlyBtn, iconSize);
        setButtonSize(downKeyBtn, iconSize);
        setButtonSize(upKeyBtn, iconSize);
        setButtonSize(originalKeyBtn, iconSize);
    }

    @Override
    protected void onPause() {
        super.onPause();

        isRecording = false;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        disconnectWebSocket();

    }


    private void findView() {
        ktvView = (WebView) findViewById(R.id.webView);
        WebChromeClient web_view_cli = new WebChromeClient();

        ktvView.setWebChromeClient(web_view_cli);
        ktvView.getSettings().setJavaScriptEnabled(true);
        ktvView.getSettings().setLoadWithOverviewMode(true);
        ktvView.getSettings().setBuiltInZoomControls(false);
        ktvView.getSettings().setSupportZoom(false);

        ktvView.loadUrl("file:///android_asset/dog.html");

        terminateBtn = (Button) findViewById(R.id.terminateBtn);
        showImageBtn = (Button) findViewById(R.id.showimage);
        pauseBtn = (Button) findViewById(R.id.pause);
        cutBtn = (Button) findViewById(R.id.cut);
        originalSingBtn = (Button) findViewById(R.id.originalsing);
        musicOnlyBtn = (Button) findViewById(R.id.musiconly);
        helpSingBtn = (Button) findViewById(R.id.helpsing);
        originalKeyBtn = (Button) findViewById(R.id.originalkey);
        downKeyBtn = (Button) findViewById(R.id.downkey);
        upKeyBtn = (Button) findViewById(R.id.upkey);
        iwantSingBtn = (Button) findViewById(R.id.iwantsing);
        requestSongBtn = (Button) findViewById(R.id.request_song);
    }

    private void setListener() {
        Button.OnClickListener showImage_lstnr = new ShowImageListener();
        showImageBtn.setOnClickListener(showImage_lstnr);

        Button.OnClickListener PauseStart_lstnr = new PauseStartListener();
        pauseBtn.setOnClickListener(PauseStart_lstnr);

        cutBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                cut();
            }
        });

        originalSingBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                originalsing();
            }
        });

        musicOnlyBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                musiconly();
                ;
            }
        });

        helpSingBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                helpsing();
            }
        });

        originalKeyBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                originalkey();
            }
        });

        downKeyBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                downkey();
                ;
            }
        });

        upKeyBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                upkey();
            }
        });

        terminateBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                onClickViewSelected(v);
                showTerminateHint();
            }
        });

        iwantSingBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                v.setAlpha(0.7f);

                final View icon = v;

                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        instance.runOnUiThread(new Runnable() {
                            public void run() {
                                icon.setAlpha(1.0f);
                            }
                        });
                    }
                }, 200);

                if (isRecording) {
                    iwantSingBtn.setText(getText(R.string.iwantsing).toString());
                    isRecording = false;
                } else {
                    iwantSingBtn.setText(getText(R.string.idonotwantsing).toString());
                    openMicrophone();
                }
            }
        });

        requestSongBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setClass(ControllerAAO.this, RequestSongAAO.class);
                startActivity(intent);

            }
        });
    }

    private void onClickViewSelected(View v) {
        v.setAlpha(0.7f);

        final View icon = v;

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        icon.setAlpha(1.0f);
                    }
                });
            }
        }, 200);

    }


    private class ShowImageListener implements Button.OnClickListener {
        private boolean show = false;

        ShowImageListener() {

        }

        @Override
        public void onClick(View v) {

            onClickViewSelected(v);

            if (show) {
                showImageBtn.setBackgroundResource(R.drawable.showimage);
                ktvView.clearHistory();
                ktvView.clearCache(true);
                ktvView.loadUrl("file:///android_asset/dog.html");
                show = false;
            } else {
                AppContext ctx = AppContext.getInstance();
                ktvView.clearHistory();
                ktvView.clearCache(true);
                ktvView.loadUrl("file:///android_asset/ktv.html?ip=" + ctx.getKTVServerIP());
                showImageBtn.setBackgroundResource(R.drawable.closeimage);
                show = true;
            }
        }
    }

    private class PauseStartListener implements Button.OnClickListener {
        private boolean show = false;

        PauseStartListener() {

        }

        @Override
        public void onClick(View v) {

            onClickViewSelected(v);

            if (show) {
                pauseBtn.setBackgroundResource(R.drawable.pause);
                start();
                show = false;
            } else {
                pauseBtn.setBackgroundResource(R.drawable.start);
                pause();
                show = true;
            }
        }
    }

    private void openMicrophone() {
        new Thread(new Runnable() {
            @Override
            public void run() {

                byte data[] = new byte[bufferSize];
                AudioRecord recorder = new AudioRecord(audioSource, samplingRate, channelConfig, audioFormat, bufferSize);
                recorder.startRecording();
                isRecording = true;


                //capture data and record to file
                int readBytes = 0, writtenBytes = 0;
                do {
                    readBytes = recorder.read(data, 0, bufferSize);

                    mWebSocketClient.send(data);
                    Log.v(LOG_TAG, "readByte = " + readBytes + " audioIndex = " + audioIndex++);
                    Log.v(LOG_TAG, "" + data[0] + ", " + data[1] + ", " + data[2] + ", " + data[3] + ", " + data[4] + ", " + data[5] + ", " + data[6]);
                }
                while (isRecording);

                recorder.stop();
                recorder.release();

            }
        }).start();

    }

    private void setButtonSize(Button btn, int size) {
        ViewGroup.LayoutParams params = btn.getLayoutParams();
        params.width = size;
        params.height = size;
        btn.setLayoutParams(params);

    }

    private void connectWebSocket() {
        URI uri;
        try {
            AppContext ctx = AppContext.getInstance();
            String websocketServer = "ws://" + ctx.getKTVServerIP() + ":" + AppContext.ktvServerPort;
            uri = new URI(websocketServer);
        } catch (Exception e) {
            Log.v(LOG_TAG, "e = " + Log.getStackTraceString(e));
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");

            }

            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v(LOG_TAG, "on message = " + message);
                    }
                });
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    private void disconnectWebSocket() {
        if (mWebSocketClient != null) {
            mWebSocketClient.close();
            mWebSocketClient = null;
        }
    }

    private void originalsing() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("volume" + "|" + "original");
    }

    private void musiconly() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("volume" + "|" + "musiconly");
    }

    private void helpsing() {
//        Toast toast = Toast.makeText(
//                ControllerAAO.this,
//                getString(R.string.not_support_now), Toast.LENGTH_LONG);
//        toast.show();
        if (mWebSocketClient != null)
            mWebSocketClient.send("volume" + "|" + "helpsing");
    }

    private void pause() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("control" + "|" + "pause");
    }

    private void start() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("control" + "|" + "start");
    }

    private void cut() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("control" + "|" + "cut");
    }


    private void downkey() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("pitch" + "|" + "downkey");
    }

    private void originalkey() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("pitch" + "|" + "originalkey");
    }

    private void upkey() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("pitch" + "|" + "upkey");
    }

    private void showTerminateHint() {
        AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
        localBuilder.setMessage(getText(R.string.terminate_hint).toString());
        final AlertDialog dialog = localBuilder.setPositiveButton(getText(R.string.ok).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {

                paramAnonymousDialogInterface.dismiss();
                terminamteKTVServer();
            }
        }).setNegativeButton(getText(R.string.cancel).toString(), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface paramAnonymousDialogInterface, int paramAnonymousInt) {
                paramAnonymousDialogInterface.dismiss();
            }
        }).create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.color_orange));
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.color_orange));
            }
        });

        dialog.show();
    }


    private void terminamteKTVServer() {
        if (mWebSocketClient != null)
            mWebSocketClient.send("terminate");

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finishAffinity();
            }
        }, 500);
    }

    private void showProgressDialog(String string) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(ControllerAAO.this);
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
}
