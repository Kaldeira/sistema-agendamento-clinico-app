package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.DAO.PacienteCardAdapter;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityProntuarioPacientesBinding;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class ProntuarioPacienteActivity extends AppCompatActivity {

    ActivityProntuarioPacientesBinding binding;
    PacienteCardAdapter adapter;
    private BancoDados    db;
    private SessionManager session;

    // Barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting, tvIniciaisUser;
    ShapeableImageView sivFotoPerfil;

    protected void onCreate(Bundle savedInstanceState)
    {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_prontuario_pacientes);

        db      = BancoDados.getInstance(this);
        session = new SessionManager(this);

        bindViews();
        setupSearch();
        BarraNavHelper.setupBottomNav(this,
                findViewById(R.id.navHome),
                findViewById(R.id.navPerfil),
                findViewById(R.id.navConsultas),
                findViewById(R.id.navChat),
                findViewById(R.id.navHistorico),
                findViewById(R.id.navPacientes),
                findViewById(R.id.navAdmin),
                findViewById(R.id.navLogin));


        carregarPacientes("");
    }

    private void bindViews()
    {
        navHome       = findViewById(R.id.navHome);
        navPerfil     = findViewById(R.id.navPerfil);
        navConsultas  = findViewById(R.id.navConsultas);
        navChat       = findViewById(R.id.navChat);
        navHistorico  = findViewById(R.id.navHistorico);
        navPacientes  = findViewById(R.id.navPacientes);
        navAdmin      = findViewById(R.id.navAdmin);
        navLoginBtn   = findViewById(R.id.navLogin);
        sivFotoPerfil = findViewById(R.id.imgUserFoto);
        tvIniciaisUser = findViewById(R.id.tvIniciaisUser);

        RecyclerView rv = findViewById(R.id.rvUsers);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PacienteCardAdapter(paciente -> {
            if (!session.isLogado()) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            Intent intent = new Intent(this, HistoricoMedicoActivity.class);
            intent.putExtra("paciente_id", paciente.getId());
            startActivity(intent);
        });

        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        EditText etBusca = findViewById(R.id.etBusca);
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarPacientes(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarPacientes(String filtro) {
        List<Usuario> medicos = db.buscarPacientes(filtro);
        adapter.setLista(medicos);
    }
}
