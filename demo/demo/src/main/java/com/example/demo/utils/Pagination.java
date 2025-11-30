package com.example.demo.utils;

public class Pagination {

    private Integer total;
    private Integer pageSize;
    private Integer currentPage;
    private Integer totalPages;
    private Integer offset;
    private Integer limit;

    public Pagination(Integer total, Integer pageSize, Integer currentPage) {
        this.total = total;
        this.pageSize = pageSize != null && pageSize > 0 ? pageSize : 10;
        this.currentPage = currentPage != null && currentPage > 0 ? currentPage : 1;
        this.totalPages = (int) Math.ceil((double) this.total / this.pageSize);
        this.offset = (this.currentPage - 1) * this.pageSize;
        this.limit = this.pageSize;
    }

    public static Pagination paginate(Integer total, Integer pageSize, Integer currentPage) {
        return new Pagination(total, pageSize, currentPage);
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }
}
