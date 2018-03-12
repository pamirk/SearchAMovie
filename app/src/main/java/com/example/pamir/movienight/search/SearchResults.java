package com.example.pamir.movienight.search;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pamir on 12-Mar-18.
 */


public class SearchResults {
    private int mCurPage;
    private int mNumPages;
    private int mTotalResults;
    private List<SearchResult> mSearchResults;

    public SearchResults() {
        mSearchResults = new ArrayList<>();
    }

    public void addSearchResult(SearchResult searchResult) {
        mSearchResults.add(searchResult);
    }


    public int getCurPage() {
        return mCurPage;
    }

    public void setCurPage(int curPage) {
        mCurPage = curPage;
    }

    public int getNumPages() {
        return mNumPages;
    }

    public void setNumPages(int numPages) {
        mNumPages = numPages;
    }

    public int getTotalResults() {
        return mTotalResults;
    }

    public void setTotalResults(int totalResults) {
        mTotalResults = totalResults;
    }

    public List<SearchResult> getSearchResults() {
        return mSearchResults;
    }

    @Override
    public String toString() {
        return "SearchResults{" +
                "mCurPage=" + mCurPage +
                ", mNumPages=" + mNumPages +
                ", mTotalResults=" + mTotalResults +
                ", mSearchResults=" + mSearchResults +
                '}';
    }
}
