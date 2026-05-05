package com.clinica.app.Modelo;

public class Usuario {
    private int id;
    private String nome;
    private String email;
    private String senha;
    private String tipo;          // paciente | medico | admin
    private String cpf;
    private String especialidade;
    private String descricao;
    private String fotoPerfil;    // local file path (nullable)
    private String genero;
    private String crm;
    private String username;

    public Usuario() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEspecialidade() {
        return especialidade;
    }

    public void setEspecialidade(String e) {
        this.especialidade = e;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String d) {
        this.descricao = d;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String f) {
        this.fotoPerfil = f;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String g) {
        this.genero = g;
    }

    public String getCRM(){
        return crm;
    }

    public void setCRM(String c){
        this.crm = c;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String u){
        this.username = u;
    }

    public boolean isMedico() {
        return "medico".equalsIgnoreCase(tipo);
    }

    public boolean isPaciente() {
        return "paciente".equalsIgnoreCase(tipo);
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(tipo);
    }

    public String getIniciais() {
        if (nome == null || nome.isEmpty()) return "?";
        String[] parts = nome.split(" ");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}
