package com.google.jerry.filesplit;

import android.app.Activity;
import android.os.AsyncTask;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
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
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class MainActivity extends AppCompatActivity {

    private Button mSelectSource;
    private Button mSelectTarget;
    private Button mSplit;
    private Button mEncrypt;
    private Button mDecrypt;
    private EditText mSourcePath;
    private EditText mTargetPath;
    private static final int READ_REQUEST_CODE = 42;
    private static final int REQUEST_CODE_OPEN_DIRECTORY = 1;
    private Crypt crypt = new Crypt();

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
        mEncrypt = findViewById(R.id.buttonEncrypt);
        mDecrypt = findViewById(R.id.buttonDecrypt);

        mSelectSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                performFileSearch();

            }
        });

//        mSelectTarget.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                performFolderSelect();
//            }
//        });

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

        mEncrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new EncryptInBG().execute(filename);
                Log.i("Encrypt", "Doing encryption in background");
                Toast.makeText(getBaseContext(),"Encrypting file...",Toast.LENGTH_LONG).show();
            }
        });

        mDecrypt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DecryptInBG().execute(filename);
                Log.i("Decrypt", "Doing decryption in background");
                Toast.makeText(getBaseContext(),"Decrypting file...",Toast.LENGTH_LONG).show();
            }
        });
    }

    private class EncryptInBG extends AsyncTask<String, Void, Void>{

        @Override
        protected Void doInBackground(String... filename) {
            try {
                crypt.AESFileEncrypt(filename[0]);
            } catch (Exception e){
                Log.e("File", e.getMessage());
                // Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                //Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(),"Encryption completed.",Toast.LENGTH_LONG).show();
        }
    }

    private class DecryptInBG extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... filename) {
            try {
                crypt.AESFileDecryption(filename[0]);
            } catch (Exception e){
                Log.e("File", e.getMessage());
                // Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                //Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(getBaseContext(),"Decryption completed.",Toast.LENGTH_LONG).show();
        }
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

//    public void performFolderSelect() {
//
//
//
//        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
//        //intent.addCategory(Intent.CATEGORY_OPENABLE);
//        //intent.setType(DocumentsContract.Document.MIME_TYPE_DIR);
//        startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY);
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        Uri uri = null;
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            if (resultData != null) {
                uri = resultData.getData();
                Log.i("URI", "Uri: " + uri.toString());
                //filename = FileUtils.getPath(this,uri);
                filename = getPath(this, uri);

                mSourcePath.setText(filename);

            }
        } else if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK) {
            //Log.d("URI", String.format("Open Directory result Uri : %s", data.getData()));
            if (resultData != null) {
                uri = resultData.getData();

                mTargetPath.setText("/storage/");
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

    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getDirPath(Context context, Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        Uri docUri = DocumentsContract.buildDocumentUriUsingTree(uri,
                DocumentsContract.getTreeDocumentId(uri));


        Cursor docCursor = contentResolver.query(docUri, new String[]{
                DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
        try {
            while (docCursor.moveToNext()) {
                //Log.d(TAG, "found doc =" + docCursor.getString(0) + ", mime=" + docCursor
                //        .getString(1));
                //mCurrentDirectoryUri = uri;

                //mCreateDirectoryButton.setEnabled(true);
            }
        } finally {
            closeQuietly(docCursor);
        }
        return docCursor.getString(0);
    }

    public void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }




}
