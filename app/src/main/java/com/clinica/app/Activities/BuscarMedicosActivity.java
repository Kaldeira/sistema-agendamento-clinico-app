package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.DAO.MedicoAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.databinding.ActivityBuscarMedicosBinding;
import com.clinica.app.Modelo.Usuario;

import java.util.List;


public class BuscarMedicosActivity extends AppCompatActivity {

    private ActivityBuscarMedicosBinding binding;
    private FirebaseManager fb;
    private MedicoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscarMedicosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fb = FirebaseManager.getInstance();

        binding.rvMedicos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicoAdapter(medico -> {
            Intent intent = new Intent(this, PerfilMedicoActivity.class);
            intent.putExtra("medico_username", medico.getUsername());
            startActivity(intent);
        });
        binding.rvMedicos.setAdapter(adapter);

        carregarMedicos("");

        binding.etBusca.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarMedicos(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void carregarMedicos(String filtro) {
        fb.buscarMedicos(filtro, medicos ->
                runOnUiThread(() -> adapter.setLista(medicos)));
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}