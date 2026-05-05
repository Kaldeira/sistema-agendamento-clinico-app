package com.clinica.app.DAO;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Controle.BancoDados;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;

import java.util.List;

public class AdminConsultaAdapter extends RecyclerView.Adapter<AdminConsultaAdapter.VH> {

    private final Context        ctx;
    private final List<Consulta> lista;

    public AdminConsultaAdapter(Context ctx, List<Consulta> lista) {
        this.ctx   = ctx;
        this.lista = lista;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_consulta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Consulta c = lista.get(pos);

        BancoDados db = BancoDados.getInstance(ctx);
        Usuario paciente = db.buscarUsuarioPorId(c.getPacienteId());
        Usuario medico   = db.buscarUsuarioPorId(c.getMedicoId());

        h.tvId.setText("Consulta #" + c.getId());
        h.tvData.setText(c.getData() + " às " + c.getHora());
        h.tvNomes.setText(
                (paciente != null ? paciente.getNome() : "Paciente #" + c.getPacienteId())
                + " → "
                + (medico != null ? medico.getNome() : "Médico #" + c.getMedicoId())
        );
        h.tvStatus.setText(c.getStatus().toUpperCase());

        int color;
        switch (c.getStatus()) {
            case "confirmada": color = 0xFF4CAF50; break;
            case "cancelada":  color = 0xFFF44336; break;
            default:           color = 0xFFFFC107; break;
        }
        h.tvStatus.setTextColor(color);
    }

    @Override
    public int getItemCount() { return lista.size(); }

    public static class VH extends RecyclerView.ViewHolder {
        TextView tvId, tvData, tvNomes, tvStatus;

        public VH(@NonNull View v) {
            super(v);
            tvId     = v.findViewById(R.id.tvConsultaId);
            tvData   = v.findViewById(R.id.tvConsultaData);
            tvNomes  = v.findViewById(R.id.tvConsultaNomes);
            tvStatus = v.findViewById(R.id.tvConsultaStatus);
        }
    }
}
