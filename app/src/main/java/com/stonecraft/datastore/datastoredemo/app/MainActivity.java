package com.stonecraft.datastore.datastoredemo.app;

import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stonecraft.database.datastoredemo.app.R;
import com.stonecraft.datastore.AggregateQuery;
import com.stonecraft.datastore.Datastore;
import com.stonecraft.datastore.DatastoreTransaction;
import com.stonecraft.datastore.DbDataLoader;
import com.stonecraft.datastore.OnQueryComplete;
import com.stonecraft.datastore.RSData;
import com.stonecraft.datastore.RowCountQuery;
import com.stonecraft.datastore.exceptions.CannotCompleteException;
import com.stonecraft.datastore.exceptions.DatabaseException;
import com.stonecraft.datastore.interaction.Insert;
import com.stonecraft.datastore.interaction.Join;
import com.stonecraft.datastore.interaction.Query;
import com.stonecraft.datastore.interfaces.OnNonQueryComplete;
import com.stonecraft.datastore.logs.ConnectionLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Shortlist[]>{

    private static final String DB_NAME = "Domain";
    private TextView myTxbStatus;
    private ContentObserver myContentObserver = new ContentObserver(new Handler()) {
        @Override
        public boolean deliverSelfNotifications() {
            return super.deliverSelfNotifications();
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            Log.d("TEST", "onChange()");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportLoaderManager().initLoader(0, null, this);
//        populateTables();
        myTxbStatus = (TextView)findViewById(R.id.txbStatus);

        Button btnClose = (Button)findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                close();
            }
        });
        Button btnReconnect = (Button)findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                reconnect();
            }
        });
        Button btnCheck = (Button)findViewById(R.id.btnCheck);
        btnCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                check();
            }
        });

        Button btnInsert = (Button)findViewById(R.id.btnInsert);
        btnInsert.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                insert();
//				testConcurrency();
            }
        });

        Button btnQueryDataMap = (Button)findViewById(R.id.btnQueryDataMap);
        btnQueryDataMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                query();
            }
        });

        Button btnCopyDB = (Button)findViewById(R.id.btnCopyDB);
        btnCopyDB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                copyDB();
            }
        });

        Button btnPhoto = (Button)findViewById(R.id.btnPhoto);
        btnPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addPhoto();
            }
        });

        Button btnDisplay = (Button)findViewById(R.id.btnDisplay);
        btnDisplay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayPhoto();
            }
        });

        Button btnJoinTest = (Button)findViewById(R.id.btnJoinTest);
        btnJoinTest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                testJoins();
            }
        });

        Button btnTestLogs = (Button)findViewById(R.id.btnTestLogs);
        btnTestLogs.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                testLogs();
            }
        });

//        Uri uri = Datastore.getDataStore(DB_NAME).getTableUri("SHORT_LIST");
//        getContentResolver().registerContentObserver(uri, true, myContentObserver);
    }

    private void testLogs() {
        List<ConnectionLog> logs = Datastore.getConnectionLogs();
        for(ConnectionLog log: logs) {
            Log.d("TEST", log.toString());
        }
    }

    /* (non-Javadoc)
	 * @see android.com.stonecraft.datastoredemo.Activity#onDestroy()
	 */
    @Override
    protected void onDestroy() {
        try
        {
            Datastore.closeAll();
        }catch(DatabaseException e)
        {
            Log.e("DB connection/creation", "Failed to close Database [" + e + "]");
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri selectedImageUri = data.getData();
        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(selectedImageUri);
            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

            Map<String, Object> row = new HashMap<>();
            row.put("PHOTO_DATA", selectedImage);
            Insert insert = new Insert("PHOTO_GALLERY", row);

            Datastore.getDataStore(DB_NAME).executeNonQuery(1, insert, new OnNonQueryComplete() {
                @Override
                public void onNonQueryComplete(int token, int updated) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            myTxbStatus.setText("Photo added Successfully");
                        }
                    });
                }

                @Override
                public void onNonQueryFailed(int token, DatabaseException e) {

                }
            });



        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void copyDB() {
        File dbOuputLocation = new File(Environment.getExternalStorageDirectory(), DB_NAME);
        Log.d("TEST", "Coping Databaes to " + dbOuputLocation.getAbsolutePath());
        Log.i(MainActivity.class.getSimpleName(), "DB will be copied to " +
                dbOuputLocation.getAbsolutePath());

        try {
            AndroidFileUtils.copyDatabaseFrom(this, dbOuputLocation, DB_NAME);

            myTxbStatus.setText("DB Copied");
        } catch (CannotCompleteException e) {
            Log.e("TEST copyDB", "" + e);
            myTxbStatus.setText("DB Copy failed");
        }
    }

    private void query() {
        Datastore ds = Datastore.getDataStore(DB_NAME);
        long millis = Calendar.getInstance().getTimeInMillis();
        Log.d("PERFORMANCETEST",
                "Query performance test start");
        final long start = Calendar.getInstance().getTimeInMillis();
        Query query = new Query("SHORT_LIST");
        Shortlist[] resultSet = new Shortlist[0];
        try {
            resultSet = ds.executeQuery(query, Shortlist.class);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        Log.d("PERFORMANCETEST",
                "Query of " + resultSet.length + " records took " +
                        (Calendar.getInstance().getTimeInMillis() - start));
        Log.d("TEST", "Time to query = " + (Calendar.getInstance().getTimeInMillis() - millis));
    }

    private void displayPhoto() {
        Query query = new Query("PHOTO_GALLERY");
//            query.orderBy("IMAGE_ID desc");

        Datastore ds = Datastore.getDataStore(DB_NAME);
        ds.executeQuery(0, query, new OnQueryComplete<Bitmap>() {

            @Override
            public Bitmap[] parseData(RSData resultSet) {
                resultSet.moveToFirst();
                if (!resultSet.isAfterLast()) {
                    try {
                        byte[] imageData = resultSet.getBlobData("PHOTO_DATA");
                        resultSet.close();
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0,
                                imageData.length);
                        return new Bitmap[]{bitmap};
                    } catch (DatabaseException ex) {
                        Log.e("TEST", Log.getStackTraceString(ex));
                    }
                }

                return null;
            }

            @Override
            public void onQueryComplete(int token, final Bitmap[] items) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView imgDisplay = (ImageView) findViewById(R.id.imgPhoto);
                        imgDisplay.setImageBitmap(items[0]);
                        items[0].recycle();
                    }
                });
            }

            @Override
            public void onQueryFailed(int token, DatabaseException e) {

            }
        });
    }

    private void addPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
    }

    private void connect() {
        try {
            Datastore.createConnection(getApplication(), getAssets().open("database.xml"), null);
        } catch(IOException e)
        {
            Log.e("DB connection/creation", "Failed to get database xml [" + e + "]");
        } catch (DatabaseException e) {
            Log.e("DB connection/creation", "Failed to create database [" + e + "]");
        }
    }

    private void close() {
        try
        {
            Datastore.getDataStore(DB_NAME).close();
            myTxbStatus.setText("Close successful");
        }
        catch(DatabaseException e)
        {
            Log.e("DB connection/creation", "Failed to close Database [" + e + "]");
            myTxbStatus.setText("Close unsuccessful");
        }
    }

    private void reconnect() {
        connect();
        myTxbStatus.setText("reconnect successful");
    }

    private void check() {
        Datastore ds = Datastore.getDataStore(DB_NAME);
        if(ds != null && ds.isConnectionAvail()) {
            myTxbStatus.setText("Connected");
        }
        else{
            myTxbStatus.setText("Connection closed");
        }
    }

    private void testJoins() {

        Query query = new Query("SHORT_LIST");
        Join join = new Join("SHORT_LIST_JOIN", Join.JOIN_CROSS);
        join.addJoinExpression(new Pair<String, String>("SHORT_LIST", "PROPERTY_ID"),
            new Pair<String, String>("SHORT_LIST_JOIN", "PROPERTY_ID"));
        query.addJoins(join);

        Datastore ds = Datastore.getDataStore(DB_NAME);
        ds.executeQuery(0, query, new OnQueryComplete<Shortlist>() {

            @Override
            public void onQueryComplete(int token, Shortlist[] resultSet) {
                Shortlist list = null;
                if(resultSet != null) {
                    for(Shortlist shortlist : resultSet) {

                        Log.d("testJoins", "join id " + shortlist.getPropertyID());
                        for(ShortlistJoin join : shortlist.getShortlistJoins()) {
                            Log.d("testJoins", "\tShortlist id " + join.getShortListPropertyID() +
                                    " join id " + join.getShortListId() + " postcode " +
                                    join.getShortListPostcode());
                        }
                    }
                }
            }

            @Override
            public void onQueryFailed(int token, DatabaseException e) {
                Log.e("testJoins", "join query failed " + e);
            }
        });
    }

    private void populateTables() {
        AggregateQuery query = new RowCountQuery("SHORT_LIST");
        long count = 0;
        try {
            count = (long) Datastore.getDataStore(DB_NAME).executeAggregateQuery(query);

            if(count == 0) {
                DatastoreTransaction dt = new DatastoreTransaction();
                Shortlist shortList = new Shortlist();
                shortList.setPropertyID(0);
                shortList.setIsFavourite(true);
                shortList.setPostcode(2000);
                dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));
                shortList = new Shortlist();
                shortList.setPropertyID(1);
                shortList.setIsFavourite(true);
                shortList.setPostcode(2001);
                dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));
                shortList = new Shortlist();
                shortList.setPropertyID(2);
                shortList.setIsFavourite(true);
                shortList.setPostcode(2002);
                dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));
                shortList = new Shortlist();
                shortList.setPropertyID(3);
                shortList.setIsFavourite(true);
                shortList.setPostcode(2003);
                dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));
                shortList = new Shortlist();
                shortList.setPropertyID(4);
                shortList.setIsFavourite(true);
                shortList.setPostcode(2004);
                dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));

                ShortlistJoin shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(0);
                shortlistJoin.setShortListPropertyID(0);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(1);
                shortlistJoin.setShortListPropertyID(0);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(2);
                shortlistJoin.setShortListPropertyID(0);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(3);
                shortlistJoin.setShortListPropertyID(1);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(4);
                shortlistJoin.setShortListPropertyID(2);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(5);
                shortlistJoin.setShortListPropertyID(2);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(6);
                shortlistJoin.setShortListPropertyID(2);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));
                shortlistJoin = new ShortlistJoin();
                shortlistJoin.setShortListId(7);
                shortlistJoin.setShortListPropertyID(3);
                shortlistJoin.setShortListPostcode(2000);
                dt.addStatement(new Insert<ShortlistJoin>("SHORT_LIST_JOIN", shortlistJoin));

                dt.execute(Datastore.getDataStore(DB_NAME), new OnNonQueryComplete() {
                    @Override
                    public void onNonQueryComplete(int token, int updated) {

                    }

                    @Override
                    public void onNonQueryFailed(int token, DatabaseException e) {

                    }
                });

                dt = new DatastoreTransaction();
                Map<String,Object> values;
                for(int i = 0; i < 10000; i++) {
                    shortList = new Shortlist();
                    shortList.setIsFavourite(true);
                    shortList.setPostcode(2000);
                    dt.addStatement(new Insert<Shortlist>("SHORT_LIST", shortList));
                }

                Log.d("PERFORMANCETEST", "Start populating tables");
                final long start = Calendar.getInstance().getTimeInMillis();
                dt.execute(Datastore.getDataStore(DB_NAME), new OnNonQueryComplete() {
                    @Override
                    public void onNonQueryComplete(int token, int updated) {
                        Log.d("PERFORMANCETEST", "Insert 10,000 records time taken = " +
                                (Calendar.getInstance().getTimeInMillis() - start));
                    }

                    @Override
                    public void onNonQueryFailed(int token, DatabaseException e) {
                        Log.d("PERFORMANCETEST", "Failed populate table test");
                    }
                });
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

    }

    private void insert() {

        long millis = Calendar.getInstance().getTimeInMillis();
        Datastore ds = Datastore.getDataStore(DB_NAME);
        DatastoreTransaction dt = new DatastoreTransaction();

        for(int i = 0; i < 25000; i++) {
            Shortlist shortlist = new Shortlist();
            shortlist.setIsFavourite(true);
            shortlist.setAddress("This is the property address");
            shortlist.setPostcode(2079);
            Insert<Shortlist> insert = new Insert("SHORT_LIST", shortlist);
            dt.addStatement(insert);
        }

        try {
            dt.executeOnCurrentThread(ds);
            Log.d("TEST", "Time to save = " + (Calendar.getInstance().getTimeInMillis() - millis));
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

    }

    final Handler returnHandler = new Handler(new Handler.Callback() {
        int count = 0;
        @Override
        public boolean handleMessage(Message msg) {
            count++;
            Log.d("TEST", "handler count = " + count);
            if(count >= 100) {
                query();
            }
            return false;
        }
    });

    private void testConcurrency() {
        for(int i = 0; i < 100; i++) {
            final AtomicInteger extra = new AtomicInteger();
            extra.set(i);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    Datastore ds = Datastore.getDataStore(DB_NAME);
                    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
                    for(int i = 0; i < 100; i++) {
                        Map<String, Object> values = new HashMap<String, Object>();
                        values.put("IS_FAVOURITE", 1);
                        values.put("PROPERTY_POSTCODE", (2000 + i + extra.get()));
                        rows.add(values);
                    }
                    Insert insert = new Insert("SHORT_LIST", rows);
                    ds.executeNonQuery(1, insert, null);
                    query();
                    Log.d("TEST", "Completed iteration " + extra.get());

                    returnHandler.sendEmptyMessage(1);
                }
            }).start();
        }
    }

    @Override
    public Loader<Shortlist[]> onCreateLoader(int id, Bundle args) {
        Query query = new Query("SHORT_LIST");
        return new DbDataLoader<Shortlist[]>(this, DB_NAME, query, Shortlist.class);
    }

    @Override
    public void onLoadFinished(Loader<Shortlist[]> loader, Shortlist[] data) {
        Log.d("TEST", "Injected data count = " + data.length);
    }

    @Override
    public void onLoaderReset(Loader<Shortlist[]> loader) {
    }

}
