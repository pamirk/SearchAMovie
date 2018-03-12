package com.example.pamir.movienight.search;

/**
 * Created by pamir on 12-Mar-18.
 */

public class Genre {
    String name;
    int id;

    public Genre(String name, int id) {
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
