package com.clinica.app.Controle;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.clinica.app.Modelo.Consulta;
import com.clinica.app.Modelo.HistoricoMedico;
import com.clinica.app.Modelo.Mensagem;
import com.clinica.app.Modelo.Pagamento;
import com.clinica.app.Modelo.Usuario;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class BancoDados extends SQLiteOpenHelper {

    private static final String BANCO_NOME = "clinica.db";
    private static final int    BANCO_VERSAO = 2;

    private static BancoDados instance;

    public static synchronized BancoDados getInstance(Context ctx) {
        if (instance == null)
            instance = new BancoDados(ctx.getApplicationContext());
        return instance;
    }

    private BancoDados(Context context) {
        super(context, BANCO_NOME, null, BANCO_VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nome TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "senha TEXT NOT NULL, " +
                "tipo TEXT NOT NULL, " +
                "cpf TEXT NOT NULL UNIQUE, " +
                "username TEXT NOT NULL UNIQUE, " +
                "genero TEXT, " +
                "especialidade TEXT, " +
                "descricao TEXT, " +
                "crm TEXT, " +
                "foto_perfil TEXT)");

        db.execSQL("CREATE TABLE consultas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "paciente_id INTEGER NOT NULL, " +
                "medico_id INTEGER NOT NULL, " +
                "data TEXT NOT NULL, " +
                "hora TEXT NOT NULL, " +
                "status TEXT NOT NULL DEFAULT 'pendente', " +
                "pagamento TEXT, " +
                "observacoes TEXT, " +
                "FOREIGN KEY(paciente_id) REFERENCES usuarios(id), " +
                "FOREIGN KEY(medico_id) REFERENCES usuarios(id))");

        db.execSQL("CREATE TABLE mensagens (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "remetente_id INTEGER NOT NULL, " +
                "destinatario_id INTEGER NOT NULL, " +
                "texto TEXT NOT NULL, " +
                "data_hora TEXT NOT NULL, " +
                "lida INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE historico_medico (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "paciente_id INTEGER NOT NULL, " +
                "medico_id INTEGER NOT NULL, " +
                "data TEXT NOT NULL, " +
                "diagnostico TEXT, " +
                "observacoes TEXT, " +
                "prescricao TEXT)");

        db.execSQL("CREATE TABLE agenda (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "medico_id INTEGER NOT NULL, " +
                "data TEXT NOT NULL, " +
                "hora TEXT NOT NULL, " +
                "disponivel INTEGER DEFAULT 1)");


        db.execSQL("CREATE TABLE pagamentos (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "consulta_id INTEGER NOT NULL UNIQUE, " +
                "metodo TEXT NOT NULL, " +           // pix | cartao | dinheiro
                "status TEXT NOT NULL DEFAULT 'pendente', " + // pendente|aprovado|recusado
                "mp_payment_id TEXT, " +             // Mercado Pago payment ID
                "mp_preference_id TEXT, " +          // MP preference ID
                "valor REAL NOT NULL, " +
                "data_hora TEXT NOT NULL, " +
                "FOREIGN KEY(consulta_id) REFERENCES consultas(id))");

        inserirDadosTeste(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE usuarios ADD COLUMN foto_perfil TEXT"); }
            catch (Exception ignored) {}

            db.execSQL("CREATE TABLE IF NOT EXISTS pagamentos (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "consulta_id INTEGER NOT NULL UNIQUE, " +
                    "metodo TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'pendente', " +
                    "mp_payment_id TEXT, " +
                    "mp_preference_id TEXT, " +
                    "valor REAL NOT NULL, " +
                    "data_hora TEXT NOT NULL, " +
                    "FOREIGN KEY(consulta_id) REFERENCES consultas(id))");
        }
    }

    private void inserirDadosTeste(SQLiteDatabase db) {
        inserirUsuarioTeste(db, "Carlos Souza", "carlos@clinica.com", "123456",
                "medico", "111.222.333-44", "Cardiologia",
                "Cardiologista com 15 anos de experiência especializado em doenças coronárias.", null, "masculino", "2024201", "carlinhos");

        inserirUsuarioTeste(db, "Ana Lima", "ana@clinica.com", "123456",
                "medico", "222.333.444-55", "Pediatria",
                "Pediatra especializada em neonatologia e desenvolvimento infantil.", null, "feminino", "2024201", "aninha");

        inserirUsuarioTeste(db, "Roberto Alves", "roberto@clinica.com", "123456",
                "medico", "333.444.555-66", "Ortopedia",
                "Ortopedista focado em medicina esportiva e reabilitação.", null, "masculino", "2024201", "robertinho");

        inserirUsuarioTeste(db, "Fernanda Costa", "fernanda@clinica.com", "123456",
                "medico", "444.555.666-77", "Dermatologia",
                "Dermatologista com ênfase em tratamentos estéticos e doenças cutâneas.", null, "feminino", "2024201", "fernandinha");

        inserirUsuarioTeste(db, "Douglas Nemes", "douglas@email.com", "123",
                "paciente", "111.222.444-66", "", "", null, "masculino", null, "douglinhas");

        inserirUsuarioTeste(db, "Gustavo Pereira", "gustavo@email.com", "123",
                "paciente", "000.222.444-66", "", "", null, "masculino", null, "gustavim");

        inserirUsuarioTeste(db, "Diana Souza", "diana@email.com", "123",
                "paciente", "111.002.444-66", "", "", null, "feminino", null, "diana");

        inserirUsuarioTeste(db, "Admin", "admin", "admin",
                "admin", "000.000.000-00", "", "", null, "masculino", null, "admin");

        String[] horarios = {"08:00", "09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
        String[] datas = {"2026-05-05", "2026-05-06", "2026-05-07", "2026-05-08", "2026-05-09",
                "2026-05-12", "2026-05-13", "2026-05-14"};
        for (int medId = 1; medId <= 4; medId++) {
            for (String data : datas) {
                for (String hora : horarios) {
                    ContentValues cv = new ContentValues();
                    cv.put("medico_id", medId);
                    cv.put("data", data);
                    cv.put("hora", hora);
                    cv.put("disponivel", 1);
                    db.insert("agenda", null, cv);
                }
            }
        }


        db.execSQL("INSERT INTO consultas (paciente_id, medico_id, data, hora, status, pagamento, observacoes) VALUES " +
                "(5, 1, '2026-05-05', '08:00', 'confirmada', 'pago', 'Dor no peito frequente')," +
                "(6, 2, '2026-05-06', '09:00', 'pendente', 'pendente', 'Consulta pediátrica de rotina')," +
                "(7, 3, '2026-05-07', '10:00', 'confirmada', 'pago', 'Lesão no joelho')," +
                "(5, 4, '2026-05-08', '11:00', 'cancelada', 'reembolsado', 'Consulta dermatológica')," +
                "(6, 1, '2026-05-09', '14:00', 'confirmada', 'pago', 'Check-up cardiológico');");


        db.execSQL("INSERT INTO pagamentos (consulta_id, metodo, status, mp_payment_id, mp_preference_id, valor, data_hora) VALUES " +
                "(1, 'pix', 'aprovado', 'MP123', 'PREF123', 200.0, '2026-05-05 07:50')," +
                "(2, 'cartao', 'pendente', 'MP124', 'PREF124', 150.0, '2026-05-06 08:50')," +
                "(3, 'pix', 'aprovado', 'MP125', 'PREF125', 180.0, '2026-05-07 09:50')," +
                "(4, 'cartao', 'recusado', 'MP126', 'PREF126', 120.0, '2026-05-08 10:50')," +
                "(5, 'dinheiro', 'aprovado', NULL, NULL, 220.0, '2026-05-09 13:50');");


        db.execSQL("INSERT INTO historico_medico (paciente_id, medico_id, data, diagnostico, observacoes, prescricao) VALUES " +
                "(5, 1, '2026-05-05', 'Angina leve', 'Paciente relatou dor ao esforço', 'Uso de beta-bloqueadores')," +
                "(7, 3, '2026-05-07', 'Entorse no joelho', 'Inflamação leve', 'Fisioterapia + anti-inflamatório')," +
                "(6, 1, '2026-05-09', 'Saudável', 'Check-up normal', 'Manter hábitos saudáveis');");


        db.execSQL("INSERT INTO mensagens (remetente_id, destinatario_id, texto, data_hora, lida) VALUES " +
                "(5, 1, 'Doutor, estou com dor no peito.', '2026-05-04 20:00', 1)," +
                "(1, 5, 'Vamos investigar na consulta.', '2026-05-04 20:10', 1)," +
                "(6, 2, 'Minha filha precisa de consulta.', '2026-05-05 18:00', 0)," +
                "(2, 6, 'Pode agendar para amanhã.', '2026-05-05 18:10', 0);");

        db.execSQL("UPDATE agenda SET disponivel = 0 WHERE medico_id = 1 AND data = '2026-05-05' AND hora = '08:00'");
        db.execSQL("UPDATE agenda SET disponivel = 0 WHERE medico_id = 2 AND data = '2026-05-06' AND hora = '09:00'");
        db.execSQL("UPDATE agenda SET disponivel = 0 WHERE medico_id = 3 AND data = '2026-05-07' AND hora = '10:00'");
        db.execSQL("UPDATE agenda SET disponivel = 0 WHERE medico_id = 4 AND data = '2026-05-08' AND hora = '11:00'");
        db.execSQL("UPDATE agenda SET disponivel = 0 WHERE medico_id = 1 AND data = '2026-05-09' AND hora = '14:00'");
    }

    private void inserirUsuarioTeste(SQLiteDatabase db, String nome, String email,
                                     String senha, String tipo, String cpf,
                                     String esp, String desc, String foto, String genero, String CRM, String username) {
        ContentValues cv = new ContentValues();
        cv.put("nome", nome);
        cv.put("email", email);
        cv.put("senha", senha);
        cv.put("tipo", tipo);
        cv.put("cpf", cpf);
        cv.put("especialidade", esp);
        cv.put("descricao", desc);
        cv.put("foto_perfil", foto);
        cv.put("genero", genero);
        cv.put("crm", CRM);
        cv.put("username", username);
        db.insert("usuarios", null, cv);
    }

    public List<String> gerarHorarios(int inicio, int fim) {
        List<String> lista = new ArrayList<>();

        for (int h = inicio; h < fim; h++) {
            lista.add(String.format("%02d:00", h));
        }

        return lista;
    }

    public List<String> buscarHorasOcupadas(int medicoId, String data) {

        List<String> ocupadas = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();

        Cursor c = db.rawQuery(
                "SELECT hora FROM agenda WHERE medico_id=? AND data=? AND disponivel=0",
                new String[]{String.valueOf(medicoId), data}
        );

        while (c.moveToNext()) {
            ocupadas.add(c.getString(0));
        }

        c.close();
        return ocupadas;
    }

    public List<String[]> montarSlots(int medicoId, String data) {

        List<String> todos = gerarHorarios(8, 18);
        List<String> ocupados = buscarHorasOcupadas(medicoId, data);

        List<String[]> slots = new ArrayList<>();

        for (String hora : todos) {

            boolean disponivel = !ocupados.contains(hora);

            slots.add(new String[]{
                    "0",
                    hora,
                    disponivel ? "1" : "0"
            });
        }

        return slots;
    }

    public List<String> gerarDatas(int dias) {

        List<String> lista = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        for (int i = 0; i < dias; i++) {
            lista.add(sdf.format(cal.getTime()));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return lista;
    }

//    private String gerarDataHojeMaisDias(int dias) {
//        java.time.LocalDate date = java.time.LocalDate.now().plusDays(dias)
//        return date.toString(); // YYYY-MM-DD
//    }
//    public void gerarAgendaSemanal(int medicoId) {
//
//        String[] horarios = {
//                "08:00", "09:00", "10:00", "11:00",
//                "14:00", "15:00", "16:00"
//        };
//
//        SQLiteDatabase db = getWritableDatabase();
//
//        for (int i = 0; i < 7; i++) {
//
//            String data = gerarDataHojeMaisDias(i); // YYYY-MM-DD
//
//            for (String hora : horarios) {
//
//                ContentValues values = new ContentValues();
//                values.put("medico_id", medicoId);
//                values.put("data", data);
//                values.put("hora", hora);
//                values.put("disponivel", 1);
//
//                db.insert("agenda", null, values);
//            }
//        }
//    }

    public long cadastrarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = buildUsuarioCV(u);
        return db.insert("usuarios", null, cv);
    }

    public boolean atualizarUsuario(Usuario u) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = buildUsuarioCV(u);
        int rows = db.update("usuarios", cv, "id=?",
                new String[]{String.valueOf(u.getId())});
        return rows > 0;
    }

    public boolean atualizarFotoPerfil(int userId, String caminhoFoto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("foto_perfil", caminhoFoto);
        return db.update("usuarios", cv, "id=?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    public boolean deletarUsuario(int userId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("usuarios", "id=?",
                new String[]{String.valueOf(userId)}) > 0;
    }

    private ContentValues buildUsuarioCV(Usuario u) {
        ContentValues cv = new ContentValues();
        cv.put("nome", u.getNome());
        cv.put("email", u.getEmail());
        cv.put("senha", u.getSenha());
        cv.put("tipo", u.getTipo());
        cv.put("cpf", u.getCpf());
        cv.put("especialidade", u.getEspecialidade());
        cv.put("descricao", u.getDescricao());
        cv.put("foto_perfil", u.getFotoPerfil());
        cv.put("crm", u.getCRM());
        cv.put("genero", u.getGenero());
        cv.put("username", u.getUsername());
        return cv;
    }

    public Usuario login(String login, String senha) {

        SQLiteDatabase db = getReadableDatabase();

        String sql =
                "SELECT * FROM usuarios " +
                        "WHERE (LOWER(email) = LOWER(?) OR LOWER(username) = LOWER(?)) " +
                        "AND senha = ?";

        Cursor c = db.rawQuery(sql, new String[]{login, login, senha});

        Usuario u = null;
        if (c.moveToFirst()) {
            u = cursorToUsuario(c);
        }

        c.close();
        return u;
    }

    public boolean emailExiste(String email) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("usuarios", new String[]{"id"},
                "email=?", new String[]{email}, null, null, null);
        boolean e = c.getCount() > 0;
        c.close();
        return e;
    }

    public boolean cpfExiste(String cpf) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("usuarios", new String[]{"id"},
                "cpf=?", new String[]{cpf}, null, null, null);
        boolean e = c.getCount() > 0;
        c.close();
        return e;
    }

    public boolean usernameExiste(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("usuarios", new String[]{"id"},
                "username=?", new String[]{username}, null, null, null);
        boolean e = c.getCount() > 0;
        c.close();
        return e;
    }

    public Usuario buscarUsuarioPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("usuarios", null,
                "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Usuario u = null;
        if (c.moveToFirst()) u = cursorToUsuario(c);
        c.close();
        return u;
    }

    public List<Usuario> buscarMedicos(String filtro) {
        SQLiteDatabase db = getReadableDatabase();
        List<Usuario> lista = new ArrayList<>();
        String where = "tipo='medico'";
        String[] args = null;
        if (filtro != null && !filtro.isEmpty()) {
            where += " AND (nome LIKE ? OR especialidade LIKE ?)";
            args = new String[]{"%" + filtro + "%", "%" + filtro + "%"};
        }
        Cursor c = db.query("usuarios", null, where, args, null, null, "nome");
        while (c.moveToNext()) lista.add(cursorToUsuario(c));
        c.close();
        return lista;
    }

//    public List<Usuario> buscarPagamentoPorTipo(String filtro) {
//        SQLiteDatabase db = getReadableDatabase();
//        List<Pagamento> lista = new ArrayList<>();
//        String where = "";
//        String[] args = null;
//        if (filtro != null && !filtro.isEmpty()) {
//            where += "metodo LIKE";
//            args = new String[]{"%" + filtro + "%"};
//        }
//        Cursor c = db.query("pagamentos", null, where, args, null, null, "nome");
//        while (c.moveToNext()) lista.add(cursorToUsuario(c));
//        c.close();
//        return lista;
//    }

    public List<Usuario> buscarPacientes(String filtro) {
        SQLiteDatabase db = getReadableDatabase();
        List<Usuario> lista = new ArrayList<>();

        String where = "tipo='paciente'";
        String[] args = null;

        if (filtro != null && !filtro.isEmpty()) {
            where += " AND nome LIKE ?";
            args = new String[]{"%" + filtro + "%"};
        }

        Cursor c = db.query("usuarios", null, where, args, null, null, "nome");

        while (c.moveToNext()) {
            lista.add(cursorToUsuario(c));
        }

        c.close();

        return lista;
    }

    public List<Usuario> buscarPacientes() {
        SQLiteDatabase db = getReadableDatabase();
        List<Usuario> lista = new ArrayList<>();
        Cursor c = db.query("usuarios", null, "tipo='paciente'",
                null, null, null, "nome");
        while (c.moveToNext()) lista.add(cursorToUsuario(c));
        c.close();
        return lista;
    }

    public List<Usuario> buscarTodosUsuarios() {
        SQLiteDatabase db = getReadableDatabase();
        List<Usuario> lista = new ArrayList<>();
        Cursor c = db.query("usuarios", null, "tipo != 'admin'",
                null, null, null, "tipo, nome");
        while (c.moveToNext()) lista.add(cursorToUsuario(c));
        c.close();
        return lista;
    }

    private Usuario cursorToUsuario(Cursor c) {
        Usuario u = new Usuario();
        u.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        u.setNome(c.getString(c.getColumnIndexOrThrow("nome")));
        u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        u.setSenha(c.getString(c.getColumnIndexOrThrow("senha")));
        u.setTipo(c.getString(c.getColumnIndexOrThrow("tipo")));
        u.setCpf(c.getString(c.getColumnIndexOrThrow("cpf")));
        u.setEspecialidade(c.getString(c.getColumnIndexOrThrow("especialidade")));
        u.setDescricao(c.getString(c.getColumnIndexOrThrow("descricao")));
        u.setCRM(c.getString(c.getColumnIndexOrThrow("crm")));
        u.setGenero(c.getString(c.getColumnIndexOrThrow("genero")));
        u.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
        int fotoIdx = c.getColumnIndex("foto_perfil");
        if (fotoIdx >= 0) u.setFotoPerfil(c.getString(fotoIdx));
        return u;
    }

    public List<String[]> buscarSlotsAgenda(int medicoId, String data) {
        SQLiteDatabase db = getReadableDatabase();
        List<String[]> slots = new ArrayList<>();
        Cursor c = db.query("agenda", null,
                "medico_id=? AND data=?",
                new String[]{String.valueOf(medicoId), data}, null, null, "hora");
        while (c.moveToNext()) {
            slots.add(new String[]{
                    c.getString(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("hora")),
                    c.getString(c.getColumnIndexOrThrow("disponivel"))
            });
        }
        c.close();
        return slots;
    }

    public List<String> buscarDatasComSlots(int medicoId) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> datas = new ArrayList<>();
        Cursor c = db.query(true, "agenda", new String[]{"data"},
                "medico_id=?", new String[]{String.valueOf(medicoId)},
                "data", null, "data", null);
        while (c.moveToNext()) datas.add(c.getString(0));
        c.close();
        return datas;
    }

    private void marcarSlotIndisponivel(SQLiteDatabase db, int medicoId, String data, String hora) {
        ContentValues cv = new ContentValues();
        cv.put("disponivel", 0);
        db.update("agenda", cv, "medico_id=? AND data=? AND hora=?",
                new String[]{String.valueOf(medicoId), data, hora});
    }

    private void marcarSlotDisponivel(SQLiteDatabase db, int medicoId, String data, String hora) {
        ContentValues cv = new ContentValues();
        cv.put("disponivel", 1);
        db.update("agenda", cv, "medico_id=? AND data=? AND hora=?",
                new String[]{String.valueOf(medicoId), data, hora});
    }

    public long agendarConsulta(Consulta c) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("paciente_id", c.getPacienteId());
        cv.put("medico_id", c.getMedicoId());
        cv.put("data", c.getData());
        cv.put("hora", c.getHora());
        cv.put("status", "pendente");
        cv.put("pagamento", c.getPagamentoTipo());
        cv.put("observacoes", c.getObservacoes());
        long id = db.insert("consultas", null, cv);
       // if (id > 0) marcarSlotIndisponivel(db, c.getMedicoId(), c.getData(), c.getHora());
        return id;
    }

    public boolean atualizarStatusConsulta(int consultaId, String novoStatus) {
        SQLiteDatabase db = getWritableDatabase();
        Consulta c = buscarConsultaPorId(consultaId);
        ContentValues cv = new ContentValues();
        cv.put("status", novoStatus);
        int rows = db.update("consultas", cv, "id=?", new String[]{String.valueOf(consultaId)});
        //if (rows > 0 && c != null && "cancelada".equals(novoStatus))
            //marcarSlotDisponivel(db, c.getMedicoId(), c.getData(), c.getHora());
        return rows > 0;
    }

    public boolean atualizarStatusPagamentoConsulta(int consultaId, String novoStatus) {
        SQLiteDatabase db = getWritableDatabase();
        Consulta c = buscarConsultaPorId(consultaId);

        ContentValues cv = new ContentValues();
        cv.put("status", novoStatus);

        // Define pagamento automaticamente
        String pagamentoStatus = null;

        switch (novoStatus) {
            case "confirmada":
                // se já tiver pagamento definido, mantém (ex: dinheiro)
                if (c != null && c.getPagamentoTipo() != null) {
                    pagamentoStatus = c.getPagamentoTipo();
                } else {
                    pagamentoStatus = "aprovado";
                }
                break;

            case "pendente":
                pagamentoStatus = "pendente";
                break;

            case "cancelada":
                pagamentoStatus = "cancelado";
                break;
        }

        cv.put("pagamento", pagamentoStatus);

        int rows = db.update("consultas", cv, "id=?",
                new String[]{String.valueOf(consultaId)});

        // libera slot se cancelou
        if (rows > 0 && c != null && "cancelada".equals(novoStatus)) {
          //  marcarSlotDisponivel(db, c.getMedicoId(), c.getData(), c.getHora());
        }

        return rows > 0;
    }


    public Consulta buscarConsultaPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("consultas", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Consulta consulta = null;
        if (c.moveToFirst()) consulta = cursorToConsulta(c);
        c.close();
        return consulta;
    }

    public List<Consulta> buscarConsultasPorPaciente(int pacienteId) {
        return queryConsultas("paciente_id=?", new String[]{String.valueOf(pacienteId)});
    }

    public List<Consulta> buscarConsultasPorMedico(int medicoId) {
        return queryConsultas("medico_id=?", new String[]{String.valueOf(medicoId)});
    }

    public List<Consulta> buscarTodasConsultas() {
        return queryConsultas(null, null);
    }

    public List<Consulta> buscarConsultasFuturas(int usuarioId, String tipo) {
        String col = "paciente".equals(tipo) ? "paciente_id" : "medico_id";
        return queryConsultas(col + "=? AND status IN ('pendente','confirmada')",
                new String[]{String.valueOf(usuarioId)});
    }

    private List<Consulta> queryConsultas(String where, String[] args) {
        SQLiteDatabase db = getReadableDatabase();
        List<Consulta> lista = new ArrayList<>();
        Cursor c = db.query("consultas", null, where, args,
                null, null, "data DESC, hora DESC");
        while (c.moveToNext()) lista.add(cursorToConsulta(c));
        c.close();
        return lista;
    }

    private Consulta cursorToConsulta(Cursor c) {
        Consulta consulta = new Consulta();
        //consulta.setId(c.getInt(c.getColumnIndexOrThrow("id")));
       // consulta.setPacienteId(c.getInt(c.getColumnIndexOrThrow("paciente_id")));
        //consulta.setMedicoId(c.getInt(c.getColumnIndexOrThrow("medico_id")));
        consulta.setData(c.getString(c.getColumnIndexOrThrow("data")));
        consulta.setHora(c.getString(c.getColumnIndexOrThrow("hora")));
        consulta.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
        consulta.setPagamentoTipo(c.getString(c.getColumnIndexOrThrow("pagamento")));
        consulta.setObservacoes(c.getString(c.getColumnIndexOrThrow("observacoes")));
        return consulta;
    }

    public long registrarPagamento(Pagamento p) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("consulta_id", p.getConsultaId());
        cv.put("metodo", p.getMetodo());
        cv.put("status", p.getStatus());
        cv.put("mp_payment_id", p.getMpPaymentId());
        cv.put("mp_preference_id", p.getMpPreferenceId());
        cv.put("valor", p.getValor());
        cv.put("data_hora", p.getDataHora());
        // upsert: replace if consulta_id already exists
        return db.insertWithOnConflict("pagamentos", null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    public boolean atualizarStatusPagamento(int consultaId, String novoStatus,
                                            String mpPaymentId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("status", novoStatus);
        if (mpPaymentId != null) cv.put("mp_payment_id", mpPaymentId);
        return db.update("pagamentos", cv, "consulta_id=?",
                new String[]{String.valueOf(consultaId)}) > 0;
    }

    public Pagamento buscarPagamentoPorConsulta(int consultaId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("pagamentos", null, "consulta_id=?",
                new String[]{String.valueOf(consultaId)}, null, null, null);
        Pagamento p = null;
        if (c.moveToFirst()) p = cursorToPagamento(c);
        c.close();
        return p;
    }

    public List<Pagamento> buscarTodosPagamentos() {
        SQLiteDatabase db = getReadableDatabase();
        List<Pagamento> lista = new ArrayList<>();
        Cursor c = db.query("pagamentos", null, null, null,
                null, null, "data_hora DESC");
        while (c.moveToNext()) lista.add(cursorToPagamento(c));
        c.close();
        return lista;
    }

    private Pagamento cursorToPagamento(Cursor c) {
        Pagamento p = new Pagamento();
        p.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        //p.setConsultaId(c.getInt(c.getColumnIndexOrThrow("consulta_id")));
        p.setMetodo(c.getString(c.getColumnIndexOrThrow("metodo")));
        p.setStatus(c.getString(c.getColumnIndexOrThrow("status")));
        p.setMpPaymentId(c.getString(c.getColumnIndexOrThrow("mp_payment_id")));
        p.setMpPreferenceId(c.getString(c.getColumnIndexOrThrow("mp_preference_id")));
        p.setValor(c.getDouble(c.getColumnIndexOrThrow("valor")));
        p.setDataHora(c.getString(c.getColumnIndexOrThrow("data_hora")));
        return p;
    }

    public long enviarMensagem(Mensagem m) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("remetente_id", m.getRemetenteId());
        cv.put("destinatario_id", m.getDestinatarioId());
        cv.put("texto", m.getTexto());
        cv.put("data_hora", m.getDataHora());
        cv.put("lida", 0);
        return db.insert("mensagens", null, cv);
    }

    public List<Mensagem> buscarConversa(int user1Id, int user2Id) {
        SQLiteDatabase db = getReadableDatabase();
        List<Mensagem> lista = new ArrayList<>();
        String where = "(remetente_id=? AND destinatario_id=?) OR " +
                "(remetente_id=? AND destinatario_id=?)";
        String[] args = {String.valueOf(user1Id), String.valueOf(user2Id),
                String.valueOf(user2Id), String.valueOf(user1Id)};
        Cursor c = db.query("mensagens", null, where, args, null, null, "data_hora");
        while (c.moveToNext()) lista.add(cursorToMensagem(c));
        c.close();
        return lista;
    }

    public List<Integer> buscarContatosChat(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT CASE WHEN remetente_id=? THEN " +
                "destinatario_id ELSE remetente_id END AS contato_id " +
                "FROM mensagens WHERE remetente_id=? OR destinatario_id=?";
        Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(userId), String.valueOf(userId), String.valueOf(userId)});
        while (c.moveToNext()) ids.add(c.getInt(0));
        c.close();
        return ids;
    }

    public List<Mensagem> buscarTodasMensagens() {
        SQLiteDatabase db = getReadableDatabase();
        List<Mensagem> lista = new ArrayList<>();
        Cursor c = db.query("mensagens", null, null, null,
                null, null, "data_hora DESC");
        while (c.moveToNext()) lista.add(cursorToMensagem(c));
        c.close();
        return lista;
    }

    public void marcarMensagensComoLidas(int remetenteId, int destinatarioId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("lida", 1);
        db.update("mensagens", cv, "remetente_id=? AND destinatario_id=?",
                new String[]{String.valueOf(remetenteId), String.valueOf(destinatarioId)});
    }

    private Mensagem cursorToMensagem(Cursor c) {
        Mensagem m = new Mensagem();
        m.setId(c.getInt(c.getColumnIndexOrThrow("id")));
//        m.setRemetenteId(c.getInt(c.getColumnIndexOrThrow("remetente_id")));
//        m.setDestinatarioId(c.getInt(c.getColumnIndexOrThrow("destinatario_id")));
        m.setTexto(c.getString(c.getColumnIndexOrThrow("texto")));
        m.setDataHora(c.getString(c.getColumnIndexOrThrow("data_hora")));
        m.setLida(c.getInt(c.getColumnIndexOrThrow("lida")) == 1);
        return m;
    }

    public long registrarHistorico(HistoricoMedico h) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("paciente_id", h.getPacienteId());
        cv.put("medico_id", h.getMedicoId());
        cv.put("data", h.getData());
        cv.put("diagnostico", h.getDiagnostico());
        cv.put("observacoes", h.getObservacoes());
        cv.put("prescricao", h.getPrescricao());
        return db.insert("historico_medico", null, cv);
    }

    public List<HistoricoMedico> buscarHistoricoPorPaciente(int pacienteId) {
        SQLiteDatabase db = getReadableDatabase();
        List<HistoricoMedico> lista = new ArrayList<>();
        Cursor c = db.query("historico_medico", null, "paciente_id=?",
                new String[]{String.valueOf(pacienteId)}, null, null, "data DESC");
        while (c.moveToNext()) lista.add(cursorToHistorico(c));
        c.close();
        return lista;
    }

    private HistoricoMedico cursorToHistorico(Cursor c) {
        HistoricoMedico h = new HistoricoMedico();
        h.setId(c.getInt(c.getColumnIndexOrThrow("id")));
//        h.setPacienteId(c.getInt(c.getColumnIndexOrThrow("paciente_id")));
//        h.setMedicoId(c.getInt(c.getColumnIndexOrThrow("medico_id")));
        h.setData(c.getString(c.getColumnIndexOrThrow("data")));
        h.setDiagnostico(c.getString(c.getColumnIndexOrThrow("diagnostico")));
        h.setObservacoes(c.getString(c.getColumnIndexOrThrow("observacoes")));
        h.setPrescricao(c.getString(c.getColumnIndexOrThrow("prescricao")));
        return h;
    }
}
