package caden.foodapp;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {

    private EditText emailfield;
    private EditText pswfield;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        emailfield = (EditText) findViewById(R.id.email);
        pswfield = (EditText) findViewById(R.id.password);

        mAuth = FirebaseAuth.getInstance();
    }

    public void signUpAction(final View view) {
        System.out.println(emailfield.getText());
        System.out.println(pswfield.getText());

        mAuth.createUserWithEmailAndPassword(emailfield.getText().toString(), pswfield.getText()
                .toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("FIREBASE", "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Snackbar.make(view, "Failed to create account, check your internet " +
                                    "connection", Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
