/**
 * Created by ccl on 16/10/13.
 */
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Created by ccl on 15-12-2.
 */
@WebServlet("/mytest")
public class Test extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        System.err.println("---");
        req.setCharacterEncoding("UTF-8");
        String name=req.getParameter("name");
        req.setAttribute("name",name);
        System.out.println(name);
        req.getRequestDispatcher("index2.jsp").forward(req, resp);
    }

    public static void main(String[] args) {
        BigDecimal big = new BigDecimal("0");
        System.out.println(big.add(new BigDecimal(500)));
    }
}
