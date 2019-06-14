package com.mo2a.example.noteapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import java.security.spec.PSSParameterSpec;

public class AddEditNoteActivity extends AppCompatActivity {
    public static final String EXTRA_TITLE= "OHOHOH";
    public static final String EXTRA_DESC= "AHAH";
    public static final String EXTRA_PRIORITY= "EHEH";
    public static final String EXTRA_ID= "UHUH";
    private EditText edittext_title;
    private EditText edittext_desc;
    private NumberPicker numberpicker_priority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        edittext_title = findViewById(R.id.edit_text_title);
        edittext_desc = findViewById(R.id.edit_text_desc);
        numberpicker_priority = findViewById(R.id.number_picker);

        numberpicker_priority.setMinValue(1);
        numberpicker_priority.setMaxValue(10);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);

        Intent intent= getIntent();

        if(intent.hasExtra(EXTRA_ID)){
            setTitle("Edit Note");
            edittext_title.setText(intent.getStringExtra(EXTRA_TITLE));
            edittext_desc.setText(intent.getStringExtra(EXTRA_DESC));
            numberpicker_priority.setValue(intent.getIntExtra(EXTRA_PRIORITY, 1));
        }else{
            setTitle("Add Note");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_note) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title= edittext_title.getText().toString();
        String description= edittext_desc.getText().toString();
        int priority= numberpicker_priority.getValue();

        if(title.trim().isEmpty()){
            Toast.makeText(this, "Title field can not be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent data= new Intent();
        data.putExtra(EXTRA_TITLE, title);
        data.putExtra(EXTRA_DESC, description);
        data.putExtra(EXTRA_PRIORITY, priority);

        int id= getIntent().getIntExtra(EXTRA_ID, -1);
        if(id != -1){
            data.putExtra(EXTRA_ID, id);
        }

        setResult(RESULT_OK, data);
        finish();
    }
}
