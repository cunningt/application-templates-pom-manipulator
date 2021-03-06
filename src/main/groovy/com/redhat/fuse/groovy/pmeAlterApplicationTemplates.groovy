/**
 * Copyright (C) 2012 Red Hat, Inc. (jcasey@redhat.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.fuse.groovy

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.Properties

import org.commonjava.maven.atlas.ident.ref.SimpleProjectRef
import org.commonjava.maven.ext.core.groovy.BaseScript
import org.commonjava.maven.ext.core.groovy.InvocationPoint
import org.commonjava.maven.ext.core.groovy.InvocationStage
import org.commonjava.maven.ext.core.groovy.PMEBaseScript

@InvocationPoint(invocationPoint = InvocationStage.FIRST)
@PMEBaseScript BaseScript pme

println "#### pmeAlterApplicationTemplates BEGIN"
Properties properties = pme.getProject().getModel().getProperties()
Enumeration<String> enums = (Enumeration<String>) properties.propertyNames();
while (enums.hasMoreElements()) {
    String key = enums.nextElement();
    String value = properties.getProperty(key);

    String quickstartName = key.replace(".version", "");
    String oldVersion = value.replaceAll(".redhat-[0-9]+", "")

    println ("For quickstart " + quickstartName + " replace " + oldVersion + " GIT_REF version with " + value)

    String fileName = "src/test/resources/quickstarts/" + quickstartName + "-template.json"
    new File( fileName+".bak" ).withWriter { w ->
        new File( fileName ).eachLine { line ->
            w << line.replaceAll(oldVersion, value) + System.getProperty("line.separator")
        }
    }
    Files.copy(Paths.get(fileName+".bak"), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING)
    boolean deleted = new File(fileName + ".bak").delete()
}

println "#### pmeAlterApplicationTemplates END"

pme.inlineProperty (pme.getProject(), SimpleProjectRef.parse("org.apache.maven:maven-core"))
