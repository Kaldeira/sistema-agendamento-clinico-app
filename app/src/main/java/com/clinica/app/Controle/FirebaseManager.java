package com.clinica.app.Controle;

import android.util.Log;

import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Mensagem;
import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.Modelo.Usuario;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FirebaseManager — substitui BancoDados (SQLite) usando Cloud Firestore.
 *
 * Coleções  →  usuarios | consultas | mensagens | historico_medico | agenda | pagamentos
 * Documentos → mesmos campos das tabelas originais.
 *
 * Todos os métodos são assíncronos. Use as interfaces de callback para receber resultados.
 *
 * CORREÇÕES v2:
 *  - docToPagamento: usa doc.getId() para consultaId quando o campo "consulta_id" está ausente
 *  - registrarPagamento: garante que consulta_id é gravado no documento
 *  - docToHistorico: preenche pacienteId, medicoId e nomeMedico
 *  - buscarHistoricoPorPaciente: resolve nomeMedico via join após buscar o histórico
 */
public class FirebaseManager {

    // ─── Coleções ────────────────────────────────────────────────────────────
    private static final String COL_USUARIOS   = "usuarios";
    private static final String COL_CONSULTAS  = "consultas";
    private static final String COL_MENSAGENS  = "mensagens";
    private static final String COL_HISTORICO  = "historico_medico";
    private static final String COL_AGENDA     = "agenda";
    private static final String COL_PAGAMENTOS = "pagamentos";

    // ─── Singleton ───────────────────────────────────────────────────────────
    private static FirebaseManager instance;
    private final FirebaseFirestore db;

    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference();

    private FirebaseManager() {
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized FirebaseManager getInstance() {
        if (instance == null) instance = new FirebaseManager();
        return instance;
    }

    // =========================================================================
    // INTERFACES DE CALLBACK
    // =========================================================================

    public interface OnSuccess  { void onSuccess(); }
    public interface OnError    { void onError(Exception e); }

    public interface OnUsuarioResult  { void onResult(Usuario usuario); }
    public interface OnUsuariosResult { void onResult(List<Usuario> lista); }

    public interface OnConsultaResult  { void onResult(Consulta consulta); }
    public interface OnConsultasResult { void onResult(List<Consulta> lista); }

    public interface OnPagamentoResult  { void onResult(Pagamento pagamento); }
    public interface OnPagamentosResult { void onResult(List<Pagamento> lista); }

    public interface OnMensagensResult { void onResult(List<Mensagem> lista); }
    public interface OnContatosResult  { void onResult(List<Integer> ids); }

    public interface OnHistoricoResult { void onResult(List<HistoricoMedico> lista); }

    public interface OnSlotsResult { void onResult(List<String[]> slots); }
    public interface OnDatasResult { void onResult(List<String> datas); }

    public interface OnIdResult    { void onResult(String id); }
    public interface OnBoolResult  { void onResult(boolean sucesso); }

    public interface OnHorariosOcupadosResult { void onResult(List<String> horarios); }
    public interface OnDatasOcupadasResult    { void onResult(List<String> datas); }

    public interface OnDatasInfoResult {
        void onResult(List<String> datasDisponiveis, List<String> datasOcupadas);
    }

    // =========================================================================
    // USUARIOS
    // =========================================================================

    public void cadastrarUsuario(Usuario u, OnIdResult onSuccess, OnError onError) {
        db.collection(COL_USUARIOS).document(u.getUsername()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) { onError.onError(new Exception("Username já existe")); return; }

                    db.collection(COL_USUARIOS).whereEqualTo("email", u.getEmail()).get()
                            .addOnSuccessListener(snapEmail -> {
                                if (!snapEmail.isEmpty()) { onError.onError(new Exception("E-mail já cadastrado")); return; }

                                db.collection(COL_USUARIOS).whereEqualTo("cpf", u.getCpf()).get()
                                        .addOnSuccessListener(snapCpf -> {
                                            if (!snapCpf.isEmpty()) { onError.onError(new Exception("CPF já cadastrado")); return; }

                                            db.collection(COL_USUARIOS)
                                                    .document(u.getUsername())
                                                    .set(usuarioToMap(u))
                                                    .addOnSuccessListener(v -> onSuccess.onResult(u.getUsername()))
                                                    .addOnFailureListener(onError::onError);
                                        })
                                        .addOnFailureListener(onError::onError);
                            })
                            .addOnFailureListener(onError::onError);
                })
                .addOnFailureListener(onError::onError);
    }

    public void atualizarUsuario(Usuario u, OnBoolResult callback) {
        db.collection(COL_USUARIOS)
                .document(u.getUsername())
                .update(usuarioToMap(u))
                .addOnSuccessListener(v -> callback.onResult(true))
                .addOnFailureListener(e -> {
                    Log.e("PERFIL", "Erro ao atualizar: " + e.getMessage());
                    callback.onResult(false);
                });
    }

    public void atualizarFotoPerfil(String username, String caminhoFoto, OnBoolResult callback) {
        Map<String, Object> update = new HashMap<>();
        update.put("foto_perfil", caminhoFoto);
        db.collection(COL_USUARIOS).document(username)
                .update(update)
                .addOnSuccessListener(v -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void deletarUsuario(String username, OnBoolResult callback) {
        db.collection(COL_USUARIOS).document(username)
                .delete()
                .addOnSuccessListener(v -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void login(String login, String senha, OnUsuarioResult callback) {
        db.collection(COL_USUARIOS)
                .whereEqualTo("email", login)
                .whereEqualTo("senha", senha)
                .get()
                .addOnSuccessListener(snap -> {
                    if (!snap.isEmpty()) {
                        callback.onResult(docToUsuario(snap.getDocuments().get(0)));
                        return;
                    }
                    db.collection(COL_USUARIOS).document(login).get()
                            .addOnSuccessListener(doc -> {
                                if (doc.exists()) {
                                    Usuario u = docToUsuario(doc);
                                    callback.onResult(senha.equals(u.getSenha()) ? u : null);
                                } else {
                                    callback.onResult(null);
                                }
                            })
                            .addOnFailureListener(e -> callback.onResult(null));
                })
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public void buscarUsuarioPorUsername(String username, OnUsuarioResult callback) {
        if (username == null || username.isEmpty()) {
            callback.onResult(null);
            return;
        }
        db.collection(COL_USUARIOS).document(username).get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists() ? docToUsuario(doc) : null))
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public void buscarMedicos(String filtro, OnUsuariosResult callback) {
        db.collection(COL_USUARIOS).whereEqualTo("tipo", "medico").get()
                .addOnSuccessListener(snap -> {
                    List<Usuario> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Usuario u = docToUsuario(doc);
                        if (filtro == null || filtro.isEmpty()
                                || u.getNome().toLowerCase().contains(filtro.toLowerCase())
                                || (u.getEspecialidade() != null &&
                                u.getEspecialidade().toLowerCase().contains(filtro.toLowerCase()))) {
                            lista.add(u);
                        }
                    }
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarPacientes(String filtro, OnUsuariosResult callback) {
        db.collection(COL_USUARIOS).whereEqualTo("tipo", "paciente").get()
                .addOnSuccessListener(snap -> {
                    List<Usuario> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Usuario u = docToUsuario(doc);
                        if (filtro == null || filtro.isEmpty()
                                || u.getNome().toLowerCase().contains(filtro.toLowerCase())) {
                            lista.add(u);
                        }
                    }
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Erro ao buscar pacienteas", e);
                    callback.onResult(new ArrayList<>()); //zerar o array
                });
    }

    public void buscarTodosUsuarios(OnUsuariosResult callback) {
        db.collection(COL_USUARIOS).get()
                .addOnSuccessListener(snap -> {
                    List<Usuario> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        Usuario u = docToUsuario(doc);
                        if (!"admin".equals(u.getTipo())) lista.add(u);
                    }
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // =========================================================================
    // AGENDA
    // =========================================================================

    public void buscarHorariosOcupados(String medicoId, String data,
                                       OnHorariosOcupadosResult callback) {
        db.collection(COL_AGENDA)
                .whereEqualTo("medico_id", medicoId)
                .whereEqualTo("data", data)
                .get()
                .addOnSuccessListener(snap -> {
                    List<String> ocupados = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String hora = doc.getString("hora");
                        if (hora != null) ocupados.add(hora);
                    }
                    callback.onResult(ocupados);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarDatasOcupadas(String medicoId, List<String> horariosFixos,
                                    OnDatasOcupadasResult callback) {
        db.collection(COL_AGENDA).whereEqualTo("medico_id", medicoId).get()
                .addOnSuccessListener(snap -> {
                    Map<String, Integer> contador = new HashMap<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String data = doc.getString("data");
                        if (data == null) continue;
                        contador.put(data, contador.containsKey(data) ? contador.get(data) + 1 : 1);
                    }
                    List<String> ocupadas = new ArrayList<>();
                    for (Map.Entry<String, Integer> e : contador.entrySet()) {
                        if (e.getValue() >= horariosFixos.size()) ocupadas.add(e.getKey());
                    }
                    callback.onResult(ocupadas);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    private void marcarSlotIndisponivel(String medicoId, String data, String hora) {
        Map<String, Object> map = new HashMap<>();
        map.put("medico_id", medicoId);
        map.put("data", data);
        map.put("hora", hora);
        db.collection(COL_AGENDA).add(map);
    }

    private void marcarSlotDisponivel(String medicoId, String data, String hora) {
        db.collection(COL_AGENDA)
                .whereEqualTo("medico_id", medicoId)
                .whereEqualTo("data", data)
                .whereEqualTo("hora", hora)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) doc.getReference().delete();
                });
    }

    public void buscarSlotsAgenda(String medicoId, String data, OnSlotsResult callback) {
        db.collection(COL_AGENDA)
                .whereEqualTo("medico_id", medicoId)
                .whereEqualTo("data", data)
                .orderBy("hora")
                .get()
                .addOnSuccessListener(snap -> {
                    List<String[]> slots = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        slots.add(new String[]{
                                doc.getId(),
                                doc.getString("hora"),
                                String.valueOf(Boolean.TRUE.equals(doc.getBoolean("disponivel")) ? 1 : 0)
                        });
                    }
                    callback.onResult(slots);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarDatasComSlots(String medicoId, OnDatasResult callback) {
        db.collection(COL_AGENDA).whereEqualTo("medico_id", medicoId).get()
                .addOnSuccessListener(snap -> {
                    List<String> datas = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String data = doc.getString("data");
                        if (data != null && !datas.contains(data)) datas.add(data);
                    }
                    java.util.Collections.sort(datas);
                    callback.onResult(datas);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarInfoDatasAgenda(String medicoId, OnDatasInfoResult callback) {
        db.collection(COL_AGENDA).whereEqualTo("medico_id", medicoId).get()
                .addOnSuccessListener(snap -> {
                    Map<String, List<Boolean>> porData = new HashMap<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        String data = doc.getString("data");
                        Boolean disp = doc.getBoolean("disponivel");
                        if (data == null) continue;
                        if (!porData.containsKey(data)) porData.put(data, new ArrayList<>());
                        porData.get(data).add(Boolean.TRUE.equals(disp));
                    }
                    List<String> disponiveis = new ArrayList<>();
                    List<String> ocupadas    = new ArrayList<>();
                    for (Map.Entry<String, List<Boolean>> entry : porData.entrySet()) {
                        boolean temLivre = false;
                        for (Boolean b : entry.getValue()) { if (b) { temLivre = true; break; } }
                        (temLivre ? disponiveis : ocupadas).add(entry.getKey());
                    }
                    callback.onResult(disponiveis, ocupadas);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>(), new ArrayList<>()));
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================

    public void agendarConsulta(Consulta c, OnIdResult onSuccess, OnError onError) {
        Map<String, Object> map = consultaToMap(c);
        map.put("status", "pendente");

        db.collection(COL_CONSULTAS).add(map)
                .addOnSuccessListener(ref -> {
                    marcarSlotIndisponivel(c.getMedicoId(), c.getData(), c.getHora());
                    onSuccess.onResult(ref.getId());
                })
                .addOnFailureListener(onError::onError);
    }

    public void atualizarStatusConsulta(String consultaId, String novoStatus, OnBoolResult callback) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", novoStatus);

        db.collection(COL_CONSULTAS).document(consultaId)
                .update(update)
                .addOnSuccessListener(v -> {
                    if ("cancelada".equals(novoStatus)) {
                        buscarConsultaPorId(consultaId, consulta -> {
                            if (consulta != null)
                                marcarSlotDisponivel(consulta.getMedicoId(), consulta.getData(), consulta.getHora());
                        });
                    }
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void atualizarStatusPagamentoConsulta(String consultaId, String novoStatus,
                                                 OnBoolResult callback) {
        buscarConsultaPorId(consultaId, consulta -> {
            String pagamentoStatus;
            switch (novoStatus) {
                case "confirmada":
                    pagamentoStatus = (consulta != null && consulta.getPagamentoTipo() != null)
                            ? consulta.getPagamentoTipo() : "aprovado";
                    break;
                case "cancelada":
                    pagamentoStatus = "cancelado";
                    break;
                default:
                    pagamentoStatus = "pendente";
            }
            Map<String, Object> update = new HashMap<>();
            update.put("status", novoStatus);
            update.put("pagamento", pagamentoStatus);

            db.collection(COL_CONSULTAS).document(consultaId)
                    .update(update)
                    .addOnSuccessListener(v -> {
                        if ("cancelada".equals(novoStatus) && consulta != null)
                            marcarSlotDisponivel(consulta.getMedicoId(), consulta.getData(), consulta.getHora());
                        callback.onResult(true);
                    })
                    .addOnFailureListener(e -> callback.onResult(false));
        });
    }

    public void buscarConsultaPorId(String consultaId, OnConsultaResult callback) {
        db.collection(COL_CONSULTAS).document(consultaId).get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists() ? docToConsulta(doc) : null))
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public void buscarConsultasPorPaciente(String pacienteUsername, OnConsultasResult callback) {
        queryConsultas("paciente_id", pacienteUsername, callback);
    }

    public void buscarConsultasPorMedico(String medicoUsername, OnConsultasResult callback) {
        queryConsultas("medico_id", medicoUsername, callback);
    }

    public void buscarTodasConsultas(OnConsultasResult callback) {
        db.collection(COL_CONSULTAS)
                .orderBy("data", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Consulta> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(docToConsulta(doc));
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarConsultasFuturas(String username, String tipo, OnConsultasResult callback) {
        String campo = "paciente".equals(tipo) ? "paciente_id" : "medico_id";
        db.collection(COL_CONSULTAS)
                .whereEqualTo(campo, username)
                .whereIn("status", java.util.Arrays.asList("pendente", "confirmada"))
                .get()
                .addOnSuccessListener(snap -> {
                    List<Consulta> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(docToConsulta(doc));
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    private void queryConsultas(String campo, String valor, OnConsultasResult callback) {
//        db.collection(COL_CONSULTAS)
//                .whereEqualTo(campo, valor) // criar indicie where -> data by decending
//                .orderBy("data", Query.Direction.DESCENDING)
//                .get()
//                .addOnFailureListener(e -> {
//                    Log.e("Firestore", "Erro ao buscar consultas", e);
//                    callback.onResult(new ArrayList<>());
//                });

        db.collection(COL_CONSULTAS)
                .whereEqualTo(campo, valor)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Consulta> lista = new ArrayList<>();

                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        lista.add(docToConsulta(doc));
                    }

                    if (lista.isEmpty()) {
                        callback.onResult(lista);
                        return;
                    }

                    lista.sort((c1, c2) -> c2.getData().compareTo(c1.getData()));

                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e("Consultas", "Erro ao buscar consultas", e);
                    callback.onResult(new ArrayList<>());
                });
    }

    // =========================================================================
    // PAGAMENTOS
    // =========================================================================

    /**
     * Registra (ou substitui) pagamento.
     * Usa consultaId como ID do documento para garantir unicidade.
     *
     * FIX: o campo "consulta_id" é explicitamente salvo no map para que
     * docToPagamento() possa lê-lo; o doc.getId() é o fallback secundário.
     */
    public void registrarPagamento(Pagamento p, OnIdResult onSuccess, OnError onError) {
        Map<String, Object> map = pagamentoToMap(p);   // já inclui "consulta_id"
        String docId = p.getConsultaId() != null ? p.getConsultaId() : db.collection(COL_PAGAMENTOS).document().getId();

        db.collection(COL_PAGAMENTOS).document(docId)
                .set(map)
                .addOnSuccessListener(v -> onSuccess.onResult(docId))
                .addOnFailureListener(onError::onError);
    }

    public void atualizarStatusPagamento(String consultaId, String novoStatus,
                                         String mpPaymentId, OnBoolResult callback) {
        Map<String, Object> update = new HashMap<>();
        update.put("status", novoStatus);
        if (mpPaymentId != null) update.put("mp_payment_id", mpPaymentId);

        db.collection(COL_PAGAMENTOS).document(consultaId)
                .update(update)
                .addOnSuccessListener(v -> callback.onResult(true))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    /**
     * Busca pagamento pelo ID da consulta.
     * O documento no Firestore tem o consultaId como seu próprio ID,
     * mas também armazena o campo "consulta_id" — lemos ambos para robustez.
     */
    public void buscarPagamentoPorConsulta(String consultaId, OnPagamentoResult callback) {
        if (consultaId == null || consultaId.isEmpty()) {
            callback.onResult(null);
            return;
        }
        db.collection(COL_PAGAMENTOS).document(consultaId).get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists() ? docToPagamento(doc) : null))
                .addOnFailureListener(e -> callback.onResult(null));
    }

    public void buscarTodosPagamentos(OnPagamentosResult callback) {
        db.collection(COL_PAGAMENTOS)
                .orderBy("data_hora", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Pagamento> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(docToPagamento(doc));
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // =========================================================================
    // MENSAGENS
    // =========================================================================

    public void enviarMensagem(Mensagem m, OnIdResult onSuccess, OnError onError) {
        Map<String, Object> map = new HashMap<>();
        map.put("remetente_id",    m.getRemetenteId());
        map.put("destinatario_id", m.getDestinatarioId());
        map.put("texto",           m.getTexto());
        map.put("data_hora",       m.getDataHora());
        map.put("lida",            false);

        db.collection(COL_MENSAGENS).add(map)
                .addOnSuccessListener(ref -> onSuccess.onResult(ref.getId()))
                .addOnFailureListener(onError::onError);
    }

    public void buscarConversa(String user1, String user2, OnMensagensResult callback) {
        db.collection(COL_MENSAGENS)
                .whereEqualTo("remetente_id", user1)
                .whereEqualTo("destinatario_id", user2)
                .get()
                .addOnSuccessListener(snap1 -> {
                    List<Mensagem> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap1.getDocuments()) lista.add(docToMensagem(doc));

                    db.collection(COL_MENSAGENS)
                            .whereEqualTo("remetente_id", user2)
                            .whereEqualTo("destinatario_id", user1)
                            .get()
                            .addOnSuccessListener(snap2 -> {
                                for (DocumentSnapshot doc : snap2.getDocuments()) lista.add(docToMensagem(doc));
                                lista.sort((a, b) -> a.getDataHora().compareTo(b.getDataHora()));
                                callback.onResult(lista);
                            })
                            .addOnFailureListener(e -> callback.onResult(lista));
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarContatosChat(String username, OnDatasResult callback) {
        db.collection(COL_MENSAGENS).whereEqualTo("remetente_id", username).get()
                .addOnSuccessListener(snap1 -> {
                    List<String> contatos = new ArrayList<>();
                    for (DocumentSnapshot doc : snap1.getDocuments()) {
                        String dest = doc.getString("destinatario_id");
                        if (dest != null && !contatos.contains(dest)) contatos.add(dest);
                    }
                    db.collection(COL_MENSAGENS).whereEqualTo("destinatario_id", username).get()
                            .addOnSuccessListener(snap2 -> {
                                for (DocumentSnapshot doc : snap2.getDocuments()) {
                                    String rem = doc.getString("remetente_id");
                                    if (rem != null && !contatos.contains(rem)) contatos.add(rem);
                                }
                                callback.onResult(contatos);
                            })
                            .addOnFailureListener(e -> callback.onResult(contatos));
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void buscarTodasMensagens(OnMensagensResult callback) {
        db.collection(COL_MENSAGENS)
                .orderBy("data_hora", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Mensagem> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(docToMensagem(doc));
                    callback.onResult(lista);
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    public void marcarMensagensComoLidas(String remetenteId, String destinatarioId) {
        db.collection(COL_MENSAGENS)
                .whereEqualTo("remetente_id", remetenteId)
                .whereEqualTo("destinatario_id", destinatarioId)
                .whereEqualTo("lida", false)
                .get()
                .addOnSuccessListener(snap -> {
                    for (QueryDocumentSnapshot doc : snap) doc.getReference().update("lida", true);
                });
    }

    // =========================================================================
    // HISTÓRICO MÉDICO
    // =========================================================================

    public void registrarHistorico(HistoricoMedico h, OnIdResult onSuccess, OnError onError) {
        Map<String, Object> map = new HashMap<>();
        map.put("paciente_id",  h.getPacienteId());
        map.put("medico_id",    h.getMedicoId());
        map.put("nome_medico",  h.getNomeMedico());   // campo desnormalizado para evitar join
        map.put("data",         h.getData());
        map.put("diagnostico",  h.getDiagnostico());
        map.put("observacoes",  h.getObservacoes());
        map.put("prescricao",   h.getPrescricao());

        db.collection(COL_HISTORICO).add(map)
                .addOnSuccessListener(ref -> onSuccess.onResult(ref.getId()))
                .addOnFailureListener(onError::onError);
    }

    /**
     * Busca histórico do paciente e resolve o nome do médico.
     * Estratégia: lê o campo "nome_medico" salvo no documento (desnormalizado).
     * Se ausente (documentos antigos), faz join com a coleção usuarios.
     */
    public void buscarHistoricoPorPaciente(String pacienteUsername, OnHistoricoResult callback) {
        db.collection(COL_HISTORICO)
                .whereEqualTo("paciente_id", pacienteUsername)
//                .orderBy("data", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<HistoricoMedico> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : snap.getDocuments()) lista.add(docToHistorico(doc));

                    if (lista.isEmpty()) {
                        callback.onResult(lista);
                        return;
                    }

                    lista.sort((c1, c2) -> c2.getData().compareTo(c1.getData()));

                    // Resolve nomeMedico via join para entradas antigas sem o campo desnormalizado
                    java.util.concurrent.atomic.AtomicInteger pendentes =
                            new java.util.concurrent.atomic.AtomicInteger(lista.size());

                    for (HistoricoMedico hm : lista) {
                        if (hm.getNomeMedico() != null && !hm.getNomeMedico().isEmpty()) {
                            if (pendentes.decrementAndGet() == 0) callback.onResult(lista);
                            continue;
                        }
                        buscarUsuarioPorUsername(hm.getMedicoId(), medico -> {
                            if (medico != null) hm.setNomeMedico(medico.getNome());
                            if (pendentes.decrementAndGet() == 0) callback.onResult(lista);
                        });
                    }
                })
                .addOnFailureListener(e -> callback.onResult(new ArrayList<>()));
    }

    // =========================================================================
    // VERIFICAÇÕES DE UNICIDADE
    // =========================================================================

    public void emailExiste(String email, OnBoolResult callback) {
        db.collection(COL_USUARIOS).whereEqualTo("email", email).get()
                .addOnSuccessListener(snap -> callback.onResult(!snap.isEmpty()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void cpfExiste(String cpf, OnBoolResult callback) {
        db.collection(COL_USUARIOS).whereEqualTo("cpf", cpf).get()
                .addOnSuccessListener(snap -> callback.onResult(!snap.isEmpty()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    public void usernameExiste(String username, OnBoolResult callback) {
        db.collection(COL_USUARIOS).document(username).get()
                .addOnSuccessListener(doc -> callback.onResult(doc.exists()))
                .addOnFailureListener(e -> callback.onResult(false));
    }

    // =========================================================================
    // MAPEAMENTO: Modelo → Map  /  DocumentSnapshot → Modelo
    // =========================================================================

    private Map<String, Object> usuarioToMap(Usuario u) {
        Map<String, Object> map = new HashMap<>();
        map.put("nome",          u.getNome());
        map.put("email",         u.getEmail());
        map.put("senha",         u.getSenha());
        map.put("tipo",          u.getTipo());
        map.put("cpf",           u.getCpf());
        map.put("username",      u.getUsername());
        map.put("genero",        u.getGenero());
        map.put("especialidade", u.getEspecialidade());
        map.put("descricao",     u.getDescricao());
        map.put("crm",           u.getCRM());
        map.put("foto_perfil",   u.getFotoPerfil());
        return map;
    }

    private Usuario docToUsuario(DocumentSnapshot doc) {
        Usuario u = new Usuario();
        u.setNome(doc.getString("nome"));
        u.setEmail(doc.getString("email"));
        u.setSenha(doc.getString("senha"));
        u.setTipo(doc.getString("tipo"));
        u.setCpf(doc.getString("cpf"));
        u.setUsername(doc.getId());           // username == document ID
        u.setGenero(doc.getString("genero"));
        u.setEspecialidade(doc.getString("especialidade"));
        u.setDescricao(doc.getString("descricao"));
        u.setCRM(doc.getString("crm"));
        u.setFotoPerfil(doc.getString("foto_perfil"));
        return u;
    }

    private Map<String, Object> consultaToMap(Consulta c) {
        Map<String, Object> map = new HashMap<>();
        map.put("paciente_id",  c.getPacienteId());
        map.put("medico_id",    c.getMedicoId());
        map.put("data",         c.getData());
        map.put("hora",         c.getHora());
        map.put("status",       c.getStatus());
        map.put("pagamento",    c.getPagamentoTipo());
        map.put("observacoes",  c.getObservacoes());
        return map;
    }

    private Consulta docToConsulta(DocumentSnapshot doc) {
        Consulta c = new Consulta();
        c.setId(doc.getId());
        c.setPacienteId(doc.getString("paciente_id"));
        c.setMedicoId(doc.getString("medico_id"));
        c.setData(doc.getString("data"));
        c.setHora(doc.getString("hora"));
        c.setStatus(doc.getString("status"));
        c.setPagamentoTipo(doc.getString("pagamento"));
        c.setObservacoes(doc.getString("observacoes"));
        return c;
    }

    private Map<String, Object> pagamentoToMap(Pagamento p) {
        Map<String, Object> map = new HashMap<>();
        map.put("consulta_id",       p.getConsultaId());   // campo explícito no documento
        map.put("metodo",            p.getMetodo());
        map.put("status",            p.getStatus());
        map.put("mp_payment_id",     p.getMpPaymentId());
        map.put("mp_preference_id",  p.getMpPreferenceId());
        map.put("valor",             p.getValor());
        map.put("data_hora",         p.getDataHora());
        return map;
    }

    /**
     * FIX: consultaId é lido do campo "consulta_id" no documento.
     * Se ausente (documentos antigos), usa doc.getId() como fallback,
     * pois registrarPagamento usa o consultaId como ID do documento.
     */
    private Pagamento docToPagamento(DocumentSnapshot doc) {
        Pagamento p = new Pagamento();
        String consultaId = doc.getString("consulta_id");
        p.setConsultaId(consultaId != null ? consultaId : doc.getId());
        p.setMetodo(doc.getString("metodo"));
        p.setStatus(doc.getString("status"));
        p.setMpPaymentId(doc.getString("mp_payment_id"));
        p.setMpPreferenceId(doc.getString("mp_preference_id"));
        Double valor = doc.getDouble("valor");
        p.setValor(valor != null ? valor : 0.0);
        p.setDataHora(doc.getString("data_hora"));
        return p;
    }

    private Mensagem docToMensagem(DocumentSnapshot doc) {
        Mensagem m = new Mensagem();
        m.setRemetenteId(doc.getString("remetente_id"));
        m.setDestinatarioId(doc.getString("destinatario_id"));
        m.setTexto(doc.getString("texto"));
        m.setDataHora(doc.getString("data_hora"));
        Boolean lida = doc.getBoolean("lida");
        m.setLida(Boolean.TRUE.equals(lida));
        return m;
    }

    /**
     * FIX: preenche pacienteId, medicoId e nomeMedico (campo desnormalizado).
     * nomeMedico vem do campo "nome_medico" salvo em registrarHistorico.
     */
    private HistoricoMedico docToHistorico(DocumentSnapshot doc) {
        HistoricoMedico h = new HistoricoMedico();
        h.setPacienteId(doc.getString("paciente_id"));
        h.setMedicoId(doc.getString("medico_id"));
        h.setNomeMedico(doc.getString("nome_medico"));   // pode ser null em docs antigos
        h.setData(doc.getString("data"));
        h.setDiagnostico(doc.getString("diagnostico"));
        h.setObservacoes(doc.getString("observacoes"));
        h.setPrescricao(doc.getString("prescricao"));
        return h;
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unused")
    private int intOrZero(DocumentSnapshot doc, String field) {
        Long v = doc.getLong(field);
        return v != null ? v.intValue() : 0;
    }
}