package com.journaldev.navigationviewexpandablelistview;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    WebView webView;
    ProgressBar progressBarWeb;
    ProgressDialog progressDialog;
    RelativeLayout relativeLayout;
    Button btnNoInternetConnection;
    SwipeRefreshLayout swipeRefreshLayout;
    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();
    private String weburl = "http://mitmysore.in/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        progressBarWeb = findViewById(R.id.ProgressBar);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Please Wait...");
        btnNoInternetConnection = findViewById(R.id.btnNoConnection);
        relativeLayout = findViewById(R.id.relativeLayout);


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setColorSchemeColors(Color.BLUE, Color.YELLOW, Color.GREEN);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.reload();
            }
        });


        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setDomStorageEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            checkConnection();

        }


        //solved Swipeup Problem

        webView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageFinished(WebView view, String url) {

                view.scrollTo(0, 0);


                swipeRefreshLayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                progressBarWeb.setVisibility(View.VISIBLE);
                progressBarWeb.setProgress(newProgress);
                setTitle("Loading...");
                progressDialog.show();

                if (newProgress == 100) {
                    progressBarWeb.setVisibility(View.GONE);
                    setTitle(view.getTitle());
                    progressDialog.dismiss();
                }

                super.onProgressChanged(view, newProgress);
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadManager.Request myRequest = new DownloadManager.Request(Uri.parse(url));
                myRequest.allowScanningByMediaScanner();
                myRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager mymanager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                mymanager.enqueue(myRequest);

                Toast.makeText(MainActivity.this, "Your file is Downloading....", Toast.LENGTH_SHORT).show();

            }
        });


        btnNoInternetConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkConnection();

            }
        });


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        expandableListView = findViewById(R.id.expandableListView);
        prepareMenuData();
        populateExpandableList();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void checkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected()) {


            webView.loadUrl(weburl);

            webView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

        } else if (mobileNetwork.isConnected()) {
            webView.loadUrl(weburl);
            webView.setVisibility(View.VISIBLE);
            relativeLayout.setVisibility(View.GONE);

        } else {

            webView.setVisibility(View.GONE);
            relativeLayout.setVisibility(View.VISIBLE);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (webView.canGoBack()) {
            webView.goBack();

        }
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you Sure you want to Exit?")
                    .setNegativeButton("No", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            finishAffinity();
                        }
                    }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {

            case R.id.nav_previous:
                onBackPressed();
                break;

            case R.id.nav_next:
                if (webView.canGoForward()) {
                    webView.goForward();
                }
                break;


            case R.id.nav_refresh:
                checkConnection();
                break;

        }


        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void prepareMenuData() {
        MenuModel menuModel = new MenuModel("Home", true, false, "http://mitmysore.in/"); //Menu of Android Tutorial. No sub menus
        headerList.add(menuModel);

        if (!menuModel.hasChildren) {
            childList.put(menuModel, null);
        }

        menuModel = new MenuModel("ABOUT", true, true, ""); //Menu of About
        headerList.add(menuModel);
        List<MenuModel> childModelsList = new ArrayList<>();
        MenuModel childModel = new MenuModel("MITM", false, false, "http://mitmysore.in/why-to-join-mitm/");
        childModelsList.add(childModel);

        childModel = new MenuModel("VISION & MISSION", false, false, "http://mitmysore.in/vision-mission/");
        childModelsList.add(childModel);

        childModel = new MenuModel("MET", false, false, "http://mitmysore.in/management/");
        childModelsList.add(childModel);
        childModel = new MenuModel("GOVERNING COUNCIL", false, false, "http://mitmysore.in/governing-council/");
        childModelsList.add(childModel);
        childModel = new MenuModel("PRINCIPAL", false, false, "http://mitmysore.in/principal-message/");
        childModelsList.add(childModel);
        childModel = new MenuModel("FROM PRESIDENTS DESK", false, false, "http://mitmysore.in/president-message-3/");
        childModelsList.add(childModel);
        childModel = new MenuModel("FROM SECRETARY DESK", false, false, "http://mitmysore.in/secretary-message-2/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            Log.d("API123", "here");
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("ACCADEMICS", true, true, ""); //Menu of ACCADEMICS
        headerList.add(menuModel);
        childModel = new MenuModel("UNDER GRADUATE", false, false, "http://mitmysore.in/undergraduates-2/");
        childModelsList.add(childModel);

        childModel = new MenuModel("POST GRADUATE", false, false, "http://mitmysore.in/postgraduates/");
        childModelsList.add(childModel);
        childModel = new MenuModel("BASIC SCIENCE AND HUMANITIES", false, false, "http://mitmysore.in/basic-science/");
        childModelsList.add(childModel);
        childModel = new MenuModel("PLACEMENT AND TRAINING", false, false, "http://mitmysore.in/placement-training/");
        childModelsList.add(childModel);
        childModel = new MenuModel("LIBRARY AND INFORMATION CENTER", false, false, "http://mitmysore.in/library2/");
        childModelsList.add(childModel);
        childModel = new MenuModel("RESEARCH CENTER", false, false, "http://mitmysore.in/rd/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("ADMINISTRATION", true, true, ""); //Menu of ADMINISTRATION
        headerList.add(menuModel);
        childModel = new MenuModel("MISSION", false, false, "http://mitmysore.in/mission/");
        childModelsList.add(childModel);

        childModel = new MenuModel("MINISTERIAL SECTIONS", true, false, "");
        childModelsList.add(childModel);
        childModel = new MenuModel("ADMISSIONS", false, false, "http://mitmysore.in/admission/");
        childModelsList.add(childModel);
        childModel = new MenuModel("ACCOUNTS", false, false, "http://mitmysore.in/accounts/");
        childModelsList.add(childModel);
        childModel = new MenuModel("EXAMINATION", false, false, "http://mitmysore.in/examination/");
        childModelsList.add(childModel);
        childModel = new MenuModel("ESTABLISHMENT", false, false, "http://mitmysore.in/examination/");
        childModelsList.add(childModel);
        childModel = new MenuModel("MINISTERIAL STAFF", false, false, "http://mitmysore.in/ministerial-staff/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("ADMISSIONS", true, true, ""); //Menu of ACCADEMICS
        headerList.add(menuModel);
        childModel = new MenuModel("UNDER GRADUATE", false, false, "http://mitmysore.in/undergraduates-2/");
        childModelsList.add(childModel);

        childModel = new MenuModel("POST GRADUATE", false, false, "http://mitmysore.in/postgraduates/");
        childModelsList.add(childModel);
        childModel = new MenuModel("BASIC SCIENCE AND HUMANITIES", false, false, "http://mitmysore.in/basic-science/");
        childModelsList.add(childModel);
        childModel = new MenuModel("PLACEMENT AND TRAINING", false, false, "http://mitmysore.in/placement-training/");
        childModelsList.add(childModel);
        childModel = new MenuModel("LIBRARY AND INFORMATION CENTER", false, false, "http://mitmysore.in/library2/");
        childModelsList.add(childModel);
        childModel = new MenuModel("RESEARCH CENTER", false, false, "http://mitmysore.in/rd/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("COMMITTEES", true, true, ""); //Menu of Commitees
        headerList.add(menuModel);
        childModel = new MenuModel("STATUATORY COMMITTES", false, true, "");
        childModelsList.add(childModel);
        childModel = new MenuModel("GRIVANCE REDRESSAL CELL", false, false, "http://mitmysore.in/grievance-redressal-cell/");
        childModelsList.add(childModel);
        childModel = new MenuModel("ANTI RANGING CELL", false, false, "http://mitmysore.in/anti-ragging-cell/");
        childModelsList.add(childModel);
        childModel = new MenuModel("INTERNAL COMPLAINT/ANTI-SEXUAL CELL", false, false, "http://mitmysore.in/anti-sexual-harassment-cell/");
        childModelsList.add(childModel);
        childModel = new MenuModel("SC/ST WELFARE CELL", false, false, "http://mitmysore.in/sc-st-welfare-cell/");
        childModelsList.add(childModel);
        childModel = new MenuModel("SWATCH BHARATH MISSION CELL", false, false, "http://mitmysore.in/swachh-bharath-mission-cell/");
        childModelsList.add(childModel);

        childModel = new MenuModel("COLLEGE  COMMITTES", false, false, "http://mitmysore.in/institute-level-committees/");
        childModelsList.add(childModel);
        childModel = new MenuModel("E-WASTE MANAGEMENT CELL", false, false, "http://mitmysore.in/downloads/pdfdownload/E-WASTE-MANAGEMENT-CELL.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("IQAC", false, false, "");
        childModelsList.add(childModel);
        childModel = new MenuModel("LIBRARY COMMITTEE", false, false, "http://mitmysore.in/downloads/pdfdownload/LIBRARYCOMMITTEE-MEMBERS-2019.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("MEDIA CELL", false, false, "http://mitmysore.in/downloads/pdfdownload/Media-Cell.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("NSS COMMITTEE", false, false, "http://mitmysore.in/downloads/pdfdownload/NSS.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("CULTURAL COMMITTEE", false, false, "http://mitmysore.in/downloads/pdfdownload/CULTURAL-COMMITTEE.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("SPORTS COMMITTES", false, false, "http://mitmysore.in/downloads/pdfdownload/SPORTS-COMMITTEE.pdf");
        childModelsList.add(childModel);
        childModel = new MenuModel("CANTEEN COMMITTES", false, false, "http://mitmysore.in/downloads/pdfdownload/CANTEEN-COMMITTEE.pdf");
        childModelsList.add(childModel);

        childModel = new MenuModel("SECRETARIAL  COMMITTEE", false, false, "http://mitmysore.in/secretarial-committee/");
        childModelsList.add(childModel);
        childModel = new MenuModel("SCMC", false, false, "http://mitmysore.in/scmc/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("CAMPUS LIFE", true, true, ""); //Menu of campus life
        headerList.add(menuModel);
        childModel = new MenuModel("TRANSPORTATION", false, false, "http://mitmysore.in/transportation/");
        childModelsList.add(childModel);

        childModel = new MenuModel("HOSTEL", false, false, "http://mitmysore.in/hostel-2/");
        childModelsList.add(childModel);
        childModel = new MenuModel("SPORTS", false, false, "http://mitmysore.in/sports1/");
        childModelsList.add(childModel);
        childModel = new MenuModel("PROFESIONAL SOCIETY", false, false, "http://mitmysore.in/professional-society/");
        childModelsList.add(childModel);
        childModel = new MenuModel("CAFETERIA", false, false, "http://mitmysore.in/cafeteria/");
        childModelsList.add(childModel);
        childModel = new MenuModel("INTERNET", false, false, "http://mitmysore.in/internet-2/");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("NSS", true, false, "http://mitmysore.in/nss/"); //Menu of ACCADEMICS
        headerList.add(menuModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("GALLERY", true, false, "http://mitmysore.in/gallery-2/"); //Menu of gallery
        headerList.add(menuModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }
        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("CONTACT US", true, false, "http://mitmysore.in/contact-us/"); //Menu of contact us
        headerList.add(menuModel);

        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        childModelsList = new ArrayList<>();
        menuModel = new MenuModel("Infotainment", true, true, ""); //Menu of Infotainment
        headerList.add(menuModel);

        childModel = new MenuModel("Videos", true, false, "https://www.youtube.com/channel/UCJ7cn5h3cXoBcnruYQLtfxw/playlists");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


    }

    private void populateExpandableList() {

        expandableListAdapter = new ExpandableListAdapter(this, headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        WebView webView = findViewById(R.id.webView);
                        webView.loadUrl(headerList.get(groupPosition).url);
                        onBackPressed();
                    }
                }

                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    if (model.url.length() > 0) {
                        WebView webView = findViewById(R.id.webView);
                        webView.loadUrl(model.url);
                        onBackPressed();
                    }
                }

                return false;
            }
        });
    }
}
