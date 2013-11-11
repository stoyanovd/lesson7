package com.example.rssDima;

public class Record {
    String title;
    String summaries;
    String link;

    Record() {
        title = "";
        summaries = "";
        link = "";
    }

    Record(String _title, String _summaries, String _link) {
        title = _title;
        summaries = _summaries;
        link = _link;
    }
}