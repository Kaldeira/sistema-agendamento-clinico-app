package com.clinica.app.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Controle.NotificacaoReceiver;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.DAO.MedicoCardAdapter;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.Utils.FirebaseSeeder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

public class HomeActivity extends AppCompatActivity {

    private static boolean firestoreCacheConfigurado = false;

    private FirebaseManager fb;
    private SessionManager session;
    private MedicoCardAdapter adapter;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting, tvIniciaisUser;
    private ShapeableImageView sivFotoPerfil;

    private String ultimoFiltro = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configurarCacheFirestore();
        pedirPermissaoNotificacao();

        setContentView(R.layout.activity_home);

        session = new SessionManager(this);
        fb = FirebaseManager.getInstance();
        NotificacaoReceiver.iniciarNotificacoesChat(this, fb, session);
        NotificacaoReceiver.iniciarNotificacoesConsultasMedico(this, fb, session);
        //aqui cria um ListenerRegistration no Firebase que executa um evento quando há mudanças no banco

//        new FirebaseSeeder().popularBanco(sucesso -> {
//            runOnUiThread(() -> {
//                if (sucesso) {
//                    Toast.makeText(this, "Firebase populado com sucesso!", Toast.LENGTH_LONG).show();
//                } else {
//                    Toast.makeText(this, "Erro ao popular Firebase.", Toast.LENGTH_LONG).show();
//                }
//            });
//        });

        if (session.isMedico()) {
           TextView greetingMsg = findViewById(R.id.greetingMsg);
            greetingMsg.setText("Bem vindo de volta!");
        }

        bindViews();
        setupSearch();
        configurarBottomNav();

        carregarMedicos("");

        atualizarHeaderUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();

        configurarBottomNav();
        atualizarHeaderUsuario();
    }

    private void configurarCacheFirestore() {
        if (firestoreCacheConfigurado) return;

        try {
            FirebaseFirestoreSettings settings =
                    new FirebaseFirestoreSettings.Builder()
                            .setPersistenceEnabled(true)
                            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                            .build();

            FirebaseFirestore.getInstance().setFirestoreSettings(settings);

            firestoreCacheConfigurado = true;

        } catch (Exception e) {
            firestoreCacheConfigurado = true;
        }
    }

    private void configurarBottomNav() {
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
        sivFotoPerfil = findViewById(R.id.imgUserFoto);
        tvIniciaisUser = findViewById(R.id.tvIniciaisUser);

        RecyclerView rv = findViewById(R.id.rvMedicos);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);

        adapter = new MedicoCardAdapter(this, medico -> {
            if (!session.isLogado()) {
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            if (!session.getAprovado()) {
                Toast.makeText(
                        this,
                        "Sua conta ainda não foi aprovada pelo administrador.",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }

            Intent intent = new Intent(this, PerfilMedicoActivity.class);
            intent.putExtra("medico_username", medico.getUsername());
            startActivity(intent);
        });

        rv.setAdapter(adapter);
    }

    private void setupSearch() {
        EditText etBusca = findViewById(R.id.etBusca);

        etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String filtro = s.toString().trim();

                if (filtro.equalsIgnoreCase(ultimoFiltro)) return;

                ultimoFiltro = filtro;
                carregarMedicos(filtro);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarMedicos(String filtro) {
        fb.buscarMedicos(filtro, medicos ->
                runOnUiThread(() -> adapter.setLista(medicos))
        );
    }

    private void atualizarHeaderUsuario() {
        tvGreeting.setText(session.isLogado()
                ? "Olá, " + primeiroNome(session.getNome())
                : "Encontre seu médico");

        String fotoPerfil = session.getFotoPerfil();

        if (session.isLogado()) {
            if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
                tvIniciaisUser.setVisibility(View.GONE);
                sivFotoPerfil.setVisibility(View.VISIBLE);

                Glide.with(this)
                        .load(fotoPerfil)
                        .circleCrop()
                        .placeholder(R.drawable.ic_menu_person)
                        .error(R.drawable.ic_menu_person)
                        .into(sivFotoPerfil);

                sivFotoPerfil.setOnClickListener(v ->
                        startActivity(new Intent(this, PerfilActivity.class)));

            } else {
                sivFotoPerfil.setVisibility(View.GONE);
                tvIniciaisUser.setVisibility(View.VISIBLE);
                tvIniciaisUser.setText(session.getIniciais());

                tvIniciaisUser.setOnClickListener(v ->
                        startActivity(new Intent(this, PerfilActivity.class)));
            }
        } else {
            tvIniciaisUser.setVisibility(View.GONE);
            sivFotoPerfil.setVisibility(View.GONE);
        }
    }

    private void pedirPermissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }
    }

    private String primeiroNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        return nome.trim().split(" ")[0];
    }
}