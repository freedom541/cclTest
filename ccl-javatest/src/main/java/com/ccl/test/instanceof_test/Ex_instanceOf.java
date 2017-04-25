package com.ccl.test.instanceof_test;

/**
 * Created by ccl on 17/3/10.
 */
/**
 * instanceof运算符用法
 * 运算符是双目运算符,左面的操作元是一个对象,右面是一个类.当
 * 左面的对象是右面的类创建的对象时,该运算符运算的结果是true,否则是false
 *
 * 说明:(1)一个类的实例包括本身的实例,以及所有直接或间接子类的实例
 * (2)instanceof左边操作元显式声明的类型与右边操作元必须是同种类或右边是左边父类的继承关系,
 * (3)不同的继承关系下,编译出错
 */
class Person {
}
class Student extends Person {
}
class Postgraduate extends Student {
}
class Animal {
}
public class Ex_instanceOf {
    public static void main(String[] args) {
        instanceofTest(new Student());
    }
    /**
     * 这个程序的输出结果是：p是类Student的实例
     *
     * Person类所在的继承树是：Object<--Person<--Student<--Postgraduate。
     *
     * 这个例子中还加入一个Animal类，它不是在Person类的继承树中，所以不能作为instanceof的右操作数。
     *
     * @param p
     */
    public static void instanceofTest(Person p) {
        // p 和 Animal类型不一样,彼此之间没有继承关系,编译会出错
        // 提示错误:Incompatible conditional operand types Person and Animal
        // if(p instanceof Animal){
        // System.out.println("p是类Animal的实例");
        // }
        //下面代码的除了第一行都会输出
        if (p instanceof Postgraduate) System.out.println("p是类Postgraduate的实例");
        if (p instanceof Person) System.out.println("p是类Person的实例");
        if (p instanceof Student) System.out.println("p是类Student的实例");
        if (p instanceof Object) System.out.println("p是类Object的实例");

    }
}