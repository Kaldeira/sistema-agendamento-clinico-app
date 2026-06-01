package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.HistoricoConsultaAdapter;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityHistoricoConsultasBinding;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Controle.SessionManager;

import java.util.concurrent.atomic.AtomicInteger;

public class HistoricoConsultasActivity extends AppCompatActivity {

    private static final double VALOR_CONSULTA = 150.00;
    private ActivityHistoricoConsultasBinding binding;
    private FirebaseManager fb;
    private SessionManager session;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoricoConsultasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb = FirebaseManager.getInstance();
        session = new SessionManager(this);

        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(this));

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

        carregarConsultas();
    }

    private void carregarConsultas() {
        HistoricoConsultaAdapter adapter = new HistoricoConsultaAdapter(session.isMedico());
        binding.rvHistorico.setAdapter(adapter);

        if (session.isMedico()) {
            carregarParaMedico(adapter);
        } else {
            adapter.setOnConsultaClick(consulta -> {

                Intent intent = new Intent(this, PagamentoActivity.class);
                intent.putExtra(PagamentoActivity.EXTRA_CONSULTA_ID, consulta.getId());
                intent.putExtra(PagamentoActivity.EXTRA_TOTAL, VALOR_CONSULTA);
                intent.putExtra(PagamentoActivity.EXTRA_MEDICO_NOME, consulta.getNomeMedico());

                startActivity(intent);
            });

            carregarParaPaciente(adapter);
        }
    }

    private void carregarParaMedico(HistoricoConsultaAdapter adapter) {
        fb.buscarConsultasPorMedico(session.getUsername(), consultas -> {

            if (consultas == null || consultas.isEmpty()) {
                runOnUiThread(() -> findViewById(R.id.tvVazio).setVisibility(View.VISIBLE));
                return;
            }

            AtomicInteger pendentes = new AtomicInteger(consultas.size());

            for (Consulta c : consultas) {
                fb.buscarUsuarioPorUsername(c.getPacienteId(), paciente -> {

                    if (paciente != null) c.setNomePaciente(paciente.getNome());

                    if (pendentes.decrementAndGet() == 0) {
                        runOnUiThread(() -> adapter.setLista(consultas));
                    }
                });
            }
        });
    }


    private void carregarParaPaciente(HistoricoConsultaAdapter adapter) {
        fb.buscarConsultasPorPaciente(session.getUsername(), consultas -> {

            Log.d("Consultas", "Username: " + session.getUsername());
            Log.d("Consultas", "Size: " + consultas.size());

            if (consultas == null || consultas.isEmpty()) {
                runOnUiThread(() -> findViewById(R.id.tvVazio).setVisibility(View.VISIBLE));
                return;
            }

            int total = consultas.size();


            AtomicInteger pendentes = new AtomicInteger(total);

            for (Consulta c : consultas) {

                Log.d("Consulta", "MedicoId: " + c.getMedicoId());

                fb.buscarUsuarioPorUsername(c.getMedicoId(), medico -> {

                    if (medico != null) {
                        c.setNomeMedico(medico.getNome());
                        c.setEspecialidadeMedico(medico.getEspecialidade());
                    }

                    fb.buscarPagamentoPorConsulta(c.getId(), pagamento -> {

                        c.setPagamento(pagamento);

                        if (pendentes.decrementAndGet() == 0) {
                            runOnUiThread(() -> adapter.setLista(consultas));
                        }
                    });
                });
            }
        });
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}