package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.DAO.HistoricoMedicoAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityHistoricoMedicoBinding;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class HistoricoMedicoActivity extends AppCompatActivity {

    private ActivityHistoricoMedicoBinding binding;
    private FirebaseManager fb;
    private SessionManager session;
    private String pacienteUsername;
    private LinearLayout layoutHistorico;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoricoMedicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb      = FirebaseManager.getInstance();
        session = new SessionManager(this);

        bindViews();
        BarraNavHelper.setupBottomNav(this,
                findViewById(R.id.navHome), findViewById(R.id.navPerfil),
                findViewById(R.id.navConsultas), findViewById(R.id.navChat),
                findViewById(R.id.navHistorico), findViewById(R.id.navPacientes),
                findViewById(R.id.navAdmin), findViewById(R.id.navLogin));

        binding.btnVoltar.setOnClickListener(v -> finish());
        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(this));

        if (session.isMedico()) {
            layoutHistorico.setVisibility(View.VISIBLE);
            binding.fabNovoRegistro.setVisibility(View.VISIBLE);
            pacienteUsername = getIntent().getStringExtra("paciente_username");

            carregarHistorico(pacienteUsername);

            binding.fabNovoRegistro.setOnClickListener(v -> {
                if (pacienteUsername == null) return;
                Intent intent = new Intent(this, RegistrarHistoricoActivity.class);
                intent.putExtra("paciente_username", pacienteUsername);
                startActivity(intent);
            });
        } else {
            layoutHistorico.setVisibility(View.GONE);
            binding.fabNovoRegistro.setVisibility(View.GONE);
            carregarHistorico(session.getUsername());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pacienteUsername != null) carregarHistorico(pacienteUsername);
    }

    private void bindViews() {
        tvGreeting      = findViewById(R.id.tvGreeting);
        navHome         = findViewById(R.id.navHome);
        navPerfil       = findViewById(R.id.navPerfil);
        navConsultas    = findViewById(R.id.navConsultas);
        navChat         = findViewById(R.id.navChat);
        navHistorico    = findViewById(R.id.navHistorico);
        navPacientes    = findViewById(R.id.navPacientes);
        navAdmin        = findViewById(R.id.navAdmin);
        navLoginBtn     = findViewById(R.id.navLogin);
        layoutHistorico = findViewById(R.id.layoutHistorico);
    }

    private void carregarHistorico(String username) {
        fb.buscarHistoricoPorPaciente(username, lista -> {
            AtomicInteger contador = new AtomicInteger(0);

            if (lista.isEmpty()) {
                runOnUiThread(() -> {
                    binding.tvVazio.setVisibility(View.VISIBLE);
                    HistoricoMedicoAdapter adapter = new HistoricoMedicoAdapter();
                    adapter.setLista(lista);
                    binding.rvHistorico.setAdapter(adapter);
                });
                return;
            }

            for (HistoricoMedico h : lista) {
                fb.buscarUsuarioPorUsername(h.getMedicoId(), medico -> {
                    if (medico != null) h.setNomeMedico(medico.getNome());

                    if (contador.incrementAndGet() == lista.size()) {
                        runOnUiThread(() -> {
                            binding.tvVazio.setVisibility(View.GONE);
                            HistoricoMedicoAdapter adapter = new HistoricoMedicoAdapter();
                            adapter.setLista(lista);
                            binding.rvHistorico.setAdapter(adapter);
                        });
                    }
                });
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}
