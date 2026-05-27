package com.clinica.app.DAO;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clinica.app.Activities.ChatActivity;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PacienteCardAdapter extends RecyclerView.Adapter<PacienteCardAdapter.ViewHolder>{
    public interface OnPacienteCardClickListener {
        void OnClick(Usuario paciente);
    }

    private List<Usuario> lista    = new ArrayList<>();
    private final PacienteCardAdapter.OnPacienteCardClickListener listener;
    public PacienteCardAdapter(PacienteCardAdapter.OnPacienteCardClickListener listener) {
        this.listener = listener;
    }

    public void setLista(List<Usuario> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_usuario_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Usuario m = lista.get(position);

//        String prefixo = m.getGenero().equalsIgnoreCase("masculino") ? "Dr. " : "Dra. ";
        h.tvNome.setText(m.getNome());
        h.tvEspecialidade.setText("Paciente");


        if (m.getFotoPerfil() != null && !m.getFotoPerfil().isEmpty()) {

            Glide.with(h.itemView.getContext())
                    .load(m.getFotoPerfil())
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_person)
                    .error(R.drawable.ic_menu_person)
                    .into(h.sivFoto);

            h.sivFoto.setVisibility(View.VISIBLE);
            h.tvIniciais.setVisibility(View.GONE);
        } else {
            showIniciais(h, m);
        }

        h.itemView.setOnClickListener(v -> listener.OnClick(m));

        h.btnChat.setOnClickListener(v -> {
            Context context = v.getContext();

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("destinatario_id", m.getId());
            intent.putExtra("destinatario_nome", m.getNome());
            intent.putExtra("foto_perfil", m.getFotoPerfil());
            context.startActivity(intent);
        });
    }

    private void showIniciais(PacienteCardAdapter.ViewHolder h, Usuario m) {
        h.sivFoto.setVisibility(View.GONE);
        h.tvIniciais.setVisibility(View.VISIBLE);
        h.tvIniciais.setText(m.getIniciais());
    }

    @Override
    public int getItemCount() { return lista.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView  tvIniciais, tvNome, tvEspecialidade;
        ImageButton btnChat;
        ShapeableImageView sivFoto;

        ViewHolder(View v) {
            super(v);
            sivFoto         = v.findViewById(R.id.ivFotoPaciente);
            tvIniciais      = v.findViewById(R.id.tvIniciasPaciente);
            tvNome          = v.findViewById(R.id.tvNomePaciente);
            tvEspecialidade = v.findViewById(R.id.tvTipo);
            btnChat         = v.findViewById(R.id.botaoChat);
        }
    }
}
