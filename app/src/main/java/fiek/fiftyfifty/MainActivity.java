package fiek.fiftyfifty;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import fiek.fiftyfifty.Klasat.HttpHandler;
import fiek.fiftyfifty.Klasat.Ngjarja;
import fiek.fiftyfifty.Klasat.Shoket;
import fiek.fiftyfifty.Klasat.Shpenzimi;
import fiek.fiftyfifty.Klasat.Utils;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private FirebaseUser user;
    private LinearLayout layEmpty;
    private TextView txtEmriMbiemriNav, txtEmailNav;
    private String userID, strEmail, strEmriMbiemri;

    private FirebaseDatabase database;
    private DatabaseReference dbRefNgjarjet, dbRefNgjarjetShpenzimet, dbRefNgjarjetShoket;
    private Query mQuery;

    private RecyclerView rcvNgjarjet;
    private FirebaseRecyclerAdapter<Ngjarja, NgjarjetHolder> firebaseRecyclerAdapter;

    private AlertDialog  dialogValutat;
    private ProgressDialog pDialog;

    private ListView listValutat;
    private JSONObject objValutat;
    private final String valutatLink = "http://data.fixer.io/api/latest?access_key=ef22431939d1bd7389f6c963ec871e2d&symbols=ALL,EUR,USD,GBP,MKD,RSD";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        layEmpty = findViewById(R.id.layEmpty); //empty view with list
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // toolbar-navbar


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);

        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);

        txtEmriMbiemriNav = headerView.findViewById(R.id.txtEmriMbiemriNav);
        txtEmailNav = headerView.findViewById(R.id.txtEmailNav);


        user = FirebaseAuth.getInstance().getCurrentUser();
        database = Utils.getDatabase();


        if (user != null)
        {
            userID = user.getUid();
            strEmail = user.getEmail();
            strEmriMbiemri = user.getDisplayName();
            txtEmailNav.setText(strEmail);
            txtEmriMbiemriNav.setText(strEmriMbiemri);
        }

        dbRefNgjarjet = database.getReference().child("ngjarjet").child(userID);
        dbRefNgjarjetShoket = database.getReference().child("ngjarjaShoket");
        dbRefNgjarjetShpenzimet = database.getReference().child("ngjarjaShpenzimet");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true);// stack from the top
        linearLayoutManager.setStackFromEnd(true);

        rcvNgjarjet = findViewById(R.id.rcvNgjarjet);
        rcvNgjarjet.setHasFixedSize(true);
        rcvNgjarjet.setLayoutManager(linearLayoutManager);

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mQuery = dbRefNgjarjet;

        mQuery.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) //datasnapshot for each added item on the list
            {
                if (dataSnapshot.hasChildren())
                {
                    rcvNgjarjet.setVisibility(View.VISIBLE);
                    layEmpty.setVisibility(View.GONE);
                } else
                {
                    rcvNgjarjet.setVisibility(View.GONE);
                    layEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        FirebaseRecyclerOptions<Ngjarja> options = new FirebaseRecyclerOptions.Builder<Ngjarja>()
                .setQuery(mQuery, Ngjarja.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Ngjarja, NgjarjetHolder>(options)
        {
            @Override
            public NgjarjetHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_ngjarja, parent, false); //fill the list with xml-layout

                return new NgjarjetHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull NgjarjetHolder holder, int position, @NonNull Ngjarja model)
            {
                final String ngjarjaID = getRef(position).getKey();
                final String titulliNgjarja = model.getTitulli() + ", " + model.getLokacioni();
                final String shpenzimet = String.valueOf(model.getShpenzimet());
                final String pershkrimi = "Regjistruar nga " + model.getRegEmriMbiemri() + " me " + Utils.getData(model.getDtRegjistrimit());
                final String valuta = model.getValuta();

                holder.setEmriNgjarjes(model.getTitulli());
                holder.setLokacioni(model.getLokacioni()); //data from database

                holder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(MainActivity.this, NgjarjaActivity.class);
                        intent.putExtra("ngjarjaID", ngjarjaID);
                        intent.putExtra("titulliNgjarja", titulliNgjarja);
                        intent.putExtra("shpenzimet", shpenzimet);
                        intent.putExtra("pershkrimi", pershkrimi);
                        intent.putExtra("valuta", valuta);
                        startActivity(intent);
                    }
                });

                holder.btnFshijeNgjarjen.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        dbRefNgjarjet.child(ngjarjaID).removeValue();
                        dbRefNgjarjetShoket.child(ngjarjaID).removeValue();
                        dbRefNgjarjetShpenzimet.child(ngjarjaID).removeValue();
                        showMsg("Ngjarja është fshirë me sukses.");
                    }
                });
            }
        };

        rcvNgjarjet.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_largohu)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        } else if (id == R.id.nav_valuta)
        {
            LayoutInflater inflater = getLayoutInflater();
            View viewDialog = inflater.inflate(R.layout.dialog_valutat, null);
            listValutat = viewDialog.findViewById(R.id.listValutat);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(viewDialog);
            alertDialogBuilder.setTitle("Valutat");
            alertDialogBuilder.setCancelable(true);

            new getValutat().execute();

            dialogValutat = alertDialogBuilder.create();
            dialogValutat.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1)
        {
            int kodi = 0;
            try
            {
                kodi = data.getIntExtra("regjistrimi", 0);
            } catch (Exception e)
            {
                //kur kthehet back nuk dergon data
            }

            if (kodi == 1)
            {
                showMsg("Regjistrimi i ngjarjes u krye me sukses.");
            }
        }
    }

    public void btnKrijoEvent(View view)
    {
        Intent intent = new Intent(this, KrijoEventActivity.class);
        startActivity(intent);
    }

    private void showMsg(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    public static class NgjarjetHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageButton btnFshijeNgjarjen;

        public NgjarjetHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            btnFshijeNgjarjen = mView.findViewById(R.id.btnFshijeNgjarjen);
        }

        public void setEmriNgjarjes(String emri)
        {
            TextView txtEmriNgjarjes = mView.findViewById(R.id.txtEmriNgjarjes);
            txtEmriNgjarjes.setText(emri);
        }

        public void setLokacioni(String emri)
        {
            TextView txtLokacioni = mView.findViewById(R.id.txtLokacioni);
            txtLokacioni.setText(emri);
        }
    }

    private class getValutat extends AsyncTask<Void, Void, Void>
    {
        String path = Environment.getExternalStorageDirectory() + File.separator + "cache" + File.separator;
        boolean error = false;

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Duke përditësuar listën e valutave, ju lutem prisni...");
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            HttpHandler sh = new HttpHandler();
            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(valutatLink);
            if (jsonStr != null)
            {
                try
                {
                    // Getting JSON Array node
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    objValutat = jsonObj.getJSONObject("rates");
                }
                catch (final JSONException e)
                {
                    error = true;
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), "Ka ndodhur një problem gjatë leximit të të dhënave.", Toast.LENGTH_LONG).show();
                            Log.i("test-valutat",e.getMessage());
                        }
                    });
                }
            }
            else
            {
                error = true;
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Toast.makeText(getApplicationContext(), "Lista nuk u përditësua pasi që ju nuk keni qasje në internet", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
            {
                pDialog.dismiss();
            }

            if (!error)
            {
                ruajCache();
                lexoJson();
            }
            else
            {
                //lexo nga te dhenat e ruajtura lokalisht
                JSONObject valutatCache = lexoCache();
                if(valutatCache != null)
                {
                    objValutat = valutatCache;
                    lexoJson();
                }
            }
        }

        private void lexoJson()
        {
            try
            {
                JSONArray arrayValutat = objValutat.names();
                List<String> list = new ArrayList<String>(arrayValutat.length());
                for (int i = 0; i < arrayValutat.length(); i++)
                {
                    String valuta = arrayValutat.getString(i);
                    list.add(valuta);
                }

                listValutat.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, list));
                listValutat.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                    {
                        SharedPreferences.Editor editor = getSharedPreferences("FIFTY-FIFTY", MODE_PRIVATE).edit();
                        editor.putString("valuta", listValutat.getItemAtPosition(position).toString());
                        editor.apply();
                        dialogValutat.hide();
                    }
                });

            }
            catch (Exception e) { e.printStackTrace();}
        }

        private void ruajCache()
        {
            SharedPreferences.Editor editor = getSharedPreferences("FIFTY-FIFTY", MODE_PRIVATE).edit();
            editor.putString("valutat",objValutat.toString());
            editor.apply();
        }

        private JSONObject lexoCache()
        {
            SharedPreferences prefs = getSharedPreferences("FIFTY-FIFTY", MODE_PRIVATE);

            try
            {
                String strValutat = prefs.getString("valutat","");
                JSONObject jsonObj = new JSONObject(strValutat);
                return jsonObj;

            } catch (Exception e)
            {
                Log.i("test-valutat",e.getMessage());
                return null;
            }
        }
    }

}
