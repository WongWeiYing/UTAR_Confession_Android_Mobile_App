package my.edu.utar.utarcommunity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment implements OnTextClickListener {
    private SharedPreferences sharedPreferences;
    private final static String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InN2Zm5qdndpcnRrbHd1dGxyeWVxIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MTI1NTcxOTUsImV4cCI6MjAyODEzMzE5NX0.kapUf05gM8cop4mtu15xwb5K6X3vsXYd341Fjh0EFxM";

    // Search bar
    private ImageButton backButton;
    private EditText searchEditText;

    // Search history and topic
    private LinearLayout searchMetricsLayout;

    // Search history
    public final static int MAX_HISTORY_ITEMS_IN_VIEW_LESS = 5;
    private Set<String> searchHistorySet;
    private ArrayList<String> searchHistories = new ArrayList<>();
    private HistoryAdapter historyAdapter;
    private TextView historyEmptyTextView;

    // Trending topic
    private LinearLayout trendingTopicsLayout;
    private LinearLayout searchResultsLayout;
    private ArrayList<String> trendingTopics = new ArrayList<>();


    // Search result
    private RecyclerView resultsRecyclerView;
    private TextView resultEmptyTextView;
    private ListView trendingTopicsListView;
    private ArrayList<SearchPost> resultPosts = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup search bar section
        backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideResultPosts();
            }
        });

        searchEditText = view.findViewById(R.id.search_text);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    String searchText = v.getText().toString().trim();
                    if (searchText.length() > 0)
                        searchPosts(searchText);
                    return true;
                }
                return false;
            }
        });

        searchMetricsLayout = view.findViewById(R.id.search_metrics_layout);

        // Setup history section
        // Get history from shared preferences
        sharedPreferences = getActivity().getSharedPreferences("app", Context.MODE_PRIVATE);
        searchHistorySet = new HashSet<>(sharedPreferences.getStringSet("searchHistory", new HashSet<String>()));
        searchHistories = new ArrayList<>(searchHistorySet);
        historyEmptyTextView = view.findViewById(R.id.search_history_empty);
        RecyclerView historyRecyclerView = view.findViewById(R.id.search_history);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(getContext());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        historyRecyclerView.setLayoutManager(layoutManager);
        historyAdapter = new HistoryAdapter(searchHistories, this);
        historyRecyclerView.setAdapter(historyAdapter);
        // History view more/less button
        Button viewMoreButton = view.findViewById(R.id.view_more_history);
        viewMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (historyAdapter.isExpanded()) {
                    viewMoreButton.setText("View More");
                    historyAdapter.setExpanded(false);
                } else {
                    viewMoreButton.setText("View Less");
                    historyAdapter.setExpanded(true);
                }
                historyAdapter.notifyDataSetChanged();
            }
        });
        if (searchHistories.size() <= MAX_HISTORY_ITEMS_IN_VIEW_LESS) {
            viewMoreButton.setVisibility(View.GONE);
        } else {
            viewMoreButton.setVisibility(View.VISIBLE);
        }
        // History clear button
        Button clearButton = view.findViewById(R.id.clear_history);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearchHistory();
            }
        });
        if (searchHistories.isEmpty()) {
            historyEmptyTextView.setVisibility(View.VISIBLE);
        }

        // Setup trending topics section
        trendingTopicsLayout = view.findViewById(R.id.topic_layout);
        trendingTopicsListView = view.findViewById(R.id.trending_topics);
        // Get trending topics from database
        getTrendingTopics();

        // Setup result section
        searchResultsLayout = view.findViewById(R.id.search_results_layout);
        resultsRecyclerView = view.findViewById(R.id.search_results);
        resultEmptyTextView = view.findViewById(R.id.search_results_empty);

        hideResultPosts();
    }

    private void searchPosts(String searchText) {
        String trimmedText = searchText.trim();
        Log.d("SearchFragment", "searchPosts: ");
        historyEmptyTextView.setVisibility(View.GONE);
        resultPosts.clear();
        saveSearchHistory(trimmedText);
        searchPostsByText(trimmedText);
        Snackbar.make(getView(), "Searching " + trimmedText, Snackbar.LENGTH_SHORT).show();

        // Hide keyboard
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
    }

    private void saveSearchHistory(String searchText) {
        searchHistorySet.add(searchText);
        if (!searchHistories.contains(searchText)) {
            searchHistories.add(searchText);
            // Update ui
            historyAdapter.notifyDataSetChanged();
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("searchHistory", searchHistorySet);
        editor.apply();
    }

    private void clearSearchHistory() {
        historyEmptyTextView.setVisibility(View.VISIBLE);
        searchHistorySet.clear();
        searchHistories.clear();
        historyAdapter.notifyDataSetChanged();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet("searchHistory", searchHistorySet);
        editor.apply();
    }

    private void searchPostsByText(String searchText) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String queryText = URLEncoder.encode(searchText, "UTF-8");
                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("app", Context.MODE_PRIVATE);
                    String userID = sharedPreferences.getString("userID", "");
                    String sqlQuery = "search_posts?search_text=" + queryText + "&current_user_id=" + userID;
                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/rpc/" + sqlQuery);
                    // Get data from database
                    JSONArray response = requestAPI(url);
                    parseResultPostResponse(response);
                    // Update UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showResultPosts();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getTrendingTopics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://svfnjvwirtklwutlryeq.supabase.co/rest/v1/rpc/get_weekly_trending_topics");
                    // Get data from database
                    JSONArray response = requestAPI(url);
                    parseTrendingTopicsResponse(response);
                    // Update UI
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showTopics();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private JSONArray requestAPI(URL url) {
        try {
            HttpURLConnection hc = (HttpURLConnection) url.openConnection();
            hc.setRequestProperty("apikey", API_KEY);
            hc.setRequestProperty("Authorization", "Bearer " + API_KEY);
            InputStream is = hc.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            is.close();
            hc.disconnect();
            return new JSONArray(jsonString);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void parseResultPostResponse(JSONArray jsonArray) throws JSONException, ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            SearchPost post = new SearchPost();
            post.setPost_ID(jo.getInt("post_ID"));
            post.setPost_Caption(jo.getString("post_Caption"));
            post.setPost_Tags(jo.getString("post_Tags"));
            post.setPost_Like(jo.getInt("post_Like"));
            post.setPost_Comment(jo.getInt("post_Comment"));
            post.setPost_Created(sdf.parse(jo.getString("post_Created")));
            post.setPost_isAnonymous(jo.getBoolean("post_isAnonymous"));
            post.setUser_id(jo.getString("user_id"));
            post.setUser_Name(jo.getString("user_Name"));
            post.setLiked_ByUser(jo.getBoolean("liked_By_User"));
            Log.i("Search Fragment", "post: " + i + " " + post);
            resultPosts.add(post);
        }
    }

    private void showResultPosts() {
        backButton.setVisibility(View.VISIBLE);
        searchMetricsLayout.setVisibility(View.GONE);
        searchResultsLayout.setVisibility(View.VISIBLE);

        if (resultPosts.isEmpty()) {
            resultsRecyclerView.setVisibility(View.GONE);
            resultEmptyTextView.setVisibility(View.VISIBLE);
        } else {
            resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            ResultAdapter resultAdapter = new ResultAdapter(resultPosts, this);
            resultsRecyclerView.setAdapter(resultAdapter);
            resultsRecyclerView.setVisibility(View.VISIBLE);
            resultEmptyTextView.setVisibility(View.GONE);
        }
    }

    private void hideResultPosts() {
        backButton.setVisibility(View.GONE);
        searchMetricsLayout.setVisibility(View.VISIBLE);
        searchResultsLayout.setVisibility(View.GONE);
    }

    private void parseTrendingTopicsResponse(JSONArray jsonArray) throws JSONException {
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jo = jsonArray.getJSONObject(i);
            trendingTopics.add(jo.getString("trimmed_hashtag"));
            Log.i("Search Fragment", "topics: " + i + " " + jo.getString("trimmed_hashtag"));
        }
    }

    private void showTopics() {
        hideResultPosts();

        if (trendingTopics.isEmpty()) {
            trendingTopicsLayout.setVisibility(View.GONE);
        } else {
            trendingTopicsLayout.setVisibility(View.VISIBLE);
            ArrayAdapter<String> trendingTopicsArrayAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, trendingTopics);
            trendingTopicsListView.setAdapter(trendingTopicsArrayAdapter);
            trendingTopicsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    onTextClicked(trendingTopicsArrayAdapter.getItem(position));
                }
            });
        }
    }

    @Override
    public void onTextClicked(String historyText) {
        searchEditText.setText(historyText);
        // Move the cursor to the end of the search text
        searchEditText.setSelection(searchEditText.getText().length());
        searchPosts(historyText);
    }
}