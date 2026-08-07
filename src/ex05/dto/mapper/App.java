package ex05.dto.mapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import ex05.dto.EmpDTO;


public class App {
    public static void main(String[] args) {
        String resource = "org/mybatis/example/mybatis-config.xml";
        InputStream inputStream;
        try {
            inputStream = Resources.getResourceAsStream(resource);
            System.out.println(inputStream);
            SqlSessionFactory sqlSessionFactory =
                            new SqlSessionFactoryBuilder().build(inputStream);

            System.out.println(sqlSessionFactory);

            try (SqlSession session = sqlSessionFactory.openSession()) {
                List<EmpDTO> list = session.selectList(
                "org.mybatis.example.EmpMapper.selectEmp", 101);
                System.out.println(list);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
