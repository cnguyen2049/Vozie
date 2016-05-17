package com.example.android.vozie;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;
import java.util.List;


public class NotificationsActivity extends Activity {
    private ListView mListView;
    private ListViewAdapter mAdapter;
    private Context mContext = this;
    private Handler mHandler;
    private List<Thread> flashThreads;
    private List<TextView> threadID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notifications);

    mHandler = new Handler();
    mListView = (ListView) findViewById(R.id.main_list);

    List<NotificationsItem> items = new ArrayList<NotificationsItem>();
    flashThreads = new ArrayList<Thread>();
    threadID = new ArrayList<TextView>();

    items.add(new NotificationsItem("Your trip from 3002 Ironside Ct, San Jose CA to 7576 Juniper Lane has concluded", 1));
    items.add(new NotificationsItem("This is an alert lol", 2));
    items.add(new NotificationsItem("text3", 3));
    items.add(new NotificationsItem("text4", 4));
    items.add(new NotificationsItem("text5", 5));

    mAdapter = new ListViewAdapter(this, items);
    mListView.setAdapter(mAdapter);

    mAdapter.setMode(Attributes.Mode.Single);
    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ((SwipeLayout) (mListView.getChildAt(position - mListView.getFirstVisiblePosition()))).open(true);
        }
    });
    mListView.setOnTouchListener(new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.e("ListView", "OnTouch");

            return false;
        }
    });
    mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

            return true;
        }
    });
    mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            Log.e("ListView", "onScrollStateChanged");
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        }
    });

    mListView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.e("ListView", "onItemSelected:" + position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            Log.e("ListView", "onNothingSelected:");
        }});
    }

    public void addNotification(List<NotificationsItem> list, NotificationsItem item, ListViewAdapter adapter) {
        list.add(item);

        adapter.size++;
        mListView.setAdapter(null);
        mListView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
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

    public class NotificationsItem {
        public String itemsText;
        public int itemsType;

        public NotificationsItem(String text, int type) {
            this.itemsText = text;
            this.itemsType = type;
        }
    }

    public class ListViewAdapter extends BaseSwipeAdapter {
        private final int TRIP_CONFIRMED = 1;
        private final int TRIP_COMPLETED = 2;
        private final int PAYMENT_ADDED = 3;
        private final int PAYMENT_RECEIVED = 4;
        private final int SUB_ADDED = 5;
        private final int DRIVER_ARRIVED = 6;

        private Context mContext;
        private List<NotificationsItem> notificationsItems;
        private int size;
        private String title;

        public ListViewAdapter(Context mContext, List<NotificationsItem> notifications) {
            this.mContext = mContext;
            this.notificationsItems = notifications;
            this.size = notifications.size();
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe;
        }

        @Override
        public View generateView(int position, ViewGroup parent) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.trip_confirmed_notification, parent, false);
            LinearLayout topPanel = (LinearLayout) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.top_panel);
            TextView titleView = (TextView) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.title);

            switch (notificationsItems.get(position).itemsType) {
                case TRIP_CONFIRMED:
                    title = "Trip Confirmed";
                    setLayoutColor(topPanel, Color.parseColor("#FFB9604F"));
                    flashComponent(titleView);
                    break;
                case TRIP_COMPLETED:
                    title = "Journey Completed";
                    setLayoutColor(topPanel, Color.parseColor("#FFC693A7"));
                    break;
                case PAYMENT_ADDED:
                    title = "New Payment Method";
                    setLayoutColor(topPanel, Color.parseColor("#FFD06764"));
                    break;
                case PAYMENT_RECEIVED:
                    title = "Payment Received";
                    setLayoutColor(topPanel, Color.parseColor("#FFD0754F"));
                    break;
                case SUB_ADDED:
                    title = "New Subscription Added";
                    setLayoutColor(topPanel, Color.parseColor("#FFD0546E"));
                    break;
                case DRIVER_ARRIVED:
                    title = "Your Driver Has Arrived!";
                    setLayoutColor(topPanel, Color.parseColor("#FFD06F66"));
                    break;
                default:
                    title = "Notification";
                    break;
            }

            SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {
                    YoYo.with(Techniques.Pulse).duration(500).delay(100).playOn(layout.findViewById(R.id.trip));
                }
            });
            swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                @Override
                public void onDoubleClick(SwipeLayout layout, boolean surface) {

                }
            });
            v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            return v;
        }

        public void flashComponent(final TextView v) {
            final int MAX_RADIUS = 4;
            final int INTERVAL = 500;

            flashThreads.add(new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                if (v.getTag() == null)
                                    v.setTag(true);

                                float rad = v.getShadowRadius();
                                boolean tag = (boolean) v.getTag();

                                if (tag)
                                    v.setShadowLayer(rad + 1, 2, 2, Color.parseColor("#FFFFFF"));
                                else if (!tag)
                                    v.setShadowLayer(rad - 1, 2, 2, Color.parseColor("#FFFFFF"));
                                if (rad == MAX_RADIUS)
                                    v.setTag(false);
                                else if (rad == 1)
                                    v.setTag(true);
                            }
                        });
                        try { Thread.currentThread().sleep(INTERVAL); } catch (Exception e) {}
                    }
                }
            }));

            flashThreads.get(0).start();
        }
        @Override
        public void fillValues(int position, View convertView) {
            TextView t2 = (TextView)convertView.findViewById(R.id.title);
            TextView t = (TextView)convertView.findViewById(R.id.description);

            t.setText(notificationsItems.get(position).itemsText);
            t2.setText(title);
        }

        @Override
        public int getCount() {
            return size;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void setLayoutColor(final LinearLayout l, final int c) {
            mHandler.post(new Runnable() {
                public void run() {
                    l.setBackgroundColor(c);
                }
            });
        }
    }
}
