package com.clinica.app.Modelo;

public class Consulta {
    private String id, pacienteId, medicoId;
    private String data, hora, status, pagamentoTipo, observacoes;
    private String nomePaciente, nomeMedico, especialidadeMedico;

    private Pagamento pagamento;

    public Consulta() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPacienteId() { return pacienteId; }
    public void setPacienteId(String pacienteId) { this.pacienteId = pacienteId; }

    public String getMedicoId() { return medicoId; }
    public void setMedicoId(String medicoId) { this.medicoId = medicoId; }

    public String getData() { return data; }
    public void setData(String data) { this.data = data; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPagamentoTipo() { return pagamentoTipo; }
    public void setPagamentoTipo(String pagamentoTipo) { this.pagamentoTipo = pagamentoTipo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }

    public String getNomeMedico() { return nomeMedico; }
    public void setNomeMedico(String nomeMedico) { this.nomeMedico = nomeMedico; }

    public String getEspecialidadeMedico() { return especialidadeMedico; }
    public void setEspecialidadeMedico(String especialidadeMedico) { this.especialidadeMedico = especialidadeMedico; }

    public Pagamento getPagamento() { return pagamento; }
    public void setPagamento(Pagamento pagamento) { this.pagamento = pagamento; }

    public String getStatusLabel() {
        if (status == null) return "";
        switch (status) {
            case "confirmada": return "Confirmada";
            case "cancelada":  return "Cancelada";
            default:           return "Pendente";
        }
    }
}