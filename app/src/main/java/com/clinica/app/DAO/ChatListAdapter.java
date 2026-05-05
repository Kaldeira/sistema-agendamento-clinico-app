package com.clinica.app.DAO;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;
import com.clinica.app.Modelo.Usuario;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.VH> {

    public interface OnContatoClick {
        void onClick(Usuario contato);
    }

    private List<Usuario> lista = new ArrayList<>();
    private final OnContatoClick listener;

    public ChatListAdapter(OnContatoClick listener) {
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
                .inflate(R.layout.item_chat_contato, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Usuario u = lista.get(pos);
        h.tvNome.setText(u.getNome());
        h.tvTipo.setText(u.isMedico()
                ? (u.getEspecialidade() != null ? u.getEspecialidade() : "Médico")
                : "Paciente");

        if (u.getFotoPerfil() != null && !u.getFotoPerfil() .isEmpty()) {
            File f = new File(u.getFotoPerfil() );
            if (f.exists()) {
                h.sivFoto.setImageURI(Uri.fromFile(f));
                h.sivFoto.setVisibility(View.VISIBLE);
            }
        }

        h.itemView.setOnClickListener(v -> listener.onClick(u));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvNome, tvTipo;
        ShapeableImageView sivFoto;

        VH(View v) {
            super(v);
            tvNome = v.findViewById(R.id.tvNomeContato);
            tvTipo = v.findViewById(R.id.tvTipoContato);
            sivFoto = v.findViewById(R.id.imgFotoPerfilContato);

        }
    }
}
