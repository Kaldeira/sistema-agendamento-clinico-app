package com.clinica.app.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import com.clinica.app.Activities.*;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.R;

public class BarraNavHelper {

    public static void setupBottomNav(
            Activity activity,
            LinearLayout navHome,
            LinearLayout navPerfil,
            LinearLayout navConsultas,
            LinearLayout navChat,
            LinearLayout navHistorico,
            LinearLayout navPacientes,
            LinearLayout navAdmin,
            View navLoginBtn
    ) {

        SessionManager session = new SessionManager(activity);

        // Reset
        navPerfil.setVisibility(View.GONE);
        navConsultas.setVisibility(View.GONE);
        navChat.setVisibility(View.GONE);
        navHistorico.setVisibility(View.GONE);
        navPacientes.setVisibility(View.GONE);
        navAdmin.setVisibility(View.GONE);
        navLoginBtn.setVisibility(View.GONE);

        // Home sempre
        navHome.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, HomeActivity.class)));

        // Não logado
        if (!session.isLogado()) {
            navLoginBtn.setVisibility(View.VISIBLE);
            navLoginBtn.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, LoginActivity.class)));
            return;
        }

        // Perfil
        navPerfil.setVisibility(View.VISIBLE);
        navPerfil.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, PerfilActivity.class)));

        // Paciente
        if (session.isPaciente()) {

            navConsultas.setVisibility(View.VISIBLE);
            navChat.setVisibility(View.VISIBLE);
            navHistorico.setVisibility(View.VISIBLE);

            navConsultas.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, HistoricoConsultasActivity.class)));

            navChat.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, ChatListActivity.class)));

            navHistorico.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, HistoricoMedicoActivity.class)));

        }

        // Médico
        else if (session.isMedico()) {

            navHistorico.setVisibility(View.VISIBLE);
            navConsultas.setVisibility(View.VISIBLE);
            navChat.setVisibility(View.VISIBLE);

            navHistorico.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, HistoricoMedicoActivity.class)));

            navConsultas.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, GerenciarConsultasActivity.class)));

            navChat.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, ChatListActivity.class)));
        }

        // Admin
        else if (session.isAdmin()) {

            navAdmin.setVisibility(View.VISIBLE);
            navAdmin.setOnClickListener(v ->
                    activity.startActivity(new Intent(activity, AdminDashboardActivity.class)));
        }
    }
}
