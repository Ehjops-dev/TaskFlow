package com.example.taskflow;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    FloatingActionButton fabAdd;
    TextView taskCounter;

    ArrayList<Task> taskList = new ArrayList<>();
    TaskAdapter adapter;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        fabAdd = findViewById(R.id.fabAdd);
        taskCounter = findViewById(R.id.taskCounter);

        preferences = getSharedPreferences("TaskFlowPrefs", MODE_PRIVATE);

        loadTasks();

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {

            @Override
            public void onEdit(Task task, int position) {
                showEditDialog(task, position);
            }

            @Override
            public void onTaskChanged() {
                updateCounter();
                saveTasks();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        updateCounter();

        fabAdd.setOnClickListener(v -> showAddDialog());

        ItemTouchHelper.SimpleCallback swipe =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
                        return 0.55f;
                    }

                    @Override
                    public void clearView(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder) {

                        super.clearView(recyclerView, viewHolder);

                        viewHolder.itemView.setTranslationX(0f);
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {

                        int pos = viewHolder.getAdapterPosition();

                        if (pos != RecyclerView.NO_POSITION) {

                            taskList.remove(pos);

                            adapter.notifyItemRemoved(pos);

                            updateCounter();

                            saveTasks();
                        }
                    }
                };

        new ItemTouchHelper(swipe).attachToRecyclerView(recyclerView);
    }

    private void showAddDialog() {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_task, null);

        EditText input = view.findViewById(R.id.dialogTaskInput);
        Spinner spinner = view.findViewById(R.id.prioritySpinner);

        String[] priorities = {"High", "Medium", "Low"};

        spinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                priorities
        ));

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Add", (d, w) -> {

                    String title = input.getText().toString().trim();

                    String priority =
                            spinner.getSelectedItem().toString();

                    if (!title.isEmpty()) {

                        taskList.add(new Task(title, priority));

                        adapter.notifyItemInserted(taskList.size() - 1);

                        updateCounter();

                        saveTasks();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditDialog(Task task, int position) {

        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_task, null);

        EditText input = view.findViewById(R.id.dialogTaskInput);
        Spinner spinner = view.findViewById(R.id.prioritySpinner);

        input.setText(task.getTitle());

        String[] priorities = {"High", "Medium", "Low"};

        spinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                priorities
        ));

        for (int i = 0; i < priorities.length; i++) {

            if (priorities[i].equals(task.getPriority())) {
                spinner.setSelection(i);
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton("Save", (d, w) -> {

                    task.setTitle(input.getText().toString());

                    task.setPriority(
                            spinner.getSelectedItem().toString()
                    );

                    adapter.notifyItemChanged(position);

                    updateCounter();

                    saveTasks();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateCounter() {

        int remaining = 0;

        for (Task task : taskList) {

            if (!task.isDone()) {
                remaining++;
            }
        }

        taskCounter.setText(remaining + " tasks remaining");
    }

    private void saveTasks() {

        JSONArray jsonArray = new JSONArray();

        try {

            for (Task task : taskList) {

                JSONObject jsonObject = new JSONObject();

                jsonObject.put("title", task.getTitle());
                jsonObject.put("priority", task.getPriority());
                jsonObject.put("done", task.isDone());
                jsonObject.put("id", task.getId());

                jsonArray.put(jsonObject);
            }

            preferences.edit()
                    .putString("tasks", jsonArray.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {

        String tasks = preferences.getString("tasks", null);

        if (tasks == null) return;

        try {

            JSONArray jsonArray = new JSONArray(tasks);

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);

                String title = object.getString("title");
                String priority = object.getString("priority");
                boolean done = object.getBoolean("done");
                long id = object.getLong("id");

                taskList.add(
                        new Task(title, priority, done, id)
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}