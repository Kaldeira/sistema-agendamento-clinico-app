package com.clinica.app.Utils;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputEditText;

public class MascaraHelper {
    public static void CpfMask(TextInputEditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isUpdating) return;

                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");

                StringBuilder mask = new StringBuilder();

                int length = str.length();

                if (length > 11)
                    str = str.substring(0, 11);

                for (int i = 0; i < str.length(); i++) {

                    if (i == 3 || i == 6)
                        mask.append(".");

                    if (i == 9)
                        mask.append("-");

                    mask.append(str.charAt(i));
                }

                editText.setText(mask.toString());
                editText.setSelection(mask.length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    public static void CrmMask(TextInputEditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isUpdating) return;

                isUpdating = true;

                String str = s.toString()
                        .replaceAll("[^\\dA-Za-z]", "")
                        .toUpperCase();

                if (str.startsWith("CRM")) {
                    str = str.substring(3);
                }

                String numeros = str.replaceAll("[^\\d]", "");
                String letras = str.replaceAll("[^A-Z]", "");

                if (numeros.length() > 6) {
                    numeros = numeros.substring(0, 6);
                }

                if (letras.length() > 2) {
                    letras = letras.substring(0, 2);
                }

                StringBuilder mask = new StringBuilder();

                if (!numeros.isEmpty()) {
                    mask.append("CRM ");
                    mask.append(numeros);
                }

                if (letras.length() > 0) {
                    mask.append("/");
                    mask.append(letras);
                }

                editText.setText(mask.toString());
                editText.setSelection(mask.length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    public static void CpfMask(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isUpdating) return;

                isUpdating = true;

                String str = s.toString().replaceAll("[^\\d]", "");

                StringBuilder mask = new StringBuilder();

                int length = str.length();

                if (length > 11)
                    str = str.substring(0, 11);

                for (int i = 0; i < str.length(); i++) {

                    if (i == 3 || i == 6)
                        mask.append(".");

                    if (i == 9)
                        mask.append("-");

                    mask.append(str.charAt(i));
                }

                editText.setText(mask.toString());
                editText.setSelection(mask.length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    public static void CrmMask(EditText editText) {

        editText.addTextChangedListener(new TextWatcher() {

            boolean isUpdating;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (isUpdating) return;

                isUpdating = true;

                String str = s.toString()
                        .replaceAll("[^\\dA-Za-z]", "")
                        .toUpperCase();

                if (str.startsWith("CRM")) {
                    str = str.substring(3);
                }

                String numeros = str.replaceAll("[^\\d]", "");
                String letras = str.replaceAll("[^A-Z]", "");

                if (numeros.length() > 6) {
                    numeros = numeros.substring(0, 6);
                }

                if (letras.length() > 2) {
                    letras = letras.substring(0, 2);
                }

                StringBuilder mask = new StringBuilder();

                if (!numeros.isEmpty()) {
                    mask.append("CRM ");
                    mask.append(numeros);
                }

                if (letras.length() > 0) {
                    mask.append("/");
                    mask.append(letras);
                }

                editText.setText(mask.toString());
                editText.setSelection(mask.length());

                isUpdating = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
