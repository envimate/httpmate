# Usecases IV: Validation

## Custom Primitives
An important aspect of Domain-Driven Design (DDD) is the usage of *custom primitives*
or *value objects* over conventional primitive datatypes (String, int, double, etc.).

A custom primitive is a 

Let's consider an online shopping portal. On checkout, your shopping cart
contains varying amounts of different products.
Each item in a shopping cart therefore has a quantity (how many) and a product id (what).
If we followed Java programming by the book, we would probably model the quantity with
as an `int` and the 

```java
public final class ShoppingCartItem {
    private final 
    private final Quantity quantity;
}
```

```java
public final class Quantity {
    private final int quantity;
    
    public Quantity(final int quantity) {
        if(quantity < 0) {
            throw new IllegalArgumentException("quantity must not be negative");
        }
        this.quantity = quantity; 
    }
}
```

```java
public final class ProductId {
    private final String id;
    
    public ProductId(final String id) {
        if(id == null) {
            throw new IllegalArgumentException("id must not be null");
        }
        this.id = id;
    }
}
```

We can now design a shopping cart:

