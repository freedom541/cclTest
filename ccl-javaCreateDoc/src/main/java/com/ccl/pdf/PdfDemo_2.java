package com.ccl.pdf;

/**
 * Created by ccl on 16/11/18.
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactoryImp;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import org.junit.Test;

import java.io.FileOutputStream;

/**
 * 字体
 *
 * author wangnian
 * date 2016/4/1
 *
 */
public class PdfDemo_2 {

    public static void create() throws Exception {
        Document document = new Document();
        PdfWriter writer = PdfWriter.getInstance(document,new FileOutputStream("D:/demo2.pdf"));
        String title = "凉州词";
        String content = "黄河远上白云间，一片孤城万仞山。羌笛何须怨杨柳，春风不度玉门关。";
        document.open();
        document.add(new Paragraph(title, getFont("方正兰亭黑简体")));
        document.add(new Paragraph(content, getFont("迷你简娃娃篆")));
        document.close();
        writer.close();
    }

    private static Font getFont(String fontName) {
        // xmlworker主要功能是html转pdf用的，非常好用，也是itext官方的

        // 这个是xmlworker提供的获取字体方法，很方便，对中文支持很好
        FontFactoryImp fp = new XMLWorkerFontProvider();
        // 注册指定的字体目录，默认构造方法中会注册全部目录，我这里注册了src/font目录
        fp.registerDirectory(PdfDemo_2.class.getClassLoader().getResource("weiruanyahei").getFile(), true);

        // 最好的地方是直接支持获取中文的名字
        return fp.getFont(fontName);

        // 当然，最好的方法是自己继承XMLWorkerFontProvider，提供一些常用字体，简单方便
    }

    @Test
    public void test() throws Exception {
        create();
        System.out.println("生成成功");
    }
}
