package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;
import com.clinica.app.Modelo.HistoricoMedico;

import java.util.ArrayList;
import java.util.List;

public class HistoricoMedicoAdapter extends RecyclerView.Adapter<HistoricoMedicoAdapter.VH> {

    private List<HistoricoMedico> lista = new ArrayList<>();

    public void setLista(List<HistoricoMedico> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico_medico, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        HistoricoMedico hm = lista.get(pos);
        h.tvData.setText(hm.getData());
        h.tvMedico.setText("Dr(a): " + (hm.getNomeMedico() != null ? hm.getNomeMedico() : "—"));
        h.tvDiagnostico.setText("Diagnóstico: " + (hm.getDiagnostico() != null ? hm.getDiagnostico() : "—"));
        h.tvObservacoes.setText("Obs: " + (hm.getObservacoes() != null ? hm.getObservacoes() : "—"));
        h.tvPrescricao.setText("Prescrição: " + (hm.getPrescricao() != null ? hm.getPrescricao() : "—"));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvData, tvMedico, tvDiagnostico, tvObservacoes, tvPrescricao;

        VH(View v) {
            super(v);
            tvData = v.findViewById(R.id.tvData);
            tvMedico = v.findViewById(R.id.tvMedico);
            tvDiagnostico = v.findViewById(R.id.tvDiagnostico);
            tvObservacoes = v.findViewById(R.id.tvObservacoes);
            tvPrescricao = v.findViewById(R.id.tvPrescricao);
        }
    }
}
