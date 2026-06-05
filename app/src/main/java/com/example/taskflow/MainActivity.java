package com.example.taskflow;

// ============================================================
//  MainActivity.java  —  Main screen
//  TEAM MEMBER: assign to Member A  (same as Task.java)
//
//  Changes from original:
//    + Save/load new Task fields (taskType, reminderTime, eventDate, color)
//    + Schedule / cancel reminders via ReminderScheduler
//    + Pass colour and type data to the enhanced dialog
//    + Request POST_NOTIFICATIONS permission (Android 13+)
// ============================================================

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // ── Palette for colour picker ─────────────────────────────
    private static final String[] PALETTE = {
        "#6366F1", // Indigo
        "#EC4899", // Pink
        "#F59E0B", // Amber
        "#10B981", // Emerald
        "#3B82F6", // Blue
        "#EF4444", // Red
        "#8B5CF6", // Violet
        "#14B8A6"  // Teal
    };
    private static final String[] PALETTE_NAMES = {
        "Indigo", "Pink", "Amber", "Emerald", "Blue", "Red", "Violet", "Teal"
    };

    // ── Views ─────────────────────────────────────────────────
    RecyclerView            recyclerView;
    FloatingActionButton    fabAdd;
    TextView                taskCounter;
    TextView                emptyState;
    TextView                titleText;
    ImageButton             themeToggle;
    ViewGroup               filterLayout;
    Chip                    chipAll, chipActive, chipDone, chipHigh;

    boolean isDark = false;
    String currentFilter = "All";

    ArrayList<Task> allTasks = new ArrayList<>();
    ArrayList<Task> displayedTasks = new ArrayList<>();
    TaskAdapter     adapter;

    // ── Lifecycle ─────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd       = findViewById(R.id.fabAdd);
        taskCounter  = findViewById(R.id.taskCounter);
        emptyState   = findViewById(R.id.emptyState);
        titleText    = findViewById(R.id.titleText);
        themeToggle  = findViewById(R.id.themeToggle);
        filterLayout = findViewById(R.id.filterLayout);
        chipAll      = findViewById(R.id.chipAll);
        chipActive   = findViewById(R.id.chipActive);
        chipDone     = findViewById(R.id.chipDone);
        chipHigh     = findViewById(R.id.chipHigh);

        adapter = new TaskAdapter(displayedTasks, new TaskAdapter.OnTaskActionListener() {
            @Override public void onEdit(Task task, int position) { showEditDialog(task); }
            @Override public void onDelete(Task task) {
                ReminderScheduler.cancel(MainActivity.this, task);
                allTasks.remove(task);
                applyFilter(currentFilter);
                saveTasks();
            }
            @Override public void onTaskChanged() { 
                // When a task changes (e.g. checked), we might need to re-filter
                applyFilter(currentFilter);
                saveTasks(); 
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTasks();
        applyFilter("All");
        applyTheme();
        updateUI();

        View.OnClickListener filterListener = v -> {
            chipAll.setChecked(v.getId() == R.id.chipAll);
            chipActive.setChecked(v.getId() == R.id.chipActive);
            chipDone.setChecked(v.getId() == R.id.chipDone);
            chipHigh.setChecked(v.getId() == R.id.chipHigh);

            if (v.getId() == R.id.chipAll) currentFilter = "All";
            else if (v.getId() == R.id.chipActive) currentFilter = "Active";
            else if (v.getId() == R.id.chipDone) currentFilter = "Done";
            else if (v.getId() == R.id.chipHigh) currentFilter = "High";
            
            applyFilter(currentFilter);
        };

        chipAll.setOnClickListener(filterListener);
        chipActive.setOnClickListener(filterListener);
        chipDone.setOnClickListener(filterListener);
        chipHigh.setOnClickListener(filterListener);

        fabAdd.setOnClickListener(v -> showAddDialog());

        // Swipe-to-delete
        ItemTouchHelper.SimpleCallback swipe =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override public boolean onMove(@NonNull RecyclerView rv,
                        @NonNull RecyclerView.ViewHolder v, @NonNull RecyclerView.ViewHolder t) {
                    return false;
                }
                @Override public void onSwiped(RecyclerView.ViewHolder vh, int dir) {
                    int pos = vh.getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        Task taskToRemove = displayedTasks.get(pos);
                        ReminderScheduler.cancel(MainActivity.this, taskToRemove);
                        allTasks.remove(taskToRemove);
                        applyFilter(currentFilter);
                        saveTasks();
                    }
                }
            };
        new ItemTouchHelper(swipe).attachToRecyclerView(recyclerView);

        themeToggle.setOnClickListener(v -> {
            isDark = !isDark;
            applyTheme();
            saveThemePreference();
        });

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Ensure notification channel exists
        ReminderReceiver.createNotificationChannel(this);
    }

    // ── Theme ─────────────────────────────────────────────────

    private void applyTheme() {
        adapter.setDarkMode(isDark);
        View mainLayout = findViewById(R.id.mainLayout);

        // Define colors for chips
        int chipSelectedBg = 0xFF2563EB; // Primary Blue
        int chipSelectedText = 0xFFFFFFFF;
        int chipUnselectedBg = isDark ? 0xFF333333 : 0xFFF3F4F6;
        int chipUnselectedText = isDark ? 0xFFFFFFFF : 0xFF111827;

        int[][] states = new int[][] {
            new int[] {android.R.attr.state_checked},
            new int[] {}
        };

        android.content.res.ColorStateList bgColors = new android.content.res.ColorStateList(states,
                new int[] {chipSelectedBg, chipUnselectedBg});
        android.content.res.ColorStateList textColors = new android.content.res.ColorStateList(states,
                new int[] {chipSelectedText, chipUnselectedText});

        Chip[] chips = {chipAll, chipActive, chipDone, chipHigh};
        for (Chip chip : chips) {
            chip.setChipBackgroundColor(bgColors);
            chip.setTextColor(textColors);
            chip.setChipStrokeWidth(0); // Cleaner segmented look
        }

        if (isDark) {
            mainLayout.setBackgroundColor(0xFF121212);
            titleText.setTextColor(0xFFFFFFFF);
            taskCounter.setTextColor(0xFFCCCCCC);
            emptyState.setTextColor(0xFF888888);
            themeToggle.setImageResource(R.drawable.ic_sun);
            themeToggle.setColorFilter(0xFFFFFFFF);
            getWindow().setStatusBarColor(0xFF121212);
            getWindow().getDecorView().setSystemUiVisibility(0);
        } else {
            mainLayout.setBackgroundColor(0xFFFFFFFF);
            titleText.setTextColor(0xFF111827);
            taskCounter.setTextColor(0xFF6B7280);
            emptyState.setTextColor(0xFF9CA3AF);
            themeToggle.setImageResource(R.drawable.ic_moon);
            themeToggle.setColorFilter(0xFF111827);
            getWindow().setStatusBarColor(0xFFFFFFFF);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    private void saveThemePreference() {
        getSharedPreferences("TaskPrefs", MODE_PRIVATE)
            .edit().putBoolean("isDark", isDark).apply();
    }

    // ── Add dialog ────────────────────────────────────────────

    private void showAddDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        setupDialogTheme(view);

        EditText   input        = view.findViewById(R.id.dialogTaskInput);
        Spinner    spinner      = view.findViewById(R.id.prioritySpinner);
        RadioGroup typeGroup    = view.findViewById(R.id.taskTypeGroup);
        RadioButton rbDaily     = view.findViewById(R.id.rbDaily);
        RadioButton rbOneTime   = view.findViewById(R.id.rbOneTime);
        TimePicker  timePicker  = view.findViewById(R.id.reminderTimePicker);
        EditText   dateInput    = view.findViewById(R.id.eventDateInput);
        View       dateLayout   = view.findViewById(R.id.eventDateLayout);
        Spinner    colorSpinner = view.findViewById(R.id.colorSpinner);

        timePicker.setIs24HourView(true);

        rbDaily.setChecked(true);
        dateLayout.setVisibility(View.GONE);

        typeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbOneTime) {
                dateLayout.setVisibility(View.VISIBLE);
            } else {
                dateLayout.setVisibility(View.GONE);
            }
        });

        setupPrioritySpinner(spinner);
        setupColorSpinner(colorSpinner);

        new AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(R.string.add, (d, w) -> {
                String title    = input.getText().toString().trim();
                String priority = spinner.getSelectedItem().toString();
                String color    = PALETTE[colorSpinner.getSelectedItemPosition()];
                String type     = rbOneTime.isChecked() ? Task.TYPE_ONE_TIME : Task.TYPE_DAILY;
                String reminder = String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute());
                String date     = rbOneTime.isChecked()
                        ? dateInput.getText().toString().trim() : "";

                if (!title.isEmpty()) {
                    Task task = new Task(title, priority, false,
                            System.currentTimeMillis(), type, reminder, date, color);
                    allTasks.add(task);
                    ReminderScheduler.schedule(this, task);
                    applyFilter(currentFilter);
                    saveTasks();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ── Edit dialog ───────────────────────────────────────────

    private void showEditDialog(Task task) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        setupDialogTheme(view);

        EditText   input        = view.findViewById(R.id.dialogTaskInput);
        Spinner    spinner      = view.findViewById(R.id.prioritySpinner);
        RadioGroup typeGroup    = view.findViewById(R.id.taskTypeGroup);
        RadioButton rbDaily     = view.findViewById(R.id.rbDaily);
        RadioButton rbOneTime   = view.findViewById(R.id.rbOneTime);
        TimePicker  timePicker  = view.findViewById(R.id.reminderTimePicker);
        EditText   dateInput    = view.findViewById(R.id.eventDateInput);
        View       dateLayout   = view.findViewById(R.id.eventDateLayout);
        Spinner    colorSpinner = view.findViewById(R.id.colorSpinner);

        // Pre-fill existing values
        input.setText(task.getTitle());
        timePicker.setIs24HourView(true);

        if (task.hasReminder()) {
            String[] parts = task.getReminderTime().split(":");
            if (parts.length == 2) {
                try {
                    timePicker.setHour(Integer.parseInt(parts[0]));
                    timePicker.setMinute(Integer.parseInt(parts[1]));
                } catch (NumberFormatException ignored) {}
            }
        }

        boolean isOneTime = task.isOneTime();
        rbOneTime.setChecked(isOneTime);
        rbDaily.setChecked(!isOneTime);
        dateLayout.setVisibility(isOneTime ? View.VISIBLE : View.GONE);
        dateInput.setText(task.getEventDate());

        typeGroup.setOnCheckedChangeListener((group, checkedId) ->
            dateLayout.setVisibility(checkedId == R.id.rbOneTime ? View.VISIBLE : View.GONE));

        String[] priorities = {"High", "Medium", "Low"};
        setupPrioritySpinner(spinner);
        for (int i = 0; i < priorities.length; i++) {
            if (Objects.equals(priorities[i], task.getPriority())) {
                spinner.setSelection(i); break;
            }
        }

        setupColorSpinner(colorSpinner);
        for (int i = 0; i < PALETTE.length; i++) {
            if (PALETTE[i].equalsIgnoreCase(task.getColor())) {
                colorSpinner.setSelection(i); break;
            }
        }

        new AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(R.string.save, (d, w) -> {
                // Cancel old alarm before updating
                ReminderScheduler.cancel(this, task);

                task.setTitle(input.getText().toString().trim());
                task.setPriority(spinner.getSelectedItem().toString());
                task.setColor(PALETTE[colorSpinner.getSelectedItemPosition()]);
                task.setTaskType(rbOneTime.isChecked() ? Task.TYPE_ONE_TIME : Task.TYPE_DAILY);
                task.setReminderTime(String.format(Locale.getDefault(), "%02d:%02d",
                        timePicker.getHour(), timePicker.getMinute()));
                task.setEventDate(rbOneTime.isChecked()
                        ? dateInput.getText().toString().trim() : "");

                // Schedule new alarm
                ReminderScheduler.schedule(this, task);
                applyFilter(currentFilter);
                saveTasks();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    // ── Spinner helpers ───────────────────────────────────────

    private void setupPrioritySpinner(Spinner spinner) {
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorities) {
            @NonNull @Override
            public View getView(int pos, @Nullable View cv, @NonNull ViewGroup p) {
                return style(super.getView(pos, cv, p), pos, false);
            }
            @Override
            public View getDropDownView(int pos, @Nullable View cv, @NonNull ViewGroup p) {
                return style(super.getDropDownView(pos, cv, p), pos, true);
            }
            private View style(View v, int pos, boolean dropdown) {
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(isDark ? 0xFFFFFFFF : 0xFF111827);
                    if (dropdown) v.setBackgroundColor(isDark ? 0xFF1E1E1E : 0xFFFFFFFF);
                    switch (priorities[pos]) {
                        case "High":   tv.setTextColor(0xFFDC2626); break;
                        case "Medium": tv.setTextColor(0xFFD97706); break;
                        case "Low":    tv.setTextColor(0xFF16A34A); break;
                    }
                }
                return v;
            }
        };
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);
    }

    private void setupColorSpinner(Spinner spinner) {
        ArrayAdapter<String> adp = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, PALETTE_NAMES);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adp);
    }

    private void setupDialogTheme(View view) {
        View root              = view.findViewById(R.id.dialogRoot);
        TextView titleTv       = view.findViewById(R.id.dialogTitle);
        TextView labelTv       = view.findViewById(R.id.priorityLabel);
        TextInputLayout layout = view.findViewById(R.id.dialogInputLayout);
        EditText input         = view.findViewById(R.id.dialogTaskInput);
        View spinnerWrapper    = view.findViewById(R.id.spinnerWrapper);
        RadioGroup typeGroup   = view.findViewById(R.id.taskTypeGroup);

        if (isDark) {
            root.setBackgroundColor(0xFF1E1E1E);
            titleTv.setTextColor(0xFFFFFFFF);
            labelTv.setTextColor(0xFFCCCCCC);
            input.setTextColor(0xFFFFFFFF);
            layout.setBoxStrokeColor(0xFF3B82F6);
            if (spinnerWrapper != null) {
                spinnerWrapper.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
            }
            if (typeGroup != null) {
                for (int i = 0; i < typeGroup.getChildCount(); i++) {
                    View child = typeGroup.getChildAt(i);
                    if (child instanceof android.widget.RadioButton) {
                        ((android.widget.RadioButton) child).setTextColor(0xFFFFFFFF);
                    }
                }
            }
        } else {
            root.setBackgroundColor(0xFFFFFFFF);
            titleTv.setTextColor(0xFF111827);
            labelTv.setTextColor(0xFF6B7280);
            input.setTextColor(0xFF111827);
            layout.setBoxStrokeColor(0xFF3B82F6);
            if (spinnerWrapper != null) {
                spinnerWrapper.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF9FAFB));
            }
        }
    }

    // ── Filter & Sort & UI ─────────────────────────────────────

    private void applyFilter(String filter) {
        currentFilter = filter;
        displayedTasks.clear();

        for (Task t : allTasks) {
            switch (filter) {
                case "Active":
                    if (!t.isCompleted()) displayedTasks.add(t);
                    break;
                case "Done":
                    if (t.isCompleted()) displayedTasks.add(t);
                    break;
                case "High":
                    if ("High".equalsIgnoreCase(t.getPriority())) displayedTasks.add(t);
                    break;
                case "All":
                default:
                    displayedTasks.add(t);
                    break;
            }
        }
        sortTasks();
        updateUI();
    }

    private void sortTasks() {
        displayedTasks.sort((t1, t2) -> {
            int comp = Boolean.compare(t1.isCompleted(), t2.isCompleted());
            if (comp != 0) return comp;
            return Integer.compare(getPriorityValue(t1.getPriority()),
                                   getPriorityValue(t2.getPriority()));
        });
        adapter.notifyDataSetChanged();
    }

    private int getPriorityValue(String p) {
        switch (p) {
            case "High":   return 1;
            case "Medium": return 2;
            case "Low":    return 3;
            default:       return 4;
        }
    }

    private void updateUI() {
        int remaining = 0;
        for (Task t : allTasks) if (!t.isCompleted()) remaining++;
        taskCounter.setText(getString(R.string.tasks_remaining, remaining));

        if (displayedTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            if (allTasks.isEmpty()) {
                emptyState.setText(R.string.empty_state);
            } else {
                emptyState.setText("No tasks match this filter");
            }
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    // ── Persistence ───────────────────────────────────────────

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        JSONArray array = new JSONArray();
        for (Task task : allTasks) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("title",        task.getTitle());
                obj.put("priority",     task.getPriority());
                obj.put("isCompleted",  task.isCompleted());
                obj.put("id",           task.getId());
                obj.put("taskType",     task.getTaskType());
                obj.put("reminderTime", task.getReminderTime());
                obj.put("eventDate",    task.getEventDate());
                obj.put("color",        task.getColor());
                array.put(obj);
            } catch (JSONException e) { e.printStackTrace(); }
        }
        prefs.edit().putString("tasks", array.toString()).apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        isDark = prefs.getBoolean("isDark", false);
        String tasksJson = prefs.getString("tasks", null);
        if (tasksJson == null) return;

        allTasks.clear();
        try {
            JSONArray array = new JSONArray(tasksJson);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                allTasks.add(new Task(
                    obj.getString("title"),
                    obj.getString("priority"),
                    obj.optBoolean("isCompleted", obj.optBoolean("isDone")),
                    obj.getLong("id"),
                    obj.optString("taskType", Task.TYPE_DAILY),
                    obj.optString("reminderTime", ""),
                    obj.optString("eventDate", ""),
                    obj.optString("color", "#6366F1")
                ));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        // applyFilter will handle adapter notification
    }
}
