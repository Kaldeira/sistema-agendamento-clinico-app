package com.clinica.app.DAO;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.R;
import com.clinica.app.Modelo.Consulta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoConsultaAdapter extends RecyclerView.Adapter<HistoricoConsultaAdapter.VH> {

    private List<Consulta> lista = new ArrayList<>();
    private final boolean isMedico;

    public HistoricoConsultaAdapter(boolean isMedico) {
        this.isMedico = isMedico;
    }

    public void setLista(List<Consulta> lista) {
        this.lista = lista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_historico_consulta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        Consulta c = lista.get(pos);

        // ID
        h.tvConsultaId.setText("Consulta #" + c.getId());

        // Data
        h.tvDataHora.setText(c.getData() + " às " + c.getHora());

        // Nome
        if (isMedico) {
            h.tvNomes.setText("Paciente: " +
                    (c.getNomePaciente() != null ? c.getNomePaciente() : "—"));
        } else {
            h.tvNomes.setText("Médico: " +
                    (c.getNomeMedico() != null ? c.getNomeMedico() : "—") +
                    (c.getEspecialidadeMedico() != null
                            ? " • " + c.getEspecialidadeMedico()
                            : ""));
        }

        Pagamento p = c.getPagamento();

        if (p != null) {
            switch (p.getMetodo()) {
                case "pix":
                    h.tvMetodo.setText("PIX");
                    break;
                case "cartao":
                    h.tvMetodo.setText("Cartão");
                    break;
                case "dinheiro":
                    h.tvMetodo.setText("Dinheiro");
                    break;
                default:
                    h.tvMetodo.setText("—");
                    break;
            }

            String statusPag = p.getStatus();

            if (Pagamento.STATUS_APROVADO.equals(statusPag)) {
                h.tvStatusPagamento.setText("✅ Aprovado");
            } else if (Pagamento.STATUS_PENDENTE.equals(statusPag)) {
                h.tvStatusPagamento.setText("⏳ Pendente");
            } else {
                h.tvStatusPagamento.setText("❌ Recusado");
            }

            String valor = "R$" + String.format(Locale.getDefault(), "%.2f", p.getValor());
            h.tvValor.setText(valor);

        } else {
            h.tvMetodo.setText("—");
            h.tvStatusPagamento.setText("Sem pagamento");
            h.tvValor.setText("-");
        }

        h.tvStatusConsulta.setText(c.getStatusLabel());
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView tvConsultaId, tvStatusConsulta;
        TextView tvDataHora, tvNomes;
        TextView tvMetodo, tvValor, tvStatusPagamento;

        VH(View v) {
            super(v);

            tvConsultaId = v.findViewById(R.id.tvConsultaId);
            tvStatusConsulta = v.findViewById(R.id.tvStatusConsulta);

            tvDataHora = v.findViewById(R.id.tvDataHora);
            tvNomes = v.findViewById(R.id.tvNomes);

            tvMetodo = v.findViewById(R.id.tvMetodo);
            tvValor = v.findViewById(R.id.tvValor);
            tvStatusPagamento = v.findViewById(R.id.tvStatusPagamento);
        }
    }
}
