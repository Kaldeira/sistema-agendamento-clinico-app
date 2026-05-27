package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.DAO.ChatListAdapter;
import com.clinica.app.R;
import com.clinica.app.databinding.ActivityChatListBinding;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.Utils.BarraNavHelper;

public class ChatListActivity extends AppCompatActivity {

    private ActivityChatListBinding binding;
    private FirebaseManager fb;
    private SessionManager session;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindViews();

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

        session = new SessionManager(this);
        fb      = FirebaseManager.getInstance();

        binding.rvContatos.setLayoutManager(new LinearLayoutManager(this));
        carregarContatos();
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

    @Override
    protected void onResume() {
        super.onResume();
        carregarContatos();
    }

    private void carregarContatos() {
        ChatListAdapter adapter = new ChatListAdapter(contato -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("destinatario_username", contato.getUsername());
            intent.putExtra("destinatario_nome",     contato.getNome());
            intent.putExtra("foto_perfil",           contato.getFotoPerfil());
            startActivity(intent);
        });

        binding.rvContatos.setAdapter(adapter);

        fb.buscarContatosChat(session.getUsername(), contatoIds -> {
            if (contatoIds.isEmpty()) {
                runOnUiThread(() -> binding.tvVazio.setVisibility(View.VISIBLE));
                return;
            }

            runOnUiThread(() -> binding.tvVazio.setVisibility(View.GONE));

            for (String username : contatoIds) {
                fb.buscarUsuarioPorUsername(username, usuario -> {
                    if (usuario != null)
                        runOnUiThread(() -> adapter.addUsuario(usuario));
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}