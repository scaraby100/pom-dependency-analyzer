
/*
 * Copyright 2018 Alessandro Patriarca.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.scarabya.pom.dependency.analyzer.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import xyz.scarabya.pom.dependency.analyzer.domain.Pom;

/**
 *
 * @author Alessandro Patriarca
 */
public class Engine
{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);  
    
    private final FilenameFilter pomFilter = (File dir, String name)
            -> name.equalsIgnoreCase("pom.xml");

    private final String xmlTagMaskOpen = "<%>";
    private final String xmlTagMaskClosed = "</%>";
    private final String xmlCharToReplace = "%";

    private final String pomParentTagName = "parent";
    private final String pomGroupIdTagName = "groupId";
    private final String pomArtifactIdTagName = "artifactId";
    private final String pomDepTagName = "dependency";
    private final String depGroupIdTagName = "groupId";
    private final String depArtifactIdTagName = "artifactId";

    private final Map<String, Pom> knowedPoms = new HashMap<>();

    public void walkAndAnalyze(File projectDir) throws IOException
    {
        FilenameFilter pomFilter = (File dir, String name)
                -> name.equalsIgnoreCase("pom.xml");

        for (File file : projectDir.listFiles())
            if (file.isDirectory())
                analyzePomFromDir(file);

    }

    public void dependencyReport()
    {
        for(String pomName : knowedPoms.keySet())
        {
            LOGGER.log(Level.INFO, "POM: {0}", pomName);
            for(Pom pom : knowedPoms.get(pomName).getDependencies().values())
                LOGGER.log(Level.INFO, ";{0}",  pom.getCompletePomName());
        }
    }

    private void analyzePomFromDir(File directory) throws IOException
    {
        Pom thisPom = null, thisDepPom = null;
        boolean created = false;
        Map<String, String> nodeDetails;
        String pomName, depName;

        for (File pomFile : directory.listFiles(pomFilter))
            try (BufferedReader br = new BufferedReader(
                    new FileReader(pomFile)))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    if (!created && line.contains(pomParentTagName))
                    {
                        nodeDetails = getXmlNodeDetail(br,
                                pomParentTagName);
                        pomName = nodeDetails.get(pomGroupIdTagName)
                                + ";"
                                + nodeDetails.get(pomArtifactIdTagName);
                        if (!knowedPoms.containsKey(pomName))
                        {
                            thisPom = new Pom(nodeDetails.get(pomGroupIdTagName),
                                    nodeDetails.get(pomArtifactIdTagName));
                            knowedPoms.put(pomName, thisPom);
                        }
                        else
                            thisPom = knowedPoms.get(pomName);
                        created = true;
                    }
                    if (created && line.contains(pomDepTagName))
                    {
                        nodeDetails = getXmlNodeDetail(br, pomDepTagName);
                        depName = nodeDetails.get(depGroupIdTagName)
                                + ";"
                                + nodeDetails.get(depArtifactIdTagName);
                        if (!knowedPoms.containsKey(depName))
                        {
                            thisDepPom = new Pom(nodeDetails.get(
                                    depGroupIdTagName), nodeDetails
                                            .get(depArtifactIdTagName));
                            knowedPoms.put(depName, thisDepPom);
                        }
                        else
                            thisDepPom = knowedPoms.get(depName);
                        thisPom.addDependency(thisDepPom);
                    }

                }
            }
    }

    private String removeXmlToken(String from, String token)
    {
        String tokenEnd = xmlTagMaskClosed.replace(xmlCharToReplace, token);
        String tokenStart = xmlTagMaskOpen.replace(xmlCharToReplace, token);
        String newString = from.substring(from.indexOf(tokenStart)
                + tokenStart.length());
        return newString.substring(0, newString.indexOf(tokenEnd));
    }

    private Map<String, String> getXmlNodeDetail(BufferedReader br,
            String nodeName) throws IOException
    {
        Map<String, String> nodeInfo = new HashMap<>();
        String line, nodeDetailName;
        while (!(line = br.readLine()).contains(nodeName))

            if (line != null && line.length() > 0)
            {
                nodeDetailName = line.substring(line.indexOf('<'),
                        line.indexOf('>'));
                nodeInfo.put(nodeDetailName,
                        removeXmlToken(line, nodeDetailName));
            }

        return nodeInfo;

    }
}
