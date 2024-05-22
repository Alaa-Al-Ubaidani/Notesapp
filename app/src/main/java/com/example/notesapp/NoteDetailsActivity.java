package com.example.notesapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.graphics.Color;
import android.widget.TextView;
import android.widget.RadioGroup;
import android.annotation.SuppressLint;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;


public class NoteDetailsActivity extends AppCompatActivity {

    EditText titleEditText,contentEditText;
    ImageButton saveNoteBtn;
    TextView pageTitleTextView;
    String title,content,docId;
    boolean isEditMode = false;
    TextView deleteNoteTextViewBtn;
    EditText fontSizeInput;
    Switch switchBold;
    boolean isBold;
    EditText noteContent;
    RadioGroup colorSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        // Defining the elements from the layout
        titleEditText = findViewById(R.id.notes_title_text);
        contentEditText = findViewById(R.id.notes_content_text);
        saveNoteBtn = findViewById(R.id.save_note_btn);
        pageTitleTextView = findViewById(R.id.page_title);
        deleteNoteTextViewBtn = findViewById(R.id.delete_note_text_view_btn);
        switchBold = findViewById(R.id.switch_bold);
        fontSizeInput = findViewById(R.id.font_size_input);


        // Set the event listener for switchBold
        switchBold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    contentEditText.setTypeface(null, Typeface.BOLD);
                } else {
                    contentEditText.setTypeface(null, Typeface.NORMAL);
                }
            }
        });


        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");
        boolean isBold = getIntent().getBooleanExtra("isBold", false); // Default value false if 'isBold' is not sent
        String color = getIntent().getStringExtra("color"); // Retrieving the background color

        isEditMode = (docId != null && !docId.isEmpty());


        titleEditText.setText(title);
        contentEditText.setText(content);
        switchBold.setChecked(isBold); // Setting the switch state based on the data

        if(isBold) {
            contentEditText.setTypeface(null, Typeface.BOLD); // Apply bold font if the condition is true
        } else {
            contentEditText.setTypeface(null, Typeface.NORMAL);
        }

        if (docId != null && !docId.isEmpty()) {
            pageTitleTextView.setText("Edit your note");
            deleteNoteTextViewBtn.setVisibility(View.VISIBLE);
        }

        saveNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        deleteNoteTextViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteNoteFromFirebase();
            }
        });

        colorSelector = findViewById(R.id.colorSelector);
        noteContent = findViewById(R.id.notes_content_text);

        // Setting a listener for color selection changes
        colorSelector.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.colorPink) {
                    noteContent.setBackgroundColor(getResources().getColor(R.color.colorNotePink));
                } else if (checkedId == R.id.colorBlue) {
                    noteContent.setBackgroundColor(getResources().getColor(R.color.colorNoteBlue));
                } else if (checkedId == R.id.colorGreen) {
                    noteContent.setBackgroundColor(getResources().getColor(R.color.colorNoteGreen));
                } else if (checkedId == R.id.colorYellow) {
                    noteContent.setBackgroundColor(getResources().getColor(R.color.colorNoteYellow));
                }
            }
        });

        // Applying color to EditText
        if (color != null && !color.isEmpty()) {
            contentEditText.setBackgroundColor(Color.parseColor(color));
        }
    }


    void saveNote(){
        String noteTitle = titleEditText.getText().toString();
        String noteContent = contentEditText.getText().toString();
        boolean isNoteBold = switchBold.isChecked();

        float fontSize;

        try {
            fontSize = Float.parseFloat(fontSizeInput.getText().toString());
        } catch (NumberFormatException e) {
            fontSize = 18; // Default font size if the user does not enter a font value
        }


        if(noteTitle==null || noteTitle.isEmpty() ){
            titleEditText.setError("Title is required");
            return;
        }

        String selectedColor = "#FFFFFF"; // Default color
        int checkedId = colorSelector.getCheckedRadioButtonId();
        if (checkedId == R.id.colorPink) {
            selectedColor = "#FFC0CB";
        } else if (checkedId == R.id.colorBlue) {
            selectedColor = "#ADD8E6";
        } else if (checkedId == R.id.colorGreen) {
            selectedColor = "#DCF6CB";
        } else if (checkedId == R.id.colorYellow) {
            selectedColor = "#FFFFB9";
        }

        Note note = new Note();
        note.setTitle(noteTitle);
        note.setContent(noteContent);
        note.setTimestamp(Timestamp.now());
        note.setBold(isNoteBold);
        note.setFontSize(fontSize);
        note.setColor(selectedColor);

        saveNoteToFirebase(note);

    }

    void saveNoteToFirebase(Note note) {
        if (isEditMode && docId != null && !docId.isEmpty()) {
            // Update the existing note
            DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document(docId);
            updateDocument(documentReference, note);
        } else if (!isEditMode) {
            // Create a new note
            DocumentReference documentReference = Utility.getCollectionReferenceForNotes().document();
            updateDocument(documentReference, note);
        } else {
            // If there is an error in setting the docId or the mode
            Utility.showToast(NoteDetailsActivity.this, "Error: Document ID is missing or invalid");
        }
    }

    void updateDocument(DocumentReference documentReference, Note note) {
        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Utility.showToast(NoteDetailsActivity.this, isEditMode ? "Note updated successfully" : "Note added successfully");
                    finish();
                } else {
                    Utility.showToast(NoteDetailsActivity.this, "Failed while saving note");
                }
            }
        });
    }

    void deleteNoteFromFirebase(){
        DocumentReference documentReference;
        documentReference = Utility.getCollectionReferenceForNotes().document(docId);

        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    //note is deleted
                    Utility.showToast(NoteDetailsActivity.this,"Note deleted successfully");
                    finish();
                }else{
                    Utility.showToast(NoteDetailsActivity.this,"Failed while deleting note");
                }
            }
        });

    }

}