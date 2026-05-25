package com.clinica.app.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.MascaraHelper;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import com.clinica.app.Utils.BarraNavHelper;

public class PerfilActivity extends AppCompatActivity {

    private static final int REQ_GALLERY = 101;

    private BancoDados    db;
    private SessionManager session;
    private Usuario       usuario;

    private ImageView  ivFoto;
    private TextView   tvIniciais;
    private EditText   etNome, etEmail, etCpf, etEspecialidade, etDescricao, etGenero, etCRM, etUsername, etSenha;
    private TextView   tvTipo;
    private androidx.cardview.widget.CardView layoutMedico;

    private ActivityResultLauncher<Intent> galleryLauncher;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View          navLoginBtn;
    private TextView      tvGreeting, tvNome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        db      = BancoDados.getInstance(this);
        session = new SessionManager(this);

        if (!session.isLogado()) { finish(); return; }

        bindViews();
        registerGalleryLauncher();

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

        carregarPerfil();

        ivFoto.setOnClickListener(v -> abrirGaleria());
        tvIniciais.setOnClickListener(v -> abrirGaleria());

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvarPerfil());

        Button btnSair = findViewById(R.id.btnSair);
        btnSair.setOnClickListener(v -> logout());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        ivFoto         = findViewById(R.id.ivFotoPerfil);
        tvIniciais     = findViewById(R.id.tvIniciais);
        tvTipo         = findViewById(R.id.tvTipo);
        etNome         = findViewById(R.id.etNome);
        etEmail        = findViewById(R.id.etEmail);
        etCpf          = findViewById(R.id.etCpf);
        etEspecialidade= findViewById(R.id.etEspecialidade);
        etDescricao    = findViewById(R.id.etDescricao);
        etGenero       = findViewById(R.id.etGenero);
        etCRM          = findViewById(R.id.etCRM);
        etUsername     = findViewById(R.id.etUsername);
        etSenha        = findViewById(R.id.etPassWord);
        layoutMedico   = findViewById(R.id.layoutMedico);
        tvNome         = findViewById(R.id.tvNome);

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

    private void carregarPerfil() {
        usuario = db.buscarUsuarioPorId(session.getUserId());
        if (usuario == null) { finish(); return; }

        etNome.setText(usuario.getNome());
        tvNome.setText(usuario.getNome());
        etEmail.setText(usuario.getEmail());
        etCpf.setText(usuario.getCpf());
        tvTipo.setText(tipoLabel(usuario.getTipo()));
        String genero = usuario.getGenero();
        etUsername.setText(usuario.getUsername());
        etSenha.setText(usuario.getSenha());

     //   Log.d("DEBUG", "Genero: " + usuario.getGenero());

        if (genero != null && !genero.isEmpty()) {
            genero = genero.substring(0, 1).toUpperCase() + genero.substring(1).toLowerCase();
            etGenero.setText(genero);
        }

        if (usuario.isMedico()) {
            layoutMedico.setVisibility(android.view.View.VISIBLE);
            etEspecialidade.setText(usuario.getEspecialidade());
            etDescricao.setText(usuario.getDescricao());
            etCRM.setText(usuario.getCRM());
        } else {
            layoutMedico.setVisibility(android.view.View.GONE);
        }

        if (usuario.getFotoPerfil() != null && !usuario.getFotoPerfil().isEmpty()) {
            File f = new File(usuario.getFotoPerfil());
            if (f.exists()) {
                ivFoto.setImageURI(Uri.fromFile(f));
                tvIniciais.setVisibility(android.view.View.GONE);
                ivFoto.setVisibility(android.view.View.VISIBLE);
                return;
            }
        }
        ivFoto.setVisibility(android.view.View.GONE);
        tvIniciais.setVisibility(android.view.View.VISIBLE);
        tvIniciais.setText(usuario.getIniciais());
    }

    private void salvarPerfil() {
        String nome  = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String novaSenha = etSenha.getText().toString().trim();
        String senhaAtual = session.getSenha();

        if (nome.isEmpty() || email.isEmpty()) {
            Snackbar.make(etNome, "Nome e e-mail são obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (!senhaAtual.equals(novaSenha)) {

            if (novaSenha.isEmpty()) {
                Snackbar.make(etSenha, "Senha não pode ser vazio!", Snackbar.LENGTH_SHORT).show();
                return;
            }
            usuario.setSenha(novaSenha);
        }

        usuario.setNome(nome);
        usuario.setEmail(email);

        if (usuario.isMedico()) {
            usuario.setEspecialidade(etEspecialidade.getText().toString().trim());
            usuario.setDescricao(etDescricao.getText().toString().trim());
        }

        if (db.atualizarUsuario(usuario)) {
            // Update session name in case it changed
            session.criarSessao(usuario.getId(), usuario.getNome(),
                    usuario.getTipo(), usuario.getEmail(), usuario.getFotoPerfil(), usuario.getUsername(), usuario.getSenha());
            Snackbar.make(etNome, "✅ Perfil atualizado!", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(etNome, "Erro ao salvar. Tente novamente.", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void registerGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) salvarFotoLocal(uri);
                    }
                });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void salvarFotoLocal(Uri uri) {
        try {
            File dir = new File(getFilesDir(), "fotos");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, "perfil_" + session.getUserId() + ".jpg");

            try (InputStream in  = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            }

            usuario.setFotoPerfil(dest.getAbsolutePath());
            db.atualizarFotoPerfil(session.getUserId(), dest.getAbsolutePath());

            ivFoto.setImageURI(Uri.fromFile(dest));
            ivFoto.setVisibility(android.view.View.VISIBLE);
            tvIniciais.setVisibility(android.view.View.GONE);

        } catch (Exception e) {
            Toast.makeText(this, "Erro ao salvar foto.", Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Logout ───────────────────────────────────────────────────────────────

    private void logout() {
        session.encerrarSessao();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String tipoLabel(String tipo) {
        switch (tipo != null ? tipo : "") {
            case "medico":   return "Médico";
            case "paciente": return "Paciente";
            case "admin":    return "Administrador";
            default:         return tipo;
        }
    }
}
