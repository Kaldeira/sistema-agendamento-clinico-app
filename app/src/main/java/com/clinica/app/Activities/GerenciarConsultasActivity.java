package com.clinica.app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.ConsultaMedicoAdapter;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityGerenciarConsultasBinding;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Controle.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GerenciarConsultasActivity extends AppCompatActivity {

    private ActivityGerenciarConsultasBinding binding;
    private FirebaseManager fb;
    private SessionManager session;
    private ConsultaMedicoAdapter adapter;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;
    ShapeableImageView sivFotoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGerenciarConsultasBinding.inflate(getLayoutInflater());
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

        fb = FirebaseManager.getInstance();
        session = new SessionManager(this);

        binding.rvConsultas.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConsultaMedicoAdapter(
                (consulta, acao) -> realizarAcao(consulta, acao));
        binding.rvConsultas.setAdapter(adapter);

        carregarConsultas();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarConsultas();
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

    private void carregarConsultas() {
        fb.buscarConsultasPorMedico(session.getUsername(), consultas -> {

            // Callback do Firebase pode chegar em thread de background.
            // Lista vazia: limpa o adapter na UI thread e encerra.
            if (consultas == null || consultas.isEmpty()) {
                runOnUiThread(() -> adapter.setLista(new ArrayList<>()));
                return;
            }

            // Contador regressivo: quando chegar a 0 todos os nomes foram resolvidos.
            AtomicInteger pendentes = new AtomicInteger(consultas.size());

            for (Consulta c : consultas) {
                fb.buscarUsuarioPorUsername(c.getPacienteId(), paciente -> {

                    // Atualiza o nome dentro da Consulta (operação em memória, thread-safe
                    // porque cada Consulta é acessada por apenas um callback de cada vez).
                    c.setNomePaciente(paciente != null ? paciente.getNome() : "Paciente");

                    // Quando todos os callbacks terminarem, atualiza a UI.
                    if (pendentes.decrementAndGet() == 0) {
                        runOnUiThread(() -> adapter.setLista(consultas));
                    }
                });
            }
        });
    }

    private void realizarAcao(Consulta consulta, String acao) {
        fb.atualizarStatusConsulta(consulta.getId(), acao, ok -> {
            runOnUiThread(() -> {
                if (ok) {
                    String msg = "confirmada".equals(acao)
                            ? "Consulta confirmada!"
                            : "Consulta cancelada.";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    carregarConsultas();
                } else {
                    Toast.makeText(this, "Erro ao atualizar consulta.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}