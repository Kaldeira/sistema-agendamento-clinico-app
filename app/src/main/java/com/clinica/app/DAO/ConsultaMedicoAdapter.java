package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;
import com.clinica.app.Modelo.Consulta;

import java.util.ArrayList;
import java.util.List;

public class ConsultaMedicoAdapter extends RecyclerView.Adapter<ConsultaMedicoAdapter.VH> {

    public interface OnAcao {
        void onAcao(Consulta consulta, String acao);
    }

    private List<Consulta> lista = new ArrayList<>();
    private final OnAcao listener;

    public ConsultaMedicoAdapter(OnAcao listener) {
        this.listener = listener;
    }

    public void setLista(List<Consulta> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_consulta_medico, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Consulta c = lista.get(pos);
        h.tvPaciente.setText("Paciente: " + (c.getNomePaciente() != null ? c.getNomePaciente() : "—"));
        h.tvDataHora.setText(c.getData() + " às " + c.getHora());
        h.tvStatus.setText("Status: " + c.getStatusLabel());
        h.tvPagamento.setText("Observação: " + (c.getObservacoes() != null ? c.getObservacoes() : "Nenhuma!"));

        boolean pendente = "pendente".equals(c.getStatus());
        h.btnConfirmar.setVisibility(pendente ? View.VISIBLE : View.GONE);
        h.btnCancelar.setVisibility(pendente ? View.VISIBLE : View.GONE);

        h.btnConfirmar.setOnClickListener(v -> listener.onAcao(c, "confirmada"));
        h.btnCancelar.setOnClickListener(v -> listener.onAcao(c, "cancelada"));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvPaciente, tvDataHora, tvStatus, tvPagamento;
        Button btnConfirmar, btnCancelar;

        VH(View v) {
            super(v);
            tvPaciente = v.findViewById(R.id.tvPaciente);
            tvDataHora = v.findViewById(R.id.tvDataHora);
            tvStatus = v.findViewById(R.id.tvStatus);
            tvPagamento = v.findViewById(R.id.tvPagamento);
            btnConfirmar = v.findViewById(R.id.btnConfirmar);
            btnCancelar = v.findViewById(R.id.btnCancelar);
        }
    }
}
