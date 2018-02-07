package com.agilion.config;

import com.agilion.domain.app.config.UIDateFormat;
import com.agilion.mock.MockJobManager;
import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.files.FileStore;
import com.agilion.services.files.LocalFileStore;
import com.agilion.services.jobmanager.JobManager;
import com.agilion.utils.NetworkFormToJobRequestConverter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter
{
    public static final String DATE_STRING_FORMAT = "MM/dd/yyyy";

    @Autowired
    DataEngineClient dataEngineClient;

    @Bean
    public UIDateFormat getUIDateFormat()
    {
        return new UIDateFormat(DATE_STRING_FORMAT);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    @Override
    public Validator getValidator() {
        LocalValidatorFactoryBean bean = new LocalValidatorFactoryBean();
        bean.setValidationMessageSource(messageSource());
        return bean;
    }

    @Bean
    public FileStore fileStore()
    {
        // TODO LocalFileStore is NOT the long-term solution. Hadoop/hdfs/S3 is.
        return new LocalFileStore(System.getProperty("user.dir")+"/AgilionLocalFileStore/");
    }

    @Bean
    public JobManager jobManager()
    {
        return new MockJobManager(fileStore(), dataEngineClient);
    }

    @Bean
    public NetworkFormToJobRequestConverter networkFormToJobRequestConverter()
    {
        return new NetworkFormToJobRequestConverter(fileStore());
    }

    @Bean
    public Gson gson()
    {
        return new Gson();
    }
}
