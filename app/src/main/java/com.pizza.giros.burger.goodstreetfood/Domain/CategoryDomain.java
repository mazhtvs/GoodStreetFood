package com.pizza.giros.burger.goodstreetfood.Domain;

public class CategoryDomain {
    private String categoryName;
    private String pic;

    public CategoryDomain(String categoryName, String pic) {
        this.categoryName = categoryName;
        this.pic = pic;
    }

    public String getTitle() {
        return categoryName;
    }


    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }
}
