package com.clinica.app.DAO;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

    public MedicoCardAdapter(OnMedicoClickListener listener) {
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
            File f = new File(m.getFotoPerfil());
            if (f.exists()) {
                h.sivFoto.setImageURI(Uri.fromFile(f));
                h.sivFoto.setVisibility(View.VISIBLE);
                h.tvIniciais.setVisibility(View.GONE);
            } else {
                showIniciais(h, m);
            }
        } else {
            showIniciais(h, m);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(m));
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
        ShapeableImageView sivFoto;

        ViewHolder(View v) {
            super(v);
            sivFoto         = v.findViewById(R.id.ivFotoMedico);
            tvIniciais     = v.findViewById(R.id.tvIniciaisMedico);
            tvNome         = v.findViewById(R.id.tvNomeMedico);
            tvEspecialidade= v.findViewById(R.id.tvEspecialidade);
        }
    }
}
