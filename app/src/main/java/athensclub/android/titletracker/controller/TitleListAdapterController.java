package athensclub.android.titletracker.controller;

import android.os.Build;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import athensclub.android.titletracker.MainActivity;
import athensclub.android.titletracker.data.Title;
import athensclub.android.titletracker.view.TitleListAdapter;

public class TitleListAdapterController {

    private TitleListAdapter adapter;

    private List<Title> titles;

    private boolean shouldSave;

    public TitleListAdapterController(TitleListAdapter adapter) {
        this.adapter = adapter;
        titles = new ArrayList<>();
    }

    /**
     * Check whether the data of the title list need to be saved.
     *
     * @return
     */
    public boolean shouldSave() {
        return shouldSave;
    }

    public int getTitleCount() {
        return titles.size();
    }

    public void readFrom(InputStream stream) {
        try (JsonReader reader = new JsonReader(new InputStreamReader(stream))) {
            List<Title> results = new ArrayList<>();
            reader.beginArray();
            while (reader.hasNext()) {
                String titleName = null;
                SortedMap<Integer, SortedSet<Integer>> finished = new TreeMap<>();
                reader.beginObject();
                while(reader.hasNext()){
                    String name = reader.nextName();
                    switch (name) {
                        case "title_name":
                            titleName = reader.nextString();
                            break;
                        case "finished_episode":
                            reader.beginObject();
                            while(reader.hasNext()){
                                int season = Integer.parseInt(reader.nextName());
                                finished.put(season, new TreeSet<>());
                                reader.beginArray();
                                while (reader.hasNext())
                                    finished.get(season).add(reader.nextInt());
                                reader.endArray();
                            }
                            reader.endObject();
                            break;
                        default:
                            Log.d("DEBUG", "Unknown name while parsing data json file" + name);
                            reader.skipValue();
                    }
                }
                results.add(new Title(titleName, finished));
                reader.endObject();
            }
            Log.d("ATHENS DEBUG", String.valueOf(results));
            reader.endArray();
            addAllTitle(results);
        } catch (IOException e) {
            Log.e("EXCEPTION", Log.getStackTraceString(e));
        }
    }

    /**
     * Save the current title list to the given output stream.
     *
     * @param stream
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void saveTo(OutputStream stream) {
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(stream))) {
            writer.beginArray();
            for (Title t : titles) {
                writer.beginObject();
                writer.name("title_name").value(t.getTitle());

                writer.name("finished_episode");
                writer.beginObject();
                t.getFinishedEpisode().forEach((season, episodes) -> {
                    try {
                        writer.name(Integer.toString(season));
                        writer.beginArray();
                        for (int ep : episodes)
                            writer.value(ep);
                        writer.endArray();
                    } catch (IOException e) {
                        Log.e("Exception", Log.getStackTraceString(e));
                    }
                });
                writer.endObject();

                writer.endObject();
            }
            writer.endArray();
            shouldSave = false;
        } catch (IOException e) {
            Log.e("Exception", Log.getStackTraceString(e));
        }
    }

    public void addTitle(Title t) {
        if (titles.contains(t)) {
            adapter.getMainActivity().showErrorSnackbar("Title with that name already exists. It will be ignored.");
            return;
        }
        shouldSave = true;
        titles.add(t);
        adapter.notifyItemInserted(titles.size() - 1);
    }

    public void addAllTitle(Collection<Title> t) {
        int oldSize = titles.size();
        titles.addAll(t);
        int diff = titles.size() - oldSize;
        adapter.notifyItemRangeInserted(oldSize, diff);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBindViewHolder(@NonNull TitleListAdapter.TitleViewHolder holder, int position) {
        Title currentTitle = titles.get(position);
        holder.setData(currentTitle);

        holder.getBinding().editButton.setOnClickListener(view -> {
            MainActivity mainActivity = adapter.getMainActivity();
            mainActivity.getEditTitleSceneController().bind(currentTitle, (newTitle) -> {
                titles.set(position, newTitle);
                shouldSave = true;
                adapter.notifyItemChanged(position);
            });
            mainActivity.getSceneManager().setScene(mainActivity.getEditTitleScene());
        });

        holder.getBinding().deleteTitle.setOnClickListener(view -> {
            MainActivity mainActivity = adapter.getMainActivity();
            mainActivity.showMessage("Are you sure?", "Are you sure you want to delete title " + titles.get(position).getTitle() + " from the list?", () -> {
                shouldSave = true;
                titles.remove(position);
                adapter.notifyItemRemoved(position);
            });
        });
    }

}
