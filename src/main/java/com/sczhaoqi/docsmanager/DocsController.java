package com.sczhaoqi.docsmanager;

import com.sczhaoqi.docsmanager.config.DocsPathProps;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sczhaoqi.docsmanager.ZipUtil.unZip;

/**
 * @author sczhaoqi
 * @date 2020/4/25 17:04
 */
@Controller
public class DocsController
        implements InitializingBean
{
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("")
    public String index(Map<String, Object> map)
    {
        return "index";
    }

    @Autowired
    private DocsPathProps docsPathProps;

    @GetMapping("/list")
    @ResponseBody
    public List<DocData> list()
    {
        List<DocRecord> docRecordList=jdbcTemplate.query("select id,project,zpath,path,name,href_subfix,utime from docs", new DocRecordRowMapper());
        List<DocData> docData = new ArrayList<>();
        Map<String,List<DocData>> map = new HashMap<>();
        for (DocRecord record:docRecordList
        ) {

            List<DocData> tmpData = map.getOrDefault(record.getProject(),new ArrayList<>());
            tmpData.add(new DocData(record));
            map.put(record.getProject(),tmpData);
        }
        map.entrySet().parallelStream()
                .forEach((kv) -> {
                    DocData data = new DocData();
                    data.setId(kv.getKey());
                    data.setLabel(kv.getKey());
                    data.setChildren(kv.getValue());
                    docData.add(data);
                });
        return docData;
    }
    public class DocRecordRowMapper implements RowMapper<DocRecord>{

        @Override
        public DocRecord mapRow(ResultSet resultSet, int i)
                throws SQLException
        {
            DocRecord docRecord = new DocRecord();
            docRecord.setId(resultSet.getInt("id"));
            docRecord.setProject(resultSet.getString("project"));
            docRecord.setPath(resultSet.getString("path"));
            docRecord.setZpath(resultSet.getString("zpath"));
            docRecord.setHrefSubfix(resultSet.getString("href_subfix"));
            docRecord.setUtime(resultSet.getTimestamp("utime"));
            docRecord.setName(resultSet.getString("name"));
            return docRecord;
        }
    }
    public class DocData{
        String id;
        String label;
        String href;
        List<DocData> children;

        public DocData(){}

        public DocData(DocRecord record)
        {
            this.id = Integer.toString(record.id);
            this.label = record.getName();
            this.href = record.hrefSubfix;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public String getLabel()
        {
            return label;
        }

        public void setLabel(String label)
        {
            this.label = label;
        }

        public String getHref()
        {
            return href;
        }

        public void setHref(String href)
        {
            this.href = href;
        }

        public List<DocData> getChildren()
        {
            return children;
        }

        public void setChildren(List<DocData> children)
        {
            this.children = children;
        }
    }
    public class DocRecord
    {
        Integer id;
        String project;
        String zpath;
        String path;
        String hrefSubfix;
        Timestamp utime;
        String name;

        public Integer getId()
        {
            return id;
        }

        public void setId(Integer id)
        {
            this.id = id;
        }

        public String getProject()
        {
            return project;
        }

        public void setProject(String project)
        {
            this.project = project;
        }

        public String getZpath()
        {
            return zpath;
        }

        public void setZpath(String zpath)
        {
            this.zpath = zpath;
        }

        public String getPath()
        {
            return path;
        }

        public void setPath(String path)
        {
            this.path = path;
        }

        public String getHrefSubfix()
        {
            return hrefSubfix;
        }

        public void setHrefSubfix(String hrefSubfix)
        {
            this.hrefSubfix = hrefSubfix;
        }

        public Timestamp getUtime()
        {
            return utime;
        }

        public void setUtime(Timestamp utime)
        {
            this.utime = utime;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
    }

    @PostMapping(value = "/docs/upload",
            headers = ("content-type=multipart/*"),
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public String upload(String name, @RequestParam("file") MultipartFile file)
            throws IOException
    {
        String ppath = md5(name);
        String fileName = file.getName();
        String type = file.getOriginalFilename().substring(fileName.length() - 1);
        long current = System.currentTimeMillis();
        File zfile = new File(docsPathProps.getZpath() + "/" + ppath + "/" + fileName + current + type);
        zfile.getParentFile().mkdirs();
        file.transferTo(zfile);
        String zpath = zfile.getAbsolutePath();
        String path = docsPathProps.getPath() + "/" + ppath + "/" + current;
        unZip(zfile, path);
        jdbcTemplate.update("insert into docs values (?,?,?,?,?,?,?)", null, name, zpath, path, "/" + ppath + "/" + current+"/index.html", new Timestamp(current),fileName+current);
        return "OK";
    }

    @Override
    public void afterPropertiesSet()
            throws Exception
    {
        Files.createDirectories(new File(docsPathProps.getPath()).toPath());
        Files.createDirectories(new File(docsPathProps.getZpath()).toPath());
    }

    public String md5(String str)
    {
        String res = null;
        try {
            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            mDigest.update(str.getBytes());
            res = bytesToHexString(mDigest.digest());
        }
        catch (NoSuchAlgorithmException e) {
        }
        return res;
    }

    private static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xFF & aByte);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
