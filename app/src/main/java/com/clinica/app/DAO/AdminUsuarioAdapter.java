package com.clinica.app.DAO;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
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

        if (u.getFotoPerfil() != null && !u.getFotoPerfil().isEmpty()) {

            Glide.with(h.itemView.getContext())
                    .load(u.getFotoPerfil())
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_person)
                    .error(R.drawable.ic_menu_person)
                    .into(h.ivFotoUser);
            h.ivFotoUser.setVisibility(View.VISIBLE);
            h.tvIniciais.setVisibility(View.GONE);

//            File f = new File(u.getFotoPerfil());
//            if (f.exists()) {
//                h.ivFotoUser.setImageURI(Uri.fromFile(f));
//                h.ivFotoUser.setVisibility(View.VISIBLE);
//                h.tvIniciais.setVisibility(View.GONE);
//            } else {
//                showIniciais(h, u);
//            }
        } else {
            showIniciais(h, u);
        }
    }

    private void showIniciais(VH h, Usuario m) {
        h.ivFotoUser.setVisibility(View.GONE);
        h.tvIniciais.setVisibility(View.VISIBLE);
        h.tvIniciais.setText(m.getIniciais());
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvNome, tvInfo, tvIniciais;
        Button   btnEditar, btnDeletar;
        ShapeableImageView ivFotoUser;

        public VH(@NonNull View v) {
            super(v);
            tvNome    = v.findViewById(R.id.tvNomeUsuario);
            tvInfo    = v.findViewById(R.id.tvInfoUsuario);
            btnEditar = v.findViewById(R.id.btnEditar);
            btnDeletar= v.findViewById(R.id.btnDeletar);
            ivFotoUser = v.findViewById(R.id.ivFotoUser);
            tvIniciais = v.findViewById(R.id.tvIniciais);
        }
    }
}
