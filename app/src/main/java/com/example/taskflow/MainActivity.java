package com.example.taskflow;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fabAdd;

    TextView taskCounter;
    TextView emptyState;
    TextView titleText;

    ImageButton themeToggle;
    boolean isDark = false;

    ArrayList<Task> taskList = new ArrayList<>();
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);

        taskCounter = findViewById(R.id.taskCounter);
        emptyState = findViewById(R.id.emptyState);
        titleText = findViewById(R.id.titleText);
        themeToggle = findViewById(R.id.themeToggle);

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onEdit(Task task, int position) {
                showEditDialog(task);
            }

            @Override
            public void onTaskChanged() {
                sortTasks();
                updateUI();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadTasks();
        applyTheme();
        updateUI();

        fabAdd.setOnClickListener(v -> showAddDialog());

        // Swipe delete
        ItemTouchHelper.SimpleCallback swipe =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int pos = viewHolder.getBindingAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {
                            taskList.remove(pos);
                            adapter.notifyItemRemoved(pos);
                            updateUI();
                        }
                    }
                };

        new ItemTouchHelper(swipe).attachToRecyclerView(recyclerView);

        // Theme toggle FIXED
        themeToggle.setOnClickListener(v -> {
            isDark = !isDark;
            applyTheme();
            saveThemePreference();
        });
    }

    private void applyTheme() {
        adapter.setDarkMode(isDark);
        View mainLayout = findViewById(R.id.mainLayout);

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
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        prefs.edit().putBoolean("isDark", isDark).apply();
    }

    private void showAddDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_task, null);

        setupDialogTheme(view);

        EditText input = view.findViewById(R.id.dialogTaskInput);
        Spinner spinner = view.findViewById(R.id.prioritySpinner);

        setupPrioritySpinner(spinner);

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.add, (d, w) -> {
                    String title = input.getText().toString().trim();
                    String priority = spinner.getSelectedItem().toString();

                    if (!title.isEmpty()) {
                        taskList.add(new Task(title, priority));
                        sortTasks();
                        updateUI();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showEditDialog(Task task) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_task, null);

        setupDialogTheme(view);

        EditText input = view.findViewById(R.id.dialogTaskInput);
        Spinner spinner = view.findViewById(R.id.prioritySpinner);

        input.setText(task.getTitle());

        String[] priorities = {"High", "Medium", "Low"};
        setupPrioritySpinner(spinner);

        for (int i = 0; i < priorities.length; i++) {
            if (Objects.equals(priorities[i], task.getPriority())) {
                spinner.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.save, (d, w) -> {
                    task.setTitle(input.getText().toString());
                    task.setPriority(spinner.getSelectedItem().toString());
                    sortTasks();
                    updateUI();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupPrioritySpinner(Spinner spinner) {
        String[] priorities = {"High", "Medium", "Low"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, priorities) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                styleSpinnerItem(v, position, false);
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                styleSpinnerItem(v, position, true);
                return v;
            }

            private void styleSpinnerItem(View v, int position, boolean isDropDown) {
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    String p = priorities[position];
                    if (isDark) {
                        tv.setTextColor(0xFFFFFFFF);
                        if (isDropDown) v.setBackgroundColor(0xFF1E1E1E);
                    } else {
                        tv.setTextColor(0xFF111827);
                        if (isDropDown) v.setBackgroundColor(0xFFFFFFFF);
                    }

                    if ("High".equals(p)) {
                        tv.setTextColor(0xFFDC2626); // Red for High
                        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                    } else if ("Medium".equals(p)) {
                        tv.setTextColor(0xFFD97706); // Orange for Medium
                        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                    } else if ("Low".equals(p)) {
                        tv.setTextColor(0xFF16A34A); // Green for Low
                        tv.setTypeface(tv.getTypeface(), android.graphics.Typeface.BOLD);
                    }
                }
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupDialogTheme(View view) {
        View root = view.findViewById(R.id.dialogRoot);
        TextView title = view.findViewById(R.id.dialogTitle);
        TextView label = view.findViewById(R.id.priorityLabel);
        TextInputLayout inputLayout = view.findViewById(R.id.dialogInputLayout);
        EditText input = view.findViewById(R.id.dialogTaskInput);
        Spinner spinner = view.findViewById(R.id.prioritySpinner);
        View spinnerWrapper = view.findViewById(R.id.spinnerWrapper);

        if (isDark) {
            root.setBackgroundColor(0xFF1E1E1E);
            title.setTextColor(0xFFFFFFFF);
            label.setTextColor(0xFFCCCCCC);
            input.setTextColor(0xFFFFFFFF);
            inputLayout.setHintTextColor(android.content.res.ColorStateList.valueOf(0xFF3B82F6));
            inputLayout.setDefaultHintTextColor(android.content.res.ColorStateList.valueOf(0xFFB0B0B0));
            inputLayout.setBoxStrokeColor(0xFF3B82F6);
            spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFFFFF));
            if (spinnerWrapper != null) {
                spinnerWrapper.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF333333));
            }
        } else {
            root.setBackgroundColor(0xFFFFFFFF);
            title.setTextColor(0xFF111827);
            label.setTextColor(0xFF6B7280);
            input.setTextColor(0xFF111827);
            inputLayout.setHintTextColor(android.content.res.ColorStateList.valueOf(0xFF3B82F6));
            inputLayout.setDefaultHintTextColor(android.content.res.ColorStateList.valueOf(0xFF6B7280));
            inputLayout.setBoxStrokeColor(0xFF3B82F6);
            spinner.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF111827));
            if (spinnerWrapper != null) {
                spinnerWrapper.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF9FAFB));
            }
        }
    }

    private void sortTasks() {
        taskList.sort((t1, t2) -> {
            // First sort by completion status (Incomplete first)
            int comp = Boolean.compare(t1.isCompleted(), t2.isCompleted());
            if (comp != 0) return comp;

            // Then sort by priority value
            return Integer.compare(
                    getPriorityValue(t1.getPriority()),
                    getPriorityValue(t2.getPriority())
            );
        });
        adapter.notifyDataSetChanged();
    }

    private int getPriorityValue(String p) {
        switch (p) {
            case "High": return 1;
            case "Medium": return 2;
            case "Low": return 3;
            default: return 4;
        }
    }

    private void updateUI() {
        int remaining = 0;
        for (Task t : taskList) {
            if (!t.isCompleted()) remaining++;
        }
        taskCounter.setText(getString(R.string.tasks_remaining, remaining));

        if (taskList.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        saveTasks();
    }

    private void saveTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        JSONArray jsonArray = new JSONArray();

        for (Task task : taskList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("title", task.getTitle());
                jsonObject.put("priority", task.getPriority());
                jsonObject.put("isCompleted", task.isCompleted());
                jsonObject.put("id", task.getId());
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        editor.putString("tasks", jsonArray.toString());
        editor.apply();
    }

    private void loadTasks() {
        SharedPreferences prefs = getSharedPreferences("TaskPrefs", MODE_PRIVATE);
        isDark = prefs.getBoolean("isDark", false);
        String tasksJson = prefs.getString("tasks", null);

        if (tasksJson != null) {
            taskList.clear();
            try {
                JSONArray jsonArray = new JSONArray(tasksJson);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    Task task = new Task(
                            jsonObject.getString("title"),
                            jsonObject.getString("priority"),
                            jsonObject.optBoolean("isCompleted", jsonObject.optBoolean("isDone")),
                            jsonObject.getLong("id")
                    );
                    taskList.add(task);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    }
}