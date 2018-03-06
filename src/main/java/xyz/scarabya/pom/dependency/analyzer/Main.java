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
package xyz.scarabya.pom.dependency.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import xyz.scarabya.pom.dependency.analyzer.engine.Engine;
import xyz.scarabya.pom.dependency.analyzer.log.LightLogger;

/**
 *
 * @author Alessandro Patriarca
 */
public class Main
{

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);  
    
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException
    {
        LightLogger.setup();
    
        File originalFolder = showFileChooser(
                "Seleziona la cartella del parent project aggiornato");
        
        Engine engine = new Engine();
        
        engine.walkAndAnalyze(originalFolder);
        engine.dependencyReport();
    }
    
    private static File showFileChooser(String message)
    {
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView()
                .getHomeDirectory());
        jfc.setDialogTitle(message);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.showOpenDialog(null);
        return jfc.getSelectedFile();
    }
    
}
