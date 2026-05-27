package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.clinica.app.Controle.FirebaseManager;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdminDashboardActivity extends AppCompatActivity {

    private FirebaseManager fb;
    private SessionManager session;

    private RecyclerView rvUsuarios, rvConsultas, rvPagamentos;
    private android.widget.ScrollView layoutMpConfig;

    private List<Pagamento> listaOriginalPagamentos = new ArrayList<>();
    private AdminPagamentoAdapter pagamentoAdapter;
    MaterialAutoCompleteTextView spinnerMetodoPagamento, spinnerStatusPagamento;

    private TextView tvStatMedicos, tvStatPacientes, tvStatConsultas, tvStatPagamentos;

    private EditText    etAccessToken, etPublicKey;
    private RadioGroup  rgAmbiente;
    private RadioButton rbTeste, rbProducao;
    private TextView    tvMpStatus;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico, layoutPagamentos, layoutFiltros;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        fb      = FirebaseManager.getInstance();
        session = new SessionManager(this);

        if (!session.isAdmin()) { finish(); return; }

        bindViews();
        setupTabs();
        carregarStats();
        carregarUsuarios();
        filtroPagamento();

        BarraNavHelper.setupBottomNav(this,
                findViewById(R.id.navHome), findViewById(R.id.navPerfil),
                findViewById(R.id.navConsultas), findViewById(R.id.navChat),
                findViewById(R.id.navHistorico), findViewById(R.id.navPacientes),
                findViewById(R.id.navAdmin), findViewById(R.id.navLogin));

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

        tvStatMedicos    = findViewById(R.id.tvStatMedicos);
        tvStatPacientes  = findViewById(R.id.tvStatPacientes);
        tvStatConsultas  = findViewById(R.id.tvStatConsultas);
        tvStatPagamentos = findViewById(R.id.tvStatPagamentos);

        tvGreeting   = findViewById(R.id.tvGreeting);
        navHome      = findViewById(R.id.navHome);
        navPerfil    = findViewById(R.id.navPerfil);
        navConsultas = findViewById(R.id.navConsultas);
        navChat      = findViewById(R.id.navChat);
        navHistorico = findViewById(R.id.navHistorico);
        navPacientes = findViewById(R.id.navPacientes);
        navAdmin     = findViewById(R.id.navAdmin);
        navLoginBtn  = findViewById(R.id.navLogin);

        etAccessToken = findViewById(R.id.etAccessToken);
        etPublicKey   = findViewById(R.id.etPublicKey);
        rgAmbiente    = findViewById(R.id.rgAmbiente);
        rbTeste       = findViewById(R.id.rbTeste);
        rbProducao    = findViewById(R.id.rbProducao);
        tvMpStatus    = findViewById(R.id.tvMpStatus);

        layoutPagamentos       = findViewById(R.id.layoutPagamentos);
        layoutFiltros          = findViewById(R.id.layoutFiltros);
        spinnerMetodoPagamento = findViewById(R.id.spinnerMetodoPagamento);
        spinnerStatusPagamento = findViewById(R.id.spinnerStatusPagamento);

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
                    case 0: rvUsuarios.setVisibility(View.VISIBLE);  carregarUsuarios();   layoutFiltros.setVisibility(View.GONE);    break;
                    case 1: rvConsultas.setVisibility(View.VISIBLE); carregarConsultas();  layoutFiltros.setVisibility(View.GONE);    break;
                    case 2: rvPagamentos.setVisibility(View.VISIBLE);carregarPagamentos(); layoutFiltros.setVisibility(View.VISIBLE); break;
                    case 3: layoutMpConfig.setVisibility(View.VISIBLE);                   layoutFiltros.setVisibility(View.GONE);    break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void ocultarTodos() {
        rvUsuarios.setVisibility(View.GONE);
        rvConsultas.setVisibility(View.GONE);
        rvPagamentos.setVisibility(View.GONE);
        layoutMpConfig.setVisibility(View.GONE);
    }

    private void carregarStats() {
        fb.buscarMedicos(null, medicos ->
                runOnUiThread(() -> { if (tvStatMedicos != null) tvStatMedicos.setText(String.valueOf(medicos.size())); }));

        fb.buscarPacientes(null, pacientes ->
                runOnUiThread(() -> { if (tvStatPacientes != null) tvStatPacientes.setText(String.valueOf(pacientes.size())); }));

        fb.buscarTodasConsultas(consultas ->
                runOnUiThread(() -> { if (tvStatConsultas != null) tvStatConsultas.setText(String.valueOf(consultas.size())); }));

        fb.buscarTodosPagamentos(pagamentos ->
                runOnUiThread(() -> { if (tvStatPagamentos != null) tvStatPagamentos.setText(String.valueOf(pagamentos.size())); }));
    }

    private void carregarUsuarios() {
        fb.buscarTodosUsuarios(usuarios -> runOnUiThread(() -> {
            AdminUsuarioAdapter adapter = new AdminUsuarioAdapter(usuarios,
                    this::editarUsuario,
                    this::confirmarDelecao);
            rvUsuarios.setAdapter(adapter);
        }));
    }

    private void editarUsuario(Usuario u) {
        Intent intent = new Intent(this, EditarUsuarioActivity.class);
        intent.putExtra("usuario_username", u.getUsername());
        startActivityForResult(intent, 100);
    }

    private void confirmarDelecao(Usuario u) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar usuário")
                .setMessage("Deseja remover " + u.getNome() + " permanentemente?")
                .setPositiveButton("Deletar", (d, w) ->
                        fb.deletarUsuario(u.getUsername(), ok -> runOnUiThread(() -> {
                            if (ok) {
                                carregarUsuarios();
                                carregarStats();
                                Snackbar.make(rvUsuarios, "Usuário removido.", Snackbar.LENGTH_SHORT).show();
                            }
                        })))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void carregarConsultas() {
        fb.buscarTodasConsultas(consultas -> {
            if (consultas.isEmpty()) {
                runOnUiThread(() -> rvConsultas.setAdapter(new AdminConsultaAdapter()));
                return;
            }

            AtomicInteger contador = new AtomicInteger(0);
            int total = consultas.size() * 2; // paciente + médico

            for (Consulta c : consultas) {
                fb.buscarUsuarioPorUsername(c.getPacienteId(), paciente -> {
                    if (paciente != null) c.setNomePaciente(paciente.getNome());
                    if (contador.incrementAndGet() == total)
                        runOnUiThread(() -> {
                            AdminConsultaAdapter adapter = new AdminConsultaAdapter();
                            adapter.setLista(consultas);
                            rvConsultas.setAdapter(adapter);
                        });
                });

                fb.buscarUsuarioPorUsername(c.getMedicoId(), medico -> {
                    if (medico != null) c.setNomeMedico(medico.getNome());
                    if (contador.incrementAndGet() == total)
                        runOnUiThread(() -> {
                            AdminConsultaAdapter adapter = new AdminConsultaAdapter();
                            adapter.setLista(consultas);
                            rvConsultas.setAdapter(adapter);
                        });
                });
            }
        });
    }

    private void carregarPagamentos() {
        layoutPagamentos.setVisibility(View.VISIBLE);
        fb.buscarTodosPagamentos(pagamentos -> runOnUiThread(() -> {
            listaOriginalPagamentos = pagamentos;
            pagamentoAdapter = new AdminPagamentoAdapter(listaOriginalPagamentos);
            rvPagamentos.setAdapter(pagamentoAdapter);
        }));
    }

    private void filtrarPagamentos() {
        String metodoSelecionado = spinnerMetodoPagamento.getText().toString();
        String statusSelecionado = spinnerStatusPagamento.getText().toString();

        List<Pagamento> filtrados = new ArrayList<>();
        for (Pagamento p : listaOriginalPagamentos) {
            boolean metodoOk = false;
            boolean statusOk = false;

            switch (metodoSelecionado) {
                case "Todos": case "": metodoOk = true; break;
                case "PIX":     metodoOk = Pagamento.METODO_PIX.equals(p.getMetodo());     break;
                case "Cartão":  metodoOk = Pagamento.METODO_CARTAO.equals(p.getMetodo());  break;
                case "Dinheiro":metodoOk = Pagamento.METODO_DINHEIRO.equals(p.getMetodo());break;
            }

            switch (statusSelecionado) {
                case "Todos": case "": statusOk = true; break;
                case "Aprovado": statusOk = Pagamento.STATUS_APROVADO.equals(p.getStatus()); break;
                case "Pendente": statusOk = Pagamento.STATUS_PENDENTE.equals(p.getStatus()); break;
                case "Recusado": statusOk = Pagamento.STATUS_RECUSADO.equals(p.getStatus()); break;
            }

            if (metodoOk && statusOk) filtrados.add(p);
        }

        pagamentoAdapter = new AdminPagamentoAdapter(filtrados);
        rvPagamentos.setAdapter(pagamentoAdapter);
    }

    private void filtroPagamento() {
        String[] metodos = {"Todos", "PIX", "Cartão", "Dinheiro"};
        String[] status  = {"Todos", "Aprovado", "Pendente", "Recusado"};

        spinnerMetodoPagamento.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown, metodos));
        spinnerStatusPagamento.setAdapter(new ArrayAdapter<>(this, R.layout.item_dropdown, status));

        spinnerMetodoPagamento.setOnItemClickListener((p, v, pos, id) -> filtrarPagamentos());
        spinnerStatusPagamento.setOnItemClickListener((p, v, pos, id) -> filtrarPagamentos());
    }

    // carregarConfigMP, atualizarStatusMP, salvarConfigMP, limparConfigMP — sem mudanças
    private void carregarConfigMP() {
        if (etAccessToken == null) return;

        android.content.SharedPreferences prefs =
                getSharedPreferences(MercadoPagoService.PREF_MP, android.content.Context.MODE_PRIVATE);

        etAccessToken.setText(prefs.getString(MercadoPagoService.KEY_ACCESS_TOKEN, ""));
        etPublicKey.setText(prefs.getString(MercadoPagoService.KEY_PUBLIC_KEY, ""));

        boolean sandbox = prefs.getBoolean(MercadoPagoService.KEY_IS_SANDBOX, true);

        if (rbTeste    != null) rbTeste.setChecked(sandbox);
        if (rbProducao != null) rbProducao.setChecked(!sandbox);

        atualizarStatusMP();
    }

    private void atualizarStatusMP() {
        if (rbTeste == null || tvMpStatus == null) return;
        boolean sandbox = rbTeste.isChecked();
        tvMpStatus.setText(sandbox ? "🟡 Ambiente de Teste — sem cobranças reais" : "🟢 Ambiente de Produção — pagamentos reais");
        tvMpStatus.setTextColor(sandbox ? 0xFFFFC107 : 0xFF4CAF50);
    }

    private void salvarConfigMP() {
        if (etAccessToken == null) return;
        String token = etAccessToken.getText().toString().trim();
        String key   = etPublicKey.getText().toString().trim();
        if (token.isEmpty() || key.isEmpty()) {
            Snackbar.make(layoutMpConfig, "Preencha Access Token e Public Key", Snackbar.LENGTH_SHORT).show();
            return;
        }
        MercadoPagoService.salvarCredenciais(this, token, key, rbTeste != null && rbTeste.isChecked());
        Snackbar.make(layoutMpConfig, "✅ Credenciais salvas!", Snackbar.LENGTH_LONG).show();
    }

    private void limparConfigMP() {
        getSharedPreferences(MercadoPagoService.PREF_MP, android.content.Context.MODE_PRIVATE).edit().clear().apply();
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