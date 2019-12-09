package com.juankyapps.igenda;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

public class LoginActivity extends AppCompatActivity {

    Button loginBtn;
    Button forgotBtn;
    TextInputEditText emailInput;
    TextInputEditText passInput;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.btnLogin);
        forgotBtn = findViewById(R.id.btnForgot);

        emailInput = findViewById(R.id.emailInput);
        passInput = findViewById(R.id.passInput);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onLoginButtonClick(v); }
        });
        forgotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onForgotButtonClick(v); }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.simpleWhite));
        }

    }

    private void onLoginButtonClick(View v) {
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();
        String email = emailInput.getText().toString();
        String password = passInput.getText().toString();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos!", Toast.LENGTH_SHORT).show();
            return;
        }
        loginBtn.setClickable(false);
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    getUserData(task.getResult());
                } else {
                    Toast.makeText(LoginActivity.this, "Could not login!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getUserData(AuthResult authResult) {
        String userid = authResult.getUser().getUid();
        db.collection("users").document(userid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Class activity = WizardActivity.class;
                if (documentSnapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Logged in!", Toast.LENGTH_SHORT).show();
                    activity = documentSnapshot.getBoolean("initialized") ? MainActivity.class : WizardActivity.class;
                }
                Intent intent = new Intent(LoginActivity.this, activity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void onForgotButtonClick(View v) {
        Toast.makeText(this, "F", Toast.LENGTH_SHORT).show();
    }

}
