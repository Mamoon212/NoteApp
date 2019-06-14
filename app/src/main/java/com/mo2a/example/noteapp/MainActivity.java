package com.mo2a.example.noteapp;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NoteAdapter.OnItemClickListener {
    private NoteViewModel noteViewModel;
    private CoordinatorLayout coordinatorLayout;
    private ColorDrawable swipeRight= new ColorDrawable(Color.GREEN);
    private ColorDrawable swipeLeft= new ColorDrawable(Color.RED);
    private Drawable deleteIcon;
    private Drawable editIcon;
    public static final int ADD_NOTE_REQUEST = 1;
    public static final int EDIT_NOTE_REQUEST = 2;
    private NoteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout= findViewById(R.id.coor_layout);

        deleteIcon= getDrawable(R.drawable.ic_delete_sweep_black_24dp);
        editIcon= getDrawable(R.drawable.ic_mode_edit_black_24dp);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = ViewModelProviders.of(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                adapter.submitList(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.LEFT) {
                    final  Note note= adapter.getNoteAt(viewHolder.getAdapterPosition());
                    noteViewModel.delete(note);
                    Snackbar.make(coordinatorLayout,"Note deleted.", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    noteViewModel.insert(note);
                                }
                            })
                            .show();
                } else if(direction == ItemTouchHelper.RIGHT){
                    updateNote(adapter.getNoteAt(viewHolder.getAdapterPosition()));
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View view= viewHolder.itemView;
                int deleteIconMargin= (view.getHeight()- deleteIcon.getIntrinsicHeight())/2;
                int editIconMargin= (view.getHeight()- editIcon.getIntrinsicHeight())/ 2;
                if(dX > 0){
                    swipeRight.setBounds(view.getLeft(), view.getTop(), (int) dX, view.getBottom());
                    editIcon.setBounds(view.getLeft() + editIconMargin, view.getTop() + editIconMargin, view.getLeft() + editIconMargin + editIcon.getIntrinsicWidth(), view.getBottom() - editIconMargin);
                    swipeRight.draw(c);
                }else{
                    swipeLeft.setBounds(view.getRight()+ (int) dX, view.getTop(), view.getRight(), view.getBottom());
                    deleteIcon.setBounds(view.getRight() - deleteIconMargin - deleteIcon.getIntrinsicWidth(), view.getTop() + deleteIconMargin, view.getRight() - deleteIconMargin, view.getBottom() - deleteIconMargin);
                    swipeLeft.draw(c);
                }

                c.save();

                if(dX > 0){
                    c.clipRect(view.getLeft(), view.getTop(), (int) dX, view.getBottom());
                    editIcon.draw(c);
                }else{
                    c.clipRect(view.getRight()+ (int) dX, view.getTop(), view.getRight(), view.getBottom());
                    deleteIcon.draw(c);
                }

                c.restore();

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                startActivityForResult(intent, ADD_NOTE_REQUEST);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK) {
            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESC);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

            Note note = new Note(title, description, priority);
            noteViewModel.insert(note);
            Toast.makeText(this, "Note saved.", Toast.LENGTH_SHORT).show();
        } else if (requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

            if (id == -1) {
                Toast.makeText(this, "Note not saved.", Toast.LENGTH_SHORT).show();
                return;
            }
            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESC);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

            Note note = new Note(title, description, priority);
            note.setId(id);
            noteViewModel.update(note);

            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Note updated.", Toast.LENGTH_SHORT).show();


        } else {
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Note not saved.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_all) {
            noteViewModel.deleteAllNotes();
            Toast.makeText(this, "All notes deleted.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Note note) {
       updateNote(note);
    }

    public void updateNote(Note note){
        Intent intent = new Intent(MainActivity.this, AddEditNoteActivity.class);
        intent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
        intent.putExtra(AddEditNoteActivity.EXTRA_DESC, note.getDescription());
        intent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());
        intent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());

        startActivityForResult(intent, EDIT_NOTE_REQUEST);

    }
}
