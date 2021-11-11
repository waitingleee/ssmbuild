package waiting.dao;

import org.apache.ibatis.annotations.Param;
import waiting.pojo.Books;

import java.awt.print.Book;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 20:41
 */
public interface BookMapper {

    //增加一本书
    int addBooks(Books books);

    //删除一本书
    int deleteById(@Param("bookID") int id);

    //更新一本书
    int updateBook(Books book);

    //根据id查询一本书
    Books queryBookById(@Param("bookID") int id);

    //查询全部书
    List<Books> queryAllBook();

    //查询一本书
    Books queryBookByName(@Param("bookName") String bookName);

}
