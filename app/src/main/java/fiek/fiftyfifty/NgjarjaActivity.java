package fiek.fiftyfifty;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fiek.fiftyfifty.Klasat.Ngjarja;
import fiek.fiftyfifty.Klasat.Shoket;
import fiek.fiftyfifty.Klasat.Shpenzimi;
import fiek.fiftyfifty.Klasat.Utils;

public class NgjarjaActivity extends AppCompatActivity
{
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRefNgjarja, dbRefNgjarjaShoket, dbRefNgjarjaShpenzimet;

    private String userID, emriMbiemri;

    private double _shpenzimetEventit;
    private TextView txtEmriLokacioni, txtShpenzimet, txtPershkrimi;
    private String ngjarjaID, titulliNgjarja, shpenzimet, pershkrimi, valuta;
    private String emriShoku, emailShoku;
    private EditText txtEmriShoku, txtEmailShoku;
    private EditText txtArsyeja, txtSasia;
    private Spinner spnShoket;

    private Shoket objShoket;
    private Shpenzimi objShpenzimi;

    private RecyclerView rcvShoket, rcvShpenzimet;
    private FirebaseRecyclerAdapter<Shoket,ShokuHolder> firebaseRecyclerAdapter;
    private FirebaseRecyclerAdapter<Shpenzimi,ShpenzimetHolder> firebaseRecyclerAdapterShpenzimet;
    private HashMap<String, Shoket> hmShoket = new HashMap<String, Shoket>();
    private HashMap<String, Shoket> hmShoketKryesore = new HashMap<String, Shoket>();

    private AlertDialog dialogShpenzimet;

    private boolean konvertoValuten = true;
    private String valutaZgjedhur;
    private JSONObject objValutat;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ngjarja);

        txtEmriLokacioni = findViewById(R.id.txtEmriLokacioni);
        txtShpenzimet = findViewById(R.id.txtShpenzimet);
        txtPershkrimi = findViewById(R.id.txtPershkrimi);

        /* Get values from Intent */
        Intent intent = getIntent();
        ngjarjaID  = intent.getStringExtra("ngjarjaID");
        titulliNgjarja  = intent.getStringExtra("titulliNgjarja");
        shpenzimet  = intent.getStringExtra("shpenzimet");
        pershkrimi  = intent.getStringExtra("pershkrimi");
        valuta = intent.getStringExtra("valuta");

        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            database = Utils.getDatabase();
            userID = user.getUid();
            emriMbiemri = user.getDisplayName();
        }

        SharedPreferences prefs = getSharedPreferences("FIFTY-FIFTY", MODE_PRIVATE);


        try
        {
            valutaZgjedhur = prefs.getString("valuta","");
            objValutat = getValutat();
        } catch (Exception e) { konvertoValuten = false; }


        txtEmriLokacioni.setText(titulliNgjarja);

        txtShpenzimet.setText(konvertoVleren(Double.parseDouble(shpenzimet)) + " të shpenzuara");
        txtPershkrimi.setText(pershkrimi);
        dbRefNgjarjaShoket = database.getReference().child("ngjarjaShoket").child(ngjarjaID);
        dbRefNgjarjaShpenzimet = database.getReference().child("ngjarjaShpenzimet").child(ngjarjaID);
        dbRefNgjarja = database.getReference().child("ngjarjet").child(userID).child(ngjarjaID);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setReverseLayout(true); //reverse order of the layout list
        linearLayoutManager.setStackFromEnd(true);

        rcvShoket = findViewById(R.id.rcvShoket);
        rcvShoket.setHasFixedSize(true);
        rcvShoket.setLayoutManager(linearLayoutManager);

        dialogShpenzimet = mbusheHistorin();

        dbRefNgjarjaShoket.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Shoket objShoket = dataSnapshot.getValue(Shoket.class);
                hmShoketKryesore.put(objShoket.getPerdoruesiID(),objShoket);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                Shoket objShoket = dataSnapshot.getValue(Shoket.class);
                hmShoketKryesore.remove(objShoket.getPerdoruesiID());
                hmShoketKryesore.put(objShoket.getPerdoruesiID(),objShoket);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {
                hmShoketKryesore.remove(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s)
            {

            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        get_shpenzimetEventit();
    }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // inflate menu from xml
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.historia_shpenzimeve_button, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if (id == R.id.btnHistoria)
        {
            //shfaqeHistorin();
            dialogShpenzimet.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        Query mQuery = dbRefNgjarjaShoket;

        FirebaseRecyclerOptions<Shoket> options = new FirebaseRecyclerOptions.Builder<Shoket>()
                .setQuery(mQuery, Shoket.class)
                .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Shoket, ShokuHolder>(options)
        {
            @Override
            public ShokuHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_shoku, parent, false);

                return new ShokuHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ShokuHolder holder, int position, @NonNull Shoket model)
            {
                final String shokuID = getRef(position).getKey();
                final double shpenzimetShoku = model.getShpenzimet();
                holder.setEmriMbiemri(model.getEmri());
                holder.setBorxhi("Borxhi: " + konvertoVleren(model.getShpenzimet()));

                holder.btnFshijeShokun.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        hmShoketKryesore.remove(shokuID);
                        double shpenzimet = shpenzimetShoku / (hmShoketKryesore.size());
                        for (Shoket shoku: hmShoketKryesore.values())
                        {
                            double vleraRe = shoku.getShpenzimet() + shpenzimet;
                            dbRefNgjarjaShoket.child(shoku.getPerdoruesiID()).child("shpenzimet").setValue(vleraRe);
                        }
                        dbRefNgjarjaShoket.child(shokuID).removeValue();
                        showMsg("Shoku është fshirë me sukses.");
                    }
                });
            }
        };

        rcvShoket.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private AlertDialog mbusheHistorin()
    {
        get_shpenzimetEventit();
        Query mQuery;

        LinearLayoutManager linearLayoutManagerHistoriku = new LinearLayoutManager(getApplicationContext());
        linearLayoutManagerHistoriku.setReverseLayout(true);
        linearLayoutManagerHistoriku.setStackFromEnd(true);

        LayoutInflater inflater = this.getLayoutInflater();
        final View viewDialog = inflater.inflate(R.layout.dialog_shpenzimet, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(viewDialog);
        alertDialogBuilder.setTitle("Historia e shpenzimeve");
        alertDialogBuilder.setCancelable(true);

        final AlertDialog alertDialog = alertDialogBuilder.create();

        rcvShpenzimet = viewDialog.findViewById(R.id.rcvShpenzimet);
        rcvShpenzimet.setHasFixedSize(true);
        rcvShpenzimet.setLayoutManager(linearLayoutManagerHistoriku);

        mQuery = dbRefNgjarjaShpenzimet;
        FirebaseRecyclerOptions<Shpenzimi> options = new FirebaseRecyclerOptions.Builder<Shpenzimi>()
                .setQuery(mQuery, Shpenzimi.class)
                .build();

        firebaseRecyclerAdapterShpenzimet = new FirebaseRecyclerAdapter<Shpenzimi, ShpenzimetHolder>(options)
        {
            @Override
            public ShpenzimetHolder onCreateViewHolder(ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.cv_shpenzimi, parent, false);

                return new ShpenzimetHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ShpenzimetHolder holder, int position, @NonNull final Shpenzimi model)
            {
                final String shpenzimiID = getRef(position).getKey();
                final double vleraShpenzimit = model.getVlera();
                holder.setTitulli(model.getArsyeja());
                holder.setPershkrimi(konvertoVleren(model.getVlera()) + " japur nga " + model.getEmriMbiemri());

                holder.btnFshijeShpenzimin.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        double shpenzimiPerkates = vleraShpenzimit / (hmShoketKryesore.size());

                        for (Shoket shoku: hmShoketKryesore.values())
                        {
                            double vleraRe = shoku.getShpenzimet() - shpenzimiPerkates;
                            dbRefNgjarjaShoket.child(shoku.getPerdoruesiID()).child("shpenzimet").setValue(vleraRe);
                        }

                        Log.i("fiek-test", String.valueOf(_shpenzimetEventit));

                        double shpenzimetTotale = _shpenzimetEventit - vleraShpenzimit;

                        txtShpenzimet.setText(String.valueOf(shpenzimetTotale) + " Euro të shpenzuara");
                        dbRefNgjarja.child("shpenzimet").setValue(shpenzimetTotale);
                        dbRefNgjarjaShpenzimet.child(shpenzimiID).removeValue();
                        get_shpenzimetEventit();
                        showMsg("Shpenzimi eshte fshire me sukses.");
                    }
                });
            }
        };

        rcvShpenzimet.setAdapter(firebaseRecyclerAdapterShpenzimet);
        firebaseRecyclerAdapterShpenzimet.startListening();

        return alertDialog;
    }

    public void btnRegjistroShokun(View view)
    {
        LayoutInflater inflater = getLayoutInflater();
        View viewDialog = inflater.inflate(R.layout.dialog_reg_shoki, null);

        txtEmriShoku = viewDialog.findViewById(R.id.txtEmriShoku);
        txtEmailShoku = viewDialog.findViewById(R.id.txtEmailShoku);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NgjarjaActivity.this);

        alertDialogBuilder.setPositiveButton("Ruaje", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                emriShoku = txtEmriShoku.getText().toString();
                emailShoku = txtEmailShoku.getText().toString();

                if (TextUtils.isEmpty(emriShoku) || TextUtils.isEmpty(emailShoku))
                {
                    showMsg("Ju duhet të plotësoni të gjitha fushat.");
                    return;
                }

                String id = dbRefNgjarjaShoket.push().getKey();
                objShoket = new Shoket(id,emriShoku,emailShoku,0);
                dbRefNgjarjaShoket.child(id).setValue(objShoket);

                showMsg("Shoku u regjistrua tek ngjarja me sukses.");
            }
        });

        alertDialogBuilder.setNegativeButton("Anulo", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });

        alertDialogBuilder.setView(viewDialog);
        alertDialogBuilder.setTitle("Regjistrimi i shokut");
        alertDialogBuilder.setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void btnRegjistroShpenzim(View view)
    {
        LayoutInflater inflater = getLayoutInflater();
        View viewDialog = inflater.inflate(R.layout.dialog_reg_shpenzim, null);

        txtArsyeja = viewDialog.findViewById(R.id.txtArsyeja);
        txtSasia = viewDialog.findViewById(R.id.txtSasia);
        spnShoket = viewDialog.findViewById(R.id.spnShoket);

        dbRefNgjarjaShoket.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final List<String> listShoket = new ArrayList<String>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Shoket objShoket = snapshot.getValue(Shoket.class);
                    hmShoket.put(objShoket.getEmri(),objShoket);
                    listShoket.add(objShoket.getEmri());
                }

                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(NgjarjaActivity.this, android.R.layout.simple_spinner_item, listShoket);
                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spnShoket.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NgjarjaActivity.this);

        alertDialogBuilder.setPositiveButton("Ruaje", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

                if (TextUtils.isEmpty(txtArsyeja.getText().toString()) || TextUtils.isEmpty(txtSasia.getText().toString()))
                {
                    showMsg("Ju duhet të plotësoni të gjitha fushat.");
                    return;
                }

                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String id = dbRefNgjarjaShpenzimet.push().getKey();

                String shokiZgjedhurEmri = spnShoket.getSelectedItem().toString();
                String shokiZgjedhurEmail = hmShoket.get(shokiZgjedhurEmri).getEmail();
                final double vlera = Double.parseDouble(txtSasia.getText().toString());

                objShpenzimi = new Shpenzimi(txtArsyeja.getText().toString(),vlera,shokiZgjedhurEmri,shokiZgjedhurEmail,timestamp.getTime());
                dbRefNgjarjaShpenzimet.child(id).setValue(objShpenzimi);

                dbRefNgjarja.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        Ngjarja objNgjarja = dataSnapshot.getValue(Ngjarja.class);
                        double vleraShpenzuar = vlera + objNgjarja.getShpenzimet();
                        dbRefNgjarja.child("shpenzimet").setValue(vleraShpenzuar);
                        txtShpenzimet.setText(String.valueOf(konvertoVleren(vleraShpenzuar)) + " të shpenzuara");

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                double vleraPerkatese = vlera / hmShoket.size();
                for (Shoket shoku: hmShoket.values())
                {
                    double vleraRe = shoku.getShpenzimet() + vleraPerkatese;
                    dbRefNgjarjaShoket.child(shoku.getPerdoruesiID()).child("shpenzimet").setValue(vleraRe);
                }

                get_shpenzimetEventit();
                showMsg("Shpenzimi u regjistrua me sukses.");
            }
        });

        alertDialogBuilder.setNegativeButton("Anulo", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {

            }
        });

        alertDialogBuilder.setView(viewDialog);
        alertDialogBuilder.setTitle("Regjistrimi i shpenzimit");
        alertDialogBuilder.setCancelable(true);

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void get_shpenzimetEventit()
    {
        dbRefNgjarja.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Ngjarja objNgjarja = dataSnapshot.getValue(Ngjarja.class);
                _shpenzimetEventit = objNgjarja.getShpenzimet();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }

    private String konvertoVleren(double vlera)
    {
        if(konvertoValuten)
        {
            try
            {
                double valutaZgjedhurNeEuro = getValutat().getDouble(valutaZgjedhur);
                double valutaEventitNeEuro = getValutat().getDouble(valuta);
                double vleraNeEuro = vlera / valutaEventitNeEuro;
                double vleraNeValutenZgjedhur = vleraNeEuro * valutaZgjedhurNeEuro;
                return String.valueOf(Utils.round(vleraNeValutenZgjedhur,2)) + " " + valutaZgjedhur;
            }
            catch (Exception e)
            { return String.valueOf(Utils.round(vlera,2)) + " " + valuta; }
        }
        else
        {
            return String.valueOf(Utils.round(vlera,2)) + " " + valuta;
        }

    }

    private JSONObject getValutat()
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

    private void showMsg(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    public static class ShpenzimetHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageButton btnFshijeShpenzimin;

        public ShpenzimetHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            btnFshijeShpenzimin = mView.findViewById(R.id.btnFshijeShpenzimin);
            //btnFshijeShpenzimin.setVisibility(View.GONE);
        }

        public void setTitulli(String titulli)
        {
            TextView txtTitulli = mView.findViewById(R.id.txtTitulli);
            txtTitulli.setText(titulli);
        }

        public void setPershkrimi(String pershkrimi)
        {
            TextView txtPershkrimi = mView.findViewById(R.id.txtPershkrimi);
            txtPershkrimi.setText(pershkrimi);
        }


    }

    public static class ShokuHolder extends RecyclerView.ViewHolder
    {
        View mView;
        ImageButton btnFshijeShokun;

        public ShokuHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
            btnFshijeShokun = mView.findViewById(R.id.btnFshijeShokun);
        }

        public void setEmriMbiemri(String emri)
        {
            TextView txtEmriShokuCV = mView.findViewById(R.id.txtEmriShokuCV);
            txtEmriShokuCV.setText(emri);
        }

        public void setBorxhi(String borxhi)
        {
            TextView txtBorxhiShoku = mView.findViewById(R.id.txtBorxhiShoku);
            txtBorxhiShoku.setText(borxhi);
        }
    }


}
