package com.clinica.app.Activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.DAO.MensagemAdapter;
import com.clinica.app.R;
import com.clinica.app.databinding.ActivityChatBinding;
import com.clinica.app.Modelo.Mensagem;
import com.clinica.app.Controle.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private FirebaseManager fb;
    private SessionManager session;
    private MensagemAdapter adapter;

    private String destinatarioNome, fotoPerfil, destinatarioUsername;
    private ListenerRegistration mensagensListener;

    TextView nomeDestinatario;
    ShapeableImageView fotoDestinario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        session = new SessionManager(this);
        fb = FirebaseManager.getInstance();

        destinatarioNome = getIntent().getStringExtra("destinatario_nome");
        destinatarioUsername = getIntent().getStringExtra("destinatario_username");
        fotoPerfil = getIntent().getStringExtra("foto_perfil");

        Log.d("CHAT", "destinatarioUsername: " + destinatarioUsername);

        if (destinatarioUsername == null || destinatarioUsername.isEmpty()) {
            finish();
            return;
        }

        nomeDestinatario = findViewById(R.id.tvNomeChat);
        fotoDestinario = findViewById(R.id.imgFotoPerfil);

        nomeDestinatario.setText(destinatarioNome);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMensagens.setLayoutManager(layoutManager);

        adapter = new MensagemAdapter(session.getUsername());
        binding.rvMensagens.setAdapter(adapter);

        binding.btnEnviar.setOnClickListener(v -> enviarMensagem());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            Glide.with(this)
                    .load(fotoPerfil)
                    .circleCrop()
                    .placeholder(R.drawable.ic_menu_person)
                    .error(R.drawable.ic_menu_person)
                    .into(fotoDestinario);

            fotoDestinario.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        iniciarListenerMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mensagensListener != null) {
            mensagensListener.remove();
            mensagensListener = null;
        }
    }

    private void iniciarListenerMensagens() {
        fb.marcarMensagensComoLidas(destinatarioUsername, session.getUsername());

        mensagensListener = fb.ouvirConversa(
                session.getUsername(),
                destinatarioUsername,
                msgs -> runOnUiThread(() -> {
                    adapter.setLista(msgs);

                    if (!msgs.isEmpty()) {
                        binding.rvMensagens.scrollToPosition(msgs.size() - 1);
                    }

                    fb.marcarMensagensComoLidas(
                            destinatarioUsername,
                            session.getUsername()
                    );
                })
        );
    }

    private void enviarMensagem() {
        String texto = binding.etMensagem.getText().toString().trim();

        if (TextUtils.isEmpty(texto)) return;

        Mensagem m = new Mensagem();
        m.setRemetenteId(session.getUsername());
        m.setDestinatarioId(destinatarioUsername);
        m.setTexto(texto);
        m.setDataHora(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date()));

        binding.etMensagem.setText("");

        fb.enviarMensagem(
                m,
                id -> {},
                e -> Toast.makeText(this, "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}