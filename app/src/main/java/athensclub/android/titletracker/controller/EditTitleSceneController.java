package athensclub.android.titletracker.controller;

import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

import athensclub.android.titletracker.BackEventListener;
import athensclub.android.titletracker.MainActivity;
import athensclub.android.titletracker.data.Title;
import athensclub.android.titletracker.databinding.EditTitleSceneBinding;
import athensclub.android.titletracker.view.SeasonListAdapter;

public class EditTitleSceneController implements BackEventListener {

    private EditTitleSceneBinding binding;

    private MainActivity mainActivity;

    private SeasonListAdapterController listAdapterController;

    private Title currentTitle;
    private Consumer<Title> onUpdateCallback;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public EditTitleSceneController(EditTitleSceneBinding binding, MainActivity mainActivity) {
        this.binding = binding;
        this.mainActivity = mainActivity;

        RecyclerView recyclerView = binding.unwatchedEpisode;

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(binding.getRoot().getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(mainActivity.getRecyclerViewDivider());

        SeasonListAdapter seasonListAdapter = new SeasonListAdapter(mainActivity, recyclerView);
        recyclerView.setAdapter(seasonListAdapter);
        listAdapterController = seasonListAdapter.getController();

        binding.floatingActionButton.setOnClickListener(view -> {
            onUpdateCallback.accept(new Title(currentTitle.getTitle(), listAdapterController.getData()));
            mainActivity.getSceneManager().setScene(mainActivity.getMainScene());
        });

        binding.addSeasonButton.setOnClickListener(view -> {
            String str = binding.addSeasonInput.getText().toString();
            Title.RangeOrNumberParseResult result = Title.parseRangeOrNumber(str);
            if(result.hasError){
                mainActivity.showErrorSnackbar(result.error);
                return;
            }

            Title.UnionIntList val = result.result;
            if(val.isInt)
                listAdapterController.addSeason(val.intValue);
            else
                listAdapterController.addAllSeason(val.listValue.get(0), val.listValue.get(1));
        });

    }

    /**
     * Bind this scene to the given title's copy and call the callback with the new user-edited
     * value of the title.
     *
     * @param title            the title that is going to be bind with this scene.
     * @param onUpdateCallback the callback that will be called when the user finished editing the title.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void bind(Title title, Consumer<Title> onUpdateCallback) {
        currentTitle = title;
        this.onUpdateCallback = onUpdateCallback;
        binding.titleName.setText(title.getTitle());
        listAdapterController.bind(title.getFinishedEpisode());
    }

    @Override
    public void onBackPressed() {
        mainActivity.getSceneManager().setScene(mainActivity.getMainScene());
    }
}
