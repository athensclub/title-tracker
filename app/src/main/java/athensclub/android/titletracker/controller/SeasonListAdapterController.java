package athensclub.android.titletracker.controller;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import athensclub.android.titletracker.MainActivity;
import athensclub.android.titletracker.data.Title;
import athensclub.android.titletracker.view.SeasonListAdapter;

public class SeasonListAdapterController {

    private SeasonListAdapter adapter;

    private SortedMap<Integer, SortedSet<Integer>> data;
    private List<Map.Entry<Integer, SortedSet<Integer>>> listData;

    public SeasonListAdapterController(SeasonListAdapter adapter) {
        this.adapter = adapter;
        data = new TreeMap<>();
        listData = new ArrayList<>();
    }

    public int getSeasonCount() {
        return listData.size();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addSeason(int season) {
        if (data.containsKey(season)) {
            Snackbar.make(adapter.getRecyclerView(), "That season is already in the list. It will be ignored.", Snackbar.LENGTH_LONG)
                    .setAction("OK", view -> {
                    }).show();
            return;
        }
        data.put(season, new TreeSet<>());
        updateListData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addAllSeason(int begin, int end) {
        for (int x = begin; x <= end; x++)
            data.putIfAbsent(x, new TreeSet<>());
        updateListData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void bind(SortedMap<Integer, SortedSet<Integer>> info) {
        data.clear();
        data.putAll(info);
        updateListData();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onBindViewHolder(@NonNull SeasonListAdapter.SeasonViewHolder holder, int position) {
        Map.Entry<Integer, SortedSet<Integer>> current = listData.get(position);
        String text = Title.makeRangeText(current.getValue());
        holder.setData(current.getKey(), text.isEmpty() ? "None" : text);

        holder.getBinding().addEpisode.setOnClickListener(view -> {
            MainActivity mainActivity = adapter.getMainActivity();
            mainActivity.showInputDialog("Add Finished Episodes", "Enter number/range here", str -> {
                Title.RangeOrNumberParseResult result = Title.parseRangeOrNumber(str);
                if (result.hasError) {
                    mainActivity.showErrorSnackbar(result.error);
                    return;
                }

                SortedSet<Integer> currentFinished = current.getValue();
                Title.UnionIntList val = result.result;
                if (val.isInt)
                    currentFinished.add(val.intValue);
                else
                    for (int i = val.listValue.get(0); i <= val.listValue.get(1); i++)
                        currentFinished.add(i);
                adapter.notifyItemChanged(position);
            });
        });

        holder.getBinding().removeEpisode.setOnClickListener(view -> {
            MainActivity mainActivity = adapter.getMainActivity();
            mainActivity.showInputDialog("Add Finished Episodes", "Enter number/range here", str -> {
                Title.RangeOrNumberParseResult result = Title.parseRangeOrNumber(str);
                if (result.hasError) {
                    mainActivity.showErrorSnackbar(result.error);
                    return;
                }

                SortedSet<Integer> currentFinished = current.getValue();
                Title.UnionIntList val = result.result;
                if (val.isInt)
                    currentFinished.remove(val.intValue);
                else
                    for (int i = val.listValue.get(0); i <= val.listValue.get(1); i++)
                        currentFinished.remove(i);
                adapter.notifyItemChanged(position);
            });
        });

        holder.getBinding().deleteSeason.setOnClickListener(view -> {
            MainActivity mainActivity = adapter.getMainActivity();
            mainActivity.showMessage("Are you sure?", "Are you sure you want to delete season " + current.getKey() + " from the list?", () -> {
                data.remove(current.getKey());
                updateListData();
            });
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateListData() {
        int prevSize = listData.size();
        listData.clear();
        listData.addAll(data.entrySet().stream().collect(Collectors.toList()));
        adapter.notifyDataSetChanged();
    }

    public SortedMap<Integer, SortedSet<Integer>> getData() {
        return data;
    }
}
