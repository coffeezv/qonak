package com.qonak.app.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

/**
 * Small builder for the booking dialogs: pickers instead of free-text dates and times,
 * so the user cannot enter an invalid or past date.
 */
public final class BookingForm {

    private BookingForm() {
    }

    public static LinearLayout container(Context context) {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(context, 20);
        layout.setPadding(pad, dp(context, 12), pad, 0);
        return layout;
    }

    public static EditText numberField(Context context, String hint, int initialValue) {
        EditText field = new EditText(context);
        field.setHint(hint);
        field.setInputType(InputType.TYPE_CLASS_NUMBER);
        field.setText(String.valueOf(initialValue));
        return field;
    }

    /** Read-only field that opens a date picker (past dates disabled). Initial value: today. */
    public static EditText dateField(Context context, String hint) {
        EditText field = pickerField(context, hint);
        field.setText(Formatters.dateFormat().format(Calendar.getInstance().getTime()));
        field.setOnClickListener(v -> {
            Calendar now = Calendar.getInstance();
            DatePickerDialog picker = new DatePickerDialog(context, (view, year, month, day) -> {
                Calendar picked = Calendar.getInstance();
                picked.set(year, month, day);
                field.setText(Formatters.dateFormat().format(picked.getTime()));
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            picker.getDatePicker().setMinDate(now.getTimeInMillis() - 1000);
            picker.show();
        });
        return field;
    }

    /** Read-only field that opens a 24h time picker. Initial value: 19:00. */
    public static EditText timeField(Context context, String hint) {
        EditText field = pickerField(context, hint);
        field.setText("19:00");
        field.setOnClickListener(v -> new TimePickerDialog(context, (view, hour, minute) ->
                field.setText(String.format(Locale.US, "%02d:%02d", hour, minute)),
                19, 0, true).show());
        return field;
    }

    public static TextView totalLabel(Context context) {
        TextView label = new TextView(context);
        label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        label.setPadding(0, dp(context, 12), 0, 0);
        return label;
    }

    /** Parses a positive integer within [min, max]; returns -1 if invalid. */
    public static int readInt(EditText field, int min, int max) {
        try {
            int value = Integer.parseInt(field.getText().toString().trim());
            return value >= min && value <= max ? value : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static EditText pickerField(Context context, String hint) {
        EditText field = new EditText(context);
        field.setHint(hint);
        field.setFocusable(false);
        field.setClickable(true);
        field.setInputType(InputType.TYPE_NULL);
        return field;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
