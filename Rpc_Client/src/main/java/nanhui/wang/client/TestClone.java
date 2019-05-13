package nanhui.wang.client;

import java.io.Serializable;

public class TestClone {
    public static void main(String[] args) throws CloneNotSupportedException {
        Person p = new Person();
        p.name = "aaas";
        p.address = "12345";
        p.age = 12;
        p.tel = "12345";

        Person p2 = (Person) p.clone();

        p.address = "1111";
        p2.address = "213";

        System.out.println(p + "\n" + p2);


        long start = System.currentTimeMillis();


        int ik = 100;
        for (long i = 0; i < 1000000000; i++) {
            int n = (ik / 2);
        }
        System.out.println(System.currentTimeMillis() - start);


    }


}


class Person implements Cloneable, Serializable {

    public String name;
    public String address;
    public int age;
    public String tel;


    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return name + "\t" + address + "\t" + age + "\t" + tel;
    }
}