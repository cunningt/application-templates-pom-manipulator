package com.redhat.fuse.groovy;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Dependency;
import org.commonjava.maven.atlas.ident.ref.SimpleArtifactRef;
import org.commonjava.maven.atlas.ident.ref.SimpleProjectRef;
import org.commonjava.maven.ext.common.ManipulationException;
import org.commonjava.maven.ext.common.model.Project;
import org.commonjava.maven.ext.core.ManipulationManager;
import org.commonjava.maven.ext.core.ManipulationSession;
import org.commonjava.maven.ext.core.fixture.TestUtils;
import org.commonjava.maven.ext.core.groovy.BaseScript;
import org.commonjava.maven.ext.core.impl.InitialGroovyManipulator;
import org.commonjava.maven.ext.io.ModelIO;
import org.commonjava.maven.ext.io.PomIO;
import org.commonjava.maven.ext.io.resolver.GalleyAPIWrapper;
import org.commonjava.maven.ext.io.resolver.GalleyInfrastructure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import junit.framework.TestCase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MavenScriptTest
{
    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void testGroovyAnnotation() throws Exception {
        final File groovy = GroovyLoader.loadGroovy("pmeAlterApplicationTemplates.groovy");
        final File pRoot = new File(TestUtils.resolveFileResource("", "")
                .getParentFile()
                .getParentFile(), "application-templates-pom.xml");
        PomIO pomIO = new PomIO();
        List<Project> projects = pomIO.parseProject(pRoot);
        ManipulationManager m = new ManipulationManager(null, Collections.emptyMap(), Collections.emptyMap(), null);
        ManipulationSession ms = TestUtils.createSession(null);
        m.init(ms);

        Project root = projects.stream().filter(p -> p.getProjectParent()==null).findAny().orElse(null);

        InitialGroovyManipulator gm = new InitialGroovyManipulator(null, null);
        gm.init(ms);
        TestUtils.executeMethod(gm, "applyGroovyScript", new Class[]{List.class, Project.class, File.class},
                new Object[]{projects, root, groovy});


        File dir = new File ("src/test/resources/quickstarts");
        assertTrue(dir.getAbsolutePath() + " does not exist", dir.exists());
        File[] matchingFiles = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("orig");
            }
        });

        for (int i = 0; i < matchingFiles.length; i++) {
            File replaced = matchingFiles[i];
            File original = new File(replaced.toString().replace(".orig", ""));
            assertTrue(original.getAbsolutePath() + " does not exist", original.exists());

            // Check that the two files are different
            assertFalse(original.getAbsolutePath() + " is the same as " + replaced.getAbsolutePath() + " and there should have been substitutions", 
                FileUtils.contentEquals(original,replaced));
        }
    }


}
