package com.tech.emailvalidation.Java17FeautesModule.RecordsTest;

public class ProductTest {

    public static void main(String[] args) {
        Product p1=new Product(1,"Iphone17",87000.23);
        Product p2=new Product(1,"Iphone17",87000.23);

        System.out.println(p1.equals(p2));
        System.out.println(p1.hashCode()==p2.hashCode());

        Object p3=p1.equals(p2);
        System.out.println(p3);

    }
}
