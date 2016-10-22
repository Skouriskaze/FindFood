package caden.foodapp;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {

    private EditText emailfield;
    private EditText pswfield;
    private ProgressBar progressBar;
    private TextView signuploading;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailfield = (EditText) findViewById(R.id.email);
        pswfield = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        signuploading = (TextView) findViewById(R.id.signuptext);

        mAuth = FirebaseAuth.getInstance();
    }

    public void signUpAction(final View view) {

        signuploading.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(emailfield.getText().toString(), pswfield.getText()
                .toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("FIREBASE", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Snackbar.make(view, "OOPS SOMETHING BAD HAS HAPPENED :(", Snackbar.LENGTH_SHORT).show();
                        }
                        signuploading.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                    }

                });
    }
}
