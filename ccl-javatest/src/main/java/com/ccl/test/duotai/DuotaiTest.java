package com.ccl.test.duotai;

import org.springframework.beans.BeansException;

import java.lang.reflect.Field;

/**
 * Created by ccl on 16/11/24.
 */
public class DuotaiTest {
    public static void main(String[] args) throws IllegalAccessException {
        try {
            Sun sun = new Sun();
            sun.setName("jjj");
            sun.setAction("Sun");
            Father father = test(sun);
            System.out.println(father.getClassName());
            Class clazz = father.getClass();
            do {
                Field[] fields = clazz.getDeclaredFields();
                for (Field f : fields) {
                    f.setAccessible(true);
                    System.out.println();
                    System.out.println(f.getName() + "==" + f.get(father));
                }
                clazz = clazz.getSuperclass();
            } while (clazz != Object.class);

            System.out.println(father);
            //Class<?> request = Class.forName(father.bean);
            Class<?> request = Class.forName(father.getClassName());
            try {
                Object newSun = request.newInstance();
                copy(newSun,father);
                System.out.println(newSun);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static void copy(Object sun, Object father){
        try {
            org.springframework.beans.BeanUtils.copyProperties(father, sun);
        } catch (BeansException e) {
            e.printStackTrace();

        }
    }

    public static Father test(Sun sun){
        return sun;
    }
}
