package com.ccl.pdf;

/**
 * Created by ccl on 16/11/18.
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * html转pdf
 * author wangnian
 * date 2016/4/1
 *
 */
public class PdfDemo_4 {

    public static void create() throws Exception {

        // html中字体非常郁闷
        // 1. html中不指定字体，则默认使用英文字体，中文会不显示。
        // 2. html中指定的字体必须是英文名称，如宋体：font-family:SimSun;
        // 3. html中不能指定自定义字体，必须指定itext支持的字体，还好itext支持字体比较多，常见操作系统带的都支持
        // 4. 暂没有找到如何html中支持自定义字体方法，网上都是修改源码实现默认字体中文，也很重要

        StringBuilder html = new StringBuilder();
        html.append("<html>");
        html.append("<body style='font-size:20px;font-family:SimSun;'>");
        html.append("<table width='19cm'border='1' cellpadding='0' cellspacing='0'>");
        html.append("<tr>");
        html.append("<td colspan='2'>凉州词</td>");
        html.append("</tr>");
        html.append("<tr>");
        html.append("<td>黄河远上白云间，一片孤城万仞山。</td>");
        html.append("<td>羌笛何须怨杨柳，春风不度玉门关。</td>");
        html.append("</tr>");
        html.append("</table>");
        html.append("</body>");
        html.append("</html>");

        InputStream is = new ByteArrayInputStream(html.toString().getBytes());

        OutputStream os = new FileOutputStream("D:/demo4.pdf");
        Document document = new Document();

        PdfWriter writer = PdfWriter.getInstance(document,os);

        document.open();

        // 将html转pdf
        XMLWorkerHelper.getInstance().parseXHtml(writer,document, is);

        document.close();
    }

    @Test
    public  void test() throws Exception {
        create();
        System.out.println("生成成功");
    }
}
