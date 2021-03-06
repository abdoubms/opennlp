/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.formats.brat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AnnotationConfiguration {

  public static final String SPAN_TYPE = "Span";
  public static final String ENTITY_TYPE = "Entity";
  public static final String RELATION_TYPE = "Relation";
  public static final String ATTRIBUTE_TYPE = "Attribute";

  private final Map<String, String> typeToClassMap;

  public AnnotationConfiguration(Map<String, String> typeToClassMap) {

    this.typeToClassMap = Collections.unmodifiableMap(
        new HashMap<String, String>(typeToClassMap));
  }

  public String getTypeClass(String type) {
    return typeToClassMap.get(type);
  }


  public static AnnotationConfiguration parse(InputStream in) throws IOException {
    Map<String, String> typeToClassMap = new HashMap<String, String>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));

    // Note: This only supports entities and relations section
    String line = null;
    String sectionType = null;

    while ((line = reader.readLine())!= null) {
      line = line.trim();

      if (line.length() == 0) {
        continue;
      } else if (line.startsWith("#")) {
        continue;
      } else if (line.startsWith("[") && line.endsWith("]")) {
        sectionType = line.substring(line.indexOf('[') + 1, line.indexOf(']'));
      }
      else {

        switch (sectionType) {
        case "entities":
          typeToClassMap.put(line, AnnotationConfiguration.ENTITY_TYPE);
          break;

        case "relations":
          typeToClassMap.put(line.substring(0, line.indexOf(' ')), AnnotationConfiguration.RELATION_TYPE);
          break;

        case "attributes":
          typeToClassMap.put(line.substring(0, line.indexOf(' ')), AnnotationConfiguration.ATTRIBUTE_TYPE);
          break;

        default:
          break;
        }
      }
    }

    return new AnnotationConfiguration(typeToClassMap);
  }
}
