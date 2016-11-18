package com.ccl.pdf;

/**
 * Created by ccl on 16/11/18.
 */
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
/**
 * iText5中并没有之前版本HeaderFooter对象设置页眉和页脚<br>
 * 不过，可以利用PdfPageEventHelper来完成页眉页脚的设置工作。<br>
 * 就是在页面完成但写入内容之前触发事件，插入页眉、页脚、水印等。<br>
 *
 * author wangnian
 * date 2016/4/1
 *
 */
public class MyHeaderFooter extends PdfPageEventHelper {
    Font font = new XMLWorkerFontProvider().getFont("宋体", 12, BaseColor.RED);
    // 总页数
    PdfTemplate totalPage;
    // 打开文档时，创建一个总页数的模版
    public void onOpenDocument(PdfWriter writer, Document document) {
        PdfContentByte cb =writer.getDirectContent();
        totalPage = cb.createTemplate(30, 16);
    }
    // 一页加载完成触发，写入页眉和页脚
    public void onEndPage(PdfWriter writer, Document document) {
        PdfPTable table = new PdfPTable(3);
        try {
            table.setTotalWidth(PageSize.A4.getWidth() - 100);
            table.setWidths(new int[] { 24, 24, 3});
            table.setLockedWidth(true);
            table.getDefaultCell().setFixedHeight(-10);
            table.getDefaultCell().setBorder(Rectangle.BOTTOM);

            table.addCell(new Paragraph("我是文字", font));// 可以直接使用addCell(str)，不过不能指定字体，中文无法显示
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(new Paragraph("第" + writer.getPageNumber() + "页/", font));
            // 总页数
            PdfPCell cell = new PdfPCell(Image.getInstance(totalPage));
            cell.setBorder(Rectangle.BOTTOM);
            table.addCell(cell);
            // 将页眉写到document中，位置可以指定，指定到下面就是页脚
            table.writeSelectedRows(0, -1, 50,PageSize.A4.getHeight() - 20, writer.getDirectContent());
        } catch (Exception de) {
            throw new ExceptionConverter(de);
        }
    }

    // 全部完成后，将总页数的pdf模版写到指定位置
    public void onCloseDocument(PdfWriter writer,Document document) {
        String text = "总" + (writer.getPageNumber() - 1) + "页";
        ColumnText.showTextAligned(totalPage, Element.ALIGN_LEFT, new Paragraph(text,font), 2, 2, 0);
    }
}
