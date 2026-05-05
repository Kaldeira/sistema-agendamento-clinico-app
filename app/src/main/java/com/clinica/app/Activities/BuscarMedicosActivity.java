package com.clinica.app.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.MedicoAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.databinding.ActivityBuscarMedicosBinding;
import com.clinica.app.Modelo.Usuario;

import java.util.List;


public class BuscarMedicosActivity extends AppCompatActivity {

    private ActivityBuscarMedicosBinding binding;
    private BancoDados db;
    private MedicoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBuscarMedicosBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSupportActionBar().setTitle("Buscar Médicos");
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = BancoDados.getInstance(this);

        binding.rvMedicos.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicoAdapter(medico -> {
            Intent intent = new Intent(this, PerfilMedicoActivity.class);
            intent.putExtra("medico_id", medico.getId());
            startActivity(intent);
        });
        binding.rvMedicos.setAdapter(adapter);

        carregarMedicos("");

        binding.etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                carregarMedicos(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void carregarMedicos(String filtro) {
        List<Usuario> medicos = db.buscarMedicos(filtro);
        adapter.setLista(medicos);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
