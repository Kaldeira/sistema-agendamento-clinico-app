package com.clinica.app.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.clinica.app.Controle.FirebaseManager;
import com.clinica.app.Controle.NotificacaoReceiver;
import com.clinica.app.Controle.SessionManager;
import com.clinica.app.Modelo.Usuario;
import com.clinica.app.R;
import com.clinica.app.Utils.BarraNavHelper;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class PerfilActivity extends AppCompatActivity {

    private FirebaseManager fb;
    private SessionManager session;
    private Usuario usuario;

    private ImageView ivFoto;
    private TextView  tvIniciais;
    private EditText  etNome, etEmail, etCpf, etEspecialidade, etDescricao, etGenero, etCRM, etUsername, etSenha;
    private TextView  tvTipo;
    private androidx.cardview.widget.CardView layoutMedico;

    private ActivityResultLauncher<Intent> galleryLauncher;

    private LinearLayout navHome, navPerfil, navConsultas, navChat, navHistorico;
    private LinearLayout navPacientes, navAdmin;
    private View navLoginBtn;
    private TextView tvGreeting, tvNome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        session = new SessionManager(this);
        fb      = FirebaseManager.getInstance();

        if (!session.isLogado()) { finish(); return; }

        bindViews();
        registerGalleryLauncher();

        BarraNavHelper.setupBottomNav(
                this,
                findViewById(R.id.navHome),
                findViewById(R.id.navPerfil),
                findViewById(R.id.navConsultas),
                findViewById(R.id.navChat),
                findViewById(R.id.navHistorico),
                findViewById(R.id.navPacientes),
                findViewById(R.id.navAdmin),
                findViewById(R.id.navLogin)
        );

        carregarPerfil();

        ivFoto.setOnClickListener(v -> abrirGaleria());
        tvIniciais.setOnClickListener(v -> abrirGaleria());

        Button btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(v -> salvarPerfil());

        Button btnSair = findViewById(R.id.btnSair);
        btnSair.setOnClickListener(v -> logout());

        findViewById(R.id.btnVoltar).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        ivFoto          = findViewById(R.id.ivFotoPerfil);
        tvIniciais      = findViewById(R.id.tvIniciais);
        tvTipo          = findViewById(R.id.tvTipo);
        etNome          = findViewById(R.id.etNome);
        etEmail         = findViewById(R.id.etEmail);
        etCpf           = findViewById(R.id.etCpf);
        etEspecialidade = findViewById(R.id.etEspecialidade);
        etDescricao     = findViewById(R.id.etDescricao);
        etGenero        = findViewById(R.id.etGenero);
        etCRM           = findViewById(R.id.etCRM);
        etUsername      = findViewById(R.id.etUsername);
        etSenha         = findViewById(R.id.etPassWord);
        layoutMedico    = findViewById(R.id.layoutMedico);
        tvNome          = findViewById(R.id.tvNome);

        tvGreeting   = findViewById(R.id.tvGreeting);
        navHome      = findViewById(R.id.navHome);
        navPerfil    = findViewById(R.id.navPerfil);
        navConsultas = findViewById(R.id.navConsultas);
        navChat      = findViewById(R.id.navChat);
        navHistorico = findViewById(R.id.navHistorico);
        navPacientes = findViewById(R.id.navPacientes);
        navAdmin     = findViewById(R.id.navAdmin);
        navLoginBtn  = findViewById(R.id.navLogin);
    }

    private void carregarPerfil() {
        fb.buscarUsuarioPorUsername(session.getUsername(), u -> {
            if (u == null) { finish(); return; }
            this.usuario = u;

            runOnUiThread(() -> {
                etNome.setText(u.getNome());
                tvNome.setText(u.getNome());
                etEmail.setText(u.getEmail());
                etCpf.setText(u.getCpf());
                etUsername.setText(u.getUsername());
                etUsername.setEnabled(false); // username é o ID do documento, não editável
                etSenha.setText("");
                //etSenha.setHint("Digite uma nova senha para alterar");

                if (u.isMedico() && !u.getAprovado()) {
                    tvTipo.setText(tipoLabel(u.getTipo()) + " : Aguardando aprovação");
                    tvTipo.setTextColor(Color.parseColor("#D32F2F"));
                } else {
                    tvTipo.setText(tipoLabel(u.getTipo()));
                }

                String genero = u.getGenero();
                if (genero != null && !genero.isEmpty()) {
                    genero = genero.substring(0, 1).toUpperCase() + genero.substring(1).toLowerCase();
                    etGenero.setText(genero);
                }

                if (u.isMedico()) {
                    layoutMedico.setVisibility(View.VISIBLE);
                    etEspecialidade.setText(u.getEspecialidade());
                    etDescricao.setText(u.getDescricao());
                    etCRM.setText(u.getCRM());
                } else {
                    layoutMedico.setVisibility(View.GONE);
                }

                if (u.getFotoPerfil() != null && !u.getFotoPerfil().isEmpty()) {
                    Glide.with(this)
                            .load(u.getFotoPerfil())
                            .circleCrop()
                            .placeholder(R.drawable.ic_menu_person)
                            .error(R.drawable.ic_menu_person)
                            .into(ivFoto);
                    ivFoto.setVisibility(View.VISIBLE);
                    tvIniciais.setVisibility(View.GONE);
                } else {
                    ivFoto.setVisibility(View.GONE);
                    tvIniciais.setVisibility(View.VISIBLE);
                    tvIniciais.setText(u.getIniciais());
                }
            });
        });
    }

    private void salvarPerfil() {
        String nome      = etNome.getText().toString().trim();
        String email     = etEmail.getText().toString().trim();
        String novaSenha = etSenha.getText().toString().trim();
        String senhaAtual= session.getSenha();

        boolean senhaAlterada = false;

        if (nome.isEmpty() || email.isEmpty()) {
            Snackbar.make(etNome, "Nome e e-mail são obrigatórios", Snackbar.LENGTH_SHORT).show();
            return;
        }

        if (novaSenha.length() >= 1 && novaSenha.length() <= 3) {
            Snackbar.make(etSenha, "Senha deve ter mais de 3 caracteres!", Snackbar.LENGTH_SHORT).show();
        }

        if (novaSenha.length() > 3) {
            usuario.setSenha(novaSenha);
            senhaAlterada = true;
        }


//        if (!senhaAtual.equals(novaSenha)) {
//            if (novaSenha.isEmpty()) {
//                Snackbar.make(etSenha, "Senha não pode ser vazio!", Snackbar.LENGTH_SHORT).show();
//                return;
//            }
//            usuario.setSenha(novaSenha);
//        }


        usuario.setNome(nome);
        usuario.setEmail(email);

        if (usuario.isMedico()) {
            usuario.setEspecialidade(etEspecialidade.getText().toString().trim());
            usuario.setDescricao(etDescricao.getText().toString().trim());
        }

        fb.atualizarUsuario(usuario, status -> {
            if (status) {

                session.criarSessao(
                        usuario.getNome(),
                        usuario.getTipo(),
                        usuario.getEmail(),
                        usuario.getFotoPerfil(),
                        usuario.getUsername(),
                        usuario.getSenha(),
                        usuario.getAprovado()
                );
                runOnUiThread(() ->
                        Snackbar.make(etNome, "✅ Perfil atualizado!", Snackbar.LENGTH_SHORT).show());
            } else {
                runOnUiThread(() ->
                        Snackbar.make(etNome, "Erro ao salvar. Tente novamente.", Snackbar.LENGTH_SHORT).show());
            }
        });
    }

        private void registerGalleryLauncher() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) salvarFotoLocal(uri);
                    }
                });
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String imageToBase64(Uri imageUri) {
        try (InputStream inputStream = getContentResolver().openInputStream(imageUri)) {
            if (inputStream == null) return null;
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("IMG", "Erro ao converter imagem", e);
            return null;
        }
    }

    private void uploadImagem(String username, Uri imageUri) {
        String base64 = imageToBase64(imageUri);
        if (base64 == null) {
            Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show();
            return;
        }

        String apiKey = this.getString(R.string.imgbb_api_key);
        String url    = "https://api.imgbb.com/1/upload?key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(this);
        final String base64Final = base64;

        StringRequest request = new StringRequest(
                Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject json     = new JSONObject(response);
                        String imageUrl     = json.getJSONObject("data").getString("url");

                        fb.atualizarFotoPerfil(username, imageUrl, ok -> {
                            if (ok) {
                                session.criarSessao(
                                        session.getNome(),
                                        session.getTipo(),
                                        session.getEmail(),
                                        imageUrl,
                                        session.getUsername(),
                                        session.getSenha(),
                                        usuario.getAprovado()
                                );
                                runOnUiThread(() -> {
                                    Glide.with(this).load(imageUrl).circleCrop().into(ivFoto);
                                    ivFoto.setVisibility(View.VISIBLE);
                                    tvIniciais.setVisibility(View.GONE);
                                    Toast.makeText(this, "✅ Foto Atualizada com Sucesso!", Toast.LENGTH_SHORT).show();
                                });
                            }
                        });
                    } catch (Exception e) {
                        Log.e("IMG", "Erro ao parsear resposta", e);
                    }
                },
                error -> {
                    if (error.networkResponse != null)
                        Log.e("IMG", "Erro ImgBB: " + new String(error.networkResponse.data));
                    else
                        Log.e("IMG", "Erro de rede", error);
                    Toast.makeText(this, "Falha no upload.", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("image", base64Final);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        queue.add(request);
    }

    private void salvarFotoLocal(Uri uri) {
        try {
            ivFoto.setImageURI(uri);
            ivFoto.setVisibility(View.VISIBLE);
            tvIniciais.setVisibility(View.GONE);
            uploadImagem(session.getUsername(), uri);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao processar foto.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        session.encerrarSessao();
        NotificacaoReceiver.pararNotificacoesChat();
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private String tipoLabel(String tipo) {
        if (tipo == null) return "";
        switch (tipo) {
            case "medico":   return "Médico";
            case "paciente": return "Paciente";
            case "admin":    return "Administrador";
            default:         return tipo;
        }
    }
}