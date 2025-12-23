package vn.ltdidong.apphoctienganh.models;

import java.util.List;

public class TranslateRequest {
    public List<String> q;
    public String source;
    public String target;

    public TranslateRequest(List<String> q, String source, String target) {
        this.q = q;
        this.source = source;
        this.target = target;
    }
}
