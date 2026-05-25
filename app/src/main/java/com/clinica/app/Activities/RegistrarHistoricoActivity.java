package com.clinica.app.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.databinding.ActivityRegistrarHistoricoBinding;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class RegistrarHistoricoActivity extends AppCompatActivity {

    private ActivityRegistrarHistoricoBinding binding;
    private BancoDados db;
    private SessionManager session;
    private int pacienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrarHistoricoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getSupportActionBar().setTitle("Novo Registro Médico");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BancoDados.getInstance(this);
        session = new SessionManager(this);
        pacienteId = getIntent().getIntExtra("paciente_id", -1);

        if (pacienteId == -1) {
            finish();
            return;
        }

        Usuario paciente = db.buscarUsuarioPorId(pacienteId);
        if (paciente != null)
            binding.tvPaciente.setText("Paciente: " + paciente.getNome());

        binding.btnSalvar.setOnClickListener(v -> salvarRegistro());

        binding.btnVoltar.setOnClickListener(v-> finish());
    }

    private void salvarRegistro() {
        String diagnostico = binding.etDiagnostico.getText().toString().trim();
        String observacoes = binding.etObservacoes.getText().toString().trim();
        String prescricao = binding.etPrescricao.getText().toString().trim();

        if (TextUtils.isEmpty(diagnostico)) {
            Toast.makeText(this, "Informe o diagnóstico.", Toast.LENGTH_SHORT).show();
            return;
        }

        HistoricoMedico h = new HistoricoMedico();
        h.setPacienteId(pacienteId);
        h.setMedicoId(session.getUserId());
        h.setData(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));
        h.setDiagnostico(diagnostico);
        h.setObservacoes(observacoes);
        h.setPrescricao(prescricao);

        long id = db.registrarHistorico(h);
        if (id > 0) {
            Toast.makeText(this, "Registro salvo com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao salvar registro.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
