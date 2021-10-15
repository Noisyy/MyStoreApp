package com.example.quantrasuaclient.Model;

public class PopularCategoryModel {
    private String menu_id;
    private String drink_id;
    private String name;

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menu_id) {
        this.menu_id = menu_id;
    }

    public String getDrink_id() {
        return drink_id;
    }

    public void setDrink_id(String drink_id) {
        this.drink_id = drink_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    private String image;

    public PopularCategoryModel() {
    }

    public PopularCategoryModel(String menu_id, String drinks_id, String name, String image) {
        this.menu_id = menu_id;
        this.drink_id = drinks_id;
        this.name = name;
        this.image = image;
    }


}
