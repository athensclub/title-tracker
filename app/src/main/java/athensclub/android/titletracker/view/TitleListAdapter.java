package athensclub.android.titletracker.view;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import athensclub.android.titletracker.MainActivity;
import athensclub.android.titletracker.controller.TitleListAdapterController;
import athensclub.android.titletracker.data.Title;
import athensclub.android.titletracker.databinding.TitleItemViewBinding;

public class TitleListAdapter extends RecyclerView.Adapter<TitleListAdapter.TitleViewHolder> {

    private TitleListAdapterController controller;

    private RecyclerView recyclerView;

    private MainActivity mainActivity;

    public TitleListAdapter(MainActivity mainActivity, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.mainActivity = mainActivity;

        controller = new TitleListAdapterController(this);
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public TitleListAdapterController getController() {
        return controller;
    }

    @NonNull
    @Override
    public TitleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TitleItemViewBinding binding = TitleItemViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        TitleViewHolder vh = new TitleViewHolder(binding, mainActivity);
        return vh;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull TitleViewHolder holder, int position) {
        controller.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemCount() {
        return controller.getTitleCount();
    }

    public static class TitleViewHolder extends RecyclerView.ViewHolder {

        private TitleItemViewBinding binding;

        private MainActivity mainActivity;

        public TitleViewHolder(@NonNull TitleItemViewBinding itemView, MainActivity mainActivity) {
            super(itemView.getRoot());
            binding = itemView;
            this.mainActivity = mainActivity;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void setData(Title title) {
            binding.titleName.setText(title.getTitle());

            String episodeText = "Finished Episodes: ";
            String episode = title.unfinishedEpisodeText();
            String[] parts = episode.split("\n");
            binding.episodeText.removeAllViews();

            for (String str : parts) {
                TextView view = new TextView(binding.episodeText.getContext());
                view.setText(str.equals("None") ? "None" : "  - " + str);
                view.setTextColor(mainActivity.getPrimaryTextColor());
                binding.episodeText.addView(view);
            }

            binding.seasons.setText("Watched Seasons: " + title.finishedSeasonText());
        }

        public TitleItemViewBinding getBinding() {
            return binding;
        }
    }

}
