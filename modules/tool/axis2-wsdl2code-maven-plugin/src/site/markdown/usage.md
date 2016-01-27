<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

Usage
-----

The WSDl2Code offers a single goal:

*   wsdl2code (default): Reads the WSDL and generates code.

To run the plugin, add the following section to your POM:

    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.axis2.maven2</groupId>
          <artifactId>axis2-wsdl2code-maven-plugin</artifactId>
          <executions>
            <execution>
              <goals>
                <goal>wsdl2code</goal>
              </goals>
            </execution>
            <configuration>
              <packageName>com.foo.myservice</packageName>
              <wsdlFile>src/main/wsdl/myservice.wsdl</wsdlFile>
              <databindingName>xmlbeans</databindingName>
            </configuration>
          </executions>
        </plugin>
      </plugins>
    </build>

The plugin will be invoked automatically in the generate-sources
phase. You can also invoke it directly from the command line by
running the command

    mvn axis2-wsdl2code:wsdl2code

By default, the plugin reads the file `src/main/axis2/service.wsdl`.
Sources for the Java programming language and the ADB data binding are
generated into `target/generated-sources/axis2/wsdl2code`.
Note the configuration element `packageName` above, which sets
the package name, thus a subdirectory.

See the detailed documentation on [properties](wsdl2code-mojo.html) for
how to configure the goal.