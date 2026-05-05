package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;

import java.util.List;

public class AdminUsuarioAdapter extends RecyclerView.Adapter<AdminUsuarioAdapter.VH> {

    public interface OnEdit   { void onEdit(Usuario u); }
    public interface OnDelete { void onDelete(Usuario u); }

    private final List<Usuario> lista;
    private final OnEdit        onEdit;
    private final OnDelete      onDelete;

    public AdminUsuarioAdapter(List<Usuario> lista, OnEdit onEdit, OnDelete onDelete) {
        this.lista    = lista;
        this.onEdit   = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_usuario, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Usuario u = lista.get(pos);
        h.tvNome.setText(u.getNome());
        h.tvInfo.setText(u.getTipo().toUpperCase() + " — " + u.getEmail());
        h.btnEditar.setOnClickListener(v -> onEdit.onEdit(u));
        h.btnDeletar.setOnClickListener(v -> onDelete.onDelete(u));
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvNome, tvInfo;
        Button   btnEditar, btnDeletar;

        public VH(@NonNull View v) {
            super(v);
            tvNome    = v.findViewById(R.id.tvNomeUsuario);
            tvInfo    = v.findViewById(R.id.tvInfoUsuario);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnDeletar= v.findViewById(R.id.btnDeletar);
        }
    }
}
