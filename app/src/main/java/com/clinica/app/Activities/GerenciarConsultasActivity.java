package com.clinica.app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.ConsultaMedicoAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityGerenciarConsultasBinding;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;


public class GerenciarConsultasActivity extends AppCompatActivity {

    private ActivityGerenciarConsultasBinding binding;
    private BancoDados db;
    private SessionManager session;
    private ConsultaMedicoAdapter adapter;

    // Barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting, tvIniciaisUser;
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

        db = BancoDados.getInstance(this);
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

    private void carregarConsultas() {
        List<Consulta> consultas = db.buscarConsultasPorMedico(session.getUserId());
        // Enriquecer com nome do paciente
        for (Consulta c : consultas) {
            Usuario paciente = db.buscarUsuarioPorId(c.getPacienteId());
            if (paciente != null) c.setNomePaciente(paciente.getNome());
        }
        adapter.setLista(consultas);
    }

    private void realizarAcao(Consulta consulta, String acao) {
        boolean ok = db.atualizarStatusConsulta(consulta.getId(), acao);
        if (ok) {
            String msg = "confirmada".equals(acao) ? "Consulta confirmada!" : "Consulta cancelada.";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            carregarConsultas();
        } else {
            Toast.makeText(this, "Erro ao atualizar consulta.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
