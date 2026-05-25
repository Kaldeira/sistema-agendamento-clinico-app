package com.clinica.app.Utils;

import android.text.Editable;
import android.text.TextWatcher;

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
}
