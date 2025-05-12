package xyz.idaoteng.audiotag.api.netease.dto;

import java.util.ArrayList;

public class Result {
    private int songCount;
    private ArrayList<Song> songs;

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public ArrayList<Song> getSongs() {
        return songs;
    }

    public void setSongs(ArrayList<Song> songs) {
        this.songs = songs;
    }
}
