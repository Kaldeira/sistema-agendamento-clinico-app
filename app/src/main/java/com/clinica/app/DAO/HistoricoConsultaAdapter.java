package com.clinica.app.DAO;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.R;
import com.clinica.app.Modelo.Consulta;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoricoConsultaAdapter extends RecyclerView.Adapter<HistoricoConsultaAdapter.VH> {

    public interface OnConsultaClick {
        void onContinuarPagamento(Consulta consulta);
    }

    private List<Consulta> lista = new ArrayList<>();
    private final boolean isMedico;
    private OnConsultaClick listener;

    public HistoricoConsultaAdapter(boolean isMedico) {
        this.isMedico = isMedico;
    }

    public void setOnConsultaClick(OnConsultaClick listener) {
        this.listener = listener;
    }

    public void setLista(List<Consulta> lista) {
        this.lista = lista != null ? lista : new ArrayList<>();
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

        h.tvConsultaId.setText("Consulta com " + c.getEspecialidadeMedico());
        h.tvDataHora.setText(c.getData() + " às " + c.getHora());

        if (isMedico) {
            h.tvNomes.setText("Paciente: " + (c.getNomePaciente() != null ? c.getNomePaciente() : "—"));
        } else {
            h.tvNomes.setText("Médico: " + (c.getNomeMedico() != null ? c.getNomeMedico() : "—"));
        }

        Pagamento p = c.getPagamento();

        boolean pagamentoPendente = false;

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
                h.tvStatusPagamento.setTextColor(Color.parseColor("#2E7D32"));

            } else if (Pagamento.STATUS_PENDENTE.equals(statusPag)) {
                pagamentoPendente = true;
                h.tvStatusPagamento.setText("⏳ Pendente - toque para pagar");
                h.tvStatusPagamento.setTextColor(Color.parseColor("#D32F2F"));

            } else {
                h.tvStatusPagamento.setText("❌ Recusado");
                h.tvStatusPagamento.setTextColor(Color.parseColor("#D32F2F"));
            }

            String valor = "R$" + String.format(Locale.getDefault(), "%.2f", p.getValor());
            h.tvValor.setText(valor);

        } else {
            /*
             * Caso não tenha objeto Pagamento,
             * usa o campo pagamento_tipo da Consulta.
             */
            String pagamentoConsulta = c.getPagamentoTipo();

            if (pagamentoConsulta != null &&
                    pagamentoConsulta.equalsIgnoreCase("pendente")) {

                pagamentoPendente = true;
                h.tvStatusPagamento.setText("⏳ Pendente - toque para pagar");
                h.tvStatusPagamento.setTextColor(Color.parseColor("#D32F2F"));

            } else if (pagamentoConsulta != null &&
                    pagamentoConsulta.equalsIgnoreCase("aprovado")) {

                h.tvStatusPagamento.setText("✅ Aprovado");
                h.tvStatusPagamento.setTextColor(Color.parseColor("#2E7D32"));

            } else {
                h.tvStatusPagamento.setText("Sem pagamento");
                h.tvStatusPagamento.setTextColor(Color.parseColor("#777777"));
            }

            h.tvMetodo.setText("—");
            h.tvValor.setText("-");
        }

        h.tvStatusConsulta.setText(c.getStatusLabel());

        /*
         * Só paciente pode continuar pagamento.
         * Médico não deve pagar consulta.
         */
        if (!isMedico && pagamentoPendente) {
            h.itemView.setClickable(true);
            h.itemView.setAlpha(1f);

            h.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContinuarPagamento(c);
                } else {
                    Toast.makeText(
                            v.getContext(),
                            "Pagamento pendente.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

        } else {
            h.itemView.setOnClickListener(null);
            h.itemView.setClickable(false);
        }
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