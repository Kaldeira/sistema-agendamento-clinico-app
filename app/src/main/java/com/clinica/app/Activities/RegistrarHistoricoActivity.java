package com.clinica.app.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.databinding.ActivityRegistrarHistoricoBinding;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;
import com.google.firebase.Firebase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class RegistrarHistoricoActivity extends AppCompatActivity {

    private ActivityRegistrarHistoricoBinding binding;
    private FirebaseManager fb;
    private SessionManager session;
    private String pacienteUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarHistoricoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb      = FirebaseManager.getInstance();
        session = new SessionManager(this);
        pacienteUsername = getIntent().getStringExtra("paciente_username");

        if (pacienteUsername == null) { finish(); return; }

        fb.buscarUsuarioPorUsername(pacienteUsername, paciente -> {
            if (paciente != null)
                runOnUiThread(() ->
                        binding.tvPaciente.setText("Paciente: " + paciente.getNome()));
        });

        binding.btnSalvar.setOnClickListener(v -> salvarRegistro());
        binding.btnVoltar.setOnClickListener(v -> finish());
    }

    private void salvarRegistro() {
        String diagnostico = binding.etDiagnostico.getText().toString().trim();
        String observacoes = binding.etObservacoes.getText().toString().trim();
        String prescricao  = binding.etPrescricao.getText().toString().trim();

        if (TextUtils.isEmpty(diagnostico)) {
            Toast.makeText(this, "Informe o diagnóstico.", Toast.LENGTH_SHORT).show();
            return;
        }

        HistoricoMedico h = new HistoricoMedico();
        h.setPacienteId(pacienteUsername);
        h.setMedicoId(session.getUsername());
        h.setData(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        h.setDiagnostico(diagnostico);
        h.setObservacoes(observacoes);
        h.setPrescricao(prescricao);

        fb.registrarHistorico(h,
                id -> runOnUiThread(() -> {
                    Toast.makeText(this, "Registro salvo!", Toast.LENGTH_SHORT).show();
                    finish();
                }),
                e -> runOnUiThread(() ->
                        Toast.makeText(this, "Erro ao salvar registro.", Toast.LENGTH_SHORT).show())
        );
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
