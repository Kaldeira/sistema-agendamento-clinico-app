package com.clinica.app.DAO;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.clinica.app.R;

import java.util.ArrayList;
import java.util.List;

public class SlotAgendaAdapter extends RecyclerView.Adapter<SlotAgendaAdapter.VH> {

    public interface OnSlotClick {
        void onClick(String data, String hora);
    }

    private List<String[]> slots = new ArrayList<>();
    private String dataAtual = "";
    private final OnSlotClick listener;

    public SlotAgendaAdapter(OnSlotClick listener) {
        this.listener = listener;
    }

    public void setSlots(List<String[]> slots, String data) {
        this.slots = slots;
        this.dataAtual = data;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_slot_agenda, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        String[] slot = slots.get(pos);
        // slot[0]=id, slot[1]=hora, slot[2]=disponivel
        String hora = slot[1];
        boolean disponivel = "1".equals(slot[2]);

        h.tvHora.setText(hora);
        if (disponivel) {
            h.tvHora.setBackgroundResource(R.drawable.bg_slot_disponivel);
            h.tvHora.setTextColor(Color.WHITE);
            h.itemView.setOnClickListener(v -> listener.onClick(dataAtual, hora));
        } else {
            h.tvHora.setBackgroundResource(R.drawable.bg_slot_ocupado);
            h.tvHora.setTextColor(Color.RED);
            h.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return slots.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvHora;

        VH(View v) {
            super(v);
            tvHora = v.findViewById(R.id.tvHora);
        }
    }
}
