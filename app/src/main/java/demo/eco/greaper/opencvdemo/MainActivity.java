package demo.eco.greaper.opencvdemo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    @BindView(R.id.btn_choose_file)
    Button btnChooseFile;
    public static final int PICK_IMAGE = 1;
    private static final int REQUEST_PERMISSION = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        checkPermissionForSDK();
    }

    private void checkPermissionForSDK() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionWrite = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int permerssionRead = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
            ArrayList<String> list = new ArrayList<>();

            if (permissionWrite != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permerssionRead != PackageManager.PERMISSION_GRANTED) {
                list.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }

            if (list.size() > 0) {
                ActivityCompat.requestPermissions(MainActivity.this, list.toArray(new String[list.size()]), REQUEST_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                ArrayList<String> listDeny = new ArrayList<>();
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        listDeny.add(permissions[i]);
                    }
                }
                if (listDeny.size() > 0) {
                    Toast.makeText(this, "Some permission must allow", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(MainActivity.this, listDeny.toArray(new String[listDeny.size()]), REQUEST_PERMISSION);
                }
                break;
        }
    }


    @OnClick(R.id.btn_choose_file)
    public void onViewClicked() {
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
//        String path = getPath(getApplicationContext(), data.getData());
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + Environment.DIRECTORY_DCIM + "/Camera/1529401200051.jpg";
        Log.d("okokok", path);
        Intent intent = new Intent(MainActivity.this, EditActivity.class);
        intent.putExtra(AppUtils.KEY_PATH_INTENT, path);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            String path = getPath(getApplicationContext(), data.getData());
            Intent intent = new Intent(MainActivity.this, EditActivity.class);
            intent.putExtra(AppUtils.KEY_PATH_INTENT, path);
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getPath(Context context, Uri uri) {
        String filePath = "";
        String wholeID = DocumentsContract.getDocumentId(uri);

        // Split at colon, use second item in the array
        String id = wholeID.split(":")[1];

        String[] column = { MediaStore.Images.Media.DATA };

        // where id is equal to
        String sel = MediaStore.Images.Media._ID + "=?";

        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column, sel, new String[]{ id }, null);

        int columnIndex = cursor.getColumnIndex(column[0]);

        if (cursor.moveToFirst()) {
            filePath = cursor.getString(columnIndex);
        }
        cursor.close();
        return filePath;
    }
}
