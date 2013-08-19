/*
 * SonarSource Language Recognizer
 * Copyright (C) 2010 SonarSource
 * dev@sonar.codehaus.org
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.sslr.annotations;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("org.sonar.sslr.annotations.Annotation")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {

  public AnnotationProcessor() {
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element e : roundEnv.getElementsAnnotatedWith(Annotation.class)) {
      TypeElement clazz = (TypeElement) e;
      try {
        JavaFileObject f = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName() + "Extras");
        Writer w = f.openWriter();
        try {
          PrintWriter pw = new PrintWriter(w);
          pw.println("package " + packageName(clazz) + ";");
          pw.println("public abstract class " + clazz.getSimpleName() + "Extras {");
          pw.println("  public String toString() {");
          pw.println("    return \"Hello!\";");
          pw.println("  }");
          pw.println("}");
        } finally {
          w.close();
        }
      } catch (IOException ex) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.toString());
      }
    }
    return true;
  }

  private static String packageName(TypeElement clazz) {
    String fullName = clazz.getQualifiedName().toString();
    int index = fullName.lastIndexOf('.');
    if (index > 0) {
      return fullName.substring(0, index);
    }
    return "";
  }

}
