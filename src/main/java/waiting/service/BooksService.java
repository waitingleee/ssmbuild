package waiting.service;

import org.apache.ibatis.annotations.Param;
import waiting.pojo.Books;

import java.awt.print.Book;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 21:00
 */
public interface BooksService {
    //增加一本书
    int addBooks(Books books);

    //删除一本书
    int deleteById(int id);

    //更新一本书
    int updateBook(Books book);

    //根据id查询一本书
    Books queryBookById(int id);

    //查询全部书
    List<Books> queryAllBook();

    //查询一本书
    Books queryBookByName(String bookName);

}
