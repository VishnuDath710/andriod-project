package com.project.dataplotter;

import android.app.Activity;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.widget.AdapterView;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecondActivity extends AppCompatActivity {

    private AlertDialog dialog;
    private MyDatabaseHelper dbHelper;
    private static final int IMAGE_PICKER_REQUEST = 1;
    private ImageView imageView;
    private Bitmap selectedImageBitmap;


    private ListView listView;

    private void updateListView() {
        List<String> categories = getCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String category = categories.get(position);
                List<Task> tasks = getTasksForCategory(category);
                showTasksDialog(tasks);
            }
        });
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        dbHelper = new MyDatabaseHelper(this);

        listView = findViewById(R.id.list);
        updateListView();
    }
    public void showAddCategoryDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a New Category");

        final EditText categoryEditText = new EditText(this);
        categoryEditText.setHint("Category Name");

        builder.setView(categoryEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = categoryEditText.getText().toString().trim();
                if (!categoryName.isEmpty()) {
                    addCategory(categoryName);
                } else {
                    showToast("Category name cannot be empty");
                }
            }
        });

        builder.setNegativeButton("Cancel", null);

        builder.show();
    }



    private List<String> getCategories() {
        List<String> categoriesList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT category_name FROM categories", null);
        if (cursor.moveToFirst()) {
            do {
                String category = cursor.getString(cursor.getColumnIndex("category_name"));
                categoriesList.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return categoriesList;
    }

    public void showAddTaskDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add a New Task");

        final LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_task, null);
        builder.setView(dialogView);

        final Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        final EditText taskEditText = dialogView.findViewById(R.id.taskEditText);
        final Button uploadImageButton = dialogView.findViewById(R.id.uploadImageButton);
        final Button submitButton = dialogView.findViewById(R.id.submitButton);
        imageView = dialogView.findViewById(R.id.imageView);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getCategories());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE_PICKER_REQUEST);
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedCategory = categorySpinner.getSelectedItem().toString();
                String task = taskEditText.getText().toString().trim();

                if (!selectedCategory.isEmpty() && !task.isEmpty() && selectedImageBitmap != null) {
                    byte[] imageData = DbBitmapUtility.getBytes(selectedImageBitmap);
                    saveTask(selectedCategory, task, imageData);

                    dialog.dismiss();
                } else {
                    showToast("Please fill in all fields and upload an image");
                }
            }
        });

        dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                try {
                    selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    imageView.setImageBitmap(selectedImageBitmap);
                    imageView.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveTask(String category, String task, byte[] image) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category", category);
        values.put("task", task);
        values.put("image", image);

        long newRowId = db.insert("tasks", null, values);
        db.close();

        if (newRowId != -1) {
            showToast("Task added successfully");
        } else {
            showToast("Failed to add task");
        }
    }





    private void addCategory(String categoryName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("category_name", categoryName);
        values.put("task_count", 0);
        long newRowId = db.insert("categories", null, values);
        db.close();

        if (newRowId != -1) {
            showToast("Category added successfully");
            updateListView();
        } else {
            showToast("Failed to add category");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }


    private static class Task {
        String name;
        Bitmap image;

        Task(String name, Bitmap image) {
            this.name = name;
            this.image = image;
        }
    }

    private void showTasksDialog(List<Task> tasks) {
        // Create a custom dialog to display tasks and images
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tasks");

        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_tasks, null);
        builder.setView(dialogView);

        ListView tasksListView = dialogView.findViewById(R.id.tasksListView);
        TaskAdapter taskAdapter = new TaskAdapter(tasks);
        tasksListView.setAdapter(taskAdapter);

        builder.setPositiveButton("Close", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private List<Task> getTasksForCategory(String category) {
        List<Task> tasksList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT task, image FROM tasks WHERE category = ?", new String[]{category});
        if (cursor.moveToFirst()) {
            do {
                String task = cursor.getString(cursor.getColumnIndex("task"));
                byte[] imageData = cursor.getBlob(cursor.getColumnIndex("image"));
                Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);

                tasksList.add(new Task(task, image));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return tasksList;
    }


    private class TaskAdapter extends ArrayAdapter<Task> {
        TaskAdapter(List<Task> tasks) {
            super(SecondActivity.this, R.layout.list_item_task, tasks);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.list_item_task, parent, false);
            }

            Task currentTask = getItem(position);
            if (currentTask != null) {
                ImageView imageView = itemView.findViewById(R.id.taskImageView);
                TextView textView = itemView.findViewById(R.id.taskNameTextView);

                imageView.setImageBitmap(currentTask.image);
                textView.setText(currentTask.name);
            }

            return itemView;
        }
    }

}
