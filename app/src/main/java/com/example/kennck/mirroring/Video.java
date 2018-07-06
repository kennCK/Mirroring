package com.example.kennck.mirroring;

public class Video {
    private String path;
    private  String filename;

    public Video(String path, String filename) {
        this.path = path;
        this.filename = filename;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFilename(){
        return filename;
    }

    public  void setFilename(String filename){
        this.filename = filename;
    }
}
