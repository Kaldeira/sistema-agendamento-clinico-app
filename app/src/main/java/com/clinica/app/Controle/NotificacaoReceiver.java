package com.clinica.app.Controle;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.clinica.app.R;
import com.clinica.app.Activities.LoginActivity;


public class NotificacaoReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "clinica_channel";
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_TEXTO = "texto";

    @Override
    public void onReceive(Context context, Intent intent) {
        String titulo = intent.getStringExtra(EXTRA_TITULO);
        String texto = intent.getStringExtra(EXTRA_TEXTO);
        if (titulo == null) titulo = "Lembrete de Consulta";
        if (texto == null) texto = "Você tem uma consulta agendada.";

        criarCanalNotificacao(context);

        Intent activityIntent = new Intent(context, LoginActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    public static void criarCanalNotificacao(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Consultas", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Lembretes de consultas médicas");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(channel);
        }
    }
}
