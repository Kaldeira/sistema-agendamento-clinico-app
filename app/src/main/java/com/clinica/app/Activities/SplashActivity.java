package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;

//TO DO:
//chat entre medicos (feito)
//notificação de confirmação de consultas
//filtro de pagamento (medico e tipo de pagamento)

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        BancoDados.getInstance(this);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, SPLASH_DELAY_MS);
    }
}
