# mybatis-generator-plugin-ext
custom plugin for mybatis generator

还在为自动生成代码而覆盖了原先自己的代码而苦恼么?
还在为数据库表字段变动而苦恼么?
那么请用这个plugin吧，一切都解决了,have a try and enjoy it.

All you need to do，just like below:
<plugin>
      <groupId>org.mybatis.generator</groupId>
      <artifactId>mybatis-generator-maven-plugin</artifactId>
      <version>1.3.2</version>
      <configuration>
          <configurationFile>src/main/resources/mybatis-generator/generatorConfig.xml</configurationFile>
          <verbose>true</verbose>
          <overwrite>true</overwrite>
      </configuration>
     <executions>
         <execution>
          <phase>default</phase>
             <id>Generate MyBatis Artifacts</id>
             <goals>
                 <goal>generate</goal>
             </goals>
         </execution>
     </executions>
     <dependencies>
         <dependency>
             <groupId>org.mybatis.generator</groupId>
             <artifactId>mybatis-generator-core</artifactId>
             <version>1.3.2</version>
         </dependency>
         <dependency>
             <groupId>com.nfsq</groupId>
             <artifactId>mybatis-generator-plugin-ext</artifactId>
             <version>0.0.1-SNAPSHOT</version>
         </dependency>
     </dependencies>
 </plugin>

