package com.clinica.app.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.DAO.SlotAgendaAdapter;
import com.clinica.app.databinding.ActivityAgendaMedicaBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

public class AgendaMedicaActivity extends AppCompatActivity {

    private ActivityAgendaMedicaBinding binding;
    private FirebaseManager fb;

    private String medicoNome;
    private String medicoUsername;
    private SlotAgendaAdapter slotAdapter;


    private Calendar calAtual = Calendar.getInstance();
    private Set<String> datasDisponiveis = new HashSet<>();
    private Set<String> datasOcupadas   = new HashSet<>();
    private String dataSelecionada = null;

    private static final SimpleDateFormat SDF_KEY  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private static final SimpleDateFormat SDF_EXIB = new SimpleDateFormat("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
    private static final SimpleDateFormat SDF_MES  = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgendaMedicaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb           = FirebaseManager.getInstance();
        medicoNome   = getIntent().getStringExtra("medico_nome");
        medicoUsername = getIntent().getStringExtra("medico_username");

        if (medicoUsername == null || medicoUsername.isEmpty()) {
            finish();
            return;
        }

        binding.tvTitulo.setText(medicoNome != null ? medicoNome : "Agenda");

        configurarRecycler();
        configurarBotoes();
        carregarDatasDoFirebase();
    }

    private void configurarRecycler() {
        slotAdapter = new SlotAgendaAdapter((data, hora) -> {
            Intent intent = new Intent(this, AgendarConsultaActivity.class);
            intent.putExtra("medico_username", medicoUsername);
            intent.putExtra("medico_nome",    medicoNome);
            intent.putExtra("data",           data);
            intent.putExtra("hora",           hora);
            startActivity(intent);
        });
        binding.rvSlots.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvSlots.setAdapter(slotAdapter);
    }

    private void configurarBotoes() {
        binding.btnVoltar.setOnClickListener(v -> finish());

        binding.btnMesAnterior.setOnClickListener(v -> {
            calAtual.add(Calendar.MONTH, -1);
            renderizarCalendario();
        });

        binding.btnProximoMes.setOnClickListener(v -> {
            calAtual.add(Calendar.MONTH, 1);
            renderizarCalendario();
        });
    }


    private void carregarDatasDoFirebase() {

        fb.buscarDatasOcupadas(
                medicoUsername,
                HORARIOS_FIXOS,
                datas -> {

                    runOnUiThread(() -> {

                        datasOcupadas.clear();
                        datasOcupadas.addAll(datas);

                        renderizarCalendario();
                    });
                });
    }

    private void carregarSlots(String data) {

        binding.tvSemHorarios.setVisibility(View.GONE);
        binding.rvSlots.setVisibility(View.GONE);
        binding.layoutLegendaHorarios.setVisibility(View.GONE);

        fb.buscarHorariosOcupados(
                medicoUsername,
                data,
                horariosOcupados -> {

                    runOnUiThread(() -> {

                        List<String[]> slots = new ArrayList<>();

                        for (String hora : HORARIOS_FIXOS) {

                            boolean ocupado =
                                    horariosOcupados.contains(hora);

                            slots.add(new String[]{
                                    "",
                                    hora,
                                    ocupado ? "0" : "1"
                            });
                        }

                        slotAdapter.setSlots(slots, data);

                        binding.rvSlots.setVisibility(View.VISIBLE);
                        binding.layoutLegendaHorarios.setVisibility(View.VISIBLE);
                    });
                });
    }
    private void renderizarCalendario() {
        binding.tvMesAno.setText(capitalize(SDF_MES.format(calAtual.getTime())));
        binding.gridCalendario.removeAllViews();

        Calendar c = (Calendar) calAtual.clone();
        c.set(Calendar.DAY_OF_MONTH, 1);

        int diaSemanaInicio = c.get(Calendar.DAY_OF_WEEK) - 1;
        int totalDias = c.getActualMaximum(Calendar.DAY_OF_MONTH);


        Calendar hoje = Calendar.getInstance();
        hoje.set(Calendar.HOUR_OF_DAY, 0);
        hoje.set(Calendar.MINUTE, 0);
        hoje.set(Calendar.SECOND, 0);
        hoje.set(Calendar.MILLISECOND, 0);


        for (int i = 0; i < diaSemanaInicio; i++) {
            adicionarCelulaVazia();
        }

        for (int dia = 1; dia <= totalDias; dia++) {

            c.set(Calendar.DAY_OF_MONTH, dia);

            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            String chave = SDF_KEY.format(c.getTime());

            boolean passado = c.before(hoje);


            boolean ocupado = datasOcupadas.contains(chave);

            boolean disponivel = !ocupado && !passado;

            boolean selecionado = chave.equals(dataSelecionada);

            adicionarCelulaDia(
                    dia,
                    chave,
                    disponivel,
                    ocupado,
                    passado,
                    selecionado
            );
        }
    }

    private void adicionarCelulaVazia() {
        TextView tv = new TextView(this);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width  = 0;
        lp.height = dpToPx(44);
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        tv.setLayoutParams(lp);
        binding.gridCalendario.addView(tv);
    }

    private void adicionarCelulaDia(int dia, String chave,
                                    boolean disponivel, boolean ocupado,
                                    boolean passado, boolean selecionado) {
        TextView tv = new TextView(this);
        tv.setText(String.valueOf(dia));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(13f);

        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width       = 0;
        lp.height      = dpToPx(44);
        lp.columnSpec  = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(2, 2, 2, 2);
        tv.setLayoutParams(lp);

        if (selecionado) {

            tv.setBackgroundColor(0xFF1565C0);
            tv.setTextColor(Color.WHITE);
            tv.setTypeface(null, Typeface.BOLD);
        } else if (passado) {

            tv.setTextColor(0xFFCCCCCC);
        } else if (disponivel) {
            tv.setBackgroundColor(0xFFE8F5E9);
            tv.setTextColor(0xFF2E7D32);
            tv.setTypeface(null, Typeface.BOLD);
            tv.setClickable(true);
            tv.setFocusable(true);
            tv.setForeground(getDrawable(android.R.drawable.list_selector_background));
            tv.setOnClickListener(v -> {
                android.util.Log.d("CAL", "Clicou no dia: " + chave);
                selecionarDia(chave, tv);
            });
        } else if (ocupado) {
            tv.setBackgroundColor(0xFFFFEBEE);
            tv.setTextColor(0xFFC62828);
        } else {
            tv.setTextColor(0xFF999999);
        }

        binding.gridCalendario.addView(tv);
    }

    private void selecionarDia(String chave, TextView tvClicado) {
        dataSelecionada = chave;

        try {
            java.util.Date d = SDF_KEY.parse(chave);
            binding.tvDataSelecionada.setText("Horários para " + SDF_EXIB.format(d));
        } catch (Exception e) {
            binding.tvDataSelecionada.setText("Horários para " + chave);
        }
        binding.tvDataSelecionada.setVisibility(View.VISIBLE);

        renderizarCalendario();

        carregarSlots(chave);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private final List<String> HORARIOS_FIXOS = java.util.Arrays.asList(
            "08:00",
            "08:30",
            "09:00",
            "09:30",
            "10:00",
            "10:30",
            "11:00",
            "11:30",
            "13:00",
            "13:30",
            "14:00",
            "14:30",
            "15:00",
            "15:30",
            "16:00",
            "16:30",
            "17:00"
    );

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}