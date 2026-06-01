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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.clinica.app.Activities.ChatActivity;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MedicoCardAdapter extends RecyclerView.Adapter<MedicoCardAdapter.ViewHolder> {

    public interface OnMedicoClickListener {
        void onClick(Usuario medico);
    }

    private List<Usuario>          lista    = new ArrayList<>();
    private final OnMedicoClickListener listener;
    private final Context context;
    public MedicoCardAdapter(Context context, OnMedicoClickListener listener) {
        this.listener = listener;
        this.context = context;
    }

    public void setLista(List<Usuario> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medico_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Usuario m = lista.get(position);

        String prefixo = m.getGenero().equalsIgnoreCase("masculino") ? "Dr. " : "Dra. ";
        h.tvNome.setText(prefixo + m.getNome());
        h.tvEspecialidade.setText(m.getEspecialidade());

        if (m.getFotoPerfil() != null && !m.getFotoPerfil().isEmpty()) {

            Glide.with(context)
                    .load(m.getFotoPerfil())
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_person)
                    .error(R.drawable.ic_menu_person)
                    .into(h.sivFoto);

            h.sivFoto.setVisibility(View.VISIBLE);
            h.tvIniciais.setVisibility(View.GONE);

//            File f = new File(m.getFotoPerfil());
//            if (f.exists()) {
//                h.sivFoto.setImageURI(Uri.fromFile(f));
//                h.sivFoto.setVisibility(View.VISIBLE);
//                h.tvIniciais.setVisibility(View.GONE);
//            } else {
//                showIniciais(h, m);
//            }
        } else {
            showIniciais(h, m);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(m));

        h.btnChat.setOnClickListener(v -> {
            Context context = v.getContext();
            SessionManager session = new SessionManager(context);

            if (!session.isLogado()) {
                Toast.makeText(context, "Faça login para iniciar uma conversa.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!session.getAprovado()) {
                Toast.makeText(context, "Sua conta ainda não foi aprovada.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (session.isMedico() && session.getUsername().equalsIgnoreCase(m.getUsername())) {
                Toast.makeText(context, "Calma ae paizao!", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("destinatario_id", m.getUsername());
            intent.putExtra("destinatario_username", m.getUsername());
            intent.putExtra("destinatario_nome", m.getNome());
            intent.putExtra("foto_perfil", m.getFotoPerfil());
            context.startActivity(intent);
        });
    }

    private void showIniciais(ViewHolder h, Usuario m) {
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
            sivFoto         = v.findViewById(R.id.ivFotoMedico);
            tvIniciais      = v.findViewById(R.id.tvIniciaisMedico);
            tvNome          = v.findViewById(R.id.tvNomeMedico);
            tvEspecialidade = v.findViewById(R.id.tvEspecialidade);
            btnChat         = v.findViewById(R.id.btnChat);
        }
    }
}
