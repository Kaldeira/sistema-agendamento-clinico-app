package com.clinica.app.Utils;

import android.util.Log;

import com.clinica.app.Utils.HashHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

public class FirebaseSeeder {

    private static final String TAG = "FIREBASE_SEED";

    private final FirebaseFirestore db;

    public FirebaseSeeder() {
        db = FirebaseFirestore.getInstance();
    }

    public interface OnSeedResult {
        void onResult(boolean sucesso);
    }

    public void popularBanco(OnSeedResult callback) {
        WriteBatch batch = db.batch();

        adicionarMedicos(batch);
        adicionarPacientes(batch);
        adicionarConsultasEPagamentos(batch);
        adicionarMensagens(batch);
        adicionarAgendaOcupada(batch);

        batch.commit()
                .addOnSuccessListener(v -> {
                    Log.d(TAG, "Banco populado com sucesso!");
                    callback.onResult(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao popular banco", e);
                    callback.onResult(false);
                });
    }

    private void adicionarMedicos(WriteBatch batch) {
        addUsuarioMedico(
                batch,
                "joaopinto",
                "João Pinto",
                "joaopinto@clinica.com",
                "1234",
                "111.111.111-11",
                "Masculino",
                "Cardiologia",
                "Cardiologista especializado em prevenção e acompanhamento de doenças do coração.",
                "CRM 123456/SP",
                true
        );

        addUsuarioMedico(
                batch,
                "marianunes",
                "Mariana Nunes",
                "marianunes@clinica.com",
                "1234",
                "222.222.222-22",
                "Feminino",
                "Dermatologia",
                "Dermatologista com foco em acne, manchas e cuidados com a pele.",
                "CRM 654321/RJ",
                true
        );

        addUsuarioMedico(
                batch,
                "carlosmendes",
                "Carlos Mendes",
                "carlosmendes@clinica.com",
                "1234",
                "333.333.333-33",
                "Masculino",
                "Ortopedia",
                "Ortopedista especializado em dores musculares, coluna e articulações.",
                "CRM 456789/MG",
                true
        );

        addUsuarioMedico(
                batch,
                "anapaula",
                "Ana Paula Ribeiro",
                "anapaula@clinica.com",
                "1234",
                "444.444.444-44",
                "Feminino",
                "Pediatria",
                "Pediatra com atendimento humanizado para crianças e adolescentes.",
                "CRM 987654/DF",
                true
        );

        addUsuarioMedico(
                batch,
                "rafaellima",
                "Rafael Lima",
                "rafaellima@clinica.com",
                "1234",
                "555.555.555-55",
                "Masculino",
                "Neurologia",
                "Neurologista com experiência em cefaleia, enxaqueca e acompanhamento neurológico.",
                "CRM 741852/SP",
                true
        );

        addUsuarioMedico(
                batch,
                "beatrizsouza",
                "Beatriz Souza",
                "beatrizsouza@clinica.com",
                "1234",
                "666.666.666-66",
                "Feminino",
                "Ginecologia",
                "Ginecologista com foco em saúde da mulher e acompanhamento preventivo.",
                "CRM 369258/GO",
                false
        );
    }

    private void adicionarPacientes(WriteBatch batch) {
        addUsuarioPaciente(
                batch,
                "brunin",
                "Bruno Henrique",
                "brunin@email.com",
                "1234",
                "777.777.777-77",
                "Masculino",
                true
        );

        addUsuarioPaciente(
                batch,
                "italo",
                "Italo Kaldeira",
                "italo@email.com",
                "1234",
                "888.888.888-88",
                "Masculino",
                true
        );

        addUsuarioPaciente(
                batch,
                "larissa",
                "Larissa Alves",
                "larissa@email.com",
                "1234",
                "999.999.999-99",
                "Feminino",
                true
        );

        addUsuarioPaciente(
                batch,
                "gabriel",
                "Gabriel Rocha",
                "gabriel@email.com",
                "1234",
                "123.123.123-12",
                "Masculino",
                true
        );

        addUsuarioPaciente(
                batch,
                "camila",
                "Camila Ferreira",
                "camila@email.com",
                "1234",
                "321.321.321-32",
                "Feminino",
                true
        );
    }

    private void adicionarConsultasEPagamentos(WriteBatch batch) {
        addConsultaComPagamento(
                batch,
                "consulta_001",
                "brunin",
                "joaopinto",
                "Bruno Henrique",
                "João Pinto",
                "Cardiologia",
                "2026-06-05",
                "08:00",
                "confirmada",
                "aprovado",
                "Paciente relatou dores no peito leves.",
                "pix",
                "aprovado",
                150.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_002",
                "italo",
                "marianunes",
                "Italo Kaldeira",
                "Mariana Nunes",
                "Dermatologia",
                "2026-06-06",
                "09:30",
                "pendente",
                "pendente",
                "Consulta para avaliação de manchas na pele.",
                "cartao",
                "pendente",
                120.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_003",
                "larissa",
                "carlosmendes",
                "Larissa Alves",
                "Carlos Mendes",
                "Ortopedia",
                "2026-06-07",
                "10:00",
                "confirmada",
                "aprovado",
                "Dor no joelho após atividade física.",
                "pix",
                "aprovado",
                180.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_004",
                "gabriel",
                "anapaula",
                "Gabriel Rocha",
                "Ana Paula Ribeiro",
                "Pediatria",
                "2026-06-08",
                "14:30",
                "pendente",
                "pendente",
                "Consulta pediátrica de rotina.",
                "pix",
                "pendente",
                100.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_005",
                "camila",
                "rafaellima",
                "Camila Ferreira",
                "Rafael Lima",
                "Neurologia",
                "2026-06-09",
                "16:00",
                "cancelada",
                "cancelado",
                "Paciente cancelou antes da confirmação.",
                "cartao",
                "cancelado",
                200.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_006",
                "brunin",
                "marianunes",
                "Bruno Henrique",
                "Mariana Nunes",
                "Dermatologia",
                "2026-06-10",
                "11:00",
                "pendente",
                "pendente",
                "Retorno para avaliação de tratamento.",
                "pix",
                "pendente",
                120.00
        );

        addConsultaComPagamento(
                batch,
                "consulta_007",
                "italo",
                "joaopinto",
                "Italo Kaldeira",
                "João Pinto",
                "Cardiologia",
                "2026-06-11",
                "15:30",
                "confirmada",
                "aprovado",
                "Check-up cardiológico.",
                "dinheiro",
                "aprovado",
                150.00
        );
    }

    private void adicionarMensagens(WriteBatch batch) {
        addMensagem(batch, "msg_001", "brunin", "joaopinto", "Olá doutor, tudo bem?", "2026-06-01 08:30:00", true);
        addMensagem(batch, "msg_002", "joaopinto", "brunin", "Olá Bruno, tudo bem sim. Como posso ajudar?", "2026-06-01 08:31:00", true);
        addMensagem(batch, "msg_003", "brunin", "joaopinto", "Estou sentindo uma dor leve no peito.", "2026-06-01 08:32:00", false);

        addMensagem(batch, "msg_004", "italo", "marianunes", "Doutora, queria tirar uma dúvida sobre minha consulta.", "2026-06-01 10:10:00", true);
        addMensagem(batch, "msg_005", "marianunes", "italo", "Claro, pode falar.", "2026-06-01 10:12:00", false);

        addMensagem(batch, "msg_006", "larissa", "carlosmendes", "Minha dor no joelho piorou depois do treino.", "2026-06-02 15:00:00", false);
        addMensagem(batch, "msg_007", "carlosmendes", "larissa", "Evite esforço até a consulta e observe se há inchaço.", "2026-06-02 15:05:00", false);

        addMensagem(batch, "msg_008", "gabriel", "anapaula", "Boa tarde, minha consulta está confirmada?", "2026-06-03 13:00:00", false);
        addMensagem(batch, "msg_009", "anapaula", "gabriel", "Ainda está pendente de pagamento.", "2026-06-03 13:02:00", false);
    }

    private void adicionarAgendaOcupada(WriteBatch batch) {
        addAgenda(batch, "joaopinto", "2026-06-05", "08:00");
        addAgenda(batch, "marianunes", "2026-06-06", "09:30");
        addAgenda(batch, "carlosmendes", "2026-06-07", "10:00");
        addAgenda(batch, "anapaula", "2026-06-08", "14:30");
        addAgenda(batch, "rafaellima", "2026-06-09", "16:00");
        addAgenda(batch, "marianunes", "2026-06-10", "11:00");
        addAgenda(batch, "joaopinto", "2026-06-11", "15:30");
    }

    private void addUsuarioMedico(
            WriteBatch batch,
            String username,
            String nome,
            String email,
            String senha,
            String cpf,
            String genero,
            String especialidade,
            String descricao,
            String crm,
            boolean aprovado
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("nome", nome);
        map.put("email", email);
        map.put("senha", HashHelper.gerarHash(senha));
        map.put("tipo", "medico");
        map.put("cpf", cpf);
        map.put("username", username);
        map.put("genero", genero);
        map.put("especialidade", especialidade);
        map.put("descricao", descricao);
        map.put("crm", crm);
        map.put("foto_perfil", "");
        map.put("aprovado", aprovado);

        batch.set(db.collection("usuarios").document(username), map);
    }

    private void addUsuarioPaciente(
            WriteBatch batch,
            String username,
            String nome,
            String email,
            String senha,
            String cpf,
            String genero,
            boolean aprovado
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("nome", nome);
        map.put("email", email);
        map.put("senha", HashHelper.gerarHash(senha));
        map.put("tipo", "paciente");
        map.put("cpf", cpf);
        map.put("username", username);
        map.put("genero", genero);
        map.put("especialidade", "");
        map.put("descricao", "");
        map.put("crm", "");
        map.put("foto_perfil", "");
        map.put("aprovado", aprovado);

        batch.set(db.collection("usuarios").document(username), map);
    }

    private void addConsultaComPagamento(
            WriteBatch batch,
            String consultaId,
            String pacienteId,
            String medicoId,
            String pacienteNome,
            String medicoNome,
            String especialidadeMedico,
            String data,
            String hora,
            String statusConsulta,
            String statusPagamentoConsulta,
            String observacoes,
            String metodoPagamento,
            String statusPagamento,
            double valor
    ) {
        Map<String, Object> consulta = new HashMap<>();
        consulta.put("paciente_id", pacienteId);
        consulta.put("medico_id", medicoId);
        consulta.put("paciente_nome", pacienteNome);
        consulta.put("medico_nome", medicoNome);
        consulta.put("especialidade_medico", especialidadeMedico);
        consulta.put("data", data);
        consulta.put("hora", hora);
        consulta.put("status", statusConsulta);
        consulta.put("pagamento", statusPagamentoConsulta);
        consulta.put("observacoes", observacoes);

        batch.set(db.collection("consultas").document(consultaId), consulta);

        Map<String, Object> pagamento = new HashMap<>();
        pagamento.put("consulta_id", consultaId);
        pagamento.put("metodo", metodoPagamento);
        pagamento.put("status", statusPagamento);
        pagamento.put("mp_payment_id", "");
        pagamento.put("mp_preference_id", "");
        pagamento.put("valor", valor);
        pagamento.put("data_hora", data + " " + hora);

        batch.set(db.collection("pagamentos").document(consultaId), pagamento);
    }

    private void addMensagem(
            WriteBatch batch,
            String mensagemId,
            String remetenteId,
            String destinatarioId,
            String texto,
            String dataHora,
            boolean lida
    ) {
        Map<String, Object> map = new HashMap<>();
        map.put("remetente_id", remetenteId);
        map.put("destinatario_id", destinatarioId);
        map.put("texto", texto);
        map.put("data_hora", dataHora);
        map.put("lida", lida);

        batch.set(db.collection("mensagens").document(mensagemId), map);
    }

    private void addAgenda(
            WriteBatch batch,
            String medicoId,
            String data,
            String hora
    ) {
        String docId = medicoId + "_" + data + "_" + hora.replace(":", "-");

        Map<String, Object> map = new HashMap<>();
        map.put("medico_id", medicoId);
        map.put("data", data);
        map.put("hora", hora);

        batch.set(db.collection("agenda").document(docId), map);
    }
}