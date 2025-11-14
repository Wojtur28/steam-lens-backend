package com.example.steamlensbackend.steam.dto.requests;

public class PageableRequest {
    private int page = 0;
    private int pageSize = 0;

    public int getPage() {
        return page;
    }
    public void setPage(int page) {
        this.page = page;
    }

    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
