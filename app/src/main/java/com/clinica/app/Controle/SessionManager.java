package com.clinica.app.Controle;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context ctx) {
        prefs  = ctx.getSharedPreferences("clinica_session", Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void criarSessao(String nome, String tipo, String email,
                            String foto, String username, String senhaAtual) {
        editor.putBoolean("is_logged",   true);
        editor.putString("user_nome",    nome);
        editor.putString("user_tipo",    tipo);
        editor.putString("user_email",   email);
        editor.putString("user_foto",    foto);
        editor.putString("username",     username);
        editor.putString("senhaAtual",   senhaAtual);
        editor.apply();
    }

    public void criarSessao(int idIgnorado, String nome, String tipo, String email,
                            String foto, String username, String senhaAtual) {
        criarSessao(nome, tipo, email, foto, username, senhaAtual);
    }

    public void encerrarSessao() {
        editor.clear().apply();
    }

    public boolean isLogado()  { return prefs.getBoolean("is_logged", false); }

    @Deprecated
    public int getUserId()     { return -1; }

    public String getNome()    { return prefs.getString("user_nome",  ""); }
    public String getTipo()    { return prefs.getString("user_tipo",  ""); }
    public String getSenha()   { return prefs.getString("senhaAtual", ""); }
    public String getEmail()   { return prefs.getString("user_email", ""); }
    public String getFotoPerfil() { return prefs.getString("user_foto", ""); }
    public String getUsername()   { return prefs.getString("username",  ""); }

    public boolean isMedico()   { return "medico".equals(getTipo()); }
    public boolean isPaciente() { return "paciente".equals(getTipo()); }
    public boolean isAdmin()    { return "admin".equals(getTipo()); }

    public String getIniciais() {
        String nome = getNome();
        if (nome == null || nome.isEmpty()) return "?";
        String[] parts = nome.split(" ");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }
}