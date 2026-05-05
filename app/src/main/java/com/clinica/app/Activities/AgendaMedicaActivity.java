package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.clinica.app.DAO.SlotAgendaAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.databinding.ActivityAgendaMedicaBinding;

import java.util.List;

public class AgendaMedicaActivity extends AppCompatActivity {

    private ActivityAgendaMedicaBinding binding;
    private BancoDados db;
    private int medicoId;
    private String medicoNome;
    private List<String> datas;
    private SlotAgendaAdapter slotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAgendaMedicaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = BancoDados.getInstance(this);
        medicoId = getIntent().getIntExtra("medico_id", -1);
        medicoNome = getIntent().getStringExtra("medico_nome");

        //getSupportActionBar().setTitle("Agenda – " + medicoNome);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (medicoId == -1) {
            finish();
            return;
        }

        datas = db.buscarDatasComSlots(medicoId);

        if (datas.isEmpty()) {
            Toast.makeText(this, "Nenhum horário disponível.", Toast.LENGTH_SHORT).show();
            return;
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, datas);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerData.setAdapter(dataAdapter);

        binding.spinnerData.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view, int pos, long id) {
                        carregarSlots(datas.get(pos));
                    }

                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> p) {
                    }
                });

        slotAdapter = new SlotAgendaAdapter((data, hora) -> {
            Intent intent = new Intent(this, AgendarConsultaActivity.class);
            intent.putExtra("medico_id", medicoId);
            intent.putExtra("medico_nome", medicoNome);
            intent.putExtra("data", data);
            intent.putExtra("hora", hora);
            startActivity(intent);
        });

        binding.rvSlots.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvSlots.setAdapter(slotAdapter);

        binding.btnVoltar.setOnClickListener(view -> finish());

        carregarSlots(datas.get(0));
    }

    private void carregarSlots(String data) {
        List<String[]> slots = db.buscarSlotsAgenda(medicoId, data);
        slotAdapter.setSlots(slots, data);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
