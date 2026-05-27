package com.clinica.app.Activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private SessionManager session;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);

        if (session.isLogado()) { goHome(); return; }

        bindViews();
        botaoCadastro();

        BarraNavHelper.setupBottomNav(
                this,
                findViewById(R.id.navHome),
                findViewById(R.id.navPerfil),
                findViewById(R.id.navConsultas),
                findViewById(R.id.navChat),
                findViewById(R.id.navHistorico),
                findViewById(R.id.navPacientes),
                findViewById(R.id.navAdmin),
                findViewById(R.id.navLogin)
        );

        binding.btnLogin.setOnClickListener(v -> realizarLogin());
    }

    private void bindViews() {
        tvGreeting   = findViewById(R.id.tvGreeting);
        navHome      = findViewById(R.id.navHome);
        navPerfil    = findViewById(R.id.navPerfil);
        navConsultas = findViewById(R.id.navConsultas);
        navChat      = findViewById(R.id.navChat);
        navHistorico = findViewById(R.id.navHistorico);
        navPacientes = findViewById(R.id.navPacientes);
        navAdmin     = findViewById(R.id.navAdmin);
        navLoginBtn  = findViewById(R.id.navLogin);
    }

    private void realizarLogin() {
        String login = binding.etEmail.getText().toString().trim();
        String senha = binding.etSenha.getText().toString().trim();

        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(senha)) {
            Toast.makeText(this, "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseManager.getInstance().login(login, senha, usuario -> {
            if (usuario == null) {
                runOnUiThread(() ->
                        Toast.makeText(this, "E-mail ou senha incorretos.", Toast.LENGTH_SHORT).show());
                return;
            }

            // Usa a sobrecarga sem int para criar sessão com dados do Firebase
            session.criarSessao(
                    usuario.getNome(),
                    usuario.getTipo(),
                    usuario.getEmail(),
                    usuario.getFotoPerfil(),
                    usuario.getUsername(),
                    usuario.getSenha()
            );

            runOnUiThread(this::goHome);
        });
    }

    private void botaoCadastro() {
        String texto = "Não tem conta? Cadastre-se";
        SpannableString spannable = new SpannableString(texto);
        int start = texto.indexOf("Cadastre-se");
        int end   = start + "Cadastre-se".length();
        spannable.setSpan(new ForegroundColorSpan(getColor(R.color.colorPrimary)),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvCadastrar.setText(spannable);
        binding.tvCadastrar.setOnClickListener(v ->
                startActivity(new Intent(this, CadastroActivity.class)));
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}