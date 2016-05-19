package com.example.android.vozie;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
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
    private final int TRIP_CONFIRMED = 1;
    private final int TRIP_COMPLETED = 2;
    private final int PAYMENT_ADDED = 3;
    private final int PAYMENT_RECEIVED = 4;
    private final int MILES_ADDED = 5;
    private final int DRIVER_ARRIVED = 6;

    private ListView mListView;
    private ListViewAdapter mAdapter;
    private List<NotificationsItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notifications);

    mListView = (ListView) findViewById(R.id.main_list);
    items = new ArrayList<NotificationsItem>();

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
        }
    });


        // Test Notifications
        addNotification(new NotificationsItem("Trip from 3002 Ironside Ct. to 2764 Rainview Drive" +
                " has been confirmed. Your driver is en route!", "05/07", "6:45 PM", TRIP_CONFIRMED));
        addNotification(new NotificationsItem("Trip from 3002 Ironside Ct. to 2764 Rainview Drive" +
                " has been completed!", "05/09", "1:58 PM", TRIP_COMPLETED));
        addNotification(new NotificationsItem("New credit card ending in 0764 added to payment methods.", "05/09", "3:45 PM", PAYMENT_ADDED));
        addNotification(new NotificationsItem("Your payment of $50.99 has been received. Miles will be " +
                "credited to your account shortly.", "05/10", "7:35 PM", PAYMENT_RECEIVED));
        addNotification(new NotificationsItem("50 miles have been credited to your account!", "05/06", "2:37 PM", MILES_ADDED));
        addNotification(new NotificationsItem("Your chauffeur has arrived! If you have specific instructions" +
                " or need help, feel free to call or text them.", "05/10", "4:58 PM", DRIVER_ARRIVED));
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

    public void addNotification(NotificationsItem item) {
        items.add(item);

        mAdapter.size++;
        mListView.setAdapter(null);
        mListView.setAdapter(mAdapter);
    }

    public class NotificationsItem {
        public String itemsText, time, date;
        public int itemsType;

        public NotificationsItem(String text,String time, String date, int type) {
            this.itemsText = text;
            this.time = time;
            this.date = date;
            this.itemsType = type;
        }
    }

    public class ListViewAdapter extends BaseSwipeAdapter {
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
            final int WIDTH = 85; // In DP units
            final int PADDING = 20;
            final int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WIDTH, getResources().getDisplayMetrics()); // Converted to pixels
            final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PADDING, getResources().getDisplayMetrics());

            View v = LayoutInflater.from(mContext).inflate(R.layout.notification, parent, false);
            LinearLayout topPanel = (LinearLayout) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.top_panel);
            LinearLayout bottomPanel = (LinearLayout) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.bottom_panel);
            TextView time = (TextView) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.time);
            TextView date = (TextView) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.date);
            TextView titleTxt = (TextView) v.findViewById(getSwipeLayoutResourceId(position)).findViewById(R.id.title);

            time.setText(notificationsItems.get(position).time);
            date.setText(notificationsItems.get(position).date);

            switch (notificationsItems.get(position).itemsType) {
                case TRIP_CONFIRMED:
                    title = "Trip Confirmed";
                    topPanel.setBackgroundColor(Color.parseColor("#FFB9604F"));

                    ImageView tripButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, tripButton, R.drawable.trip, padding, width, Color.parseColor("#71dbb8"));
                    break;
                case TRIP_COMPLETED:
                    title = "Trip Completed";
                    topPanel.setBackgroundColor(Color.parseColor("#FFC693A7"));

                    ImageView historyButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, historyButton, R.drawable.history, padding, width, Color.parseColor("#ffa152"));
                    ImageView facebookButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, facebookButton, R.drawable.facebook, padding, width, Color.parseColor("#ADD8E6"));
                    break;
                case PAYMENT_ADDED:
                    title = "New Payment Method";
                    topPanel.setBackgroundColor(Color.parseColor("#FFD06764"));

                    ImageView paymentButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, paymentButton, R.drawable.payment, padding, width, Color.parseColor("#83D489"));
                    break;
                case PAYMENT_RECEIVED:
                    title = "Payment Received";
                    topPanel.setBackgroundColor(Color.parseColor("#FFD0754F"));
                    break;
                case MILES_ADDED:
                    title = "Miles Credited To Account";
                    topPanel.setBackgroundColor(Color.parseColor("#FFD0546E"));

                    ImageView milesButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, milesButton, R.drawable.miles, padding, width, Color.parseColor("#e4a6f0"));
                    break;
                case DRIVER_ARRIVED:
                    title = "Driver Has Arrived";
                    topPanel.setBackgroundColor(Color.parseColor("#FFD06F66"));

                    ImageView phoneButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, phoneButton, R.drawable.phone, padding, width, Color.parseColor("#7198db"));
                    ImageView textButton = new ImageView(parent.getContext());
                    addSwipeButton(bottomPanel, textButton, R.drawable.text, padding, width, Color.parseColor("#f0848f"));
                    break;
                default:
                    title = "Notification";
                    break;
            }

            ImageView deleteButton = new ImageView(parent.getContext());
            addSwipeButton(bottomPanel, deleteButton, R.drawable.delete, padding, width, Color.parseColor("#DFDFDF"));

            SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                @Override
                public void onOpen(SwipeLayout layout) {
                    //YoYo.with(Techniques.Pulse).duration(500).delay(100).playOn(layout.findViewById(R.id.));
                }
            });
            swipeLayout.setOnDoubleClickListener(new SwipeLayout.DoubleClickListener() {
                @Override
                public void onDoubleClick(SwipeLayout layout, boolean surface) {

                }
            });
            /*v.findViewById(R.id.delete_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });*/
            return v;
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

        public void addSwipeButton(LinearLayout panel, ImageView image, int resource, int padding, int width, int background) {
            image.setImageResource(resource);
            image.setPadding(padding, padding, padding, padding);
            image.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT));
            image.setBackgroundColor(background);

            panel.addView(image);
        }
    }
}
