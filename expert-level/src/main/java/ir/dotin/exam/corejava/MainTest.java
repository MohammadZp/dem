package ir.dotin.exam.corejava;

import java.util.*;
import java.util.stream.Collectors;

public class MainTest {
    public static void main(String[] args) {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("A", 200, "car"));
        productList.add(new Product("B", 100, "truck"));
        productList.add(new Product("C", 50, "bicycle"));
        productList.add(new Product("D", 30, "motor"));
        productList.add(new Product("E", 90, "computer"));

        productList.stream().filter(product -> product.getPrice() > 200).toList();

        Map<String, List<Product>> result =
                productList.stream()
                        .filter(p -> p.getPrice() > 100)
                        .collect(Collectors.groupingBy(Product::getCategory));

        result.values().forEach(list ->
                list.sort(Comparator.comparing(Product::getPrice).reversed())
        );

        List<Integer> numbers = Arrays.asList(5, 3, 5, 2, 3, 5, 7, 2, 2, 8);
        Map<Integer, Integer> characterCountMap = new HashMap<>();

        for (Integer number : numbers) {
            characterCountMap.put(number, characterCountMap.getOrDefault(number, 0) + 1);

        }


    }


}



