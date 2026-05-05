package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.databinding.ActivityPacienteDashboardBinding;
import com.clinica.app.Controle.NotificacaoReceiver;
import com.clinica.app.Controle.SessionManager;


public class PacienteDashboardActivity extends AppCompatActivity {

    private ActivityPacienteDashboardBinding binding;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPacienteDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        binding.tvBemVindo.setText("Olá, " + session.getNome() + "!");

        NotificacaoReceiver.criarCanalNotificacao(this);

        binding.cardBuscarMedicos.setOnClickListener(v ->
                startActivity(new Intent(this, BuscarMedicosActivity.class)));

        binding.cardHistoricoConsultas.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoConsultasActivity.class)));

        binding.cardChat.setOnClickListener(v ->
                startActivity(new Intent(this, ChatListActivity.class)));

        binding.btnSair.setOnClickListener(v -> {
            session.encerrarSessao();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
