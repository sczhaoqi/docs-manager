package com.sczhaoqi.docsmanager.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;

/**
 * @author sczhaoqi
 * @date 2020/4/24 23:51
 */
@Component
public class WebSecurityConfig
        extends WebSecurityConfigurerAdapter
        implements WebMvcConfigurer, InitializingBean
{
    @Autowired
    private DocsPathProps docsPathProps;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry)
    {
        registry.addResourceHandler("/docs/**").addResourceLocations(docsPathProps.getPath());
    }

    @Override
    protected void configure(HttpSecurity http)
            throws Exception
    {
        http.csrf().ignoringAntMatchers("/h2-console/**").ignoringAntMatchers("/docs/upload")
                .and().authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
        ;
        super.configure(http);
    }

    @Bean
    private PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AbstractUserDetailsAuthenticationProvider authenticationProvider()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        daoAuthenticationProvider.setUserDetailsService(jdbcDao());
        return daoAuthenticationProvider;
    }

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcDaoImpl jdbcDao()
    {
        JdbcDaoImpl jdbcDao = new JdbcDaoImpl();
        jdbcDao.setDataSource(dataSource);
        jdbcDao.setAuthoritiesByUsernameQuery("select id,authority from authorities  where id in ( select aid from user_authorities where uid = (select id from users where username = ?) )");
        return jdbcDao;
    }

    @Autowired
    private JdbcDaoImpl jdbcDao;

    @Override
    public void afterPropertiesSet()
    {
        // if not encode
        String originalPwd = "123456";
        String encodedPwd = this.passwordEncoder().encode(originalPwd);
        JdbcTemplate tpl = jdbcDao.getJdbcTemplate();
        if (tpl != null) {
            tpl.update("update users set password=? where id = 1 and password=?", encodedPwd, originalPwd);
        }
    }
}
