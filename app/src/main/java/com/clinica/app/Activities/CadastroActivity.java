package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.Utils.MascaraHelper;
import com.clinica.app.databinding.ActivityCadastroBinding;
import com.clinica.app.Modelo.Usuario;


public class CadastroActivity extends AppCompatActivity {

    private ActivityCadastroBinding binding;
    private BancoDados db;

    // barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = BancoDados.getInstance(this);

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

        ArrayAdapter<String> spinner_tipo = new ArrayAdapter<>(this,
                R.layout.item_dropdown,
                new String[]{"Paciente", "Medico"});

        spinner_tipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerTipo.setAdapter(spinner_tipo);

        ArrayAdapter<String> spinner_genero = new ArrayAdapter<>(this,
                R.layout.item_dropdown,
                new String[]{"Masculino", "Feminino"});

        spinner_genero.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerGenero.setAdapter(spinner_genero);


        binding.spinnerTipo.setOnItemClickListener((parent, view, position, id) -> {

            String tipo = binding.spinnerTipo.getText().toString().trim();

            boolean isMedico = tipo.equalsIgnoreCase("medico");

            binding.layoutMedico.setVisibility(isMedico ? View.VISIBLE : View.GONE);
        });

        binding.btnCadastrar.setOnClickListener(v -> realizarCadastro());
        binding.tvLogin.setOnClickListener(v -> finish());

        MascaraHelper.CpfMask(binding.etCpf);
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

    private void realizarCadastro() {
        String nome = binding.etNome.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String senha = binding.etSenha.getText().toString().trim();
        String cpf = binding.etCpf.getText().toString().trim();
        String tipo = binding.spinnerTipo.getText().toString();// 0=paciente, 1=medico
        String genero = binding.spinnerGenero.getText().toString();
        String especialidade = binding.etEspecialidade.getText().toString().trim();
        String descricao = binding.etDescricao.getText().toString().trim();
        String crm = binding.etCRM.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();

        // Fluxo de exceção – campos em branco
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(senha) || TextUtils.isEmpty(cpf) || TextUtils.isEmpty(username) ) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipo.equalsIgnoreCase("medico") && TextUtils.isEmpty(especialidade)) {
            Toast.makeText(this, "Informe a especialidade.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tipo.equalsIgnoreCase("medico") && TextUtils.isEmpty(crm)) {
            Toast.makeText(this, "Informe o CRM.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.emailExiste(email)) {
            Toast.makeText(this, "E-mail já cadastrado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (db.cpfExiste(cpf)) {
            Toast.makeText(this, "CPF já cadastrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (db.usernameExiste(username)){
            Toast.makeText(this, "Username já cadastrado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Usuario u = new Usuario();
        u.setNome(nome);
        u.setEmail(email);
        u.setSenha(senha);
        u.setCpf(cpf);
        u.setTipo(tipo.toLowerCase());
        u.setEspecialidade(especialidade);
        u.setDescricao(descricao);
        u.setCRM(crm);
        u.setGenero(genero);
        u.setUsername(username);

        long id = db.cadastrarUsuario(u);
        if (id > 0) {
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Erro ao cadastrar. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
}
