package com.example.pamir.movienight;


import com.example.pamir.movienight.search.SearchResult;

import android.content.Context;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


import com.example.pamir.movienight.adapters.ResultsAdapter;
import com.example.pamir.movienight.search.Genre;
import com.example.pamir.movienight.search.SearchResults;
import com.example.pamir.movienight.search.Year;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
/**
 * Created by pamir on 12-Mar-18.
 */


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String MOVIE_URL = "discover/movie/?api_key=";
    public static final String TV_URL = "discover/tv?api_key=";

    private String mResults;
    private String mUrl;
    private SearchResults mSearchResults;
    private ResultsAdapter mAdapter;

    private boolean mCanLoadNewMovies;

    private EditText mSearchBox;
    private LinearLayout mFilterOptionsLayout;
    private LinearLayout mSortLayout;
    private LinearLayout mSortScrollLayout;
    private LinearLayout mFilterLayout;
    private CheckedTextView mFilterButton;
    private LinearLayout mGenreLayout;
    private CheckedTextView mSortButton;

    public boolean mIsFilterOpen;

    // genres
    private List<Genre> mGenreList;
    private int mSelectedGenre;
    private int mSelectedSort;

    // release year
    private LinearLayout mReleaseYearLayout;
    private int mSelectedReleaseYear;
    private List<Year> mYearList;

    private Switch mSourceSwitch;
    private ProgressBar mLoadingNewSearchIcon;
    private boolean mIsMovies;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;

    // API stuff
    public static final String API_URL = "https://api.themoviedb.org/3/";
    public static final String API_KEY = "e924bfb7ddb531cb8116f491052edfdd";
    public static final OkHttpClient client = new OkHttpClient();
    public Call mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSearchBox = (EditText) findViewById(R.id.searchBoxEditText);
        mSearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0) {
                    mFilterLayout.setVisibility(View.GONE);
                } else {
                    mFilterLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mSourceSwitch = (Switch) findViewById(R.id.sourceSwitch);
        mSourceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Switch sourceSwitch = (Switch) buttonView;
                String text = isChecked ? sourceSwitch.getTextOn().toString() : sourceSwitch.getTextOff().toString();
                buttonView.setText(text);
                mIsMovies = isChecked;
                getGenreList();
                changeSortMethod();
            }
        });
        mSourceSwitch.setChecked(true);
        mLoadingNewSearchIcon = (ProgressBar) findViewById(R.id.loadingNewSearchIcon);
        mIsMovies = true;
        mFilterOptionsLayout = (LinearLayout) findViewById(R.id.filterOptionsLayout);
        mFilterOptionsLayout.setVisibility(View.GONE);
        mSortScrollLayout = (LinearLayout) findViewById(R.id.sortScrollLayout);
        mSortLayout = (LinearLayout) findViewById(R.id.sortLayout);
        mSortLayout.setVisibility(View.GONE);
        mFilterLayout = (LinearLayout) findViewById(R.id.filterLayout);
        mFilterButton = (CheckedTextView) findViewById(R.id.filterButton);
        mSortButton = (CheckedTextView) findViewById(R.id.sortButton);

        mIsFilterOpen = false;
        // prepare genre list
        mGenreLayout = (LinearLayout) findViewById(R.id.genreLayout);
        mSelectedGenre = -1;

        mReleaseYearLayout = (LinearLayout) findViewById(R.id.releaseYearLayout);
        mSelectedReleaseYear = -1;

        mRecyclerView = (RecyclerView) findViewById(R.id.resultsRecyclerView);
        mGridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);
        mCanLoadNewMovies = true;
        getYearList();
        getGenreList();
        getMovieList();
    }

    private void getYearList() {
        Calendar calendar = Calendar.getInstance();
        int curYear = calendar.get(Calendar.YEAR);
        mYearList = new ArrayList<>();
        // first get the current year and last year
        mYearList.add(new Year(curYear--));
        mYearList.add(new Year(curYear--));
        // next get years until 1960
        int endYear = (curYear / 10) * 10;
        endYear = (endYear != curYear) ? endYear : endYear - 10;
        do {
            mYearList.add(new Year(curYear, endYear));
            curYear = endYear;
            endYear -= 10;
        } while (endYear >= 1960);
        mYearList.add(new Year(curYear, 1900, "pre-1960"));

        int i = 0;
        for (Year year : mYearList) {
            CheckedTextView button = new CheckedTextView(MainActivity.this);
            button.setText(year.getTitle());
            button.setTextColor(getResources().getColorStateList(R.color.genre_button_text));
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                button.setBackground(getResources().getDrawable(R.drawable.genre_button));
            }
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            int marginH = (int) TypedValue.applyDimension(
                    1,
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDisplayMetrics());
            int marginV = (int) TypedValue.applyDimension(
                    2,
                    TypedValue.COMPLEX_UNIT_DIP,
                    getResources().getDisplayMetrics());
            lp.setMargins(marginH, marginV, marginH, marginV);
            button.setLayoutParams(lp);
            button.setOnClickListener(MainActivity.this);
            button.setTag(i++);
            mReleaseYearLayout.addView(button);
        }
    }

    private void getGenreList() {
        CheckedTextView allButton = (CheckedTextView) findViewById(R.id.allButton);
        allButton.setChecked(true);
        mGenreLayout.removeAllViews();
        mGenreLayout.addView(allButton);
        if (isNetworkAvailable()) {
            String url = API_URL + "genre/" + (mIsMovies ? "movie" : "tv") + "/list?api_key=" + API_KEY;
            Log.d(TAG, url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Error: " + response);
                    } else {
                        try {
                            unpackGenres(response.body().string());
                        } catch (JSONException jse) {
                            Log.d(TAG, "Error parsing JSON data");
                            jse.printStackTrace();
                        }
                    }

                }
            });
        }
    }

    private void unpackGenres(String string) throws JSONException {
        mGenreList = new ArrayList<>();
        JSONObject jsonResult = new JSONObject(string);
        JSONArray genreResults = jsonResult.getJSONArray("genres");
        for (int i = 0; i < genreResults.length(); i++) {
            JSONObject genre = genreResults.getJSONObject(i);
            int id = genre.getInt("id");
            String name = genre.getString("name");
            mGenreList.add(new Genre(name, id));
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (Genre genre : mGenreList) {
                    CheckedTextView button = new CheckedTextView(MainActivity.this);
                    button.setText(genre.getName());
                    button.setTextColor(getResources().getColorStateList(R.color.genre_button_text));
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        button.setBackground(getResources().getDrawable(R.drawable.genre_button));
                    }
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    int marginH = (int) TypedValue.applyDimension(
                            1,
                            TypedValue.COMPLEX_UNIT_DIP,
                            getResources().getDisplayMetrics());
                    int marginV = (int) TypedValue.applyDimension(
                            2,
                            TypedValue.COMPLEX_UNIT_DIP,
                            getResources().getDisplayMetrics());
                    lp.setMargins(marginH, marginV, marginH, marginV);
                    button.setLayoutParams(lp);
                    button.setOnClickListener(MainActivity.this);
                    button.setTag(genre.getId());
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                    mGenreLayout.addView(button);
                }
            }
        });
    }

    // toggle button state for the CheckedTextView buttons
    public void onClick(View view) {
        CheckedTextView button = (CheckedTextView) view;
        LinearLayout listHolder = (LinearLayout) view.getParent();
        int numSubViews = listHolder.getChildCount();
        for (int i = 0; i < numSubViews; i++) {
            CheckedTextView b = (CheckedTextView) listHolder.getChildAt(i);
            b.setChecked(false);
        }
        button.setChecked(true);
        int tag = Integer.parseInt(button.getTag().toString());
        LinearLayout l1 = listHolder;
        LinearLayout l2 = mSortLayout;

        if (listHolder == mGenreLayout) {
            Log.d(TAG, "Genre changed");
            mSelectedGenre = tag;
        } else if (listHolder == mReleaseYearLayout) {
            Log.d(TAG, "Release Year changed");
            mSelectedReleaseYear = tag;
        } else if (listHolder == mSortScrollLayout) {
            Log.d(TAG, "Sort type changed");
            mSelectedSort = tag;
            changeSortMethod();
        }
    }

    private void changeSortMethod() {
        Log.d(TAG, mUrl);
        View v = new View(this);
        newSearch(v);
    }

    private String getSortUrl() {
        String sortBy = "&sort_by=";
        String sortOrder = ".desc";
        if (mUrl.contains(sortBy)) {
            int sortStart = mUrl.indexOf(sortBy);
            mUrl = mUrl.substring(0, sortStart);
        }

        switch (mSelectedSort) {
            case 0:
                sortBy += "popularity";
                break;
            case 1:
                sortBy += "vote_average";
                break;
            case 2:
                sortBy += "vote_count";
                break;
            case 3:
                sortBy += "release_date";
                break;
            case 4:
                sortBy += "revenue";
                break;
            default:
                sortBy="";
        }
        return sortBy + sortOrder;
    }

    // default just pulls page one
    public void getMovieList() {
        String url = API_URL + (mIsMovies ? MOVIE_URL : TV_URL) + API_KEY;
        getMovieList(url, 1, true);
    }

    // pulls url and refreshes screen if it's a new search
    public void getMovieList(String url, int page, final boolean isNewSearch) {
        if(mCall != null) {
            mCall.cancel();
        }
        mUrl = url;
        if (isNetworkAvailable()) {
            mLoadingNewSearchIcon.setVisibility(View.VISIBLE);
            url += "&page=" + page;
            Log.d(TAG, url);
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            mCall = client.newCall(request);
            mCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d(TAG, "Seems there was an error with the URL.");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        throw new IOException("Error: " + response);
                    } else {
                        try {
                            mCanLoadNewMovies = true;
                            mResults = response.body().string();
                            Log.d(TAG, mResults);
                            if (isNewSearch)
                                mAdapter = null;
                            unpackResults(mResults);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mLoadingNewSearchIcon.setVisibility(View.GONE);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // update activity with data
                        updateActivity();
                    }
                }
            });
        }
    }

    private void updateActivity() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                        super.onScrolled(recyclerView, dx, dy);

                        if (mCanLoadNewMovies) {
                            if (dy > 0) //check for scroll down
                            {
                                int visibleItemCount = mGridLayoutManager.getChildCount();
                                int totalItemCount = mGridLayoutManager.getItemCount();
                                int pastVisiblesItems = mGridLayoutManager.findFirstVisibleItemPosition();

                                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount - 4) {
                                    mCanLoadNewMovies = false;
                                    Log.v(TAG, "Load new movies");
                                    getMovieList(mUrl, mSearchResults.getCurPage() + 1, false);
                                }
                            }
                        }

                    }
                });
            }
        });
    }

    private void unpackResults(String results) throws JSONException {
        JSONObject jsonObject = new JSONObject(results);
        JSONArray jsonArray = jsonObject.getJSONArray("results");

        int curPage = jsonObject.getInt("page");
        int totalResults = jsonObject.getInt("total_results");
        int totalPages = jsonObject.getInt("total_pages");
        mSearchResults = new SearchResults();
        mSearchResults.setCurPage(curPage);
        mSearchResults.setNumPages(totalPages);
        mSearchResults.setTotalResults(totalResults);

        for (int i = 0; i < jsonArray.length(); i++) {
            // these objects change depending on whether we're looking at movies or TV shows
            String TITLE = mIsMovies ? "title" : "name";
            String DATE = mIsMovies ? "release_date" : "first_air_date";
            // extract json object into searchresult object
            JSONObject movie = jsonArray.getJSONObject(i);
            String posterPath = movie.getString("poster_path");
            String title = movie.getString(TITLE);
            String originalLanguage = movie.getString("original_language");
            String originalTitle = movie.getString("original_" + TITLE);
            String overview = movie.getString("overview");
            String releaseDate = movie.getString(DATE);
            boolean isAdult = mIsMovies && movie.getBoolean("adult");
            int id = movie.getInt("id");
            int voteCount = movie.getInt("vote_count");
            // pull out the genre ids
            JSONArray genre_ids = movie.getJSONArray("genre_ids");
            int[] genreIds = new int[genre_ids.length()];
            for (int j = 0; j < genre_ids.length(); j++) {
                genreIds[j] = genre_ids.getInt(j);
            }
            // continue parsing rest of data
            double popularityRating = movie.getDouble("popularity");
            double voteAverage = movie.getDouble("vote_average");
            SearchResult result = new SearchResult(
                    posterPath,
                    title,
                    originalLanguage,
                    originalTitle,
                    overview,
                    releaseDate,
                    isAdult,
                    id,
                    voteCount,
                    genreIds,
                    popularityRating,
                    voteAverage);

            mSearchResults.addSearchResult(result);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null) {
                    mAdapter.addAll(mSearchResults.getSearchResults());
                    mAdapter.notifyDataSetChanged();
                } else {
                    mAdapter = new ResultsAdapter(mSearchResults.getSearchResults(), MainActivity.this);
                    mRecyclerView.setAdapter(mAdapter);
                }
            }
        });
        Log.d(TAG, mSearchResults.toString());
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public void newSearch(View v) {
        Log.d(TAG, "Search");
        mFilterOptionsLayout.setVisibility(View.GONE);
        String searchQuery = mSearchBox.getText().toString();
        String url = "";
        if (searchQuery.length() > 0) {
            url = API_URL + "search/movie?api_key=" + API_KEY + "&query=" + searchQuery;
        } else {
            // check genre
            String genreAttr = "";
            if (mSelectedGenre >= 0) {
                genreAttr = "&with_genres=" + mSelectedGenre;
            }
            // year
            String yearAttr = "";
            if (mSelectedReleaseYear >= 0) {
                Year year = mYearList.get(mSelectedReleaseYear);
                Log.d(TAG, year.getTitle());
                if (year.getLowYear() == 0) {
                    yearAttr = "&primary_release_year=" + year.getHighYear();
                } else {
                    yearAttr = "&primary_release_date.gte=" + year.getLowYear() + "-1-1&primary_release_date.lte=" + (year.getHighYear() - 1) + "-12-31";
                }
            }
            // rating
            String ratingAttr = "";
            float rating = ((RatingBar) findViewById(R.id.ratingBar)).getRating();
            ratingAttr = "&vote_average.gte=" + rating;
            // vote count
            String voteCount = "";
            String numVotes = ((EditText) findViewById(R.id.voteCountEditText)).getText().toString();
            Log.d(TAG, "number of votes: " + numVotes);
            if (!numVotes.equals("")) {
                voteCount = "&vote_count.gte=" + Integer.parseInt(numVotes);
            }
            // build string
            url = API_URL + (mIsMovies ? MOVIE_URL : TV_URL) + API_KEY + genreAttr + yearAttr + ratingAttr + voteCount + getSortUrl();
        }
        getMovieList(url, 1, true);
    }

    public void toggleFilters(View v) {
        hideSort();
        int visibility = mFilterOptionsLayout.getVisibility();
        if (visibility == View.GONE) {
            showFilters();
        } else {
            hideFilters();
        }
    }

    private void showFilters() {
        mFilterOptionsLayout.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterLayout.setBackground(getResources().getDrawable(R.drawable.filter_box_expanded));
        }
        mFilterButton.setTypeface(null, Typeface.BOLD);
        mIsFilterOpen = true;
    }

    public void hideFilterSort() {
        hideFilters();
        hideSort();
    }

    private void hideFilters() {
        mFilterOptionsLayout.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterLayout.setBackground(getResources().getDrawable(R.drawable.filter_box_collapsed));
        }
        mFilterButton.setTypeface(null, Typeface.NORMAL);
        mIsFilterOpen = false;
    }

    public void toggleSort(View v) {
        hideFilters();
        int visibility = mSortLayout.getVisibility();
        if (visibility == View.GONE) {
            showSort();
        } else {
            hideSort();
        }
    }

    private void hideSort() {
        mSortLayout.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterLayout.setBackground(getResources().getDrawable(R.drawable.filter_box_collapsed));
        }
        mSortButton.setTypeface(null, Typeface.NORMAL);
        mIsFilterOpen = false;
    }

    private void showSort() {
        mSortLayout.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mFilterLayout.setBackground(getResources().getDrawable(R.drawable.filter_box_expanded));
        }
        mSortButton.setTypeface(null, Typeface.BOLD);
        mIsFilterOpen = true;
    }
}
