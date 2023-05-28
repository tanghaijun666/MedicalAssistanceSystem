### 1. 启动时参数

启动时设置MongoDB参数以及文件存储路径
下面分别是MongoDB的IP地址，端口号，数据库名，以及服务器上文件存储的位置
--spring.data.mongodb.host=43.142.168.114 --spring.data.mongodb.port=27017 --spring.data.mongodb.database=MEDICAL --dicom.file.location=/projects/medical/dicomFile

### 2. nginx配置

```
server {
  listen  8002;
  server_name {服务器ip} somename alias another.alias;
  location /dicomfile{
    alias /projects/medical/dicomFile;#这里写自己存文件的位置，需要和启动参数一样
    index index.html;
  }
}
```

### 3. 一些maven里面没有的jar包我放到了resources目录下some-jar目录里面，可以自己放到自己的maven厂库

具体步骤就是 将some-jar目录下的文件复制到自己的本地厂库repository目录下，也可以自己去官网下载