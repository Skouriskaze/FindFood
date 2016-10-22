package caden.foodapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private AutoCompleteTextView emailfield;
    private EditText pswfield;
    private ProgressBar progressBar;
    private TextView signinloading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/economica.otf");
        Typeface tf_i = Typeface.createFromAsset(getAssets(), "fonts/economica_italic.otf");
        TextView tv1 = (TextView) findViewById(R.id.appLbl);
        TextView tv2 = (TextView) findViewById(R.id.ffd);
        tv1.setTypeface(tf);
        tv2.setTypeface(tf_i);

        mAuth = FirebaseAuth.getInstance();

        emailfield = (AutoCompleteTextView) findViewById(R.id.email);
        pswfield = (EditText) findViewById(R.id.password);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        signinloading = (TextView) findViewById(R.id.signintext);

        pswfield.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return true;
                }
                return false;
            }
        });
    }

    public void transition(final View view) {
//        Close off keyboard
        ConstraintLayout main;
//        Get your layout set up, this is just an example
        main = (ConstraintLayout) findViewById(R.id.activity_login);
//        Then just use the following:
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(main.getWindowToken(), 0);

        signinloading.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(emailfield.getText().toString(), pswfield.getText()
                .toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Intent intent = new Intent(Login.this, Map.class);
                    startActivity(intent);
                } else {
                    Snackbar.make(view, task.getException().getMessage(), Snackbar
                            .LENGTH_SHORT).show();
                }
                signinloading.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
            }
        });


    }

    public void toSignUp(View view) {
        Intent intent = new Intent(this, Signup.class);
        startActivity(intent);
    }
}
