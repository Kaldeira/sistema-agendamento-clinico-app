package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.ChatListAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.databinding.ActivityChatListBinding;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;

import java.util.ArrayList;
import java.util.List;
import com.clinica.app.Utils.BarraNavHelper;


public class ChatListActivity extends AppCompatActivity {

    private ActivityChatListBinding binding;
    private BancoDados db;
    private SessionManager session;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View          navLoginBtn;
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


//        getSupportActionBar().setTitle("Mensagens");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BancoDados.getInstance(this);
        session = new SessionManager(this);

        binding.rvContatos.setLayoutManager(new LinearLayoutManager(this));
        carregarContatos();
    }

    private void bindViews() {
        tvGreeting    = findViewById(R.id.tvGreeting);
        navHome       = findViewById(R.id.navHome);
        navPerfil     = findViewById(R.id.navPerfil);
        navConsultas  = findViewById(R.id.navConsultas);
        navChat       = findViewById(R.id.navChat);
        navHistorico  = findViewById(R.id.navHistorico);
        navPacientes  = findViewById(R.id.navPacientes);
        navAdmin      = findViewById(R.id.navAdmin);
        navLoginBtn   = findViewById(R.id.navLogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarContatos();
    }

    private void carregarContatos() {
        List<Integer> contatoIds = db.buscarContatosChat(session.getUserId());
        List<Usuario> contatos = new ArrayList<>();
        for (int id : contatoIds) {
            Usuario u = db.buscarUsuarioPorId(id);
            if (u != null) contatos.add(u);
        }

        binding.tvVazio.setVisibility(contatos.isEmpty() ? View.VISIBLE : View.GONE);

        ChatListAdapter adapter = new ChatListAdapter(contato -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("destinatario_id", contato.getId());
            intent.putExtra("destinatario_nome", contato.getNome());
            intent.putExtra("foto_perfil", contato.getFotoPerfil());
            startActivity(intent);
        });
        adapter.setLista(contatos);
        binding.rvContatos.setAdapter(adapter);
    }

    private void mostrarSeletorPaciente() {
        List<Usuario> pacientes = db.buscarPacientes();
        String[] nomes = new String[pacientes.size()];
        for (int i = 0; i < pacientes.size(); i++) nomes[i] = pacientes.get(i).getNome();

        new android.app.AlertDialog.Builder(this)
                .setTitle("Selecionar Paciente")
                .setItems(nomes, (dialog, which) -> {
                    Usuario paciente = pacientes.get(which);
                    Intent intent = new Intent(this, ChatActivity.class);
                    intent.putExtra("destinatario_id", paciente.getId());
                    intent.putExtra("destinatario_nome", paciente.getNome());
                    startActivity(intent);
                })
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
