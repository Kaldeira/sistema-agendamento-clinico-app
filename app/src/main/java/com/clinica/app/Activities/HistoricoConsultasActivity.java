package com.clinica.app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.HistoricoConsultaAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityHistoricoConsultasBinding;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;

import java.util.List;

public class HistoricoConsultasActivity extends AppCompatActivity {

    private ActivityHistoricoConsultasBinding binding;
    private BancoDados db;
    private SessionManager session;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoricoConsultasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSupportActionBar().setTitle("Histórico de Consultas");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BancoDados.getInstance(this);
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

        List<Consulta> consultas;
        if (session.isMedico()) {
            consultas = db.buscarConsultasPorMedico(session.getUserId());
            for (Consulta c : consultas) {
                Usuario p = db.buscarUsuarioPorId(c.getPacienteId());
                if (p != null) c.setNomePaciente(p.getNome());
            }
        } else {
            consultas = db.buscarConsultasPorPaciente(session.getUserId());
            for (Consulta c : consultas) {
                Usuario m = db.buscarUsuarioPorId(c.getMedicoId());
                if (m != null) {
                    c.setNomeMedico(m.getNome());
                    c.setEspecialidadeMedico(m.getEspecialidade());
                }
                Pagamento p = db.buscarPagamentoPorConsulta(c.getId());
                c.setPagamento(p);
            }
        }

        if (consultas.isEmpty()) {
            findViewById(R.id.tvVazio).setVisibility(View.VISIBLE);
        }

        HistoricoConsultaAdapter adapter = new HistoricoConsultaAdapter(session.isMedico());
        adapter.setLista(consultas);
        binding.rvHistorico.setAdapter(adapter);
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
