package com.clinica.app.Controle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.clinica.app.Activities.ChatActivity;
import com.clinica.app.Activities.GerenciarConsultasActivity;
import com.clinica.app.Activities.LoginActivity;
import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.Mensagem;
import com.clinica.app.R;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashSet;
import java.util.Set;

public class NotificacaoReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "clinica_channel";

    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_TEXTO = "texto";

    public static final String TIPO_CHAT = "chat";
    public static final String TIPO_CONSULTA = "consulta";

    private static ListenerRegistration chatListener;
    private static boolean primeiraCarga = true;
    private static final Set<String> usuariosCarregando = new HashSet<>();

    private static ListenerRegistration consultasListener;
    private static boolean primeiraCargaConsultas = true;
    private static final Set<String> consultasNotificadas = new HashSet<>();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;

        String tipo = intent.getStringExtra("tipo");
        String titulo = intent.getStringExtra(EXTRA_TITULO);
        String texto = intent.getStringExtra(EXTRA_TEXTO);

        if (tipo == null || tipo.isEmpty()) {
            return;
        }

        if (!TIPO_CHAT.equals(tipo) && !TIPO_CONSULTA.equals(tipo)) {
            return;
        }

        if (titulo == null || titulo.isEmpty()) {
            if (TIPO_CHAT.equals(tipo)) {
                titulo = "Nova mensagem";
            } else {
                titulo = "Nova consulta";
            }
        }

        if (texto == null || texto.isEmpty()) {
            if (TIPO_CHAT.equals(tipo)) {
                texto = "Você recebeu uma nova mensagem.";
            } else {
                texto = "Você tem uma nova consulta.";
            }
        }

        criarCanalNotificacao(context);

        Intent activityIntent;

        if (TIPO_CHAT.equals(tipo)) { //ao clicar no chat leva para o chat com remetente
            String remetenteUsername = intent.getStringExtra("remetente_username");
            String remetenteNome = intent.getStringExtra("remetente_nome");
            String fotoPerfil = intent.getStringExtra("foto_perfil");

            activityIntent = new Intent(context, ChatActivity.class);
            activityIntent.putExtra("destinatario_username", remetenteUsername);
            activityIntent.putExtra("destinatario_nome", remetenteNome);
            activityIntent.putExtra("foto_perfil", fotoPerfil);

        } else if (TIPO_CONSULTA.equals(tipo)) {
            activityIntent = new Intent(context, GerenciarConsultasActivity.class); //ao clicar na consulta leva pra tela de consultas

        } else {
            activityIntent = new Intent(context, LoginActivity.class);
        }

        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null) {
            nm.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    public static void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Clínica",
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("Notificações da clínica");

            NotificationManager nm = context.getSystemService(NotificationManager.class);

            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }
    }

    public static void iniciarNotificacoesChat(Context context, FirebaseManager fb, SessionManager session) {
        if (context == null || fb == null || session == null) return;
        if (!session.isLogado()) return;

        pararNotificacoesChat();

        Context appContext = context.getApplicationContext();

        primeiraCarga = true;
//listener registration
        chatListener = fb.ouvirNotificacoesMensagens(
                session.getUsername(),
                mensagens -> {

                    if (primeiraCarga) {
                        primeiraCarga = false;
                        return;
                    }

                    for (Mensagem msg : mensagens) {
                        String remetenteId = msg.getRemetenteId();

                        if (remetenteId == null || remetenteId.isEmpty()) {
                            continue;
                        }

                        if (usuariosCarregando.contains(remetenteId)) {
                            continue;
                        }

                        usuariosCarregando.add(remetenteId);

                        fb.buscarUsuarioPorUsername(remetenteId, usuario -> {
                            usuariosCarregando.remove(remetenteId);

                            String nome = remetenteId;
                            String foto = "";

                            if (usuario != null) {
                                nome = usuario.getNome();
                                foto = usuario.getFotoPerfil();
                            }

                            Intent intent = new Intent(appContext, NotificacaoReceiver.class);
                            intent.putExtra("tipo", TIPO_CHAT);
                            intent.putExtra(EXTRA_TITULO, nome);
                            intent.putExtra(EXTRA_TEXTO, msg.getTexto());
                            intent.putExtra("remetente_username", remetenteId);
                            intent.putExtra("remetente_nome", nome);
                            intent.putExtra("foto_perfil", foto);

                            appContext.sendBroadcast(intent);
                        });
                    }
                }
        );
    }

    public static void pararNotificacoesChat() {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }

        usuariosCarregando.clear();
        primeiraCarga = true;
    }

    public static void iniciarNotificacoesConsultasMedico(Context context, FirebaseManager fb, SessionManager session
    ) {
        if (context == null || fb == null || session == null)
            return;

        if (!session.isLogado())
            return;

        if (!session.isMedico()) {
            return;
        }

        pararNotificacoesConsultasMedico();

        Context appContext = context.getApplicationContext();

        primeiraCargaConsultas = true;
//listener registration
        consultasListener = fb.ouvirConsultasConfirmadasMedico(
                session.getUsername(),
                consultas -> {

                    if (primeiraCargaConsultas) {
                        primeiraCargaConsultas = false;
                        return;
                    }

                    for (Consulta consulta : consultas) {
                        if (consulta == null || consulta.getId() == null) continue;

                        if (consultasNotificadas.contains(consulta.getId())) {
                            continue;
                        }

                        consultasNotificadas.add(consulta.getId());

                        String titulo = "Nova consulta confirmada";
                        String texto = "Consulta em " + consulta.getData() + " às " + consulta.getHora();

                        if (consulta.getNomePaciente() != null &&
                                !consulta.getNomePaciente().isEmpty()) {

                            texto = "Paciente: " + consulta.getNomePaciente()
                                    + " • " + consulta.getData()
                                    + " às " + consulta.getHora();
                        }

                        Intent intent = new Intent(appContext, NotificacaoReceiver.class);
                        intent.putExtra("tipo", TIPO_CONSULTA);
                        intent.putExtra(EXTRA_TITULO, titulo);
                        intent.putExtra(EXTRA_TEXTO, texto);
                        intent.putExtra("consulta_id", consulta.getId());
                        intent.putExtra("data", consulta.getData());
                        intent.putExtra("hora", consulta.getHora());

                        appContext.sendBroadcast(intent);
                    }
                }
        );
    }

    public static void pararNotificacoesConsultasMedico() {
        if (consultasListener != null) {
            consultasListener.remove();
            consultasListener = null;
        }

        consultasNotificadas.clear();
        primeiraCargaConsultas = true;
    }
}