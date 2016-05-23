package com.example.android.vozie;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import com.braintreepayments.cardform.view.CardForm;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;
import com.daimajia.swipe.util.Attributes;

import java.util.ArrayList;
import java.util.List;


public class PaymentActivity extends ActionBarActivity {
    final private int VISA = 0;
    final private int MASTERCARD = 1;
    final private int DISCOVER = 2;
    final private int AMEX = 3;

    private ListView mListView;
    private List<PaymentItem> items;
    private ListViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mListView = (ListView) findViewById(R.id.main_list);
        items = new ArrayList<PaymentItem>();
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

        addPaymentItem(new PaymentItem("Shane Duffy", "04/18", "XXXX-XXXX-XXXX-0495", VISA));
        addPaymentItem(new PaymentItem("Ryan Duong", "02/15", "XXXX-XXXX-XXXX-7583", MASTERCARD));
        addPaymentItem(new PaymentItem("Chris Tran", "11/17", "XXXX-XXXX-XXXX-0383", DISCOVER));
        addPaymentItem(new PaymentItem("Jackie Hoang", "05/18", "XXXX-XXXX-XXXX-2837", AMEX));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment, menu);
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

    public void addPaymentItem(PaymentItem item) {
        items.add(item);

        mAdapter.size++;
        mListView.setAdapter(null);
        mListView.setAdapter(mAdapter);
    }

    public void showNewCardMenu() {

    }

    public class PaymentItem {
        public String name, date, card;
        int type;

        public PaymentItem(String name, String date, String card, int type) {
            this.name = name;
            this.date = date;
            this.card = card;
            this.type = type;
        }
    }

    public class ListViewAdapter extends BaseSwipeAdapter {
        private Context mContext;
        private List<PaymentItem> PaymentItems;
        private int size;

        public ListViewAdapter(Context mContext, List<PaymentItem> paymentItems) {
            this.mContext = mContext;
            this.PaymentItems = paymentItems;
            this.size = paymentItems.size() + 1;
        }

        @Override
        public int getSwipeLayoutResourceId(int position) {
            return R.id.swipe;
        }

        @Override
        public View generateView(int position, ViewGroup parent) {
            View v;

            if (position != size - 1) {
                v = LayoutInflater.from(mContext).inflate(R.layout.payment_item, parent, false);
                SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
                swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                    @Override
                    public void onOpen(SwipeLayout layout) {

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
            }
            else {
                v = LayoutInflater.from(mContext).inflate(R.layout.new_payment_item, parent, false);
                SwipeLayout swipeLayout = (SwipeLayout)v.findViewById(getSwipeLayoutResourceId(position));
                swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                    @Override
                    public void onOpen(SwipeLayout layout) {
                        showAddNewCardMenu();
                    }
                });
            }
            return v;
        }

        public void showAddNewCardMenu() {
            runOnUiThread(new Runnable(){
                public void run() {
                    showNewCardMenu();
                }
            });
        }

        @Override
        public void fillValues(int position, View convertView) {
            if (position != size - 1) {
                TextView name = (TextView) convertView.findViewById(R.id.name);
                TextView date = (TextView) convertView.findViewById(R.id.date);
                TextView cardNumber = (TextView) convertView.findViewById(R.id.card_number);
                ImageView cardImage = (ImageView) convertView.findViewById(R.id.card_image);

                name.setText(PaymentItems.get(position).name);
                date.setText(PaymentItems.get(position).date);
                cardNumber.setText(PaymentItems.get(position).card);

                switch (PaymentItems.get(position).type) {
                    case VISA:
                        cardImage.setImageResource(R.drawable.visa);
                        break;
                    case MASTERCARD:
                        cardImage.setImageResource(R.drawable.mastercard);
                        break;
                    case DISCOVER:
                        cardImage.setImageResource(R.drawable.discover);
                        break;
                    case AMEX:
                        cardImage.setImageResource(R.drawable.amex);
                        break;
                }
            }
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
    }
}
