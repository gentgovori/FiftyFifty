package fiek.fiftyfifty;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Timestamp;

import fiek.fiftyfifty.Klasat.Ngjarja;
import fiek.fiftyfifty.Klasat.Shoket;
import fiek.fiftyfifty.Klasat.Utils;

public class KrijoEventActivity extends AppCompatActivity
{
    private FirebaseUser user;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference dbRefNgjarjet, dbRefNgjarjaShoket;

    private String userID, emriMbiemri;
    private EditText txtEmriNgjarjes, txtLokacioni;
    private Spinner spnParja;

    private Ngjarja objNgjarja;
    private String emriNgjarjes, lokacioni;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_krijo_event);


        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        database = Utils.getDatabase();
        dbRefNgjarjet = database.getReference().child("ngjarjet");
        dbRefNgjarjaShoket = database.getReference().child("ngjarjaShoket");

        if(user != null)
        {
            userID = user.getUid();
            emriMbiemri = user.getDisplayName();
        }

        spnParja = findViewById(R.id.spnParja);
        txtEmriNgjarjes = findViewById(R.id.txtEmriNgjarjes);
        txtLokacioni = findViewById(R.id.txtLokacioni);
        mbushListenParet(); //gr8

    }

    public void btnRuajeEventin(View view)
    {
        String ngjarjaID, ngjarjaShokuID;
        emriNgjarjes = txtEmriNgjarjes.getText().toString();
        lokacioni = txtLokacioni.getText().toString();

        if(emriNgjarjes.equals("") || lokacioni.equals(""))
        {
            showMsg("Ju duhet të plotësoni të gjitha fushat."); //
            return;
        }

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        ngjarjaID = dbRefNgjarjet.push().getKey();

        objNgjarja = new Ngjarja(emriNgjarjes, lokacioni, spnParja.getSelectedItem().toString(),0, emriMbiemri, userID, timestamp.getTime());
        dbRefNgjarjet.child(userID).child(ngjarjaID).setValue(objNgjarja);

        ngjarjaShokuID = dbRefNgjarjaShoket.child(ngjarjaID).push().getKey();
        dbRefNgjarjaShoket.child(ngjarjaID).child(ngjarjaShokuID).setValue(new Shoket(ngjarjaShokuID,user.getDisplayName(),user.getEmail(),0));

        Intent intent=new Intent();
        intent.putExtra("regjistrimi",1);
        setResult(1,intent);
        finish();
    }


    private void mbushListenParet()
    {

        // Create an ArrayAdapter using the string array and a default spinner
        ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter
                .createFromResource(this, R.array.paret_array,
                        android.R.layout.simple_spinner_item); // fill spinner with values from xml

        // Specify the layout to use when the list of choices appears
        staticAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spnParja.setAdapter(staticAdapter);
    }

    private void showMsg(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

}
