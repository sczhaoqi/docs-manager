package com.sczhaoqi.docsmanager.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sczhaoqi
 * @date 2020/4/25 21:44
 */
@Component
@ConfigurationProperties(prefix = "docs.location")
public class DocsPathProps
{
    private String path;
    private String zpath;

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public String getZpath()
    {
        return zpath;
    }

    public void setZpath(String zpath)
    {
        this.zpath = zpath;
    }
}
