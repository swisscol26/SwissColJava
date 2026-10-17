package modelo;

public class Producto {

    private int productId;
    private String name;
    private String description;
    private double price;
    private Double offerPrice;
    private int stock;
    private String image;
    private int categoryId;
    private String technicalDetails;
    private String reference;
    private int availableStock;


    public Producto() {
    }

    public Producto(int productId, String name, String description,
                    double price, int stock, String image, int categoryId) {

        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.categoryId = categoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTechnicalDetails() {
    return technicalDetails;
    }

public void setTechnicalDetails(String technicalDetails) {
    this.technicalDetails = technicalDetails;
    }

    public String getReference() {
    return reference;
    }

public void setReference(String reference) {
    this.reference = reference;
    }

    public Double getOfferPrice() {
    return offerPrice;
}

public void setOfferPrice(Double offerPrice) {
    this.offerPrice = offerPrice;
}

public int getAvailableStock() {
    return availableStock;
}

public void setAvailableStock(int availableStock) {
    this.availableStock = availableStock;
}
}