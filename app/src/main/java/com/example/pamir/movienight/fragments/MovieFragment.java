package com.example.pamir.movienight.fragments;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.pamir.movienight.MainActivity;
import com.example.pamir.movienight.R;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static com.example.pamir.movienight.search.SearchResult.IMAGE_URL_BASE;

/**
 * Created by pamir on 12-Mar-18.
 */

public class MovieFragment extends DialogFragment {
    public final static String TAG = MovieFragment.class.getSimpleName();
    public static final String TITLE = "title";
    public static final String OVERVIEW = "overview";
    public static final String POSTERURL = "posterurl";
    public static final String RELEASE_DATE = "releasedate";
    public static final String RATING = "rating";
    public static final String VOTE_COUNT = "votecount";

    private ImageView mPosterImageView;
    private Call mCall;

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = layoutInflater.inflate(R.layout.movie_fragment, container);
        TextView titleTextView = (TextView) rootView.findViewById(R.id.movieTitleLabel);
        TextView summaryTextView = (TextView) rootView.findViewById(R.id.movieSummaryLabel);
        TextView releaseDateTextView = (TextView) rootView.findViewById(R.id.releaseDateLabel);
        TextView voteCountTextView = (TextView) rootView.findViewById(R.id.voteCountLabel);
        RatingBar ratingBar = (RatingBar) rootView.findViewById(R.id.ratingBar);
        mPosterImageView = (ImageView) rootView.findViewById(R.id.movieImageView);

        Bundle bundle = getArguments();
        String title = bundle.getString(TITLE);
        String overview = bundle.getString(OVERVIEW);
        String posterUrl = bundle.getString(POSTERURL);
        String releaseDate = bundle.getString(RELEASE_DATE);
        double rating = bundle.getDouble(RATING);
        int voteCount = bundle.getInt(VOTE_COUNT);

        String url = IMAGE_URL_BASE + "500" + posterUrl;
        Log.d(TAG,url);

        titleTextView.setText(title);
        summaryTextView.setText(overview);
        releaseDateTextView.setText(releaseDate);
        ratingBar.setRating((float)rating);
        String votes = voteCount + " vote" + (voteCount == 1 ? "" : "s");
        voteCountTextView.setText(votes);

        Request request = new Request.Builder()
                .url(url)
                .build();
        mCall = MainActivity.client.newCall(request);
        mCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"Failed loading fragment poster");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()) {
                    Log.d(TAG,"Success loading fragment poster");
                    InputStream input = response.body().byteStream();
                    final Bitmap poster = BitmapFactory.decodeStream(input);
                    if (poster != null) {
                        try {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mPosterImageView.setImageBitmap(poster);
                                }
                            });
                        } catch (NullPointerException npe) {
                            Log.d(TAG, "Window was closed before poster loaded");
                            npe.printStackTrace();
                        }
                    }
                }
            }
        });


        return rootView;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "Cancelling call " + mCall);
        mCall.cancel();
    }
}
