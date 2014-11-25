/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.colostate.vchill.iris;

import java.util.ArrayList;


/**
 * Class to represent each product in a sweep
 *
 * @author Joseph Hardin <josephhardinee@gmail.com>
 */
public class Product {

    String data_type;
    int number_of_rays;
    ArrayList<Ray> ray_list;
    int product_ID;

    public Product() {

        ray_list = new ArrayList<Ray>();

    }

    public Product(String dtype) {
        data_type = dtype;
        ray_list = new ArrayList<Ray>();
    }

    public void addRay(Ray ray) {
        ray_list.add(ray);
    }


}
