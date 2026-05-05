package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;
import com.clinica.app.Modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

public class MedicoAdapter extends RecyclerView.Adapter<MedicoAdapter.VH> {

    public interface OnMedicoClick {
        void onClick(Usuario medico);
    }

    private List<Usuario> lista = new ArrayList<>();
    private final OnMedicoClick listener;

    public MedicoAdapter(OnMedicoClick listener) {
        this.listener = listener;
    }

    public void setLista(List<Usuario> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medico, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Usuario m = lista.get(pos);
        h.tvNome.setText(m.getNome());
        h.tvEsp.setText(m.getEspecialidade() != null ? m.getEspecialidade() : "");
        h.itemView.setOnClickListener(v -> listener.onClick(m));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNome, tvEsp;

        VH(View v) {
            super(v);
            tvNome = v.findViewById(R.id.tvNomeMedico);
            tvEsp = v.findViewById(R.id.tvEspecialidade);
        }
    }
}
