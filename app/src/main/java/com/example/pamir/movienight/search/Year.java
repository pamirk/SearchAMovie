package com.example.pamir.movienight.search;

/**
 * Created by pamir on 12-Mar-18.
 */


public class Year {
    private int mHighYear;
    private int mLowYear;
    private String mTitle;

    public Year(int highYear, int lowYear, String title) {
        mHighYear = highYear;
        mLowYear = lowYear;
        mTitle = title;
    }

    public Year(int startYear, int endYear) {
        mHighYear = startYear;
        mLowYear = endYear;
        if (endYear < 2000) {
            mTitle = endYear - 1900 + "s";
        } else {
            mTitle = startYear + "-" + endYear;
        }
    }

    public Year(int highYear) {
        mHighYear = highYear;
        mLowYear = 0;
        mTitle = highYear + "";
    }

    public int getHighYear() {
        return mHighYear;
    }

    public int getLowYear() {
        return mLowYear;
    }

    public String getTitle() {
        return mTitle;
    }
}
