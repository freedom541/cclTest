package com.ccl.pdf;

/**
 * Created by ccl on 16/11/18.
 */
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import org.junit.Test;

import java.io.FileOutputStream;

/**
 * 页眉、页脚
 * author wangnian
 * date 2016/4/1
 */
public class PdfDemo_3 {

    public static void create() throws Exception {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter writer = PdfWriter.getInstance(document,new FileOutputStream("d:/demo3.pdf"));

        // 增加页眉页脚
        writer.setPageEvent(new MyHeaderFooter());

        String title = "凉州词";
        String content = "黄河远上白云间，一片孤城万仞山。羌笛何须怨杨柳，春风不度玉门关。";
        document.open();

        Font font = new XMLWorkerFontProvider().getFont("宋体");
        for (int i = 0; i <100; i++) {
            document.add(new Paragraph(title, font));
            document.add(new Paragraph(content,font));
            document.add(new Paragraph("\n"));
        }
        document.close();
        writer.close();
    }

    @Test
    public  void test() throws Exception {
        create();
        System.out.println("生成成功");
    }
}
