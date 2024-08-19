package athensclub.android.titletracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.transition.Scene;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import athensclub.android.titletracker.controller.EditTitleSceneController;
import athensclub.android.titletracker.controller.TitleListAdapterController;
import athensclub.android.titletracker.data.Title;
import athensclub.android.titletracker.databinding.ActivityMainBinding;
import athensclub.android.titletracker.databinding.EditTitleSceneBinding;
import athensclub.android.titletracker.databinding.MainSceneBinding;
import athensclub.android.titletracker.databinding.WrapTextViewBinding;
import athensclub.android.titletracker.view.TitleListAdapter;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_JSON_FILE_CODE = 942;

    private ColorStateList primaryTextColor;

    private Scene mainScene;
    private Scene editTitleScene;

    private SceneManager sceneManager;

    private Map<Scene, Object> controller;

    private TitleListAdapterController listAdapterController;
    private EditTitleSceneController editTitleSceneController;

    private DividerItemDecoration recyclerViewDivider;

    private ActivityMainBinding activityMainBinding;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        primaryTextColor = getColorStateList(R.color.text_primary);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        controller = new HashMap<>();
        sceneManager = new SceneManager();

        recyclerViewDivider = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        recyclerViewDivider.setDrawable(Objects.requireNonNull(getDrawable(R.drawable.recyclerview_divider)));

        MainSceneBinding mainSceneBinding = MainSceneBinding.inflate(LayoutInflater.from(activityMainBinding.getRoot().getContext()));
        RecyclerView recyclerView = mainSceneBinding.titleList;

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(recyclerViewDivider);

        TitleListAdapter titleListAdapter = new TitleListAdapter(this, recyclerView);
        recyclerView.setAdapter(titleListAdapter);
        listAdapterController = titleListAdapter.getController();

        EditTitleSceneBinding editTitleSceneBinding = EditTitleSceneBinding.inflate(LayoutInflater.from(activityMainBinding.getRoot().getContext()));
        editTitleSceneController = new EditTitleSceneController(editTitleSceneBinding, this);

        mainScene = new Scene(activityMainBinding.getRoot(), (View) mainSceneBinding.getRoot());
        editTitleScene = new Scene(activityMainBinding.getRoot(), (View) editTitleSceneBinding.getRoot());
        controller.put(editTitleScene, editTitleSceneController);

        sceneManager.setScene(mainScene);

        try (FileInputStream stream = openFileInput("data.json")) {
            listAdapterController.readFrom(stream);
        } catch (FileNotFoundException e) {
            // DO NOTHING
        }catch (IOException e){
            Log.e("EXCEPTION", Log.getStackTraceString(e));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        if (listAdapterController.shouldSave())
            save();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();
        if (listAdapterController.shouldSave())
            save();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON_FILE_CODE
                && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                try(InputStream stream = getContentResolver().openInputStream(uri)){
                    listAdapterController.readFrom(stream);
                }catch (FileNotFoundException e){
                    showErrorSnackbar("File not found!");
                }catch (IOException ex){
                    Log.e("EXCEPTION", Log.getStackTraceString(ex));
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.importFile:
                importFile();
                return true;
            case R.id.shareFile:
                shareFile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void importFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        startActivityForResult(intent, PICK_JSON_FILE_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void shareFile() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/json");
        String data = "";
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        listAdapterController.saveTo(stream);
        try {
            String body = stream.toString("UTF-8");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "data.json");
            shareIntent.putExtra(Intent.EXTRA_TEXT, body);
            shareIntent.putExtra(Intent.EXTRA_MIME_TYPES, "application/json");
            startActivity(Intent.createChooser(shareIntent, "Share via..."));
        } catch (UnsupportedEncodingException e) {
            Log.e("EXCEPTION", Log.getStackTraceString(e));
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void showInputDialog(String title, String hintText, Consumer<String> callback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(hintText);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> callback.accept(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showMessage(String title, String message, Runnable onAcceptCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        WrapTextViewBinding wrapText = WrapTextViewBinding.inflate(getLayoutInflater());
        wrapText.textView.setText(message);
        builder.setView(wrapText.getRoot());

        builder.setPositiveButton("OK", (dialog, which) -> onAcceptCallback.run());
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    public void showErrorSnackbar(String errorMessage) {
        Snackbar.make(activityMainBinding.getRoot(), errorMessage, Snackbar.LENGTH_LONG)
                .setAction("OK", view -> {
                }).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onAddTitleClicked(View view) {
        showInputDialog("Add Title", "Enter title name here",
                str -> listAdapterController.addTitle(new Title(str)));
    }

    @Override
    public void onBackPressed() {
        Object o = controller.get(sceneManager.getCurrentScene());
        if (o instanceof BackEventListener)
            ((BackEventListener) o).onBackPressed();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void save() {
        try (FileOutputStream out = openFileOutput("data.json", Context.MODE_PRIVATE)) {
            listAdapterController.saveTo(out);
        } catch (IOException ex) {
            Log.e("EXCEPTION", Log.getStackTraceString(ex));
        }
    }

    public Scene getMainScene() {
        return mainScene;
    }

    public Scene getEditTitleScene() {
        return editTitleScene;
    }

    public EditTitleSceneController getEditTitleSceneController() {
        return editTitleSceneController;
    }

    public SceneManager getSceneManager() {
        return sceneManager;
    }

    public DividerItemDecoration getRecyclerViewDivider() {
        return recyclerViewDivider;
    }

    public ColorStateList getPrimaryTextColor() {
        return primaryTextColor;
    }
}