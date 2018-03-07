package com.agilion.config;

import com.agilion.domain.app.config.UIDateFormat;
import com.agilion.services.dao.NetworkBuildRepository;
import com.agilion.services.dataengine.DataEngineClient;
import com.agilion.services.files.FileStore;
import com.agilion.services.files.LocalFileStore;
import com.agilion.services.jobmanager.JobManager;
import com.agilion.services.jobmanager.LocalNoQueryApiJobManager;
import com.agilion.utils.NetworkFormToJobRequestConverter;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.format.FormatterRegistry;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Configuration
public class WebAppConfig extends WebMvcConfigurerAdapter
{
    public static final String DATE_STRING_FORMAT = "MM/dd/yyyy";
    public static final String DATE_REGEX_PATTERN = "(0?[1-9]|1[012])/(0?[1-9]|[12][0-9]|3[01])/((19|20)\\d\\d)";

    @Autowired
    DataEngineClient dataEngineClient;

    @Autowired
    NetworkBuildRepository networkBuildRepository;

    @Bean
    public UIDateFormat getUIDateFormat()
    {
        return new UIDateFormat(DATE_STRING_FORMAT);
    }

    @Bean
    public DateFormat dateFormat()
    {
        return new SimpleDateFormat(DATE_STRING_FORMAT);
    }

    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:messages");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    @Bean
    public MessageSourceAccessor accessor()
    {
        return new MessageSourceAccessor(messageSource(), Locale.ENGLISH);
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
        return new LocalNoQueryApiJobManager(dataEngineClient, networkBuildRepository);
    }

    @Bean
    public NetworkFormToJobRequestConverter networkFormToJobRequestConverter()
    {
        return new NetworkFormToJobRequestConverter(fileStore(), dateFormat());
    }

    @Bean
    public Gson gson()
    {
        return new Gson();
    }
}
