package waiting.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import waiting.pojo.Books;
import waiting.service.BooksService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-10 9:59
 */
@Controller
@RequestMapping("/book")
public class BookController {


    //controller层调dao层
    @Autowired
    @Qualifier("booksServiceImpl")
    private BooksService booksService;


    //查询全部的书籍，并返回到一个书籍展示的页面
    @RequestMapping("/allBook")
    public String allBook(Model model) {
        List<Books> list = booksService.queryAllBook();
        model.addAttribute("list", list);
        return "allBook";
    }

    //跳转到书籍添加的页面
    @RequestMapping("/toAddBook")
    public String toAddPage() {
        return "addBook";
    }

    //添加书籍的请求
    @RequestMapping("/addBook")
    public String addBook(Books books) {
        System.out.println("addBook==>" + books);
        booksService.addBooks(books);
        return "redirect:/book/allBook";
    }

    //跳转到修改书籍的页面
    @RequestMapping("/toUpdateBook")
    public String toUpdatPage(int id,Model model) {
        Books books = booksService.queryBookById(id);
        model.addAttribute("queryBook",books);
        return "updateBook";
    }

    //修改书籍的页面
    @RequestMapping("/updateBook")
    public String updateBook(Books books) {
        System.out.println("updateBook ==>" + books);
        booksService.updateBook(books);
        return "redirect:/book/allBook";
    }

    //删除书籍
    @RequestMapping("/deleteBook/{bookId}")
    public String deleteBook(@PathVariable("bookId") int id){
        booksService.deleteById(id);
        return "redirect:/book/allBook";
    }

    //查询书籍
    @RequestMapping("/queryBook")
    public String queryBook(String queryBookName,Model model){
        Books books = booksService.queryBookByName(queryBookName);
        List<Books> list = new ArrayList<>();
        list.add(books);
        if(books == null){
            list = booksService.queryAllBook();
            model.addAttribute("error","未查到该书籍");
        }
        model.addAttribute("list", list);
        return "allBook";

    }




}
