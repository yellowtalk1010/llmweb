## 问题1
```
已经在resources/templates/index.html了， 
还是警告Cannot find template location: classpath:/templates/ (please add some templates, check your Thymeleaf configuration, 
or set spring.thymeleaf.check-template-location=false)
```
## 问题2
```
打开：http://localhost:8080/pages/stock 报错404
```
## 解决方案
```
原因： 因为target/classes中没有templates
解决： mvn clean package -Dmaven.test.skip=true 这样在target中将生成templates文件夹
```