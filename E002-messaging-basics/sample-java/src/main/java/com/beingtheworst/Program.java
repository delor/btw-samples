package com.beingtheworst;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Program {

    public static void main(String[] args) {
        ProductBasket basket = new ProductBasket();

        basket.addProduct("butter", 1);
        basket.addProduct("pepper", 2);

        applyMessage(basket, new AddProductToBasketMessage("candles", 5));

        Queue<Object> queue = new LinkedList<>();
        queue.add(new AddProductToBasketMessage("Chablis wine", 1));
        queue.add(new AddProductToBasketMessage("shrimps", 10));

        while (!queue.isEmpty()) {
            applyMessage(basket, queue.poll());
        }

        try (FileOutputStream file = new FileOutputStream("message.bin")) {
            ObjectOutputStream out = new ObjectOutputStream(file);
            out.writeObject(new AddProductToBasketMessage("rosemary", 1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (FileInputStream file = new FileInputStream("message.bin")) {
            ObjectInputStream in = new ObjectInputStream(file);
            Object msg = in.readObject();
            applyMessage(basket, msg);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (Map.Entry<String, Double> total : basket.getProductTotals().entrySet()) {
            System.out.println("  " + total.getKey() + ": " + total.getValue());
        }
    }

    private static void applyMessage(ProductBasket basket, Object message) {
        try {
            Method method = basket.getClass().getMethod("when", message.getClass());
            method.invoke(basket, message);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ProductBasket {

        private final Map<String, Double> products;

        private ProductBasket() {
            products = new HashMap<>();
        }

        public void addProduct(String name, double quantity) {
            double currentQuantity = 0;

            if (products.containsKey(name))
                currentQuantity = products.get(name);

            products.put(name, quantity + currentQuantity);
        }

        public void when(AddProductToBasketMessage message) {
            addProduct(message.getName(), message.getQuantity());
        }

        public Map<String, Double> getProductTotals() {
            return new HashMap<>(products);
        }
    }

    private static class AddProductToBasketMessage implements Serializable {

        private final String name;
        private final double quantity;

        public String getName() {
            return name;
        }

        public double getQuantity() {
            return quantity;
        }

        public AddProductToBasketMessage(String name, double quantity) {
            this.name = name;
            this.quantity = quantity;
        }

        @Override
        public String toString() {
            return "AddProductToBasketMessage{" +
                    "name='" + name + '\'' +
                    ", quantity=" + quantity +
                    '}';
        }
    }
}
