package xyz.idaoteng.audiotag.api.netease.dto;

import java.util.ArrayList;

public class Song {
    private String name;
    private String id;
    private ArrayList<Artist> ar;
    private Album al;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Artist> getAr() {
        return ar;
    }

    public void setAr(ArrayList<Artist> ar) {
        this.ar = ar;
    }

    public Album getAl() {
        return al;
    }

    public void setAl(Album al) {
        this.al = al;
    }
}
