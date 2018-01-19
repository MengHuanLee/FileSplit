package com.google.jerry.filesplit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button mSelectSource;
    private Button mSelectTarget;
    private Button mSplit;
    private EditText mSourcePath;
    private EditText mTargetPath;
    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1;

    private String filename;
    public static long chunkSize = (long)(1.4 * 1024);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSelectSource = findViewById(R.id.buttonSource);
        mSelectTarget = findViewById(R.id.buttonTarget);
        mSplit = findViewById(R.id.buttonSplit);
        mSourcePath = findViewById(R.id.sourceText);
        mTargetPath = findViewById(R.id.targetText);

        mSelectSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                performFileSearch();

            }
        });

        mSelectTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                performFolderSelect();
            }
        });

        mSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    split(filename);
                } catch (Exception e){
                    Log.e("File", e.getMessage());
                    Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    public void performFolderSelect() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("URI", "Uri: " + uri.toString());

                filename = getRealPathFromURI(this,uri);

                mSourcePath.setText(filename);

            }
        } else if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            //Log.d("URI", String.format("Open Directory result Uri : %s", data.getData()));
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                mTargetPath.setText(uri.getPath());
            }
        }
    }

    public static void split(String filename) throws IOException
    {
        // open the file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));

        // get the file length
        File f = new File(filename);
        long fileSize = f.length();

        // loop for each full chunk
        int subfile;
        for (subfile = 0; subfile < fileSize / chunkSize; subfile++)
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the right amount of bytes
            for (int currentByte = 0; currentByte < chunkSize; currentByte++)
            {
                // load one byte from the input file and write it to the output file
                out.write(in.read());
            }

            // close the file
            out.close();
        }

        // loop for the last chunk (which may be smaller than the chunk size)
        if (fileSize != chunkSize * (subfile - 1))
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));

            // write the rest of the file
            int b;
            while ((b = in.read()) != -1)
                out.write(b);

            // close the file
            out.close();
        }

        // close the file
        in.close();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }



}
