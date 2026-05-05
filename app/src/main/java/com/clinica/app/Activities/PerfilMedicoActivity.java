package com.clinica.app.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.databinding.ActivityPerfilMedicoBinding;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.Controle.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;


public class PerfilMedicoActivity extends AppCompatActivity {

    private ActivityPerfilMedicoBinding binding;
    private BancoDados db;
    private SessionManager session;
    private int medicoId;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;
    ShapeableImageView fotoMedico;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPerfilMedicoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = BancoDados.getInstance(this);
        session = new SessionManager(this);
        medicoId = getIntent().getIntExtra("medico_id", -1);

        if (medicoId == -1) {
            finish();
            return;
        }

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

        Usuario medico = db.buscarUsuarioPorId(medicoId);
        if (medico == null) {
            finish();
            return;
        }

        if (medico.getFotoPerfil() != null && !medico.getFotoPerfil().isEmpty()) {
            binding.tvPlaceholderFoto.setVisibility(View.GONE);
            File f = new File(medico.getFotoPerfil());
            if (f.exists()) {
                fotoMedico.setImageURI(Uri.fromFile(f));
                fotoMedico.setVisibility(View.VISIBLE);
            }
        } else {
            fotoMedico.setVisibility(View.GONE);
            binding.tvPlaceholderFoto.setVisibility(View.VISIBLE);
        }

        binding.tvNome.setText(medico.getNome());
        binding.tvEspecialidade.setText(medico.getEspecialidade() != null ? medico.getEspecialidade() : "");
        binding.tvDescricao.setText(medico.getDescricao() != null ? medico.getDescricao() : "");

        // UC005 – Ver agenda e agendar
        binding.btnVerAgenda.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendaMedicaActivity.class);
            intent.putExtra("medico_id", medicoId);
            intent.putExtra("medico_nome", medico.getNome());
            startActivity(intent);
        });

        // UC003 – Iniciar chat
        binding.btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("destinatario_id", medicoId);
            intent.putExtra("destinatario_nome", medico.getNome());
            intent.putExtra("foto_perfil", medico.getFotoPerfil());
            startActivity(intent);
        });

        if (session.isMedico()) {
            binding.btnVerAgenda.setVisibility(View.GONE);
            binding.btnChat.setVisibility(View.GONE);
        }
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
        fotoMedico = findViewById(R.id.imgPerfilMedico);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
