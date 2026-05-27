package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Controle.NotificacaoReceiver;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.databinding.ActivityAgendarConsultaBinding;

public class AgendarConsultaActivity extends AppCompatActivity {

    private static final double VALOR_CONSULTA = 150.00;

    private ActivityAgendarConsultaBinding binding;
    private FirebaseManager fb;
    private SessionManager session;

    private String medicoUsername;
    private String medicoNome;
    private String data;
    private String hora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAgendarConsultaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb = FirebaseManager.getInstance();
        session = new SessionManager(this);

        receberDadosIntent();

        if (!validarDados()) {
            finish();
            return;
        }

        preencherTela();

        binding.btnAgendar.setOnClickListener(v -> confirmarAgendamento());
        binding.btnVoltar.setOnClickListener(v -> finish());
    }

    private void receberDadosIntent() {
        Intent intent = getIntent();
        medicoUsername = intent.getStringExtra("medico_username");
        medicoNome     = intent.getStringExtra("medico_nome");
        data           = intent.getStringExtra("data");
        hora           = intent.getStringExtra("hora");
    }

    private boolean validarDados() {
        if (medicoUsername == null || medicoUsername.isEmpty()
                || medicoNome == null
                || data == null
                || hora == null) {
            Toast.makeText(this, "Erro ao carregar dados da consulta", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (session.getUsername() == null || session.getUsername().isEmpty()) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void preencherTela() {
        binding.tvMedico.setText("Médico: " + medicoNome);
        binding.tvDataHora.setText("Data: " + data + "  |  Hora: " + hora);
    }

    private void confirmarAgendamento() {
        String observacoes = binding.etObservacoes.getText().toString().trim();

        Consulta consulta = new Consulta();
        consulta.setPacienteId(session.getUsername());
        consulta.setMedicoId(medicoUsername);
        consulta.setData(data);
        consulta.setHora(hora);
        consulta.setPagamentoTipo("pendente");
        consulta.setObservacoes(observacoes);

        binding.btnAgendar.setEnabled(false);

        fb.agendarConsulta(consulta,
                consultaId -> {
                    agendarNotificacao(consultaId);

                    Intent intent = new Intent(this, PagamentoActivity.class);
                    intent.putExtra(PagamentoActivity.EXTRA_CONSULTA_ID, consultaId);
                    intent.putExtra(PagamentoActivity.EXTRA_TOTAL, VALOR_CONSULTA);
                    intent.putExtra(PagamentoActivity.EXTRA_MEDICO_NOME, medicoNome);

                    startActivity(intent);
                    finish();
                },
                e -> {
                    binding.btnAgendar.setEnabled(true);
                    Toast.makeText(this, "Erro ao criar consulta. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void agendarNotificacao(String consultaId) {
        try {
            NotificacaoReceiver.criarCanalNotificacao(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}