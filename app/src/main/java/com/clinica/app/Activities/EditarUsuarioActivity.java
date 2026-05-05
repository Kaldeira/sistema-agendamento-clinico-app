package com.clinica.app.Activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;


public class EditarUsuarioActivity extends AppCompatActivity {

    private BancoDados db;
    private Usuario    usuario;

    private EditText      etNome, etEmail, etSenha, etEspecialidade, etDescricao, etCRM, etUsername;
    private CardView layoutMedico;
    private TextView      tvTipo;
    private ImageButton btnVoltar;
    private MaterialAutoCompleteTextView spinnerGenero, spinnerTipo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_usuario);

        db = BancoDados.getInstance(this);

        int usuarioId = getIntent().getIntExtra("usuario_id", -1);
        if (usuarioId == -1) { finish(); return; }

        usuario = db.buscarUsuarioPorId(usuarioId);
        if (usuario == null) { finish(); return; }

        bindViews();
        preencherCampos();

        ArrayAdapter<String> spinner_tipo = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Paciente", "Medico"});

        spinner_tipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(spinner_tipo);

        ArrayAdapter<String> spinner_genero = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Masculino", "Feminino"});

        spinner_genero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGenero.setAdapter(spinner_genero);

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvar());

        Button btnCancelar = findViewById(R.id.btnCancelar);
        btnCancelar.setOnClickListener(v -> finish());

        btnVoltar.setOnClickListener(v-> finish());
    }

    private void bindViews() {
        tvTipo         = findViewById(R.id.tvTipoUsuario);
        etNome         = findViewById(R.id.etNome);
        etEmail        = findViewById(R.id.etEmail);
        etSenha        = findViewById(R.id.etSenha);
        etEspecialidade= findViewById(R.id.etEspecialidade);
        etDescricao    = findViewById(R.id.etDescricao);
        layoutMedico   = findViewById(R.id.layoutMedico);
        btnVoltar      = findViewById(R.id.btnVoltar);
        spinnerGenero  = findViewById(R.id.spinnerGenero);
        spinnerTipo    = findViewById(R.id.spinnerTipo);
        etCRM          = findViewById(R.id.etCRM);
        etUsername     = findViewById(R.id.etUsername);
    }

    private void preencherCampos() {
        tvTipo.setText(tipoLabel(usuario.getTipo()));
        etNome.setText(usuario.getNome());
        etEmail.setText(usuario.getEmail());
        etSenha.setText(usuario.getSenha());
        spinnerTipo.setText(usuario.getTipo());
        spinnerGenero.setText(usuario.getGenero());
        etUsername.setText(usuario.getUsername());

        if (usuario.isMedico()) {
            layoutMedico.setVisibility(android.view.View.VISIBLE);
            etEspecialidade.setText(usuario.getEspecialidade());
            etDescricao.setText(usuario.getDescricao());
            etCRM.setText(usuario.getCRM());
        } else {
            layoutMedico.setVisibility(android.view.View.GONE);
        }
    }

    private void salvar() {
        String nome  = etNome.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String genero = spinnerGenero.getText().toString().trim();
        String tipo = spinnerTipo.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty() || username.isEmpty()) {
            Snackbar.make(etNome, "Nome, e-mail, username e senha são obrigatórios",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(senha);
        usuario.setGenero(genero.toLowerCase());
        usuario.setTipo(tipo.toLowerCase());
        usuario.setUsername(username);

        if (usuario.isMedico()) {
            usuario.setEspecialidade(etEspecialidade.getText().toString().trim());
            usuario.setDescricao(etDescricao.getText().toString().trim());
            usuario.setCRM(etCRM.getText().toString().trim());
        }

        if (db.atualizarUsuario(usuario)) {
            setResult(RESULT_OK);
            finish();
        } else {
            Snackbar.make(etNome, "Erro ao salvar. E-mail pode já estar em uso.",
                    Snackbar.LENGTH_SHORT).show();
        }
    }

    private String tipoLabel(String tipo) {
        switch (tipo != null ? tipo : "") {
            case "medico":   return "Médico";
            case "paciente": return "Paciente";
            default:         return tipo;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
