package com.example.taskflow;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText taskInput;
    Button addButton;
    RecyclerView recyclerView;

    ArrayList<Task> taskList = new ArrayList<>();
    TaskAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        taskInput = findViewById(R.id.taskInput);
        addButton = findViewById(R.id.addButton);
        recyclerView = findViewById(R.id.recyclerView);

        adapter = new TaskAdapter(taskList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // ➕ Add Task
        addButton.setOnClickListener(v -> {
            String task = taskInput.getText().toString();

            if (!task.isEmpty()) {
                taskList.add(new Task(task));
                adapter.notifyItemInserted(taskList.size() - 1);
                taskInput.setText("");
            }
        });

        // 🧨 Swipe to Delete
        ItemTouchHelper.SimpleCallback simpleCallback =
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
                        return 0.6f;
                    }

                    @Override
                    public float getSwipeVelocityThreshold(float defaultValue) {
                        return defaultValue * 2.5f;
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

                        int position = viewHolder.getAdapterPosition();

                        if (position != RecyclerView.NO_POSITION) {
                            taskList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                    }
                };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }
}