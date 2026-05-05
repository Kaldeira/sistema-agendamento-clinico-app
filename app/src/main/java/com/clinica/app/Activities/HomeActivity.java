package com.clinica.app.Activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.DAO.MedicoCardAdapter;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.List;


public class HomeActivity extends AppCompatActivity {

    private BancoDados    db;
    private SessionManager session;
    private MedicoCardAdapter adapter;

    // Barra de botoes nav
    private LinearLayout  navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View          navLoginBtn;
    private TextView      tvGreeting, tvIniciaisUser;
    ShapeableImageView sivFotoPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db      = BancoDados.getInstance(this);
        session = new SessionManager(this);

        bindViews();
        setupSearch();
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
        carregarMedicos("");

        tvGreeting.setText(session.isLogado()
                ? "Olá, " + primeiroNome(session.getNome())
                : "Encontre seu médico");

        String fotoPerfil = session.getFotoPerfil();

        if (session.isLogado()) {
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                tvIniciaisUser.setVisibility(View.GONE);
                File f = new File(fotoPerfil);
                if (f.exists()) {
                    sivFotoPerfil.setImageURI(Uri.fromFile(f));
                    sivFotoPerfil.setVisibility(View.VISIBLE);

                    sivFotoPerfil.setOnClickListener(v -> this.startActivity(new Intent(this, PerfilActivity.class)));
                }
            } else {
                sivFotoPerfil.setVisibility(View.GONE);
                tvIniciaisUser.setVisibility(View.VISIBLE);
                tvIniciaisUser.setOnClickListener(v -> this.startActivity(new Intent(this, PerfilActivity.class)));
                tvIniciaisUser.setText(session.getIniciais());
            }
        } else {
            tvIniciaisUser.setVisibility(View.GONE);
            sivFotoPerfil.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        tvGreeting.setText(session.isLogado()
                ? "Olá, " + primeiroNome(session.getNome())
                : "Encontre seu médico");
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
        sivFotoPerfil = findViewById(R.id.imgUserFoto);
        tvIniciaisUser = findViewById(R.id.tvIniciaisUser);

        RecyclerView rv = findViewById(R.id.rvMedicos);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MedicoCardAdapter(medico -> {
            if (!session.isLogado()) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

//            if (session.isMedico()) {
//                return;
//            }
            Intent intent = new Intent(this, PerfilMedicoActivity.class);
            intent.putExtra("medico_id", medico.getId());
            startActivity(intent);
        });
        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        EditText etBusca = findViewById(R.id.etBusca);
        etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarMedicos(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarMedicos(String filtro) {
        List<Usuario> medicos = db.buscarMedicos(filtro);
        adapter.setLista(medicos);
    }

    private String primeiroNome(String nome) {
        if (nome == null) return "";
        String[] parts = nome.split(" ");
        return parts[0];
    }
}
