package athensclub.android.titletracker.view;

import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import athensclub.android.titletracker.MainActivity;
import athensclub.android.titletracker.controller.SeasonListAdapterController;
import athensclub.android.titletracker.databinding.SeasonItemViewBinding;

public class SeasonListAdapter extends RecyclerView.Adapter<SeasonListAdapter.SeasonViewHolder> {

    private SeasonListAdapterController controller;

    private RecyclerView recyclerView;

    private MainActivity mainActivity;

    public SeasonListAdapter(MainActivity mainActivity, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.mainActivity = mainActivity;

        controller = new SeasonListAdapterController(this);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }

    public SeasonListAdapterController getController() {
        return controller;
    }

    @NonNull
    @Override
    public SeasonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("DEBUG", "ON CREATE VIEW HOLDER CALLED");
        SeasonItemViewBinding binding = SeasonItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        SeasonViewHolder vh = new SeasonViewHolder(binding);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull SeasonViewHolder holder, int position) {
        Log.d("DEBUG", "ON BIND VIEW HOLDER CALLED");
        controller.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return controller.getSeasonCount();
    }

    public static class SeasonViewHolder extends RecyclerView.ViewHolder {

        private SeasonItemViewBinding binding;

        public SeasonViewHolder(@NonNull SeasonItemViewBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;
        }

        public void setData(int season, String finishedEpisodeText) {
            binding.seasonNumber.setText("Season " + season);
            binding.finishedEpisode.setText("Finished Episodes: " + finishedEpisodeText);
        }

        public SeasonItemViewBinding getBinding() {
            return binding;
        }
    }
}
