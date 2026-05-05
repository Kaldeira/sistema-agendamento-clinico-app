package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.R;

import java.util.List;
import java.util.Locale;

public class AdminPagamentoAdapter extends RecyclerView.Adapter<AdminPagamentoAdapter.VH> {

    private final List<Pagamento> lista;

    public AdminPagamentoAdapter(List<Pagamento> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_pagamento, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Pagamento p = lista.get(pos);

        h.tvConsulta.setText("Consulta #" + p.getConsultaId());
        h.tvMetodo.setText(p.getMetodoLabel());
        h.tvValor.setText(String.format(Locale.getDefault(), "R$ %.2f", p.getValor()));
        h.tvStatus.setText(p.getStatusLabel());
        h.tvDataHora.setText(p.getDataHora() != null ? p.getDataHora() : "");

        int color = p.isAprovado() ? 0xFF4CAF50
                : p.isRecusado()   ? 0xFFF44336
                : 0xFFFFC107;
        h.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvConsulta, tvMetodo, tvValor, tvStatus, tvDataHora;

        public VH(@NonNull View v) {
            super(v);
            tvConsulta = v.findViewById(R.id.tvPagConsulta);
            tvMetodo   = v.findViewById(R.id.tvPagMetodo);
            tvValor    = v.findViewById(R.id.tvPagValor);
            tvStatus   = v.findViewById(R.id.tvPagStatus);
            tvDataHora = v.findViewById(R.id.tvPagDataHora);
        }
    }
}
