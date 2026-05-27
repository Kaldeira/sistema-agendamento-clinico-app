package com.clinica.app.Modelo;

public class Pagamento {
    public static final String METODO_PIX      = "pix";
    public static final String METODO_CARTAO   = "cartao";
    public static final String METODO_DINHEIRO = "dinheiro";

    public static final String STATUS_PENDENTE  = "pendente";
    public static final String STATUS_APROVADO  = "aprovado";
    public static final String STATUS_RECUSADO  = "recusado";

    private int    id;
    private String    consultaId;
    private String metodo;
    private String status;
    private String mpPaymentId;    // Mercado Pago payment_id (nullable)
    private String mpPreferenceId; // Mercado Pago preference_id (nullable)
    private double valor;
    private String dataHora;

    public Pagamento() {}

    public Pagamento(String consultaId, String metodo, double valor, String dataHora) {
        this.consultaId = consultaId;
        this.metodo     = metodo;
        this.valor      = valor;
        this.dataHora   = dataHora;
        this.status     = STATUS_PENDENTE;
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public int    getId()              { return id; }
    public void   setId(int id)        { this.id = id; }

    public String    getConsultaId()                    { return consultaId; }
    public void   setConsultaId(String consultaId)      { this.consultaId = consultaId; }

    public String getMetodo()                        { return metodo; }
    public void   setMetodo(String metodo)           { this.metodo = metodo; }

    public String getStatus()                        { return status; }
    public void   setStatus(String status)           { this.status = status; }

    public String getMpPaymentId()                   { return mpPaymentId; }
    public void   setMpPaymentId(String id)          { this.mpPaymentId = id; }

    public String getMpPreferenceId()                { return mpPreferenceId; }
    public void   setMpPreferenceId(String id)       { this.mpPreferenceId = id; }

    public double getValor()                         { return valor; }
    public void   setValor(double valor)             { this.valor = valor; }

    public String getDataHora()                      { return dataHora; }
    public void   setDataHora(String dataHora)       { this.dataHora = dataHora; }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    public boolean isAprovado()  { return STATUS_APROVADO.equals(status); }
    public boolean isPendente()  { return STATUS_PENDENTE.equals(status); }
    public boolean isRecusado()  { return STATUS_RECUSADO.equals(status); }

    public String getMetodoLabel() {
        switch (metodo != null ? metodo : "") {
            case METODO_PIX:      return "PIX";
            case METODO_CARTAO:   return "Cartão de Crédito";
            case METODO_DINHEIRO: return "Dinheiro";
            default:              return metodo;
        }
    }

    public String getStatusLabel() {
        switch (status != null ? status : "") {
            case STATUS_APROVADO: return "✅ Aprovado";
            case STATUS_PENDENTE: return "⏳ Pendente";
            case STATUS_RECUSADO: return "❌ Recusado";
            default:              return status;
        }
    }
}
