package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.HistoricoMedicoAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityHistoricoMedicoBinding;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;

import java.util.List;


public class HistoricoMedicoActivity extends AppCompatActivity {

    private ActivityHistoricoMedicoBinding binding;
    private BancoDados db;
    private SessionManager session;
    private List<Usuario> pacientes;
    private int idPaciente;
    private LinearLayout layoutHistorico;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoricoMedicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //getSupportActionBar().setTitle("Histórico Médico");
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BancoDados.getInstance(this);
        session = new SessionManager(this);

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

        binding.btnVoltar.setOnClickListener(v->finish());
        binding.rvHistorico.setLayoutManager(new LinearLayoutManager(this));

        if (session.isMedico()) {
            layoutHistorico.setVisibility(View.VISIBLE);
            binding.fabNovoRegistro.setVisibility(View.VISIBLE);
            idPaciente = getIntent().getIntExtra("paciente_id", -1);
            carregarHistorico(idPaciente);
//            pacientes = db.buscarPacientes();
//            String[] nomes = new String[pacientes.size()];
//            for (int i = 0; i < pacientes.size(); i++) nomes[i] = pacientes.get(i).getNome();
//
//            android.widget.ArrayAdapter<String> spinnerAdapter = new android.widget.ArrayAdapter<>(
//                    this, android.R.layout.simple_spinner_item, nomes);
//            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            binding.spinnerPaciente.setAdapter(spinnerAdapter);
//
//            binding.spinnerPaciente.setOnItemSelectedListener(
//                    new android.widget.AdapterView.OnItemSelectedListener() {
//                        @Override
//                        public void onItemSelected(android.widget.AdapterView<?> p,
//                                                   View v, int pos, long id) {
//                            pacienteSelecionadoId = pacientes.get(pos).getId();
//                            carregarHistorico(pacienteSelecionadoId);
//                        }
//
//                        @Override
//                        public void onNothingSelected(android.widget.AdapterView<?> p) {
//                        }
//                    });


            binding.fabNovoRegistro.setOnClickListener(v -> {
                if (idPaciente == -1) return;
                Intent intent = new Intent(this, RegistrarHistoricoActivity.class);
                intent.putExtra("paciente_id", idPaciente);
                startActivity(intent);
            });

//            if (!pacientes.isEmpty()) {
//                pacienteSelecionadoId = pacientes.get(0).getId();
//                carregarHistorico(pacienteSelecionadoId);
//            }
        } else {
            layoutHistorico.setVisibility(View.GONE);
            binding.fabNovoRegistro.setVisibility(View.GONE);

            //aqui simplesmente puxa o historico do paciente logado
            carregarHistorico(session.getUserId());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (idPaciente != -1) carregarHistorico(idPaciente);
    }

    private void bindViews() {
        tvGreeting = findViewById(R.id.tvGreeting);
        navHome = findViewById(R.id.navHome);
        navPerfil = findViewById(R.id.navPerfil);
        navConsultas = findViewById(R.id.navConsultas);
        navChat = findViewById(R.id.navChat);
        navHistorico = findViewById(R.id.navHistorico);
        navPacientes = findViewById(R.id.navPacientes);
        navAdmin = findViewById(R.id.navAdmin);
        navLoginBtn = findViewById(R.id.navLogin);
        layoutHistorico = findViewById(R.id.layoutHistorico);
    }

    private void carregarHistorico(int pacienteId) {
        List<HistoricoMedico> lista = db.buscarHistoricoPorPaciente(pacienteId);
        for (HistoricoMedico h : lista) {
            Usuario m = db.buscarUsuarioPorId(h.getMedicoId());
            if (m != null) h.setNomeMedico(m.getNome());
        }
        binding.tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        HistoricoMedicoAdapter adapter = new HistoricoMedicoAdapter();
        adapter.setLista(lista);
        binding.rvHistorico.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
