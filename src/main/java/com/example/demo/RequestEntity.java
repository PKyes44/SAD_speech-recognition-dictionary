package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestEntity {

    private String key;
    private String q;
    private String translated;
    private String transLang;
    private String advanced;
    private String method;

    public RequestEntity(String key, String q) {
        this.key = key; // 인증키
        this.q = q; // 검색어
        this.translated = "y"; //다국어 번역 여부
        this.transLang = "1"; //영어
        this.advanced = "y"; //자세히찾기 여부
        this.method = "exact"; //일치 검색
    }

    public String getParameter() {
        return "?key=" + this.key +
                "&q=" + this.q +
                "&advanced=" + this.advanced +
                "&method=" + this.method +
                "&translated=" + this.translated +
                "&trans_lang=" + this.transLang;
    }
}
