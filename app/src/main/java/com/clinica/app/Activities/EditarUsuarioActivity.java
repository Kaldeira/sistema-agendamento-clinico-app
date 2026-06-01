package com.clinica.app.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.MascaraHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

public class EditarUsuarioActivity extends AppCompatActivity {

    private FirebaseManager fb;
    private Usuario usuario;

    private EditText      etNome, etEmail, etSenha, etEspecialidade, etDescricao, etCRM, etUsername, etEspecialidade2, etDescricao2, etCRM2, etCPF;
    private CardView      layoutMedico, layoutAprovacao, layoutInfo;
    private TextView      tvTipo;
    private ImageButton   btnVoltar;
    private Button        btnSalvar;
    private MaterialAutoCompleteTextView spinnerGenero, spinnerTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        fb = FirebaseManager.getInstance();

        String usuarioUsername = getIntent().getStringExtra("usuario_username");
        if (usuarioUsername == null || usuarioUsername.isEmpty()) { finish(); return; }

        bindViews();

        fb.buscarUsuarioPorUsername(usuarioUsername, u -> runOnUiThread(() -> {
            if (u == null) { finish(); return; }
            this.usuario = u;
            preencherCampos();
        }));

        String[] tipos = {"paciente", "medico"};
        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                tipos
        );
        spinnerTipo.setAdapter(adapterTipo);

        String[] generos = {"Masculino", "Feminino"};
        ArrayAdapter<String> adapterGenero = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                generos
        );
        spinnerGenero.setAdapter(adapterGenero);

        Button btnCancelar = findViewById(R.id.btnCancelar);
        Button btnAprovar = findViewById(R.id.btnAprovar);
        btnSalvar.setOnClickListener(v -> salvar());
        btnAprovar.setOnClickListener(v -> aprovar());
        btnCancelar.setOnClickListener(v -> finish());
        btnVoltar.setOnClickListener(v -> finish());

        MascaraHelper.CpfMask(etCPF);
        MascaraHelper.CrmMask(etCRM);
    }

    private void bindViews() {
        tvTipo           = findViewById(R.id.tvTipoUsuario);
        etNome           = findViewById(R.id.etNome);
        etEmail          = findViewById(R.id.etEmail);
        etCPF            = findViewById(R.id.etCPF);
        etSenha          = findViewById(R.id.etSenha);
        etEspecialidade  = findViewById(R.id.etEspecialidade);
        etDescricao      = findViewById(R.id.etDescricao);
        layoutMedico     = findViewById(R.id.layoutMedico);
        layoutAprovacao  = findViewById(R.id.layoutAprovacao);
        layoutInfo       = findViewById(R.id.layoutInfo);
        btnVoltar        = findViewById(R.id.btnVoltar);
        spinnerGenero    = findViewById(R.id.spinnerGenero);
        spinnerTipo      = findViewById(R.id.spinnerTipo);
        etCRM            = findViewById(R.id.etCRM);
        etUsername       = findViewById(R.id.etUsername);
        etEspecialidade2 = findViewById(R.id.etEspecialidade2);
        etDescricao2     = findViewById(R.id.etDescricao2);
        etCRM2           = findViewById(R.id.etCRM2);
        btnSalvar        = findViewById(R.id.btnSalvar);
    }

    private void preencherCampos() {
        tvTipo.setText(tipoLabel(usuario.getTipo()));
        etNome.setText(usuario.getNome());
        etEmail.setText(usuario.getEmail());
        etSenha.setText(usuario.getSenha());
        spinnerTipo.setText(tipoLabel(usuario.getTipo()), false);
        spinnerGenero.setText(usuario.getGenero(), false);
        etCPF.setText(usuario.getCpf());
        etUsername.setText(usuario.getUsername());
        etUsername.setEnabled(false);

        if (usuario.isMedico()) {
            layoutMedico.setVisibility(View.VISIBLE);
            etEspecialidade.setText(usuario.getEspecialidade());
            etDescricao.setText(usuario.getDescricao());
            etCRM.setText(usuario.getCRM());
        } else {
            layoutMedico.setVisibility(View.GONE);
        }

        if (!usuario.getAprovado()) {
            layoutAprovacao.setVisibility(View.VISIBLE);
            layoutMedico.setVisibility(View.GONE);
            layoutInfo.setVisibility(View.GONE);
            btnSalvar.setVisibility(View.GONE);

            etEspecialidade2.setText(usuario.getEspecialidade());
            etEspecialidade2.setEnabled(false);
            etDescricao2.setText(usuario.getDescricao());
            etDescricao2.setEnabled(false);
            etCRM2.setText(usuario.getCRM());
            etCRM2.setEnabled(false);
        }
    }

    private void aprovar() {
        String nome     = etNome.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String senha    = etSenha.getText().toString().trim();
        String genero   = spinnerGenero.getText().toString().trim();
        String tipo     = spinnerTipo.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Snackbar.make(etNome, "Nome, e-mail e senha são obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setGenero(genero.toLowerCase());
        usuario.setTipo(tipo.toLowerCase());
        usuario.setAprovado(true);

        if (usuario.isMedico()) {
            usuario.setEspecialidade(etEspecialidade.getText().toString().trim());
            usuario.setDescricao(etDescricao.getText().toString().trim());
            usuario.setCRM(etCRM.getText().toString().trim());
        }

        fb.atualizarUsuario(usuario, ok -> runOnUiThread(() -> {
            if (ok) {
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar.make(etNome, "Erro ao efetuar aprovação do usuario!",
                        Snackbar.LENGTH_SHORT).show();
            }
        }));
    }

    private void salvar() {
        String nome     = etNome.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String senha    = etSenha.getText().toString().trim();
        String genero   = spinnerGenero.getText().toString().trim();
        String tipo     = spinnerTipo.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Snackbar.make(etNome, "Nome, e-mail e senha são obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setGenero(genero.toLowerCase());
        usuario.setTipo(tipo.toLowerCase());

        if (usuario.isMedico()) {
            usuario.setEspecialidade(etEspecialidade.getText().toString().trim());
            usuario.setDescricao(etDescricao.getText().toString().trim());
            usuario.setCRM(etCRM.getText().toString().trim());
        }

        fb.atualizarUsuario(usuario, ok -> runOnUiThread(() -> {
            if (ok) {
                setResult(RESULT_OK);
                finish();
            } else {
                Snackbar.make(etNome, "Erro ao salvar. E-mail pode já estar em uso.",
                        Snackbar.LENGTH_SHORT).show();
            }
        }));
    }

    private String tipoLabel(String tipo) {
        if (tipo == null) return "";
        switch (tipo) {
            case "medico":   return "Médico";
            case "paciente": return "Paciente";
            default:         return tipo;
        }
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}