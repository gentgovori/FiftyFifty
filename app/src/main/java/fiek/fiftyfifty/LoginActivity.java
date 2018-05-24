package fiek.fiftyfifty;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class LoginActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback //
{
    private static final int RC_SIGN_IN = 123;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        if(user != null)
        {
            kaloNeApp();
        }
        else
        {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .setTheme(R.style.LoginTheme)
                            .setLogo(R.drawable.logo)
                            .setIsSmartLockEnabled(false)
                            .build(),
                    RC_SIGN_IN);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK)
            {
                kaloNeApp();
                finish();
            }
            else
            {
                // Sign in failed
                if (response == null)
                {
                    // User pressed back button
                    showMsg("Kyçja ka dështuar!");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                {
                    showMsg("Ju nuk keni çasje në internet!");
                    return;
                }


            }
        }
    }

    public void kaloNeApp()
    {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void showMsg(String msg)
    {
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }


}
