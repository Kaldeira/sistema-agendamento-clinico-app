package com.clinica.app.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.R;
import com.clinica.app.Utils.MercadoPagoService;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.Uri;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

public class PagamentoActivity extends AppCompatActivity {

    public static final String EXTRA_CONSULTA_ID = "consulta_id";
    public static final String EXTRA_TOTAL       = "total";
    public static final String EXTRA_MEDICO_NOME = "medico_nome";

    private ScrollView   stateSelecao;
    private LinearLayout stateCarregando;
    private LinearLayout stateResultado;
    private WebView      webViewMP;

    private TextView tvTotalSelecao, tvMedicoNome;
    private View cardPix, cardCartao, cardDinheiro;
    private View checkPix, checkCartao, checkDinheiro;
    private Button btnConfirmar;

    private TextView tvResultadoTitulo, tvResultadoMsg, tvResultadoDetalhe;
    private Button   btnVoltarInicio;

    private ProgressBar progressBar;

    private FirebaseManager fb;
    private String consultaId;
    private double total;
    private String medicoNome;
    private String metodoSelecionado;

    private final ExecutorService executor    = Executors.newSingleThreadExecutor();
    private final Handler         mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pagamento);

        consultaId = getIntent().getStringExtra(EXTRA_CONSULTA_ID);
        total      = getIntent().getDoubleExtra(EXTRA_TOTAL, 0.0);
        medicoNome = getIntent().getStringExtra(EXTRA_MEDICO_NOME);
        fb         = FirebaseManager.getInstance();

        bindViews();
        configurarWebView();

        Uri data = getIntent().getData();
        if (data != null) processarRetornoMP(data.toString());

        tvTotalSelecao.setText(String.format(Locale.getDefault(), "R$ %.2f", total));
        if (tvMedicoNome != null && medicoNome != null)
            tvMedicoNome.setText("Pagamento para: " + medicoNome);

        cardPix.setOnClickListener(v      -> selecionarPagamento(Pagamento.METODO_PIX));
        cardCartao.setOnClickListener(v   -> selecionarPagamento(Pagamento.METODO_CARTAO));
        cardDinheiro.setOnClickListener(v -> selecionarPagamento(Pagamento.METODO_DINHEIRO));

        btnConfirmar.setOnClickListener(v -> iniciarPagamento());
        btnVoltarInicio.setOnClickListener(v ->
                startActivity(new Intent(this, HistoricoConsultasActivity.class)));
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null && intent.getData() != null)
            processarRetornoMP(intent.getData().toString());
    }

    private void bindViews() {
        stateSelecao    = findViewById(R.id.stateSelecao);
        stateCarregando = findViewById(R.id.stateCarregando);
        stateResultado  = findViewById(R.id.stateResultado);
        webViewMP       = findViewById(R.id.webViewMP);

        tvTotalSelecao     = findViewById(R.id.tvTotalSelecao);
        tvMedicoNome       = findViewById(R.id.tvMedicoNome);
        tvResultadoTitulo  = findViewById(R.id.tvResultadoTitulo);
        tvResultadoMsg     = findViewById(R.id.tvResultadoMsg);
        tvResultadoDetalhe = findViewById(R.id.tvResultadoDetalhe);
        progressBar        = findViewById(R.id.progressBar);
        btnConfirmar       = findViewById(R.id.btnConfirmar);
        btnVoltarInicio    = findViewById(R.id.btnVoltarInicio);

        cardPix      = findViewById(R.id.cardPix);
        cardCartao   = findViewById(R.id.cardCartao);
        cardDinheiro = findViewById(R.id.cardDinheiro);
        checkPix     = findViewById(R.id.checkPix);
        checkCartao  = findViewById(R.id.checkCartao);
        checkDinheiro= findViewById(R.id.checkDinheiro);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configurarWebView() {
        WebSettings s = webViewMP.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        webViewMP.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest req) {
                String url = req.getUrl().toString();
                if (url.startsWith("clinicaapp://")) {
                    processarRetornoMP(url);
                    return true;
                }
                return false;
            }
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap f) {
                progressBar.setVisibility(View.VISIBLE);
            }
            @Override public void onPageFinished(WebView v, String url) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    public void abrirPagamentoMercadoPago(String url) {
        CustomTabColorSchemeParams params = new CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(this, R.color.black))
                .build();

        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(params)
                .setShowTitle(true)
                .build();

        customTabsIntent.launchUrl(this, Uri.parse(url));
    }

    private void iniciarPagamento() {
        if (metodoSelecionado == null) {
            Snackbar.make(stateSelecao, "Selecione uma forma de pagamento",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        switch (metodoSelecionado) {
            case Pagamento.METODO_PIX:
                // PIX: aprovação imediata → marca slot como ocupado
                registrarPagamento(Pagamento.METODO_PIX, Pagamento.STATUS_APROVADO, null, null);
                fb.atualizarStatusPagamentoConsulta(consultaId, "confirmada", ok -> {});
                exibirResultado("sucesso", null);
                break;

            case Pagamento.METODO_DINHEIRO:
                // Dinheiro: pendente → slot JÁ foi reservado ao agendar, mas dia não fica "ocupado"
                // no calendário até confirmação. Mantemos pendente.
                registrarPagamento(Pagamento.METODO_DINHEIRO, Pagamento.STATUS_PENDENTE, null, null);
                fb.atualizarStatusConsulta(consultaId, "pendente", ok -> {});
                exibirResultado("pendente", null);
                break;

            case Pagamento.METODO_CARTAO:
                mostrarTela(stateCarregando);
                criarPreferenciaMP();
                break;
        }
    }

    private void criarPreferenciaMP() {
        executor.execute(() -> {
            MercadoPagoService service = new MercadoPagoService(this);
            String desc = "Consulta #" + consultaId + (medicoNome != null ? " — " + medicoNome : "");
            MercadoPagoService.PreferenceResult result =
                    service.criarPreferencia(consultaId, desc, 1, total);
            String url = service.resolverUrlPagamento(result);

            if (result.sucesso() && result.preferenceId != null)
                registrarPagamento(Pagamento.METODO_CARTAO, Pagamento.STATUS_PENDENTE,
                        null, result.preferenceId);

            mainHandler.post(() -> {
                if (result.sucesso() && url != null && !url.isEmpty()) {
                    abrirPagamentoMercadoPago(url);
                   // abrirCheckoutMP(url);
                } else {
                    mostrarTela(stateSelecao);
                    String msg = result.erro != null ? result.erro : "URL inválida";
                    Snackbar.make(stateSelecao, "Erro MP: " + msg, Snackbar.LENGTH_LONG).show();
                }
            });
        });
    }

    private void abrirCheckoutMP(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Snackbar.make(stateSelecao, "Erro ao abrir pagamento", Snackbar.LENGTH_LONG).show();
        }
    }

    private void processarRetornoMP(String url) {
        Uri uri     = Uri.parse(url);
        String tipo = uri.getLastPathSegment();
        String payId = uri.getQueryParameter("payment_id");

        String status = "sucesso".equals(tipo) || "approved".equals(tipo)
                ? Pagamento.STATUS_APROVADO
                : "falha".equals(tipo) || "rejected".equals(tipo)
                ? Pagamento.STATUS_RECUSADO
                : Pagamento.STATUS_PENDENTE;

        fb.atualizarStatusPagamento(consultaId, status, payId, ok -> {});

        if (Pagamento.STATUS_APROVADO.equals(status)) {
            // Pagamento aprovado → confirma consulta (slot já está indisponível desde o agendamento)
            fb.atualizarStatusPagamentoConsulta(consultaId, "confirmada", ok -> {});

        } else if (Pagamento.STATUS_PENDENTE.equals(status)) {
            fb.atualizarStatusPagamentoConsulta(consultaId, "pendente", ok -> {});

        } else {
            // Recusado → cancela consulta E libera o slot de volta
            fb.atualizarStatusPagamentoConsulta(consultaId, "cancelada", ok -> {});
        }

        exibirResultado(tipo, payId);
    }

    private void exibirResultado(String tipo, String paymentId) {
        mostrarTela(stateResultado);
        switch (tipo != null ? tipo : "") {
            case "sucesso": case "approved":
                tvResultadoTitulo.setText("✅ Pagamento aprovado!");
                tvResultadoTitulo.setTextColor(0xFF4CAF50);
                tvResultadoMsg.setText("Consulta #" + consultaId + " confirmada.");
                tvResultadoDetalhe.setText(paymentId != null ? "ID: " + paymentId : "");
                break;
            case "pendente": case "pending":
                tvResultadoTitulo.setText("⏳ Aguardando confirmação");
                tvResultadoTitulo.setTextColor(0xFFFFC107);
                tvResultadoMsg.setText(Pagamento.METODO_DINHEIRO.equals(metodoSelecionado)
                        ? "Consulta confirmada! Pague ao chegar."
                        : "Aguardando confirmação do banco.");
                tvResultadoDetalhe.setText("Você será notificado quando aprovado.");
                break;
            default:
                tvResultadoTitulo.setText("❌ Pagamento não aprovado");
                tvResultadoTitulo.setTextColor(0xFFF44336);
                tvResultadoMsg.setText("O pagamento não foi processado.");
                tvResultadoDetalhe.setText("Tente novamente ou escolha outro método.");
        }
    }

    private void registrarPagamento(String metodo, String status,
                                    String mpPaymentId, String mpPreferenceId) {
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        Pagamento p = new Pagamento(consultaId, metodo, total, now);
        p.setStatus(status);
        p.setMpPaymentId(mpPaymentId);
        p.setMpPreferenceId(mpPreferenceId);
        fb.registrarPagamento(p, id -> {}, e -> {});
    }

    private void mostrarTela(View tela) {
        stateSelecao.setVisibility(View.GONE);
        stateCarregando.setVisibility(View.GONE);
        webViewMP.setVisibility(View.GONE);
        stateResultado.setVisibility(View.GONE);
        tela.setVisibility(View.VISIBLE);

        View btnVoltar = findViewById(R.id.btnVoltar);
        if (btnVoltar != null)
            btnVoltar.setVisibility(tela == webViewMP ? View.GONE : View.VISIBLE);
    }

    private void selecionarPagamento(String metodo) {
        metodoSelecionado = metodo.equals(metodoSelecionado) ? null : metodo;
        atualizarUISelecao();
    }

    private void atualizarUISelecao() {
        atualizarCard(cardPix,     checkPix,     Pagamento.METODO_PIX.equals(metodoSelecionado));
        atualizarCard(cardCartao,  checkCartao,  Pagamento.METODO_CARTAO.equals(metodoSelecionado));
        atualizarCard(cardDinheiro,checkDinheiro,Pagamento.METODO_DINHEIRO.equals(metodoSelecionado));
    }

    private void atualizarCard(View card, View check, boolean selecionado) {
        androidx.cardview.widget.CardView cv = (androidx.cardview.widget.CardView) card;
        if (selecionado) {
            cv.setCardBackgroundColor(0xFFE3F2FD);
            card.animate().scaleX(1.04f).scaleY(1.04f).setDuration(150);
            card.setAlpha(1f);
            check.setVisibility(View.VISIBLE);
        } else {
            cv.setCardBackgroundColor(0xFFFFFFFF);
            card.animate().scaleX(1f).scaleY(1f).setDuration(150);
            card.setAlpha(0.9f);
            check.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (webViewMP.getVisibility() == View.VISIBLE) {
            if (webViewMP.canGoBack()) webViewMP.goBack();
            else mostrarTela(stateSelecao);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        webViewMP.destroy();
    }
}