package com.juv3nil3.icdg.domain;

public class GitHubFile {

    private String name;
    private String path;
    private String type;
    private String download_url;
    private String sha;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDownload_url() {
        return download_url;
    }

    public void setDownload_url(String download_url) {
        this.download_url = download_url;
    }

    public String getSha() {
        return sha;
    }

    public void setSha(String sha) {
        this.sha = sha;
    }

    @Override
    public String toString() {
        return "GitHubFile{" +
            "name='" + name + '\'' +
            ", path='" + path + '\'' +
            ", type='" + type + '\'' +
            ", download_url='" + download_url + '\'' +
            ", sha='" + sha + '\'' +
            '}';
    }
}
