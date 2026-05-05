package com.clinica.app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MercadoPagoService {

    // ─── Config keys (must match MpConfigActivity) ────────────────────────────
    public static final String PREF_MP          = "mp_config";
    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_PUBLIC_KEY   = "public_key";
    public static final String KEY_IS_SANDBOX   = "is_sandbox";

    // ─── Deep-link URLs ───────────────────────────────────────────────────────
    public static final String DEEP_LINK_SUCCESS = "clinicaapp://pagamento/sucesso";
    public static final String DEEP_LINK_FAILURE = "clinicaapp://pagamento/falha";
    public static final String DEEP_LINK_PENDING = "clinicaapp://pagamento/pendente";

    private static final String BASE_URL = "https://api.mercadopago.com";
    private static final MediaType JSON  = MediaType.get("application/json; charset=utf-8");

    private static final String DEFAULT_ACCESS_TOKEN = "YOUR_ACCESS_TOKEN_HERE";
    private static final String DEFAULT_PUBLIC_KEY   = "YOUR_PUBLIC_KEY_HERE";

    private final OkHttpClient client;
    private final String accessToken;
    private final boolean isSandbox;

    public MercadoPagoService(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_MP, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_ACCESS_TOKEN, "");
        this.accessToken = token.isEmpty() ? DEFAULT_ACCESS_TOKEN : token;
        this.isSandbox   = prefs.getBoolean(KEY_IS_SANDBOX, true);
        this.client      = buildClient();
    }

    public MercadoPagoService() {
        this.accessToken = DEFAULT_ACCESS_TOKEN;
        this.isSandbox   = true;
        this.client      = buildClient();
    }

    private OkHttpClient buildClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public static class PreferenceResult {
        public final String initPoint;
        public final String initPointSandbox;
        public final String preferenceId;
        public final String erro;

        public PreferenceResult(String initPoint, String initPointSandbox,
                                String preferenceId, String erro) {
            this.initPoint        = initPoint;
            this.initPointSandbox = initPointSandbox;
            this.preferenceId     = preferenceId;
            this.erro             = erro;
        }

        public boolean sucesso() { return erro == null; }
    }

    public PreferenceResult criarPreferencia(int consultaId, String descricao,
                                             int quantidade, double valor) {
        try {
            JSONObject item = new JSONObject();
            item.put("id",          String.valueOf(consultaId));
            item.put("title",       descricao);
            item.put("quantity",    quantidade);
            item.put("unit_price",  valor);
            item.put("currency_id", "BRL");

            JSONArray items = new JSONArray();
            items.put(item);

            JSONObject backUrls = new JSONObject();
            backUrls.put("success", DEEP_LINK_SUCCESS);
            backUrls.put("failure", DEEP_LINK_FAILURE);
            backUrls.put("pending", DEEP_LINK_PENDING);

            JSONObject payload = new JSONObject();
            payload.put("items",               items);
            payload.put("back_urls",           backUrls);
            payload.put("auto_return",         "approved");
            payload.put("external_reference",  String.valueOf(consultaId));
            payload.put("statement_descriptor","Clinica App");

            RequestBody body = RequestBody.create(payload.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/checkout/preferences")
                    .post(body)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type",  "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                String rb = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    return new PreferenceResult(null, null, null,
                            "HTTP " + response.code() + ": " + rb);
                }
                JSONObject json = new JSONObject(rb);
                return new PreferenceResult(
                        json.optString("init_point"),
                        json.optString("sandbox_init_point"),
                        json.optString("id"),
                        null);
            }
        } catch (Exception e) {
            return new PreferenceResult(null, null, null,
                    "Falha na conexão: " + e.getMessage());
        }
    }

    public String resolverUrlPagamento(PreferenceResult result) {
        if (result == null || !result.sucesso()) return null;
        String url = isSandbox ? result.initPointSandbox : result.initPoint;
        return (url != null && !url.isEmpty()) ? url : result.initPoint;
    }

    public String consultarPagamento(String paymentId) {
        try {
            Request request = new Request.Builder()
                    .url(BASE_URL + "/v1/payments/" + paymentId)
                    .get()
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) return null;
                return new JSONObject(response.body().string()).optString("status");
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static void salvarCredenciais(Context context, String accessToken,
                                         String publicKey, boolean isSandbox) {
        context.getSharedPreferences(PREF_MP, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_PUBLIC_KEY, publicKey)
                .putBoolean(KEY_IS_SANDBOX, isSandbox)
                .apply();
    }

    public static String getPublicKey(Context context) {
        String key = context.getSharedPreferences(PREF_MP, Context.MODE_PRIVATE)
                .getString(KEY_PUBLIC_KEY, "");
        return key.isEmpty() ? DEFAULT_PUBLIC_KEY : key;
    }

    public static boolean isSandbox(Context context) {
        return context.getSharedPreferences(PREF_MP, Context.MODE_PRIVATE)
                .getBoolean(KEY_IS_SANDBOX, true);
    }
}
