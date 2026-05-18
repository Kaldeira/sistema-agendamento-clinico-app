package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.DAO.AdminUsuarioAdapter;
import com.clinica.app.DAO.AdminConsultaAdapter;
import com.clinica.app.DAO.AdminPagamentoAdapter;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.clinica.app.Utils.MercadoPagoService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {

    private BancoDados    db;
    private SessionManager session;

    // Tab content containers
    private RecyclerView rvUsuarios, rvConsultas, rvPagamentos;
    private android.widget.ScrollView layoutMpConfig;

    // Stats
    private TextView tvStatMedicos, tvStatPacientes, tvStatConsultas, tvStatPagamentos;

    // MP Config fields
    private EditText    etAccessToken, etPublicKey;
    private RadioGroup  rgAmbiente;
    private RadioButton rbTeste, rbProducao;
    private TextView    tvMpStatus;

    // Barra de botoes nav
    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout  navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db      = BancoDados.getInstance(this);
        session = new SessionManager(this);

        if (!session.isAdmin()) { finish(); return; }

        bindViews();
        setupTabs();
        carregarStats();
        carregarUsuarios();

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

        // Header
        TextView tvBemVindo = findViewById(R.id.tvBemVindo);
        tvBemVindo.setText("Painel Admin — " + session.getNome());

        Button btnSair = findViewById(R.id.btnSair);
        if (btnSair != null) btnSair.setOnClickListener(v -> {
            session.encerrarSessao();
            Intent i = new Intent(this, HomeActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        });
    }

    private void bindViews() {
        rvUsuarios      = findViewById(R.id.rvUsuarios);
        rvConsultas     = findViewById(R.id.rvConsultas);
        rvPagamentos    = findViewById(R.id.rvPagamentos);
        layoutMpConfig  = findViewById(R.id.layoutMpConfig);

        tvStatMedicos   = findViewById(R.id.tvStatMedicos);
        tvStatPacientes = findViewById(R.id.tvStatPacientes);
        tvStatConsultas = findViewById(R.id.tvStatConsultas);
        tvStatPagamentos= findViewById(R.id.tvStatPagamentos);

        tvGreeting    = findViewById(R.id.tvGreeting);
        navHome       = findViewById(R.id.navHome);
        navPerfil     = findViewById(R.id.navPerfil);
        navConsultas  = findViewById(R.id.navConsultas);
        navChat       = findViewById(R.id.navChat);
        navHistorico  = findViewById(R.id.navHistorico);
        navPacientes  = findViewById(R.id.navPacientes);
        navAdmin      = findViewById(R.id.navAdmin);
        navLoginBtn   = findViewById(R.id.navLogin);

        // MP Config
        etAccessToken = findViewById(R.id.etAccessToken);
        etPublicKey   = findViewById(R.id.etPublicKey);
        rgAmbiente    = findViewById(R.id.rgAmbiente);
        rbTeste       = findViewById(R.id.rbTeste);
        rbProducao    = findViewById(R.id.rbProducao);
        tvMpStatus    = findViewById(R.id.tvMpStatus);


        rvUsuarios.setLayoutManager(new LinearLayoutManager(this));
        rvConsultas.setLayoutManager(new LinearLayoutManager(this));
        rvPagamentos.setLayoutManager(new LinearLayoutManager(this));


        Button btnSalvarMP = findViewById(R.id.btnSalvarMP);
        if (btnSalvarMP != null) btnSalvarMP.setOnClickListener(v -> salvarConfigMP());
        Button btnLimparMP = findViewById(R.id.btnLimparMP);
        if (btnLimparMP != null) btnLimparMP.setOnClickListener(v -> limparConfigMP());

        if (rgAmbiente != null)
            rgAmbiente.setOnCheckedChangeListener((g, id) -> atualizarStatusMP());

        carregarConfigMP();
    }

    private void setupTabs() {
        TabLayout tabs = findViewById(R.id.tabLayout);
        if (tabs == null) return;

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                ocultarTodos();
                switch (tab.getPosition()) {
                    case 0: rvUsuarios.setVisibility(android.view.View.VISIBLE); carregarUsuarios(); break;
                    case 1: rvConsultas.setVisibility(android.view.View.VISIBLE); carregarConsultas(); break;
                    case 2: rvPagamentos.setVisibility(android.view.View.VISIBLE); carregarPagamentos(); break;
                    case 3: layoutMpConfig.setVisibility(android.view.View.VISIBLE); break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void ocultarTodos() {
        rvUsuarios.setVisibility(android.view.View.GONE);
        rvConsultas.setVisibility(android.view.View.GONE);
        rvPagamentos.setVisibility(android.view.View.GONE);
        layoutMpConfig.setVisibility(android.view.View.GONE);
    }

    private void carregarStats() {
        int medicos    = db.buscarMedicos(null).size();
        int pacientes  = db.buscarPacientes().size();
        int consultas  = db.buscarTodasConsultas().size();
        int pagamentos = db.buscarTodosPagamentos().size();

        if (tvStatMedicos   != null) tvStatMedicos.setText(String.valueOf(medicos));
        if (tvStatPacientes != null) tvStatPacientes.setText(String.valueOf(pacientes));
        if (tvStatConsultas != null) tvStatConsultas.setText(String.valueOf(consultas));
        if (tvStatPagamentos!= null) tvStatPagamentos.setText(String.valueOf(pagamentos));
    }

    private void carregarUsuarios() {
        List<Usuario> usuarios = db.buscarTodosUsuarios();
        AdminUsuarioAdapter adapter = new AdminUsuarioAdapter(usuarios,
                this::editarUsuario,
                usuario -> confirmarDelecao(usuario));
        rvUsuarios.setAdapter(adapter);
    }

    private void editarUsuario(Usuario u) {
        Intent intent = new Intent(this, EditarUsuarioActivity.class);
        intent.putExtra("usuario_id", u.getId());
        startActivityForResult(intent, 100);
    }

    private void confirmarDelecao(Usuario u) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar usuário")
                .setMessage("Deseja remover " + u.getNome() + " permanentemente?")
                .setPositiveButton("Deletar", (d, w) -> {
                    if (db.deletarUsuario(u.getId())) {
                        carregarUsuarios();
                        carregarStats();
                        Snackbar.make(rvUsuarios, "Usuário removido.", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void carregarConsultas() {
        List<Consulta> consultas = db.buscarTodasConsultas();
        AdminConsultaAdapter adapter = new AdminConsultaAdapter(this, consultas);
        rvConsultas.setAdapter(adapter);
    }

    private void carregarPagamentos() {
        List<Pagamento> pagamentos = db.buscarTodosPagamentos();
        AdminPagamentoAdapter adapter = new AdminPagamentoAdapter(pagamentos);
        rvPagamentos.setAdapter(adapter);
    }

    private void carregarConfigMP() {
        if (etAccessToken == null) return;
        android.content.SharedPreferences prefs =
                getSharedPreferences(MercadoPagoService.PREF_MP, android.content.Context.MODE_PRIVATE);
        String token   = prefs.getString(MercadoPagoService.KEY_ACCESS_TOKEN, "");
        String key     = prefs.getString(MercadoPagoService.KEY_PUBLIC_KEY, "");
        boolean sandbox = prefs.getBoolean(MercadoPagoService.KEY_IS_SANDBOX, true);

        etAccessToken.setText(token);
        etPublicKey.setText(key);
        if (rbTeste != null) rbTeste.setChecked(sandbox);
        if (rbProducao != null) rbProducao.setChecked(!sandbox);
        atualizarStatusMP();
    }

    private void atualizarStatusMP() {
        if (rbTeste == null || tvMpStatus == null) return;
        boolean sandbox = rbTeste.isChecked();
        tvMpStatus.setText(sandbox
                ? "🟡 Ambiente de Teste — sem cobranças reais"
                : "🟢 Ambiente de Produção — pagamentos reais");
        tvMpStatus.setTextColor(sandbox ? 0xFFFFC107 : 0xFF4CAF50);
    }

    private void salvarConfigMP() {
        if (etAccessToken == null) return;
        String token = etAccessToken.getText().toString().trim();
        String key   = etPublicKey.getText().toString().trim();

        if (token.isEmpty() || key.isEmpty()) {
            Snackbar.make(layoutMpConfig, "Preencha Access Token e Public Key",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        boolean sandbox = rbTeste != null && rbTeste.isChecked();
        MercadoPagoService.salvarCredenciais(this, token, key, sandbox);
        Snackbar.make(layoutMpConfig, "✅ Credenciais salvas!", Snackbar.LENGTH_LONG).show();
    }

    private void limparConfigMP() {
        getSharedPreferences(MercadoPagoService.PREF_MP, android.content.Context.MODE_PRIVATE)
                .edit().clear().apply();
        if (etAccessToken != null) etAccessToken.setText("");
        if (etPublicKey   != null) etPublicKey.setText("");
        if (rbTeste       != null) rbTeste.setChecked(true);
        atualizarStatusMP();
        Snackbar.make(layoutMpConfig, "Configurações limpas", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            carregarUsuarios();
            carregarStats();
        }
    }
}
