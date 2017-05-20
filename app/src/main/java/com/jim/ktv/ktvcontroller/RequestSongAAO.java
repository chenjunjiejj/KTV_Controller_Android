package com.jim.ktv.ktvcontroller;


import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.NavigationView;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.jim.ktv.ktvcontroller.app.AppContext;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

public class RequestSongAAO extends AppCompatActivity {

    private static final String NAV_ITEM_ID = "nav_index";
    private static final String LOG_TAG = "ktvrequestsongaao";

    private static final int ANIM_SWING = 7;
    private static final int ANIM_BOUNCE_IN = 16;
    private static final int ANIM_BOUNCE_IN_UP = 20;
    private static final int ANIM_SLIDE_OUT_DOWN = 52;

    private static final int SEARCHPANEL_DOWN = 0;
    private static final int SEARCHPANEL_UP = 1;

    private static final String SINGERTYPE_MALE = "0";
    private static final String SINGERTYPE_FEMALE = "1";
    private static final String SINGERTYPE_CHORUS = "2";

    private static final String LANGUAGE_CHINESE = "0";
    private static final String LANGUAGE_TAIWANESE = "1";
    private static final String LANGUAGE_JAPAN = "2";
    private static final String LANGUAGE_ENGLISH = "3";
    private static final String LANGUAGE_YUEYU = "4";

    private static final String CHORUS_NO = "0";
    private static final String CHORUS_YES = "1";

    private static final int DRAWER_PAGE_GENERAL = 0;
    private static final int DRAWER_PAGE_SINGER = 1;
    private static final int DRAWER_PAGE_HOT = 2;
    private static final int DRAWER_PAGE_NEW = 3;
    private static final int DRAWER_PAGE_HISTORY = 4;

    private static final int SONG_STATUS_READY = 0;
    private static final int SONG_STATUS_PLAYING = 1;
    private static final int SONG_STATUS_PLAYED = 2;
    private static final int SONG_STATUS_REMOVED = 3;

    private static final int DEFAULT_POP_NUM = 50;
    private static final int DEFAULT_NEW_NUM = 50;

    private static final int NOT_SELECTED = -1;

    private static final int SET_TIME = 1500;
    private static final int SEARCH_TIME = 3000;


    private int drawer_page = DRAWER_PAGE_GENERAL;
    private int selectedPos = NOT_SELECTED;

    private RequestSongAAO instance = null;
    private ProgressDialog progressDialog = null;
    private WebSocketClient mWebSocketClient = null;

    DrawerLayout drawerLayout;

    private ListView searchListView = null;
    private ArrayList<ItemDPO> itemLists = new ArrayList<ItemDPO>();
    private ListAdapter listAdapters = null;

    private RelativeLayout generalRequestRal = null;
    private RelativeLayout singerRequestRal = null;


    private RelativeLayout generalRequestTriangleRal = null;
    private RelativeLayout singerRequestTriangleRal = null;

    private ImageView tringleImg_general = null;
    private ImageView tringleImg_singer = null;

    private RelativeLayout optionPanel = null;
    private RelativeLayout listviewRal = null;

    private TextView totalSearchNum = null;

    // general request

    private Button requestSongBtn = null;
    private Button insertSongBtn = null;
    private Button cancelBtn = null;
    private Button searchBtn = null;

    private EditText singerEdit = null;
    private EditText songnameEdit = null;
    private EditText songnamewordEdit = null;

    private CheckBox allSingerChb = null;
    private CheckBox maleSingerChb = null;
    private CheckBox femaleSingerChb = null;
    private CheckBox chorusSingerChb = null;

    private CheckBox allLanguageChb = null;
    private CheckBox chineseChb = null;
    private CheckBox taiwaneseChb = null;
    private CheckBox japanChb = null;
    private CheckBox englishChb = null;
    private CheckBox yueiyuChb = null;

    private CheckBox allChorusChb = null;
    private CheckBox yesChorusChb = null;
    private CheckBox noChorusChb = null;
    private int navItemId;

    // singer request
    private Button maleBtn = null;
    private Button femaleBtn = null;
    private Button chorusBtn = null;
    private Button searchSingerBtn = null;

    private EditText searchSingerEdit = null;


    private String selectedID = null;
    private String selectedKey = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        instance = this;

        setContentView(R.layout.requestsong_activiity);


        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView view = (NavigationView) findViewById(R.id.navigation_view);
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                Toast.makeText(RequestSongAAO.this, menuItem.getTitle(), Toast.LENGTH_LONG).show();
                navigateTo(menuItem);

                optionPanel.setVisibility(View.GONE);
                selectedPos = NOT_SELECTED;
                selectedID = null;
                selectedKey = null;
                resetListview();

                if (menuItem.getTitle().toString().equals(getString(R.string.nav1_general).toString())) {
                    drawer_page = DRAWER_PAGE_GENERAL;
                    generalRequestRal.setVisibility(View.VISIBLE);
                    singerRequestRal.setVisibility(View.INVISIBLE);
                    requestSongBtn.setText(getText(R.string.request_song).toString());
                    searchPanelLayout(generalRequestRal, tringleImg_general, SEARCHPANEL_UP);
                } else if (menuItem.getTitle().toString().equals(getString(R.string.nav2_singer).toString())) {
                    drawer_page = DRAWER_PAGE_SINGER;
                    generalRequestRal.setVisibility(View.INVISIBLE);
                    singerRequestRal.setVisibility(View.VISIBLE);
                    requestSongBtn.setText(getText(R.string.request_song).toString());
                    searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_UP);
                } else if (menuItem.getTitle().toString().equals(getString(R.string.nav3_hot).toString())) {
                    drawer_page = DRAWER_PAGE_HOT;
                    generalRequestRal.setVisibility(View.INVISIBLE);
                    singerRequestRal.setVisibility(View.INVISIBLE);
                    requestSongBtn.setText(getText(R.string.request_song).toString());

                    getPopSongList();

                } else if (menuItem.getTitle().toString().equals(getString(R.string.nav4_new).toString())) {
                    drawer_page = DRAWER_PAGE_NEW;
                    generalRequestRal.setVisibility(View.INVISIBLE);
                    singerRequestRal.setVisibility(View.INVISIBLE);
                    requestSongBtn.setText(getText(R.string.request_song).toString());

                    getNewSongList();

                } else if (menuItem.getTitle().toString().equals(getString(R.string.nav5_history).toString())) {
                    drawer_page = DRAWER_PAGE_HISTORY;
                    generalRequestRal.setVisibility(View.INVISIBLE);
                    singerRequestRal.setVisibility(View.INVISIBLE);
                    requestSongBtn.setText(getText(R.string.delete_song).toString());

                    getRequestSongList();

                } else
                    Toast.makeText(RequestSongAAO.this, getText(R.string.function_not_ready).toString(), Toast.LENGTH_LONG).show();

                //setListWeight(drawer_page);
                drawerLayout.closeDrawers();
                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        if (null != savedInstanceState) {
            navItemId = savedInstanceState.getInt(NAV_ITEM_ID, R.id.navigation_item_1);
        } else {
            navItemId = R.id.navigation_item_1;
        }

        navigateTo(view.getMenu().findItem(navItemId));

        findView();

        setListener();

        connectWebSocket();

    }

    @Override
    protected void onStart() {
        super.onStart();

        hideSoftKeyboard();
        layout();

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        disconnectWebSocket();

    }


    private void findView() {
        tringleImg_general = (ImageView) findViewById(R.id.tringleImg_general);
        tringleImg_singer = (ImageView) findViewById(R.id.tringleImg_singer);

        listviewRal = (RelativeLayout) findViewById(R.id.listview_rav);
        generalRequestRal = (RelativeLayout) findViewById(R.id.general_request);
        singerRequestRal = (RelativeLayout) findViewById(R.id.singer_request);
        optionPanel = (RelativeLayout) findViewById(R.id.option_rav);

        generalRequestTriangleRal = (RelativeLayout) findViewById(R.id.triangle_rav);
        singerRequestTriangleRal = (RelativeLayout) findViewById(R.id.triangle_rav_singer);


        requestSongBtn = (Button) findViewById(R.id.request_song);
        insertSongBtn = (Button) findViewById(R.id.insert_song);
        cancelBtn = (Button) findViewById(R.id.cancel);
        searchBtn = (Button) findViewById(R.id.search);

        searchListView = (ListView) findViewById(R.id.searchList);

        totalSearchNum = (TextView) findViewById(R.id.totalNum);

        songnameEdit = (EditText) findViewById(R.id.songname_edit);
        songnamewordEdit = (EditText) findViewById(R.id.songnameword_edit);
        singerEdit = (EditText) findViewById(R.id.singer_edit);

        allSingerChb = (CheckBox) findViewById(R.id.all_singer_check);
        maleSingerChb = (CheckBox) findViewById(R.id.male_singer_check);
        femaleSingerChb = (CheckBox) findViewById(R.id.female_singer_check);
        chorusSingerChb = (CheckBox) findViewById(R.id.chorus_singer_check);

        allLanguageChb = (CheckBox) findViewById(R.id.all_language_check);
        chineseChb = (CheckBox) findViewById(R.id.chinese_check);
        taiwaneseChb = (CheckBox) findViewById(R.id.taiwan_check);
        japanChb = (CheckBox) findViewById(R.id.japan_check);
        englishChb = (CheckBox) findViewById(R.id.english_check);
        yueiyuChb = (CheckBox) findViewById(R.id.yeiyu_check);

        allChorusChb = (CheckBox) findViewById(R.id.chorus_all_check);
        yesChorusChb = (CheckBox) findViewById(R.id.chorus_yes_check);
        noChorusChb = (CheckBox) findViewById(R.id.chorus_no_check);

        // singer request
        maleBtn = (Button) findViewById(R.id.male_btn);
        femaleBtn = (Button) findViewById(R.id.female_btn);
        chorusBtn = (Button) findViewById(R.id.chorus_btn);
        searchSingerBtn = (Button) findViewById(R.id.search_singer);

        searchSingerEdit = (EditText) findViewById(R.id.singer_search_edit);

    }

    private void layout() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

        int iconSize = screenWidth / 4;

        setButtonSize(maleBtn, iconSize);
        setButtonSize(femaleBtn, iconSize);
        setButtonSize(chorusBtn, iconSize);

        //  bottom hidden control panel
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                // general control panel
                ViewGroup.LayoutParams params_triangle = generalRequestTriangleRal.getLayoutParams();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) generalRequestRal.getLayoutParams();
                int height = generalRequestRal.getMeasuredHeight();

                int trangle_height = generalRequestTriangleRal.getMeasuredHeight();

                params.bottomMargin = -(height - trangle_height);
                generalRequestRal.setLayoutParams(params);


                // singer control panel
                params_triangle = singerRequestTriangleRal.getLayoutParams();
                params = (RelativeLayout.LayoutParams) singerRequestRal.getLayoutParams();
                height = singerRequestRal.getMeasuredHeight();

                trangle_height = singerRequestTriangleRal.getMeasuredHeight();

                params.bottomMargin = -(height - trangle_height);
                singerRequestRal.setLayoutParams(params);

                if(drawer_page == DRAWER_PAGE_GENERAL)
                    searchPanelLayout(generalRequestRal, tringleImg_general, SEARCHPANEL_UP);
                else if(drawer_page == DRAWER_PAGE_SINGER)
                    searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_UP);
            }
        }, 500);

    }


    private void setListener() {
        listAdapters = new ListAdapter();
        searchListView.setAdapter(listAdapters);

        SingertypeCheckListener scl = new SingertypeCheckListener();
        LanguageCheckListener lcl = new LanguageCheckListener();
        ChorusCheckListener ccl = new ChorusCheckListener();

        allSingerChb.setOnCheckedChangeListener(scl);
        maleSingerChb.setOnCheckedChangeListener(scl);
        femaleSingerChb.setOnCheckedChangeListener(scl);
        chorusSingerChb.setOnCheckedChangeListener(scl);

        allLanguageChb.setOnCheckedChangeListener(lcl);
        chineseChb.setOnCheckedChangeListener(lcl);
        taiwaneseChb.setOnCheckedChangeListener(lcl);
        japanChb.setOnCheckedChangeListener(lcl);
        englishChb.setOnCheckedChangeListener(lcl);
        yueiyuChb.setOnCheckedChangeListener(lcl);

        allChorusChb.setOnCheckedChangeListener(ccl);
        yesChorusChb.setOnCheckedChangeListener(ccl);
        noChorusChb.setOnCheckedChangeListener(ccl);

        searchBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                searchSong();

            }
        });


        requestSongBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                optionPanel.setVisibility(View.GONE);

                if (drawer_page == DRAWER_PAGE_HISTORY) {
                    if (selectedKey != null) {
                        deleteSongFromRequestList(selectedKey);
                        getRequestSongList();
                    }
                } else {
                    if (selectedID != null)
                        requestSong(selectedID);
                }
            }
        });

        insertSongBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                optionPanel.setVisibility(View.GONE);

                if (drawer_page == DRAWER_PAGE_HISTORY) {
                    if (selectedKey != null) {
                        deleteSongFromRequestList(selectedKey);
                        insertSong(selectedID);

                        showProgressDialog(getString(R.string.setting));

                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                instance.runOnUiThread(new Runnable() {
                                    public void run() {
                                        if (progressDialog != null)
                                            if (progressDialog.isShowing())
                                                progressDialog.dismiss();


                                        getRequestSongList();
                                    }
                                });
                            }
                        }, SET_TIME);

                    }

                } else {
                    if (selectedID != null)
                        insertSong(selectedID);
                }
            }
        });

        cancelBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                optionPanel.setVisibility(View.GONE);
            }
        });

        maleBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                searchSingerType(SINGERTYPE_MALE);
            }
        });

        femaleBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                searchSingerType(SINGERTYPE_FEMALE);
            }
        });

        chorusBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                searchSingerType(SINGERTYPE_CHORUS);
            }
        });

        searchSingerBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                searchSingerBtnClick();
            }
        });

        generalRequestTriangleRal.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) generalRequestRal.getLayoutParams();
                if (params.bottomMargin == 0)
                    searchPanelLayout(generalRequestRal, tringleImg_general, SEARCHPANEL_DOWN);
                else
                    searchPanelLayout(generalRequestRal, tringleImg_general, SEARCHPANEL_UP);
                //viewAnimate(ANIM_BOUNCE_IN_UP, generalRequestRal, 1.0f, 1.0f, View.VISIBLE, 200 );
            }
        });

        singerRequestTriangleRal.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) singerRequestRal.getLayoutParams();
                if (params.bottomMargin == 0)
                    searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_DOWN);
                else
                    searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_UP);
                //viewAnimate(ANIM_BOUNCE_IN_UP, generalRequestRal, 1.0f, 1.0f, View.VISIBLE, 200 );
            }
        });

    }

    private class SingertypeCheckListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            resetSingerType();
            buttonView.setChecked(isChecked);
        }
    }

    private class LanguageCheckListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            resetLanguage();
            buttonView.setChecked(isChecked);
        }
    }

    private class ChorusCheckListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            resetChorus();
            buttonView.setChecked(isChecked);
        }
    }

    private void setListWeight(int drawer_page) {
        if (drawer_page == DRAWER_PAGE_HOT || drawer_page == DRAWER_PAGE_NEW || drawer_page == DRAWER_PAGE_HISTORY) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listviewRal.getLayoutParams();
            params.weight = 1.0f;
            listviewRal.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listviewRal.getLayoutParams();
            params.weight = 0.7f;
            listviewRal.setLayoutParams(params);
        }
    }

    private void resetSingerType() {
        allSingerChb.setChecked(false);
        maleSingerChb.setChecked(false);
        femaleSingerChb.setChecked(false);
        chorusSingerChb.setChecked(false);
    }

    private void resetLanguage() {
        allLanguageChb.setChecked(false);
        chineseChb.setChecked(false);
        taiwaneseChb.setChecked(false);
        japanChb.setChecked(false);
        englishChb.setChecked(false);
        yueiyuChb.setChecked(false);
    }

    private void resetChorus() {
        allChorusChb.setChecked(false);
        yesChorusChb.setChecked(false);
        noChorusChb.setChecked(false);
    }

    private void resetListview() {
        itemLists.clear();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        totalSearchNum.setText(getString(R.string.total) + 0 + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();
                    }
                });
            }
        });
    }

    private class ListAdapter extends ArrayAdapter<ItemDPO> {
        ListAdapter() {
            super(RequestSongAAO.this, R.layout.song_list_item, itemLists);
        }


        @Override
        public View getView(int pos, View convert_view, ViewGroup parent) {
            View list_item_view = convert_view;

            if (list_item_view == null) {
                LayoutInflater inflater = getLayoutInflater();
                list_item_view = inflater.inflate(R.layout.song_list_item, parent, false);
//Log.d(getString(R.string.APP_NAME), "list_item_view=" + list_item_view);
            }

            ItemDPO item_dpo = (ItemDPO) searchListView.getAdapter().getItem(pos);
            ItemViewDPO item_view_dpo = (ItemViewDPO) list_item_view.getTag();

            if (drawer_page == DRAWER_PAGE_HISTORY) {
                if (pos == selectedPos)
                    list_item_view.setBackgroundResource(R.color.color_orange);
                else if (item_dpo.status == SONG_STATUS_PLAYED)
                    list_item_view.setBackgroundResource(R.color.color_light_grey);
                else if (item_dpo.status == SONG_STATUS_PLAYING)
                    list_item_view.setBackgroundResource(R.color.color_red);
                else if (item_dpo.status == SONG_STATUS_READY)
                    list_item_view.setBackgroundResource(R.color.color_white);
            } else {
                if (pos == selectedPos) {
                    // your color for selected item
                    list_item_view.setBackgroundResource(R.color.color_orange);
                } else {
                    // your color for non-selected item
                    list_item_view.setBackgroundResource(R.color.color_white);
                }
            }

            //Log.d(LOG_TAG, "getv: pos=" + pos + ", " + selectedItem + ", " + item_dpo.createTime + ", " + list_item_view + ", " + lastSelectedItem + ", " + item_view_dpo);
            //Log.d(LOG_TAG, "getv: fn=" + item_dpo.filename);
            if (item_view_dpo == null) {
                TextView songname = (TextView) list_item_view.findViewById(R.id.songname);
                songname.setText(item_dpo.songname);
                songname.setSelected(true);

                TextView singer = (TextView) list_item_view.findViewById(R.id.singer);
                singer.setText(item_dpo.singer);
                singer.setSelected(true);

                TextView singertype = (TextView) list_item_view.findViewById(R.id.singertype);
                singertype.setText(item_dpo.singertype);

                TextView language = (TextView) list_item_view.findViewById(R.id.language);
                language.setText(item_dpo.language);

                item_view_dpo = new ItemViewDPO(pos, songname, singer, singertype, language);
                list_item_view.setTag(item_view_dpo);


                list_item_view.setOnClickListener(onListItemClicked);

            } else {
                item_view_dpo.position = pos;
                item_view_dpo.songname.setText(item_dpo.songname);
                item_view_dpo.singer.setText(item_dpo.singer);
                item_view_dpo.singertype.setText(item_dpo.singertype);
                item_view_dpo.language.setText(item_dpo.language);
            }

            return list_item_view;
        }
    }

    private View.OnClickListener onListItemClicked = new View.OnClickListener() {
        public void onClick(View v) {

            ItemViewDPO iv_dpo = (ItemViewDPO) v.getTag();
            iv_dpo.songname.setSelected(true);
            iv_dpo.singer.setSelected(true);
            onListItemClick(RequestSongAAO.this.searchListView, v, iv_dpo.position, v.getId());
        }
    };

    protected void onListItemClick(ListView lv, View v, int pos, long id) {
        if (selectedPos == pos) {
            selectedPos = NOT_SELECTED;
        } else {
            selectedPos = pos;
        }

        listAdapters.notifyDataSetChanged();

        ItemDPO item_dpo = itemLists.get(pos);

        int ID = item_dpo.ID;
        if (ID > 0) {
            // search song
            optionPanel.setVisibility(View.VISIBLE);
            selectedID = String.valueOf(ID);
            selectedKey = item_dpo.key;
        } else {
            // search singer
            // we use the songname position to store the singer data
            String singer = item_dpo.songname;
            searchSongbySinger(singer);

        }
    }


    private class ItemDPO {
        int ID = 0;
        int status = -1;
        String key = null;
        String songname = null;
        String singer = null;
        String singertype = null;
        String language = null;


        ItemDPO(int new_ID, String new_songname, String new_singer, String new_singertype, String new_language, String new_key, int new_status) {
            ID = new_ID;
            songname = new_songname;
            singer = new_singer;
            singertype = new_singertype;
            language = new_language;
            key = new_key;
            status = new_status;
        }

    }

    private class ItemViewDPO {
        int position = 0;
        TextView songname = null;
        TextView singer = null;
        TextView singertype = null;
        TextView language = null;


        ItemViewDPO(int new_pos, TextView new_songname, TextView new_singer, TextView new_singertype, TextView new_language) {
            position = new_pos;
            songname = new_songname;
            singer = new_singer;
            singertype = new_singertype;
            language = new_language;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void navigateTo(MenuItem menuItem) {
        //contentView.setText(menuItem.getTitle());

        navItemId = menuItem.getItemId();
        menuItem.setChecked(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(NAV_ITEM_ID, navItemId);
    }

    private void setButtonSize(Button btn, int size) {
        ViewGroup.LayoutParams params = btn.getLayoutParams();
        params.width = size;
        params.height = size;
        btn.setLayoutParams(params);

    }

    private void searchPanelLayout(final RelativeLayout view, final ImageView triangleBtn, int upOrdown) {
        final int up_down = upOrdown;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {

                final int height = view.getMeasuredHeight();
                final int trangle_height = generalRequestTriangleRal.getMeasuredHeight();
                Animation am = null;
                switch (up_down) {
                    case SEARCHPANEL_DOWN:

                        am = new TranslateAnimation(0.0f, 0.0f, 0.0f, height - trangle_height);
                        am.setDuration(300);
                        am.setRepeatCount(0);
                        am.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {

                                triangleBtn.setAlpha(0.0f);

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        triangleBtn.setAlpha(1.0f);

                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                        params.bottomMargin = -(height - trangle_height);
                                        view.setLayoutParams(params);

                                        triangleBtn.setImageResource(R.drawable.triangleup);
                                    }
                                }, 0);


                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        view.startAnimation(am);

                        break;

                    case SEARCHPANEL_UP:

                        am = new TranslateAnimation(0.0f, 0.0f, 0.0f, -(height - trangle_height));
                        am.setDuration(300);
                        am.setRepeatCount(0);
                        am.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                triangleBtn.setAlpha(0.0f);

                                Handler handler = new Handler();
                                handler.postDelayed(new Runnable() {

                                    @Override
                                    public void run() {
                                        triangleBtn.setAlpha(1.0f);

                                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
                                        params.bottomMargin = 0;
                                        view.setLayoutParams(params);

                                        triangleBtn.setImageResource(R.drawable.triangledown);
                                    }
                                }, 0);
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {

                            }
                        });
                        view.startAnimation(am);

                        break;

                }
            }
        }, 500);

    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }

    private void viewAnimate(int animateType, View v, float scaleAfterAnim, float alphaAfterAnim, int visibleAfterAnim, long duration) {

//        if (rope != null) {
//            rope.stop(true);
//        }

        v.setVisibility(View.VISIBLE);
        v.setAlpha(alphaAfterAnim);

        final View view = v;
        final float scale = scaleAfterAnim;
        final float alpha = alphaAfterAnim;
        final int visible = visibleAfterAnim;

        Techniques technique = (Techniques) Techniques.values()[animateType];
        YoYo.YoYoString rope = YoYo.with(technique)
                .duration(duration)
                .pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT)
                .interpolate(new AccelerateDecelerateInterpolator())
                .withListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        view.setScaleX(scale);
                        view.setScaleY(scale);

                        view.setAlpha(alpha);
                        view.setVisibility(visible);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                        //Toast.makeText(LiveViewAAO.this, "canceled previous animation", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                })
                .playOn(v);

    }

    private void requestSong(String ID) {
        // websocket_send("playsong" + "|" + "standard" + "|" + ID);
        String msg = "playsong|standard|" + ID;
        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);
    }

    private void insertSong(String ID) {
        // websocket_send("playsong" + "|" + "insert" + "|" + ID);
        String msg = "playsong|insert|" + ID;
        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);
    }

    private void getPopSongList() {
        String msg = "getPopSong|" + DEFAULT_POP_NUM;

        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();
                    }
                });
            }
        }, SEARCH_TIME);
    }

    private void getNewSongList() {
        String msg = "getNewSong|" + DEFAULT_NEW_NUM;

        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();
                    }
                });
            }
        }, SEARCH_TIME);
    }

    private void getRequestSongList() {
        String msg = "getrequestsong";

        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();
                    }
                });
            }
        }, SEARCH_TIME);
    }

    private void searchSingerType(String singerType) {
        String searchInfo = "searchsingertype|" + singerType;

        if (mWebSocketClient != null)
            mWebSocketClient.send(searchInfo);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();

                        searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_DOWN);
                    }
                });
            }
        }, SEARCH_TIME);


    }

    private void deleteSongFromRequestList(String song_key) {
        String msg = "deletesong|" + song_key;

        if (mWebSocketClient != null)
            mWebSocketClient.send(msg);
    }


    private void searchSingerBtnClick() {
        String singer = searchSingerEdit.getText().toString();
        if (singer.equals("")) {
            Toast.makeText(RequestSongAAO.this, getText(R.string.search_singer_hint).toString(), Toast.LENGTH_LONG).show();
            return;
        }

        searchSongbySinger(singer);
    }

    private void searchSongbySinger(String singer) {
        String searchInfo = "searchsinger|" + singer;

        if (mWebSocketClient != null)
            mWebSocketClient.send(searchInfo);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();

                        searchPanelLayout(singerRequestRal, tringleImg_singer, SEARCHPANEL_DOWN);
                    }
                });
            }
        }, SEARCH_TIME);


    }

    private void searchSong() {
        //  // searchsong|SONGNAME:<value>|SONGNAMEWORD:<value>....
        // // SINGER,SINGERTYPE, SONGNAME, SONGNAMEWORD, PATH,LANGUAGE, CHORUS
        String searchInfo = "searchsong|";
        String songname = songnameEdit.getText().toString();
        String singer = singerEdit.getText().toString();
        String songnameword = songnamewordEdit.getText().toString();

        String singerType = null;
        if (maleSingerChb.isChecked())
            singerType = SINGERTYPE_MALE;
        else if (femaleSingerChb.isChecked())
            singerType = SINGERTYPE_FEMALE;
        else if (chorusSingerChb.isChecked())
            singerType = SINGERTYPE_CHORUS;

        String language = null;
        if (chineseChb.isChecked())
            language = LANGUAGE_CHINESE;
        else if (taiwaneseChb.isChecked())
            language = LANGUAGE_TAIWANESE;
        else if (japanChb.isChecked())
            language = LANGUAGE_JAPAN;
        else if (englishChb.isChecked())
            language = LANGUAGE_ENGLISH;
        else if (yueiyuChb.isChecked())
            language = LANGUAGE_YUEYU;

        String chorus = null;
        if (yesChorusChb.isChecked())
            chorus = CHORUS_YES;
        else if (noChorusChb.isChecked())
            chorus = CHORUS_NO;

        if (!songname.equals(""))
            searchInfo += ("SONGNAME:" + songname + "|");

        if (!songnameword.equals(""))
            searchInfo += ("SONGNAMEWORD:" + songnameword + "|");

        if (!singer.equals(""))
            searchInfo += ("SINGER:" + singer + "|");

        if (singerType != null)
            searchInfo += ("SINGERTYPE:" + singerType + "|");

        if (language != null)
            searchInfo += ("LANGUAGE:" + language + "|");

        if (chorus != null)
            searchInfo += ("CHORUS:" + chorus + "|");


        if (mWebSocketClient != null)
            mWebSocketClient.send(searchInfo);

        itemLists.clear();
        showProgressDialog(getString(R.string.searching));

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                instance.runOnUiThread(new Runnable() {
                    public void run() {
                        if (progressDialog != null)
                            if (progressDialog.isShowing())
                                progressDialog.dismiss();

                        totalSearchNum.setText(getString(R.string.total) + itemLists.size() + getString(R.string.Num));
                        selectedPos = NOT_SELECTED;
                        listAdapters.notifyDataSetChanged();

                        searchPanelLayout(generalRequestRal, tringleImg_general, SEARCHPANEL_DOWN);
                    }
                });
            }
        }, SEARCH_TIME);
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

                        if (message.indexOf("search_result") != -1) {
                            // get search result
//                            var new_ID = msg.split("|")[1];
//                            var new_songname = msg.split("|")[2];
//                            var new_songnameword = msg.split("|")[3];
//                            var new_singer = msg.split("|")[4];
//                            var new_singertype = msg.split("|")[5];
//                            var new_language = msg.split("|")[6];
//                            var new_chorus = msg.split("|")[7];
//                            var new_path = msg.split("|")[8];

                            try {
                                String[] strBuf = message.split("\\|");

                                String ID = strBuf[1];
                                String songname = strBuf[2];
                                String songnameword = strBuf[3];
                                String singer = strBuf[4];
                                String singertype = strBuf[5];
                                String language = strBuf[6];
                                String chorus = strBuf[7];
                                String hits = strBuf[8];

                                Log.v(LOG_TAG, "Search song ID = " + ID + " songname = " + songname + " songnameword = " + songnameword + " singer = " + singer + " singertype = " + singertype + " language = " + language + " chorus = " + chorus + " hits = " + hits);

                                String singerType = getString(R.string.male_singer);
                                switch (singertype) {
                                    case SINGERTYPE_MALE:
                                        singerType = getString(R.string.male_singer);
                                        break;

                                    case SINGERTYPE_FEMALE:
                                        singerType = getString(R.string.female_singer);
                                        break;

                                    case SINGERTYPE_CHORUS:
                                        singerType = getString(R.string.chorus_singer);
                                        break;
                                }

                                String language_str = getString(R.string.chinese);
                                switch (language) {
                                    case LANGUAGE_CHINESE:
                                        language_str = getString(R.string.chinese);
                                        break;
                                    case LANGUAGE_TAIWANESE:
                                        language_str = getString(R.string.taiwanese);
                                        break;
                                    case LANGUAGE_JAPAN:
                                        language_str = getString(R.string.japan);
                                        break;
                                    case LANGUAGE_ENGLISH:
                                        language_str = getString(R.string.english);
                                        break;
                                    case LANGUAGE_YUEYU:
                                        language_str = getString(R.string.yueiyu);
                                        break;
                                }

                                if (drawer_page == DRAWER_PAGE_HOT)
                                    songname = hits + " " + songname;
                                if (drawer_page == DRAWER_PAGE_NEW)
                                    songname = ID + " " + songname;

                                itemLists.add(new ItemDPO(Integer.valueOf(ID), songname, singer, singerType, language_str, "", -1));
                                // itemLists.add(new ItemDPO(0, "告白氣球", "周杰倫", "男", "國"));
                                //itemLists.add(new ItemDPO(1,"謝謝妳愛我", "謝和弦", "男" ,"台"));

                            } catch (Exception e) {
                                Log.e(LOG_TAG, "e = " + Log.getStackTraceString(e));
                            }
                        } else if (message.indexOf("search_singertype_result") != -1) {
                            String[] strBuf = message.split("\\|");

                            String singer = strBuf[1];
                            // NOTE : use the songname as singer
                            itemLists.add(new ItemDPO(-1, singer, "", "", "", "", -1));

                        } else if (message.indexOf("requestsong_result") != -1) {
                            try {
                                String[] strBuf = message.split("\\|");

                                String ID = strBuf[1];
                                String songname = strBuf[2];
                                String songnameword = strBuf[3];
                                String singer = strBuf[4];
                                String singertype = strBuf[5];
                                String language = strBuf[6];
                                String chorus = strBuf[7];
                                String status = strBuf[8];
                                String key = strBuf[9];
                                String hits = strBuf[10];

                                Log.v(LOG_TAG, "Search song ID = " + ID + " songname = " + songname + " songnameword = " + songnameword + " singer = " + singer + " singertype = " + singertype + " language = " + language + " chorus = " + chorus + " status = " + status + " key = " + key + " hits = " + hits);

                                String singerType = getString(R.string.male_singer);
                                switch (singertype) {
                                    case SINGERTYPE_MALE:
                                        singerType = getString(R.string.male_singer);
                                        break;

                                    case SINGERTYPE_FEMALE:
                                        singerType = getString(R.string.female_singer);
                                        break;

                                    case SINGERTYPE_CHORUS:
                                        singerType = getString(R.string.chorus_singer);
                                        break;
                                }

                                String language_str = getString(R.string.chinese);
                                switch (language) {
                                    case LANGUAGE_CHINESE:
                                        language_str = getString(R.string.chinese);
                                        break;
                                    case LANGUAGE_TAIWANESE:
                                        language_str = getString(R.string.taiwanese);
                                        break;
                                    case LANGUAGE_JAPAN:
                                        language_str = getString(R.string.japan);
                                        break;
                                    case LANGUAGE_ENGLISH:
                                        language_str = getString(R.string.english);
                                        break;
                                    case LANGUAGE_YUEYU:
                                        language_str = getString(R.string.yueiyu);
                                        break;
                                }

                                itemLists.add(new ItemDPO(Integer.valueOf(ID), songname, singer, singerType, language_str, key, Integer.valueOf(status)));
                                // itemLists.add(new ItemDPO(0, "告白氣球", "周杰倫", "男", "國"));
                                //itemLists.add(new ItemDPO(1,"謝謝妳愛我", "謝和弦", "男" ,"台"));

                            } catch (Exception e) {
                                Log.e(LOG_TAG, "e = " + Log.getStackTraceString(e));
                            }
                        } else if (message.indexOf("nowplay") != -1) {
                            if (drawer_page == DRAWER_PAGE_HISTORY) {
                                getRequestSongList();
                            }

                        }
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

    private void showProgressDialog(String string) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(RequestSongAAO.this);
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
