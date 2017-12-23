package com.agilion.config;

import com.agilion.domain.app.config.UIDateFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter
{
    public static final String DATE_STRING_FORMAT = "MM/dd/yyyy";

    @Bean
    public UIDateFormat getUIDateFormat()
    {
        return new UIDateFormat(DATE_STRING_FORMAT);
    }
}
