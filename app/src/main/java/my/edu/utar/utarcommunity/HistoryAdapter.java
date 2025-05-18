package my.edu.utar.utarcommunity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<String> historyItems;
    private boolean isExpanded = false;
    private OnTextClickListener onTextClickListener;

    public HistoryAdapter(List<String> historyItems, OnTextClickListener onHistoryClickListener) {
        this.historyItems = historyItems;
        this.onTextClickListener = onHistoryClickListener;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        String historyItem = historyItems.get(position);
        holder.historyTextView.setText(historyItem);
        holder.historyTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTextClickListener.onTextClicked(historyItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (isExpanded || historyItems.size() <= SearchFragment.MAX_HISTORY_ITEMS_IN_VIEW_LESS) {
            return historyItems.size();
        }
        return SearchFragment.MAX_HISTORY_ITEMS_IN_VIEW_LESS;
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView historyTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            historyTextView = itemView.findViewById(R.id.history_text);
        }
    }
}