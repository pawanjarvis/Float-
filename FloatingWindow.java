package com.example.multiwindowapp;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

public class FloatingWindow {
    private Context context;
    private WindowManager windowManager;
    private View floatingView;
    private WindowManager.LayoutParams params;
    private String windowId;
    private boolean isMinimized = false;
    private List<FloatingWindow> floatingWindows;
    private String windowTitle;
    
    public FloatingWindow(Context context, String contentUrl, List<FloatingWindow> windowsList, String title) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        this.floatingWindows = windowsList;
        this.windowTitle = title;
        
        initView(contentUrl);
    }
    
    private void initView(String contentUrl) {
        LayoutInflater inflater = LayoutInflater.from(context);
        floatingView = inflater.inflate(R.layout.floating_window_layout, null);
        
        // विंडो पैरामीटर्स
        params = new WindowManager.LayoutParams(
            800,  // width
            600,  // height
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        );
        
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;
        
        setupWindowControls();
        loadContent(contentUrl);
        
        windowManager.addView(floatingView, params);
    }
    
    private void setupWindowControls() {
        // Title सेट करें
        TextView titleView = floatingView.findViewById(R.id.window_title);
        titleView.setText(windowTitle);
        
        ImageButton btnClose = floatingView.findViewById(R.id.btn_close);
        ImageButton btnMinimize = floatingView.findViewById(R.id.btn_minimize);
        LinearLayout header = floatingView.findViewById(R.id.header);
        
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
        
        btnMinimize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMinimize();
            }
        });
        
        // ड्रैग फंक्शनलिटी
        header.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(floatingView, params);
                        return true;
                }
                return false;
            }
        });
    }
    
    private void loadContent(String contentUrl) {
        WebView webView = floatingView.findViewById(R.id.content_view);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(contentUrl);
    }
    
    public void toggleMinimize() {
        isMinimized = !isMinimized;
        View content = floatingView.findViewById(R.id.content_view);
        
        if (isMinimized) {
            content.setVisibility(View.GONE);
            params.width = 400;
            params.height = 80;
        } else {
            content.setVisibility(View.VISIBLE);
            params.width = 800;
            params.height = 600;
        }
        
        windowManager.updateViewLayout(floatingView, params);
    }
    
    public void close() {
        try {
            if (floatingView != null && windowManager != null) {
                windowManager.removeView(floatingView);
            }
            if (floatingWindows != null) {
                floatingWindows.remove(this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}