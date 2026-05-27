package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;
import com.clinica.app.Modelo.Mensagem;

import java.util.ArrayList;
import java.util.List;

public class MensagemAdapter extends RecyclerView.Adapter<MensagemAdapter.VH> {

    private static final int VIEW_ENVIADA = 1;
    private static final int VIEW_RECEBIDA = 2;

    private List<Mensagem> lista = new ArrayList<>();
    private final String userId;

    public MensagemAdapter(String userId) {
        this.userId = userId;
    }

    public void setLista(List<Mensagem> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int pos) {
        return lista.get(pos).getRemetenteId().equals(userId) ? VIEW_ENVIADA : VIEW_RECEBIDA;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == VIEW_ENVIADA
                ? R.layout.item_mensagem_enviada
                : R.layout.item_mensagem_recebida;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Mensagem m = lista.get(pos);
        h.tvTexto.setText(m.getTexto());
        // Mostrar apenas HH:mm
        String dh = m.getDataHora();
        if (dh != null && dh.length() >= 16)
            h.tvHora.setText(dh.substring(11, 16));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvTexto, tvHora;

        VH(View v) {
            super(v);
            tvTexto = v.findViewById(R.id.tvTexto);
            tvHora = v.findViewById(R.id.tvHora);
        }
    }
}
