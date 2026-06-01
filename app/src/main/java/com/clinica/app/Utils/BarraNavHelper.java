package com.clinica.app.Utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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

        navPerfil.setVisibility(View.GONE);
        navConsultas.setVisibility(View.GONE);
        navChat.setVisibility(View.GONE);
        navHistorico.setVisibility(View.GONE);
        navPacientes.setVisibility(View.GONE);
        navAdmin.setVisibility(View.GONE);
        navLoginBtn.setVisibility(View.GONE);

        navHome.setOnClickListener(v ->
                navegar(activity, HomeActivity.class));

        if (!session.isLogado()) {

            navLoginBtn.setVisibility(View.VISIBLE);

            navLoginBtn.setOnClickListener(v ->
                    navegar(activity, LoginActivity.class));

            return;
        }

        navPerfil.setVisibility(View.VISIBLE);

        navPerfil.setOnClickListener(v ->
                navegar(activity, PerfilActivity.class));

        if (session.isPaciente()) {

            navConsultas.setVisibility(View.VISIBLE);
            navChat.setVisibility(View.VISIBLE);
            navHistorico.setVisibility(View.VISIBLE);

            navConsultas.setOnClickListener(v ->
                    navegar(activity, HistoricoConsultasActivity.class));

            navChat.setOnClickListener(v ->
                    navegar(activity, ChatListActivity.class));

            navHistorico.setOnClickListener(v ->
                    navegar(activity, HistoricoMedicoActivity.class));

        }

        else if (session.isMedico()) {

            navHistorico.setVisibility(View.VISIBLE);
            navConsultas.setVisibility(View.VISIBLE);
            navChat.setVisibility(View.VISIBLE);

            if (!session.getAprovado()) {
                navHistorico.setOnClickListener(v -> Toast.makeText(
                        activity,
                        "Sua conta ainda não foi aprovada pelo administrador.",
                        Toast.LENGTH_LONG
                ).show());

                navConsultas.setOnClickListener(v -> Toast.makeText(
                        activity,
                        "Sua conta ainda não foi aprovada pelo administrador.",
                        Toast.LENGTH_LONG
                ).show());

                navChat.setOnClickListener(v -> Toast.makeText(
                        activity,
                        "Sua conta ainda não foi aprovada pelo administrador.",
                        Toast.LENGTH_LONG
                ).show());

                return;
            }

            navHistorico.setOnClickListener(v ->
                    navegar(activity, ProntuarioPacienteActivity.class));

            navConsultas.setOnClickListener(v ->
                    navegar(activity, GerenciarConsultasActivity.class));

            navChat.setOnClickListener(v ->
                    navegar(activity, ChatListActivity.class));
        }

        else if (session.isAdmin()) {

            navAdmin.setVisibility(View.VISIBLE);

            navAdmin.setOnClickListener(v ->
                    navegar(activity, AdminDashboardActivity.class));
        }
    }

    private static void navegar(Activity activity, Class<?> destino) {

        // se ja estiver na tela, nao vai fazer nada
        if (activity.getClass().equals(destino)) {
            return;
        }

        Intent intent = new Intent(activity, destino);
        activity.startActivity(intent);

        activity.overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.fade_out
        );
    }
}