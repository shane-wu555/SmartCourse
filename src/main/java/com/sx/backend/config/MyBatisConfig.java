package com.sx.backend.config;

import com.sx.backend.typehandler.JsonQuestionListTypeHandler;
import com.sx.backend.typehandler.JsonStringListTypeHandler;
import com.sx.backend.typehandler.RelationTypeHandler;
import com.sx.backend.typehandler.ResourceTypeTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MyBatisConfig {
    
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        
        // 设置mapper文件位置
        factoryBean.setMapperLocations(
            new PathMatchingResourcePatternResolver()
                .getResources("classpath:com/sx/backend/mapper/*.xml")
        );
        
        // 注册类型处理器 - 添加新的JsonQuestionListTypeHandler
        factoryBean.setTypeHandlers(
            new RelationTypeHandler(), 
            new ResourceTypeTypeHandler(),
            new JsonStringListTypeHandler(),
            new JsonQuestionListTypeHandler()
        );
        
        // 其他配置
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        factoryBean.setConfiguration(configuration);
        
        return factoryBean.getObject();
    }
}
