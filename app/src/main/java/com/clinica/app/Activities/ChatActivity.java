package com.clinica.app.Activities;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.clinica.app.DAO.MensagemAdapter;
import com.clinica.app.Controle.BancoDados;
import com.clinica.app.R;
import com.clinica.app.databinding.ActivityChatBinding;
import com.clinica.app.Modelo.Mensagem;
import com.clinica.app.Controle.SessionManager;
import com.google.android.material.imageview.ShapeableImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private BancoDados db;
    private SessionManager session;
    private MensagemAdapter adapter;
    private int destinatarioId;
    private String destinatarioNome, fotoPerfil;

    TextView nomeDestinatario;
    ShapeableImageView fotoDestinario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = BancoDados.getInstance(this);
        session = new SessionManager(this);

        destinatarioId = getIntent().getIntExtra("destinatario_id", -1);
        destinatarioNome = getIntent().getStringExtra("destinatario_nome");
        fotoPerfil = getIntent().getStringExtra("foto_perfil");

        //getSupportActionBar().setTitle(destinatarioNome);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (destinatarioId == -1) {
            finish();
            return;
        }

        nomeDestinatario = findViewById(R.id.tvNomeChat);
        fotoDestinario = findViewById(R.id.imgFotoPerfil);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        binding.rvMensagens.setLayoutManager(layoutManager);

        adapter = new MensagemAdapter(session.getUserId());
        binding.rvMensagens.setAdapter(adapter);

        carregarMensagens();

        binding.btnEnviar.setOnClickListener(v -> enviarMensagem());
        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
        nomeDestinatario.setText(destinatarioNome);

        if (fotoPerfil != null && !fotoPerfil.isEmpty()) {
            File f = new File(fotoPerfil);
            if (f.exists()) {
                fotoDestinario.setImageURI(Uri.fromFile(f));
                fotoDestinario.setVisibility(View.VISIBLE);
            }
        }
    }

    private void carregarMensagens() {
        db.marcarMensagensComoLidas(destinatarioId, session.getUserId());
        List<Mensagem> msgs = db.buscarConversa(session.getUserId(), destinatarioId);
        adapter.setLista(msgs);
        if (!msgs.isEmpty())
            binding.rvMensagens.scrollToPosition(msgs.size() - 1);
    }

    private void enviarMensagem() {
        String texto = binding.etMensagem.getText().toString().trim();
        if (TextUtils.isEmpty(texto)) return;

        Mensagem m = new Mensagem();
        m.setRemetenteId(session.getUserId());
        m.setDestinatarioId(destinatarioId);
        m.setTexto(texto);
        m.setDataHora(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date()));

        long id = db.enviarMensagem(m);
        if (id > 0) {
            binding.etMensagem.setText("");
            carregarMensagens();
        } else {
            Toast.makeText(this, "Erro ao enviar mensagem.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
