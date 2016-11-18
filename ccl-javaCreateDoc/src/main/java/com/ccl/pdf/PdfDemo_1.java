package com.ccl.pdf;

/**
 * Created by ccl on 16/11/18.
 */
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPHeaderCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.junit.Test;
import java.io.FileOutputStream;
/**
 * author wangnian
 * date 2016/4/1
 */
public class PdfDemo_1 {

    private static void create() throws Exception {

        // 创建一个文档（默认大小A4，边距36, 36, 36, 36）
        Document document = new Document();
        // 设置文档大小
        document.setPageSize(PageSize.A4);
        // 设置边距，单位都是像素，换算大约1厘米=28.33像素
        document.setMargins(50, 50, 50, 50);
        // 设置pdf生成的路径
        FileOutputStream fileOutputStream= new FileOutputStream("D:/demo.pdf");
        // 创建writer，通过writer将文档写入磁盘
        PdfWriter writer = PdfWriter.getInstance(document,fileOutputStream);
        // demo
        String title = "打油诗";
        String content = "大学校园真开放，宿舍竟能打麻将。东南西北皆可碰，哪管父母心血浓。";

        // 定义字体
        FontFactoryImp ffi = new FontFactoryImp();
        // 注册全部默认字体目录，windows会自动找fonts文件夹的，返回值为注册到了多少字体
        ffi.registerDirectories();
        // 获取字体，其实不用这么麻烦，后面有简单方法
        Font font = ffi.getFont("宋体",BaseFont.IDENTITY_H,BaseFont.EMBEDDED, 12, Font.UNDEFINED, null);

        // 打开文档，只有打开后才能往里面加东西
        document.open();

        // 设置作者
        document.addAuthor("校园作者");
        // 设置创建者
        document.addCreator("wangnian");
        // 设置主题
        document.addSubject("测试");
        // 设置标题
        document.addTitle("打油诗");

        // 增加一个段落
        document.add(new Paragraph(title, font));
        document.add(new Paragraph(content, font));
        document.add(new Paragraph("\n\r", font));

        // 创建表格，5列的表格
        PdfPTable table = new PdfPTable(4);
        table.setTotalWidth(PageSize.A4.getWidth()- 100);
        table.setLockedWidth(true);
        // 创建头
        PdfPHeaderCell header = new PdfPHeaderCell();
        header.addElement(new Paragraph(title, font));
        header.setColspan(4);
        table.addCell(header);
        // 添加内容
        table.addCell(new Paragraph("大学校园真开放",font));
        table.addCell(new Paragraph("宿舍竟能打麻将",font));
        table.addCell(new Paragraph("东南西北皆可碰",font));
        table.addCell(new Paragraph("哪管父母心血浓",font));

        document.add(table);
        // 关闭文档，才能输出
        document.close();
        writer.close();
    }
    @Test
    public  void test()  {
        try {
            create();
            System.out.println("生成成功");
        }catch (Exception ex){
            System.out.println("文件路径错误或者权限不够");
        }

    }
}
