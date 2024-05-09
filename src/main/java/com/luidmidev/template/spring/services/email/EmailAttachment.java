package com.luidmidev.template.spring.services.email;

import lombok.Data;
import org.springframework.core.io.InputStreamSource;

import java.io.InputStream;

@Data
public class EmailAttachment implements InputStreamSource {
    private String name;
    private InputStream inputStream;
}
